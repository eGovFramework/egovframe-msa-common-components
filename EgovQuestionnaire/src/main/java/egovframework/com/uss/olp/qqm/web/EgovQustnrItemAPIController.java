package egovframework.com.uss.olp.qqm.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.uss.olp.qqm.service.EgovQustnrItemService;
import egovframework.com.uss.olp.qqm.service.QustnrIemDTO;
import egovframework.com.uss.olp.qqm.service.QustnrIemVO;
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

@Controller("qqmEgovQustnrItemAPIController")
@RequestMapping("/uss/olp/qqm")
@RequiredArgsConstructor
public class EgovQustnrItemAPIController {

    private final EgovQustnrItemService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value="/qustnrIemList")
    public ResponseEntity<?> qustnrIemList(@RequestBody QustnrIemVO qustnrIemVO) {
        int pageUnit = 5;
        int pageSize = 5;

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(qustnrIemVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        qustnrIemVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        qustnrIemVO.setLastIndex(paginationInfo.getLastRecordIndex());
        qustnrIemVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<QustnrIemDTO> list = service.list(qustnrIemVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "qustnrItemListLinkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("qustnrIemList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

}
