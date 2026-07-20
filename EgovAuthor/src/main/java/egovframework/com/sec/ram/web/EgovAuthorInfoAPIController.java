package egovframework.com.sec.ram.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.sec.ram.service.AuthorInfoVO;
import egovframework.com.sec.ram.service.EgovAuthorManageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller("ramEgovAuthorInfoAPIController")
@RequestMapping("/sec/ram")
@RequiredArgsConstructor
public class EgovAuthorInfoAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovAuthorManageService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    @PostMapping(value="/authorInfoList")
    public ResponseEntity<?> authorInfoList(@ModelAttribute AuthorInfoVO authorInfoVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(authorInfoVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        authorInfoVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        authorInfoVO.setLastIndex(paginationInfo.getLastRecordIndex());
        authorInfoVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<AuthorInfoVO> list = service.list(authorInfoVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("authorInfoList", list.getContent());
        response.put("pagination", pagination);
        response.put("lineNumber", (authorInfoVO.getPageIndex()-1)*pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/authorInfoDetail")
    public ResponseEntity<?> authorInfoDetail(@ModelAttribute AuthorInfoVO authorInfoVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        AuthorInfoVO result = service.detail(authorInfoVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/authorInfoInsert")
    public ResponseEntity<?> authorInfoInsert(@Valid @ModelAttribute AuthorInfoVO authorInfoVO, BindingResult bindingResult) {
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

        AuthorInfoVO result = service.insert(authorInfoVO);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/authorInfoUpdate")
    public ResponseEntity<?> authorInfoUpdate(@Valid @ModelAttribute AuthorInfoVO authorInfoVO, BindingResult bindingResult) {
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

        AuthorInfoVO result = service.update(authorInfoVO);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/authorInfoDelete")
    public ResponseEntity<?> authorInfoDelete(@ModelAttribute AuthorInfoVO authorInfoVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        boolean result = service.delete(authorInfoVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
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
