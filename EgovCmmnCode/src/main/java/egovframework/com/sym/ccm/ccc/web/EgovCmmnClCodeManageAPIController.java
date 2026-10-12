package egovframework.com.sym.ccm.ccc.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.sym.ccm.ccc.service.CmmnClCodeVO;
import egovframework.com.sym.ccm.ccc.service.EgovCmmnClCodeManageService;
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

@Controller("cccEgovCmmnClCodeManageAPIController")
@RequestMapping("/sym/ccm/ccc")
@RequiredArgsConstructor
public class EgovCmmnClCodeManageAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovCmmnClCodeManageService service;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value = "/cmmnClCodeList")
    public ResponseEntity<?> cmmnClCodeList(@ModelAttribute CmmnClCodeVO cmmnClCodeVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(cmmnClCodeVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        cmmnClCodeVO.setFirstIndex(paginationInfo.getCurrentPageNo() - 1);
        cmmnClCodeVO.setLastIndex(paginationInfo.getLastRecordIndex());
        cmmnClCodeVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<CmmnClCodeVO> list = service.list(cmmnClCodeVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("cmmnClCodeList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/cmmnClCodeDetail")
    public ResponseEntity<?> cmmnClCodeDetail(@ModelAttribute CmmnClCodeVO cmmnClCodeVO) {
        CmmnClCodeVO result = service.detail(cmmnClCodeVO);

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

    @PostMapping(value = "/cmmnClCodeInsert")
    public ResponseEntity<?> cmmnClCodeInsert(@Valid @ModelAttribute CmmnClCodeVO cmmnClCodeVO, BindingResult bindingResult, HttpServletRequest request) {
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
        CmmnClCodeVO result = service.insert(cmmnClCodeVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/cmmnClCodeUpdate")
    public ResponseEntity<?> cmmnClCodeUpdate(@Valid @ModelAttribute CmmnClCodeVO cmmnClCodeVO, BindingResult bindingResult, HttpServletRequest request) {
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
        CmmnClCodeVO result = service.update(cmmnClCodeVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/cmmnClCodeDelete")
    public ResponseEntity<?> cmmnClCodeDelete(@ModelAttribute CmmnClCodeVO cmmnClCodeVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        CmmnClCodeVO result = service.delete(cmmnClCodeVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
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
