package egovframework.com.sym.ccm.cde.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.sym.ccm.cde.service.CmmnDetailCodeVO;
import egovframework.com.sym.ccm.cde.service.EgovCmmnDetailCodeManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Controller("cdeEgovCmmnDetailCodeManageAPIController")
@RequestMapping("/sym/ccm/cde")
@RequiredArgsConstructor
@Slf4j
public class EgovCmmnDetailCodeManageAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovCmmnDetailCodeManageService service;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value = "/cmmnDetailCodeList")
    public ResponseEntity<?> cmmnDetailCodeList(@ModelAttribute CmmnDetailCodeVO cmmnDetailCodeVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(cmmnDetailCodeVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        cmmnDetailCodeVO.setFirstIndex(paginationInfo.getCurrentPageNo() - 1);
        cmmnDetailCodeVO.setLastIndex(paginationInfo.getLastRecordIndex());
        cmmnDetailCodeVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<CmmnDetailCodeVO> list = service.list(cmmnDetailCodeVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("cmmnDetailCodeList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/cmmnDetailCodeDetail")
    public ResponseEntity<?> cmmnDetailCodeDetail(@ModelAttribute CmmnDetailCodeVO cmmnDetailCodeVO) {
        CmmnDetailCodeVO result = service.detail(cmmnDetailCodeVO);

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

    @PostMapping(value = "/cmmnDetailCodeInsert")
    public ResponseEntity<?> cmmnDetailCodeInsert(@Valid @ModelAttribute CmmnDetailCodeVO cmmnDetailCodeVO, BindingResult bindingResult, HttpServletRequest request) {
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
        CmmnDetailCodeVO result = service.insert(cmmnDetailCodeVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/cmmnDetailCodeUpdate")
    public ResponseEntity<?> cmmnDetailCodeUpdate(@Valid @ModelAttribute CmmnDetailCodeVO cmmnDetailCodeVO, BindingResult bindingResult, HttpServletRequest request) {
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
        CmmnDetailCodeVO result = service.update(cmmnDetailCodeVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value = "/cmmnDetailCodeDelete")
    public ResponseEntity<?> cmmnDetailCodeDelete(@ModelAttribute CmmnDetailCodeVO cmmnDetailCodeVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        CmmnDetailCodeVO result = service.delete(cmmnDetailCodeVO, userInfo);

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
