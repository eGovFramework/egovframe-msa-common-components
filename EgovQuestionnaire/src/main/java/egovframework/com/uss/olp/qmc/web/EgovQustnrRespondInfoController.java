package egovframework.com.uss.olp.qmc.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qmc.service.EgovQustnrRespondInfoService;
import egovframework.com.uss.olp.qmc.service.QustnrRespondInfoDTO;
import egovframework.com.uss.olp.qmc.service.QustnrRespondInfoVO;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller("qmcEgovQustnrRespondInfoController")
@RequestMapping("/uss/olp/qmc")
@RequiredArgsConstructor
public class EgovQustnrRespondInfoController {

    private final EgovQustnrRespondInfoService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping("/qustnrRespondInfoList")
    public ResponseEntity<?> qustnrRespondInfoList(@RequestBody QustnrRespondInfoVO qustnrRespondInfoVO) {
        int pageUnit = 5;
        int pageSize = 5;

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrRespondInfoVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrRespondInfoVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrRespondInfoVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrRespondInfoVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrRespondInfoDTO> list = service.list(qustnrRespondInfoVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "qustnrRespondInfoListLinkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrRespondInfoList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

}
