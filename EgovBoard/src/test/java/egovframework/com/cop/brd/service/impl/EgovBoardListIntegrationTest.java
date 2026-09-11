package egovframework.com.cop.brd.service.impl;

import egovframework.com.cop.brd.service.BbsVO;
import egovframework.com.cop.brd.service.BoardDTO;
import egovframework.com.cop.brd.service.EgovBoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EgovBoardListIntegrationTest {

    @Autowired
    private EgovBoardService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listDoesNotDuplicateBoardWithMultipleComments() {
        String bbsId = "BBS_TEST_DUPLICATE";
        long nttId = 999999998L;
        String userId = "TEST_USER";
        jdbcTemplate.update("INSERT INTO COMTNBBSMASTER "
                + "(BBS_ID, BBS_NM, BBS_TY_CODE, FILE_ATCH_POSBL_AT, ATCH_POSBL_FILE_NUMBER, "
                + "USE_AT, FRST_REGISTER_ID, FRST_REGIST_PNTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                bbsId, "중복 검증 게시판", "BBST01", "N", 0, "Y", userId);
        jdbcTemplate.update("INSERT INTO COMTNBBS "
                + "(NTT_ID, BBS_ID, NTT_NO, NTT_SJ, NTT_CN, USE_AT, FRST_REGISTER_ID, FRST_REGIST_PNTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                nttId, bbsId, 1, "댓글 조인 중복 검증", "테스트 내용", "Y", userId);
        jdbcTemplate.update("INSERT INTO COMTNCOMMENT "
                + "(NTT_ID, BBS_ID, ANSWER_NO, ANSWER, USE_AT, FRST_REGISTER_ID, FRST_REGIST_PNTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW()), (?, ?, ?, ?, ?, ?, NOW())",
                nttId, bbsId, 1, "첫 번째 댓글", "Y", userId,
                nttId, bbsId, 2, "두 번째 댓글", "Y", userId);

        BbsVO vo = new BbsVO();
        vo.setBbsId(bbsId);
        vo.setPageIndex(1);
        vo.setFirstIndex(0);
        vo.setPageUnit(10);

        Map<String, Object> result = service.list(vo);

        @SuppressWarnings("unchecked")
        List<BoardDTO> content = (List<BoardDTO>) result.get("content");
        assertThat(content).extracting(BoardDTO::getNttId).containsExactly(nttId);
        assertThat(result.get("totalElements")).isEqualTo(1L);
    }
}
