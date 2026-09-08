package egovframework.com.cop.brd.web;

import egovframework.com.cop.brd.service.CommentVO;
import egovframework.com.cop.brd.service.EgovCommentService;
import egovframework.com.cop.brd.service.EgovStsfdgService;
import egovframework.com.cop.brd.service.StsfdgVO;
import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.crypto.service.EgovEnvCryptoService;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller("brdEgovCommentAPIController")
@RequestMapping("/cop/brd")
@RequiredArgsConstructor
@Slf4j
public class EgovCommentAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovCommentService articleCommentService;
    private final EgovStsfdgService bbsStsfdgService;
    private final EgovEnvCryptoService egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping("/selectCommentList")
    public ResponseEntity<?> selectCommentList(@RequestBody CommentVO commentVO) {
        pageUnit = 3;
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(commentVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        commentVO.setFirstIndex(paginationInfo.getCurrentPageNo() - 1);
        commentVO.setLastIndex(paginationInfo.getLastRecordIndex());
        commentVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<CommentVO> response = articleCommentService.selectArticleCommentList(commentVO);
        paginationInfo.setTotalRecordCount((int) response.getTotalElements());

        egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> result = new HashMap<>();
        result.put("response", response);

        String paginationHtml = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "Comment_linkPage");

        result.put("pagination", paginationHtml);
        result.put("lineNumber", (commentVO.getPageIndex() - 1) * commentVO.getPageSize());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/insertComment")
    public ResponseEntity<?> insertComment(@Valid @RequestBody CommentVO commentVO, BindingResult bindingResult, HttpServletRequest request) throws FdlException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        log.debug("댓글 >> " + commentVO.getAnswer());

        Map<String, String> userInfo = extracted(request);
        if (!commentVO.getAnswer().isEmpty()) {
            articleCommentService.insertArticleComment(commentVO, userInfo);
        }

        return ResponseEntity.ok().body("댓글이 등록되었습니다.");
    }

    @PostMapping("/deleteComment")
    public ResponseEntity<?> deleteComment(@RequestBody CommentVO commentVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        try {
            articleCommentService.deleteArticleComment(commentVO, userInfo);
            return ResponseEntity.ok().body("댓글이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if ("인증 정보가 없습니다.".equals(msg)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
            }
            if ("댓글을 찾을 수 없습니다.".equals(msg)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
            if (msg != null && msg.contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
            }
            throw e;
        }
    }

    @PostMapping("/selectStsfdgList")
    public ResponseEntity<?> selectStsfdgList(@RequestBody StsfdgVO stsfdgVO) {
        pageUnit = 3;
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(stsfdgVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        stsfdgVO.setFirstIndex(paginationInfo.getCurrentPageNo() - 1);
        stsfdgVO.setLastIndex(paginationInfo.getLastRecordIndex());
        stsfdgVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Map<String, Object> map = bbsStsfdgService.selectStsfdgList(stsfdgVO);
        Page<StsfdgVO> response = (Page<StsfdgVO>) map.get("sList");
        paginationInfo.setTotalRecordCount((int) response.getTotalElements());

        egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        result.put("satisAvr", map.get("stsfdgAverage"));

        String paginationHtml = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "Satisfaction_linkPage");

        result.put("pagination", paginationHtml);
        result.put("lineNumber", (stsfdgVO.getPageIndex() - 1) * stsfdgVO.getPageSize());


        return ResponseEntity.ok(result);
    }

    @PostMapping("/insertStsfdg")
    public ResponseEntity<?> insertStsfdg(@Valid @RequestBody StsfdgVO stsfdgVO, BindingResult bindingResult, HttpServletRequest request) throws FdlException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        } else {
            if (stsfdgVO.getStsfdg() >= 0) {
                Map<String, String> userInfo = extracted(request);
                bbsStsfdgService.insertStsfdg(stsfdgVO, userInfo);
            }
        }

        return ResponseEntity.ok().body("등록이 완료되었습니다.");
    }

    @Transactional
    @PostMapping("/deleteStsfdg")
    public ResponseEntity<?> deleteStsfdg(@RequestBody StsfdgVO stsfdgVO, HttpServletRequest request) {
        String stsfdgNo = stsfdgVO.getStsfdgNo() != null ? stsfdgVO.getStsfdgNo() : "";
        if (stsfdgNo.isEmpty()) {
            return ResponseEntity.badRequest().body("stsfdgNo is required");
        }
        Map<String, String> userInfo = extracted(request);
        try {
            bbsStsfdgService.deleteStsfdg(stsfdgNo, userInfo);
            return ResponseEntity.ok().body("삭제되었습니다.");
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            if ("인증 정보가 없습니다.".equals(msg)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
            }
            if (msg != null && msg.contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
            }
            throw e;
        }
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
