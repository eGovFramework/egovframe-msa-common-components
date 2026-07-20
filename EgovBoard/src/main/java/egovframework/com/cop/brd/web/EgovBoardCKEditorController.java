package egovframework.com.cop.brd.web;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class EgovBoardCKEditorController {

    @Value("${file.ck.upload-dir:#{systemProperties['user.home'] + '/upload/ckeditor'}}")
    private String uploadDir;

    @Value("${file.ck.allowed-extensions:jpg,jpeg,gif,bmp,png}")
    private String allowedExtensions;

    @Value("${file.max-file-size:1048576}")
    private Long maxFileSize;

    private Set<String> allowedExtensionSet;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    public EgovBoardCKEditorController(@Qualifier("egovEnvCryptoService") EgovEnvCryptoServiceImpl egovEnvCryptoService) {
        this.egovEnvCryptoService = egovEnvCryptoService;
    }

    @PostConstruct // 객체 생성 후 실행
    public void init() {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            log.warn("Failed to create upload directory >>> {}", uploadDir);
        }

        // 확장자 목록을 설정 값에서 가져와 Set으로 변환
        allowedExtensionSet = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @PostMapping("/cop/brd/ckImageUpload")
    public Map<String, Object> ckImageUpload(MultipartRequest request) throws IOException {
        Map<String, Object> response = new HashMap<>();

        MultipartFile uploadFile = request.getFile("upload");

        // 파일 Null 검사
        if (ObjectUtils.isEmpty(uploadFile)) {
            response.put("uploaded", 0);
            response.put("error", "No files uploaded.");
            return response;
        }

        // 파일 크기 검사 (1MB 초과 시 업로드 불가)
        if (uploadFile.getSize() > maxFileSize) {
            response.put("uploaded", 0);
            response.put("error", "File size exceeds the maximum limit of 1MB.");
            return response;
        }

        String newFileName = getFileName(uploadFile);

        // 파일 확장자 검사
        if (!isValidImageFile(newFileName)) {
            response.put("uploaded", 0);
            response.put("error", "Invalid file type.");
            return response;
        }

        // 실제 파일 콘텐츠(매직바이트)를 디코딩해 진짜 이미지인지 검증 (확장자 위조 방지)
        if (!isActuallyImage(uploadFile)) {
            response.put("uploaded", 0);
            response.put("error", "Invalid file content.");
            return response;
        }

        File destinationFile = new File(uploadDir, newFileName);
        uploadFile.transferTo(destinationFile);

        String fileUrl = "/cop/brd/ckeditor/" + newFileName;
        response.put("url", fileUrl);

        return response;
    }

    // 2026.07.13 KISA 보안취약점 조치: 확장자만이 아니라 실제 이미지 콘텐츠(매직바이트)를 디코딩하여 검증
    private boolean isActuallyImage(MultipartFile uploadFile) {
        try (java.io.InputStream is = uploadFile.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            return image != null;
        } catch (IOException e) {
            log.warn("Failed to validate image content >>> {}", e.getMessage());
            return false;
        }
    }

    private String resolveImageContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

    @GetMapping("/cop/brd/ckeditor/{filename}")
    public void ckeditorImage(@PathVariable String filename, HttpServletResponse response) {
        if (!isSafeFilename(filename)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(filename).normalize();

        // 업로드 디렉터리 밖으로 벗어나는 경로 접근 차단
        if (!filePath.startsWith(basePath) || !Files.isRegularFile(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = filePath.toFile();

        // 2026.07.13 KISA 보안취약점 조치: 허용된 이미지 확장자에 대한 정확한 Content-Type 지정 및
        // MIME 스니핑으로 인한 저장된 XSS 방지를 위한 X-Content-Type-Options 헤더 설정
        response.setContentType(resolveImageContentType(filename));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        // 파일을 읽어서 응답 스트림으로 전송
        try (FileInputStream fis = new FileInputStream(file);
             ServletOutputStream outStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096]; // 4KB 버퍼
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush(); // 스트림 비우기
        } catch (IOException e) {
            log.warn("Failed to create image >>> {}", e.getMessage());
        }
    }

    //2026.02.28 KISA 보안취약점 조치
    private String getFileName(MultipartFile uploadFile) {
        String originalFileName = uploadFile.getOriginalFilename();
        String ext = null;
        if (originalFileName != null) {
            ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueIdentifier = UUID.randomUUID().toString();
        String fileName = egovEnvCryptoService.encrypt(originalFileName + uniqueIdentifier);
        return fileName + ext;
    }

    private boolean isValidImageFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return allowedExtensionSet.contains(extension);
    }

    private boolean isSafeFilename(String filename) {
        if (ObjectUtils.isEmpty(filename)) {
            return false;
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        Path normalized = Paths.get(filename).normalize();
        return normalized.getNameCount() == 1 && filename.equals(normalized.toString());
    }

}
