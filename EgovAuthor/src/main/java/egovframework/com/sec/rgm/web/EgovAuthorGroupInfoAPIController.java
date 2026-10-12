package egovframework.com.sec.rgm.web;

import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import egovframework.com.sec.rgm.service.AuthorGroupInfoVO;
import egovframework.com.sec.rgm.service.EgovAuthorGroupInfoService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller("rgmEgovAuthorGroupInfoAPIController")
@RequestMapping("/sec/rgm")
@RequiredArgsConstructor
public class EgovAuthorGroupInfoAPIController {

    @Value("${egov.page.unit}")
    private int pageUnit;

    @Value("${egov.page.size}")
    private int pageSize;

    private final EgovAuthorGroupInfoService service;
    private final EgovKrdsPaginationRenderer egovKrdsPaginationRenderer;

    @PostMapping(value="/authorGroupInfoList")
    public ResponseEntity<?> authorGroupInfoList(@RequestBody AuthorGroupInfoVO authorGroupInfoVO) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(authorGroupInfoVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        authorGroupInfoVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        authorGroupInfoVO.setLastIndex(paginationInfo.getLastRecordIndex());
        authorGroupInfoVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Page<AuthorGroupInfoVO> list = service.list(authorGroupInfoVO);
        paginationInfo.setTotalRecordCount((int) list.getTotalElements());

        String pagination = egovKrdsPaginationRenderer.renderPagination(paginationInfo, "linkPage");

        Map<String, Object> response = new HashMap<>();
        response.put("authorGroupInfoList", list.getContent());
        response.put("pagination", pagination);
        response.put("paginationInfo", paginationInfo);

        return ResponseEntity.ok(response);
    }

}
