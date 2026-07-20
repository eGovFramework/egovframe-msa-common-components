package egovframework.com.uss.olp.qri.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qri.service.*;
import lombok.RequiredArgsConstructor;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

@Controller("qriEgovQustnrRspnsResultAPIController")
@RequestMapping("/uss/olp/qri")
@RequiredArgsConstructor
public class EgovQustnrRspnsResultAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovQustnrRspnsResultService service;
    private final EgovQestnrInfoService qustnrInfoService;
    private final EgovQustnrQesitmService qustnrQesitmService;
    private final EgovQustnrIemService qustnrIemService;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping("/qustnrRspnsResultList")
    public ResponseEntity<?> qustnrRspnsResultList(@ModelAttribute QustnrRspnsResultVO qustnrRspnsResultVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrRspnsResultVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrRspnsResultVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrRspnsResultVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrRspnsResultVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrRspnsResultDTO> list = service.list(qustnrRspnsResultVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrRspnsResultList", list.getContent());
        response.put("pagination", pagination);
        response.put("lineNumber", (qustnrRspnsResultVO.getPageIndex()-1)*pageSize);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/qustnrRspnsResultInsert")
    public ResponseEntity<?> qustnrRspnsResultInsert(@Valid @ModelAttribute QustnrRspnsResultVO qustnrRspnsResultVO, BindingResult bindingResult, HttpServletRequest request) {
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
        boolean result = service.insert(qustnrRspnsResultVO, userInfo);

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
    public ResponseEntity<?> qustnrRspnsResultStats(@RequestBody QustnrRspnsResultVO qustnrRspnsResultVO, HttpServletRequest request) {
        QestnrInfoDTO qestnrInfo = qustnrInfoService.detail(qustnrRspnsResultVO);
        Map<String, String> userInfo = extracted(request);
        List<QustnrQesitmDTO> qustnrQesitmList = qustnrQesitmService.qustnrQesitmList(qustnrRspnsResultVO);
        List<QustnrIemDTO> qustnrIemList = qustnrIemService.qustnrIemList(qustnrRspnsResultVO);
        List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats = service.qustnrRspnsResultMCStats(qustnrRspnsResultVO, userInfo);
        List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats = service.qustnrRspnsResultESStats(qustnrRspnsResultVO, userInfo);

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
