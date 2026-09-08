package egovframework.com.cop.brd.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cop.brd.entity.Bbs;
import egovframework.com.cop.brd.entity.BbsId;
import egovframework.com.cop.brd.entity.QBbs;
import egovframework.com.cop.brd.entity.QBbsMaster;
import egovframework.com.cop.brd.entity.QUserMaster;
import egovframework.com.cop.brd.repository.EgovBbsSyncLogRepository;
import egovframework.com.cop.brd.repository.EgovBoardRepository;
import egovframework.com.cop.brd.repository.EgovCommentRepository;
import egovframework.com.cop.brd.service.BbsVO;
import egovframework.com.cop.brd.service.EgovFileService;
import egovframework.com.cop.brd.util.EgovFileUtility;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EgovBoardServiceImplTest {

    @Mock
    private EgovBoardRepository repository;

    @Mock
    private EgovBbsSyncLogRepository bbsSyncLogRepository;

    @Mock
    private EgovCommentRepository commentRepository;

    @Mock
    private EgovFileUtility fileUtility;

    @Mock
    private EgovFileService fileService;

    @Mock
    private EgovIdGnrService boardIdGnrService;

    @Mock
    private EgovIdGnrService bbsSyncLogIdGnrService;

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private JPAQueryFactory queryFactory;

    private EgovBoardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EgovBoardServiceImpl(
                repository,
                bbsSyncLogRepository,
                commentRepository,
                fileUtility,
                fileService,
                boardIdGnrService,
                bbsSyncLogIdGnrService,
                streamBridge,
                queryFactory);
    }

    @Test
    void updateSavesBoardOnce() throws Exception {
        Bbs existingBoard = createExistingBoard();
        when(repository.findById(existingBoard.getBbsId())).thenReturn(Optional.of(existingBoard));

        Tuple tuple = mock(Tuple.class);
        when(tuple.get(QBbs.bbs)).thenReturn(existingBoard);
        when(tuple.get(QUserMaster.userMaster)).thenReturn(null);
        when(tuple.get(QBbsMaster.bbsMaster)).thenReturn(null);

        @SuppressWarnings("unchecked")
        JPAQuery<Tuple> detailQuery = mock(JPAQuery.class, RETURNS_SELF);
        when(queryFactory.select(any(Expression[].class))).thenReturn(detailQuery);
        when(detailQuery.fetchOne()).thenReturn(tuple);
        when(repository.save(any(Bbs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BbsVO board = new BbsVO();
        board.setBbsId(existingBoard.getBbsId().getBbsId());
        board.setNttId(existingBoard.getBbsId().getNttId());
        board.setNttSj("수정된 제목");
        board.setNttCn("수정된 내용");

        BbsVO result = service.update(
                board,
                Collections.emptyList(),
                Map.of("uniqId", existingBoard.getFrstRegisterId()));

        verify(repository, times(1)).save(any(Bbs.class));
        assertThat(result.getNttSj()).isEqualTo("수정된 제목");
        assertThat(result.getNttCn()).isEqualTo("수정된 내용");
    }

    private Bbs createExistingBoard() {
        BbsId id = new BbsId();
        id.setBbsId("BBSMSTR_000000000001");
        id.setNttId(1L);

        Bbs board = new Bbs();
        board.setBbsId(id);
        board.setNttNo(1);
        board.setRdcnt(0);
        board.setParntscttNo(0);
        board.setAnswerLc(0);
        board.setSortOrdr(0);
        board.setNttSj("기존 제목");
        board.setNttCn("기존 내용");
        board.setFrstRegisterId("USRCNFRM_00000000000");
        board.setFrstRegistPnttm(LocalDateTime.now());
        board.setUseAt("Y");
        return board;
    }
}
