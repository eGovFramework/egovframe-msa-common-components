package egovframework.com.uss.olp.qri.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qri.entity.QustnrRspnsResult;
import egovframework.com.uss.olp.qri.repository.EgovQestnrInfoRepository;
import egovframework.com.uss.olp.qri.repository.EgovQustnrRespondInfoRepository;
import egovframework.com.uss.olp.qri.repository.EgovQustnrRspnsResultRepository;
import egovframework.com.uss.olp.qri.service.QustnrRspnsResultVO;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EgovQustnrRspnsResultServiceImplTest {

    @Mock
    private EgovQustnrRspnsResultRepository repository;
    @Mock
    private EgovQustnrRespondInfoRepository qustnrRespondInfoRepository;
    @Mock
    private EgovQestnrInfoRepository qestnrInfoRepository;
    @Mock
    private EgovIdGnrService idgenService;
    @Mock
    private EgovIdGnrService qustnrRespondInfoIdgenService;
    @Mock
    private JPAQueryFactory queryFactory;

    @Test
    void insertPreservesCommaInEssayAnswer() throws Exception {
        EgovQustnrRspnsResultServiceImpl service = new EgovQustnrRspnsResultServiceImpl(
                repository,
                qustnrRespondInfoRepository,
                qestnrInfoRepository,
                idgenService,
                qustnrRespondInfoIdgenService,
                queryFactory
        );
        QustnrRspnsResultVO request = new QustnrRspnsResultVO();
        request.setQustnrTmplatId("TMPLAT_1");
        request.setQestnrId("QESTNR_1");
        request.setQustnrItemList(new String[]{"2,QESITM_1,좋음, 보통"});

        when(qustnrRespondInfoIdgenService.getNextStringId()).thenReturn("RESPOND_1");
        when(idgenService.getNextStringId()).thenReturn("RESULT_1");

        boolean result = service.insert(request, Map.of("uniqId", "USER_1"));

        ArgumentCaptor<QustnrRspnsResult> captor = ArgumentCaptor.forClass(QustnrRspnsResult.class);
        verify(repository).save(captor.capture());
        assertThat(result).isTrue();
        assertThat(captor.getValue().getRespondAnswerCn()).isEqualTo("좋음, 보통");
    }
}
