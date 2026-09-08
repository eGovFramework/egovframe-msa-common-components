package egovframework.com.cop.brd.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.com.cop.brd.service.EgovCommentService;
import egovframework.com.cop.brd.service.EgovStsfdgService;
import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import org.egovframe.boot.crypto.service.EgovEnvCryptoService;

/**
 * 댓글 삭제 요청 계약.
 *
 * 화면(boardDetail.html)은 삭제에 bbsId · nttId · answerNo 만 보내고
 * 삭제 로직(EgovCommentServiceImpl#deleteArticleComment)도 그 셋만 쓴다.
 *
 * @author 최완택
 * @since 2026-09-04
 */
@DisplayName("댓글 삭제 API 요청 계약")
class EgovCommentDeleteRequestTest {

	private static final String SCREEN_PAYLOAD =
		"{\"bbsId\":\"BBSMSTR_000000000001\",\"nttId\":1,\"answerNo\":1}";

	@Test
	@DisplayName("화면이 보내는 삭제 요청을 400 으로 거절하지 않는다")
	void acceptsPayloadTheScreenSends() throws Exception {
		EgovEnvCryptoService crypto = mock(EgovEnvCryptoService.class);
		when(crypto.decrypt(anyString())).thenReturn("USRCNFRM_00000000000");

		EgovCommentAPIController controller = new EgovCommentAPIController(
			mock(EgovCommentService.class),
			mock(EgovStsfdgService.class),
			crypto,
			mock(EgovKrdsPaginationRenderer.class));

		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(post("/cop/brd/deleteComment")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-USER-ID", "encrypted")
				.header("X-USER-NM", "encrypted")
				.header("X-UNIQ-ID", "encrypted")
				.content(SCREEN_PAYLOAD))
			.andExpect(status().isOk());
	}

}
