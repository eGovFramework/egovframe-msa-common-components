package egovframework.com.uss.olp.qqm.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qqm.service.*;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("qqmEgovQustnrQesitmAPIController")
@RequestMapping("/uss/olp/qqm")
@RequiredArgsConstructor
public class EgovQustnrQesitmAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovQustnrQesitmService service;
    private final EgovCmmnDetailCodeService cmmnDetailCodeService;
    private final EgovQestnrInfoService qestnrInfoService;
    private final EgovQustnrItemService qustnrItemService;
    private final EgovQustnrRspnsResultService qustnrRspnsResultService;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping("/qustnrQesitmList")
    public ResponseEntity<?> qustnrQesitmList(@ModelAttribute QustnrQesitmVO qustnrQesitmVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrQesitmVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrQesitmVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrQesitmVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrQesitmVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrQesitmDTO> list = service.list(qustnrQesitmVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrQesitmList", list.getContent());
        response.put("pagination", pagination);
        response.put("lineNumber", (qustnrQesitmVO.getPageIndex()-1)*pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value="/qustnrQesitmDetail")
    public ResponseEntity<?> qustnrQesitmDetail(@ModelAttribute QustnrQesitmVO qustnrQesitmVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        QustnrQesitmDTO result = service.detail(qustnrQesitmVO, userInfo);
        List<CmmnDetailCodeVO> cmmnDetailCodeList = cmmnDetailCodeService.list("COM018");

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            response.put("result", result);
            response.put("cmmnDetailCodeList", cmmnDetailCodeList);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrQesitmInsert")
    public ResponseEntity<?> qustnrQesitmInsert(@Valid @ModelAttribute QustnrQesitmVO qustnrQesitmVO, BindingResult bindingResult, HttpServletRequest request) {
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
        QustnrQesitmVO result = service.insert(qustnrQesitmVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrQesitmUpdate")
    public ResponseEntity<?> qustnrQesitmUpdate(@Valid @ModelAttribute QustnrQesitmVO qustnrQesitmVO, BindingResult bindingResult, HttpServletRequest request) {
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
        QustnrQesitmVO result = service.update(qustnrQesitmVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (!ObjectUtils.isEmpty(result)) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping(value="/qustnrQesitmDelete")
    public ResponseEntity<?> qustnrQesitmDelete(@ModelAttribute QustnrQesitmVO qustnrQesitmVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        boolean result = service.delete(qustnrQesitmVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/qustnrRspnsResultStats")
    public ResponseEntity<?> qustnrRspnsResultStats(@RequestBody QustnrQesitmVO qustnrQesitmVO, HttpServletRequest request) {
        Map<String, String> userInfo = extracted(request);
        QestnrInfoDTO qestnrInfo = qestnrInfoService.detail(qustnrQesitmVO);
        List<QustnrQesitmDTO> qustnrQesitmList = service.qustnrQusitmList(qustnrQesitmVO);
        List<QustnrIemDTO> qustnrIemList = qustnrItemService.qustnrIemList(qustnrQesitmVO);
        List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats = qustnrRspnsResultService.qustnrRspnsResultMCStats(qustnrQesitmVO, userInfo);
        List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats = qustnrRspnsResultService.qustnrRspnsResultESStats(qustnrQesitmVO, userInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("qestnrInfo", qestnrInfo);
        response.put("qustnrQesitmList", qustnrQesitmList);
        response.put("qustnrIemList", qustnrIemList);
        response.put("qustnrRspnsResultMCStats", qustnrRspnsResultMCStats);
        response.put("qustnrRspnsResultESStats", qustnrRspnsResultESStats);

        return ResponseEntity.ok(response);
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
