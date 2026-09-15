package egovframework.com.ext.ops.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import egovframework.com.ext.ops.service.BoardVO;
import egovframework.com.ext.ops.service.EgovBbsSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class EgovBbsSearchAPIControllerTest {

    @Test
    void textSearchRejectsPageZero() throws Exception {
        EgovBbsSearchService service = mock(EgovBbsSearchService.class);
        EgovBbsSearchAPIController controller = controller(service);
        BoardVO request = new BoardVO();
        request.setPageIndex(0);
        when(service.textSearch(any(BoardVO.class))).thenReturn(Page.empty());

        ResponseEntity<?> response = controller.selectBbsTextSearchList(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(service);
    }

    @Test
    void vectorSearchRejectsNegativePage() throws Exception {
        EgovBbsSearchService service = mock(EgovBbsSearchService.class);
        EgovBbsSearchAPIController controller = controller(service);
        BoardVO request = new BoardVO();
        request.setPageIndex(-1);
        when(service.vectorSearch(any(BoardVO.class))).thenReturn(Page.empty());

        ResponseEntity<?> response = controller.selectBbsVectorSearchList(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(service);
    }

    @Test
    void textSearchKeepsValidFirstPage() throws Exception {
        EgovBbsSearchService service = mock(EgovBbsSearchService.class);
        EgovBbsSearchAPIController controller = controller(service);
        when(service.textSearch(any(BoardVO.class))).thenReturn(Page.empty());

        ResponseEntity<?> response = controller.selectBbsTextSearchList(new BoardVO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private EgovBbsSearchAPIController controller(EgovBbsSearchService service) {
        EgovBbsSearchAPIController controller = new EgovBbsSearchAPIController(service);
        ReflectionTestUtils.setField(controller, "textSearchPageSize", 10);
        ReflectionTestUtils.setField(controller, "vectorSearchPageSize", 10);
        return controller;
    }
}
