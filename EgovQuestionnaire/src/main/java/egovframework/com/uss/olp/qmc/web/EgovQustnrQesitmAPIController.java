package egovframework.com.uss.olp.qmc.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qmc.service.EgovQustnrQesitmService;
import egovframework.com.uss.olp.qmc.service.QustnrQesitmDTO;
import egovframework.com.uss.olp.qmc.service.QustnrQesitmVO;
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

@Controller("qmcEgovQustnrQesitmAPIController")
@RequestMapping("/uss/olp/qmc")
@RequiredArgsConstructor
public class EgovQustnrQesitmAPIController {

    private final EgovQustnrQesitmService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value="/qustnrQesitmList")
    public ResponseEntity<?> qustnrQesitmList(@RequestBody QustnrQesitmVO qustnrQesitmVO) {
        int pageUnit = 5;
        int pageSize = 5;

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrQesitmVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrQesitmVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrQesitmVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrQesitmVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrQesitmDTO> list = service.list(qustnrQesitmVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "qustnrQesitmListLinkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrQesitmList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

}
