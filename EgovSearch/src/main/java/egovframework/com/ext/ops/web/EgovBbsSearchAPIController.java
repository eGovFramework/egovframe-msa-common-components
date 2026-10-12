package egovframework.com.ext.ops.web;

import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.BoardVectorVO;
import egovframework.com.ext.ops.service.EgovBbsSearchService;
import egovframework.com.pagination.EgovPaginationFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/ext/ops")
@RequiredArgsConstructor
@Slf4j
public class EgovBbsSearchAPIController {

	@Value("${egov.textsearch.count}")
    private int textSearchCount;
	
	@Value("${egov.textsearch.page.size}")
    private int textSearchPageSize;
	
	@Value("${egov.vectorsearch.count}")
    private int vectorSearchCount;
	
	@Value("${egov.vectorsearch.page.size}")
    private int  vectorSearchPageSize;
	
	private final EgovBbsSearchService service;
	
	@PostMapping("/textSearchResult")
	public ResponseEntity<?> selectBbsTextSearchList(@ModelAttribute BoardVO boardVO) throws Exception {
		if (boardVO.getPageIndex() < 1) {
			return ResponseEntity.badRequest().body("pageIndex must be greater than zero");
		}
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(textSearchPageSize);
		paginationInfo.setPageSize(boardVO.getPageSize()); // 화면에 보여질 페이지 번호 개수

        boardVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
        boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
        boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		Page<BoardVO> textSearchList = this.service.textSearch(boardVO);
		paginationInfo.setTotalRecordCount((int)textSearchList.getTotalElements()); // 실제 검색 결과 총 개수 사용

		EgovPaginationFormat egovPaginationFormat = new EgovPaginationFormat();
		String pagination = egovPaginationFormat.paginationFormat(paginationInfo, "linkPage");

		Map<String, Object> response = new HashMap<>();
		response.put("bbsTextSearchList", textSearchList.getContent());
		response.put("pagination", pagination);
		response.put("lineNumber", (boardVO.getPageIndex() - 1) * boardVO.getPageSize());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/vectorSearchResult")
	public ResponseEntity<?> selectBbsVectorSearchList(@ModelAttribute BoardVO boardVO) throws Exception {
		if (boardVO.getPageIndex() < 1) {
			return ResponseEntity.badRequest().body("pageIndex must be greater than zero");
		}
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(boardVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(vectorSearchPageSize);
		paginationInfo.setPageSize(boardVO.getPageSize()); // 화면에 보여질 페이지 번호 개수

		boardVO.setFirstIndex(paginationInfo.getCurrentPageNo()-1);
		boardVO.setLastIndex(paginationInfo.getLastRecordIndex());
		boardVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		Page<BoardVectorVO> vectorSearchList = this.service.vectorSearch(boardVO);
		paginationInfo.setTotalRecordCount((int)vectorSearchList.getTotalElements()); // 실제 검색 결과 총 개수 사용

		EgovPaginationFormat egovPaginationFormat = new EgovPaginationFormat();
		String pagination = egovPaginationFormat.paginationFormat(paginationInfo, "linkPage");

		Map<String, Object> response = new HashMap<>();
		response.put("bbsVectorSearchList", vectorSearchList.getContent());
		response.put("pagination", pagination);
		response.put("lineNumber", (boardVO.getPageIndex() - 1) * boardVO.getPageSize());
		return ResponseEntity.ok(response);
	}

}
