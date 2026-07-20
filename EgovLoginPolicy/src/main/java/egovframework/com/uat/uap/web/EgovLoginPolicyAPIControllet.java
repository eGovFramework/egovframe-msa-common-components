package egovframework.com.uat.uap.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uat.uap.service.EgovLoginPolicyService;
import egovframework.com.uat.uap.service.LoginPolicyDTO;
import egovframework.com.uat.uap.service.LoginPolicyVO;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller("uapEgovLoginPolicyAPIControllet")
@RequestMapping("/uat/uap")
@RequiredArgsConstructor
public class EgovLoginPolicyAPIControllet {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovLoginPolicyService service;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value="/loginPolicyList")
    public ResponseEntity<?> loginPolicyList(@ModelAttribute LoginPolicyVO loginPolicyVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(loginPolicyVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        loginPolicyVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        loginPolicyVO.setLastIndex(paginationInfo.getLastRecordIndex());
        loginPolicyVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<LoginPolicyDTO> list = service.list(loginPolicyVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("loginPolicyList", list.getContent());
        response.put("pagination", pagination);
        response.put("lineNumber", (loginPolicyVO.getPageIndex()-1)*pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/loginPolicyDetail")
    public ResponseEntity<?> loginPolicyDetail(@ModelAttribute LoginPolicyVO loginPolicyVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        LoginPolicyVO result = service.detail(loginPolicyVO, userInfo);

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

    @PostMapping(value="/loginPolicyInsert")
    public ResponseEntity<?> loginPolicyInsert(@Valid @ModelAttribute LoginPolicyVO loginPolicyVO, BindingResult bindingResult, HttpServletRequest request) {
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

        Map<String, String> userInfo = extracted(request);
        LoginPolicyVO result = service.insert(loginPolicyVO, userInfo);

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

    @PostMapping(value="/loginPolicyUpdate")
    public ResponseEntity<?> loginPolicyUpdate(@Valid @ModelAttribute LoginPolicyVO loginPolicyVO, BindingResult bindingResult, HttpServletRequest request) {
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

        Map<String, String> userInfo = extracted(request);
        LoginPolicyVO result = service.update(loginPolicyVO, userInfo);

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

    @PostMapping(value="/loginPolicyDelete")
    public ResponseEntity<?> loginPolicyDelete(@ModelAttribute LoginPolicyVO loginPolicyVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        service.delete(loginPolicyVO, userInfo);
        return ResponseEntity.ok("success");
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
