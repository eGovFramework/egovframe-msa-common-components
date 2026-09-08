package egovframework.com.cop.brd.web;

import egovframework.com.cop.brd.service.CommentVO;
import egovframework.com.cop.brd.service.EgovCommentService;
import egovframework.com.cop.brd.service.EgovStsfdgService;
import egovframework.com.cop.brd.service.StsfdgVO;
import egovframework.com.pagination.EgovKrdsPaginationRenderer;
import org.egovframe.boot.crypto.service.EgovEnvCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EgovCommentAPIControllerTest {

    @Mock
    private EgovCommentService commentService;

    @Mock
    private EgovStsfdgService stsfdgService;

    @Mock
    private EgovEnvCryptoService cryptoService;

    @Mock
    private EgovKrdsPaginationRenderer paginationRenderer;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EgovCommentAPIController controller = new EgovCommentAPIController(
                commentService, stsfdgService, cryptoService, paginationRenderer);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deleteCommentDoesNotRequireAnswer() throws Exception {
        when(cryptoService.decrypt(anyString())).thenReturn("test-user");

        mockMvc.perform(post("/cop/brd/deleteComment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "encrypted-user-id")
                        .header("X-USER-NM", "encrypted-user-name")
                        .header("X-UNIQ-ID", "encrypted-uniq-id")
                        .content("""
                                {
                                  "bbsId": "BBSMSTR_000000000001",
                                  "nttId": 1,
                                  "answerNo": 1
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<CommentVO> commentCaptor = ArgumentCaptor.forClass(CommentVO.class);
        verify(commentService).deleteArticleComment(commentCaptor.capture(), anyMap());

        CommentVO comment = commentCaptor.getValue();
        assertThat(comment.getBbsId()).isEqualTo("BBSMSTR_000000000001");
        assertThat(comment.getNttId()).isEqualTo(1L);
        assertThat(comment.getAnswerNo()).isEqualTo(1L);
        assertThat(comment.getAnswer()).isNull();
    }

    @Test
    void insertSatisfactionAcceptsFivePoints() throws Exception {
        when(cryptoService.decrypt(anyString())).thenReturn("test-user");

        mockMvc.perform(post("/cop/brd/insertStsfdg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "encrypted-user-id")
                        .header("X-USER-NM", "encrypted-user-name")
                        .header("X-UNIQ-ID", "encrypted-uniq-id")
                        .content("""
                                {
                                  "bbsId": "BBSMSTR_000000000001",
                                  "nttId": 1,
                                  "stsfdgCn": "만족합니다.",
                                  "stsfdg": 5
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<StsfdgVO> satisfactionCaptor = ArgumentCaptor.forClass(StsfdgVO.class);
        verify(stsfdgService).insertStsfdg(satisfactionCaptor.capture(), anyMap());
        assertThat(satisfactionCaptor.getValue().getStsfdg()).isEqualTo(5);
    }

    @Test
    void insertSatisfactionRejectsMoreThanFivePoints() throws Exception {
        mockMvc.perform(post("/cop/brd/insertStsfdg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bbsId": "BBSMSTR_000000000001",
                                  "nttId": 1,
                                  "stsfdgCn": "범위를 벗어난 점수",
                                  "stsfdg": 6
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(stsfdgService);
    }
}
