package egovframework.com.cop.brd.web;

import egovframework.com.cop.brd.service.BbsVO;
import egovframework.com.cop.brd.service.BoardDTO;
import egovframework.com.cop.brd.service.EgovBoardService;
import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.security.GatewayUserContextResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("brdEgovBoardAPIController")
@RequestMapping("/cop/brd")
@RequiredArgsConstructor
@Slf4j
public class EgovBoardAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovBoardService service;
    private final GatewayUserContextResolver gatewayUserContextResolver;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value = "/boardList")
    public ResponseEntity<?> boardList(@ModelAttribute BbsVO bbsVO, HttpServletRequest request) {

        bbsVO.setPageIndex((bbsVO.getPageIndex()));
        bbsVO.setFirstIndex(bbsVO.getPageIndex() - 1);
        bbsVO.setPageUnit(pageUnit);

        List<BoardDTO> noticeList = service.noticeList(bbsVO);
        int noticeCnt = noticeList.size();
        if (noticeCnt != 0) {
            bbsVO.setPageUnit(pageUnit - noticeCnt);
        }

        Map<String, String> userInfo = gatewayUserContextResolver.resolve(request);
        Map<String, Object> response = service.list(bbsVO);

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(bbsVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit - noticeCnt);
        paginationInfo.setPageSize(pageSize);
        paginationInfo.setTotalRecordCount(((Long) response.get("totalElements")).intValue());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        response.put("noticeList", noticeList);
        response.put("pagination", pagination);
        response.put("userId", userInfo.get("uniqId"));

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/boardDetail")
    public ResponseEntity<?> boardDetail(@ModelAttribute BbsVO bbsVO, HttpServletRequest request) {
        Map<String, String> userInfo = gatewayUserContextResolver.resolve(request);
        BoardDTO result = service.detail(bbsVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (ObjectUtils.isEmpty(result)) {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }

        if (StringUtils.isNotBlank(result.getNttCn())) {
            result.setNttCn(Jsoup.clean(result.getNttCn(), Safelist.relaxed()));
        }
        String uniqKey = userInfo.get("uniqId");
        if (!ObjectUtils.isEmpty(result.getAtchFileId()) && StringUtils.isNotBlank(uniqKey)) {
            result.setAtchFileId(egovEnvCryptoService.encrypt(result.getAtchFileId() + "|" + uniqKey));
        }

        response.put("status", "success");
        response.put("result", result);
        response.put("userId", userInfo.get("uniqId"));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/boardInsert")
    public ResponseEntity<?> boardInsert(
            @Valid @ModelAttribute BbsVO bbsVO,
            MultipartHttpServletRequest multiRequest,
            BindingResult bindingResult,
            HttpServletRequest request
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "valid");
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }

        List<MultipartFile> files = multiRequest.getFiles("fileList");
        log.debug("##### boardInsert >>> {}", files.size());

        Map<String, String> userInfo = gatewayUserContextResolver.resolve(request);
        BbsVO result = service.insert(bbsVO, files, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/boardUpdate")
    public ResponseEntity<?> boardUpdate(
            @Valid @ModelAttribute BbsVO bbsVO,
            MultipartHttpServletRequest multiRequest,
            BindingResult bindingResult,
            HttpServletRequest request
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "valid");
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }

        List<MultipartFile> files = multiRequest.getFiles("fileList");
        log.debug("##### boardInsert >>> {}", files.size());

        Map<String, String> userInfo = gatewayUserContextResolver.resolve(request);

        // 첨부 없음·히든 미전송 시 빈 문자열이면 decrypt 시 ARIA 패딩 예외(ArrayIndexOutOfBoundsException) 발생
        if (StringUtils.isNotBlank(bbsVO.getAtchFileId())) {
            String decodeId = egovEnvCryptoService.decrypt(bbsVO.getAtchFileId());
            String decodeFileId = StringUtils.substringBefore(decodeId, "|");
            bbsVO.setAtchFileId(decodeFileId);
        }

        BbsVO result;
        try {
            result = service.update(bbsVO, files, userInfo);
        } catch (IllegalStateException e) {
            return responseEntityForBoardAuthorException(e);
        }

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/deleteBoard")
    public ResponseEntity<?> deleteBoard(@RequestBody BbsVO bbsVO, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        Map<String, String> userInfo = gatewayUserContextResolver.resolve(request);
        try {
            service.delete(bbsVO, userInfo);
            return ResponseEntity.ok().body("게시글이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            return responseEntityForBoardAuthorException(e);
        }
    }

    /**
     * 게시글 삭제·수정 서비스에서 던지는 {@link IllegalStateException}을 HTTP 상태·본문으로 변환한다.
     */
    private ResponseEntity<?> responseEntityForBoardAuthorException(IllegalStateException e) {
        String msg = e.getMessage();
        if ("인증 정보가 없습니다.".equals(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
        }
        if ("삭제 대상 글을 찾을 수 없습니다.".equals(msg)
                || "수정 대상 글을 찾을 수 없습니다.".equals(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }
        if (msg != null && (msg.contains("권한") || msg.contains("익명"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        }
        throw e;
    }
}
