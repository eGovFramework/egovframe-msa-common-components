package egovframework.com.uss.olp.qrm.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qrm.service.*;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("qrmEgovQustnrRespondInfoAPIController")
@RequestMapping("/uss/olp/qrm")
@RequiredArgsConstructor
public class EgovQustnrRespondInfoAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovQustnrRespondInfoService service;
    private final EgovCmmnDetailCodeService cmmnDetailCodeService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    @PostMapping("/qustnrRespondInfoList")
    public ResponseEntity<?> qustnrRespondInfoList(@ModelAttribute QustnrRespondInfoVO qustnrRespondInfoVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrRespondInfoVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrRespondInfoVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrRespondInfoVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrRespondInfoVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrRespondInfoDTO> list = service.list(qustnrRespondInfoVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrRespondInfoList", list.getContent());
        response.put("pagination", pagination);
        response.put("lineNumber", (qustnrRespondInfoVO.getPageIndex()-1)*pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/qustnrRespondInfoDetail")
    public ResponseEntity<?> qustnrRespondInfoDetail(@ModelAttribute QustnrRespondInfoVO qustnrRespondInfoVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        QustnrRespondInfoDTO result = service.detail(qustnrRespondInfoVO,userInfo);
        List<CmmnDetailCodeVO> sexdstnList = cmmnDetailCodeService.list("COM014");
        List<CmmnDetailCodeVO> occpTyList = cmmnDetailCodeService.list("COM034");

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            response.put("result", result);
            response.put("sexdstnList", sexdstnList);
            response.put("occpTyList", occpTyList);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrRespondInfoInsert")
    public ResponseEntity<?> qustnrRespondInfoInsert(
            @Valid @ModelAttribute QustnrRespondInfoVO qustnrRespondInfoVO,
            BindingResult bindingResult,
            @RequestHeader("X-UNIQ-ID") String uniqId
    ) {
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

        qustnrRespondInfoVO.setFrstRegisterId(uniqId);
        qustnrRespondInfoVO.setLastUpdusrId(uniqId);
        QustnrRespondInfoVO result = service.insert(qustnrRespondInfoVO);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrRespondInfoUpdate")
    public ResponseEntity<?> qustnrRespondInfoUpdate(
            @Valid @ModelAttribute QustnrRespondInfoVO qustnrRespondInfoVO,
            BindingResult bindingResult,
            @RequestHeader("X-UNIQ-ID") String uniqId,
            HttpServletRequest request
    ) {
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

        qustnrRespondInfoVO.setLastUpdusrId(uniqId);
        Map<String, String> userInfo = extracted(request);
        QustnrRespondInfoVO result = service.update(qustnrRespondInfoVO,userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrRespondInfoDelete")
    public ResponseEntity<?> qustnrRespondInfoDelete(@ModelAttribute QustnrRespondInfoVO qustnrRespondInfoVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        boolean result = service.delete(qustnrRespondInfoVO, userInfo);

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
