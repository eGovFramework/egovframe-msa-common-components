package egovframework.com.uss.olp.qmc.web;

import egovframework.com.uss.olp.qmc.service.EgovQustnrTmplatService;
import egovframework.com.uss.olp.qmc.service.QustnrTmplatVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("qmcEgovQustnrTmplatController")
@RequestMapping("/uss/olp/qmc")
@RequiredArgsConstructor
public class EgovQustnrTmplatAPIController {

    private final EgovQustnrTmplatService service;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    @PostMapping(value="/qustnrTmplatList")
    public ResponseEntity<?> qustnrTmplatList() {
        List<QustnrTmplatVO> list = service.list();

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrTmplatList", list);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/qustnrTmplatImage")
    public void qustnrTmplatImage(HttpServletResponse response, @RequestParam("qustnrTmplatId") String qustnrTmplatId, HttpServletRequest request) throws IOException {
        Map<String, String> userInfo = extracted(request);
        byte[] image = service.getImage(qustnrTmplatId,userInfo);
        // 2026.07.13 KISA 보안취약점 조치: 실제 이미지 콘텐츠 유형으로 Content-Type 지정 및 MIME 스니핑 방지 헤더 추가
        response.setContentType(detectImageContentType(image));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(image.length);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.getOutputStream().write(image);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    private String detectImageContentType(byte[] bytes) {
        if (bytes != null) {
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }
            if (bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "image/png";
            }
            if (bytes.length >= 4 && bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
                return "image/gif";
            }
        }
        return "application/octet-stream";
    }

    private Map<String, String> extracted(HttpServletRequest request) {
        Map<String, String> userInfo = new HashMap<>();

        String encryptUserId = request.getHeader("X-USER-ID");
        String encryptUserNm = request.getHeader("X-USER-NM");
        String encryptUniqId = request.getHeader("X-UNIQ-ID");

        userInfo.put("userId", egovEnvCryptoService.decrypt(encryptUserId));
        userInfo.put("userName", egovEnvCryptoService.decrypt(encryptUserNm));
        userInfo.put("uniqId", egovEnvCryptoService.decrypt(encryptUniqId));

        return userInfo;
    }

}
