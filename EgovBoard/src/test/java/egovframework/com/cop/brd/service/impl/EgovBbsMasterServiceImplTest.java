package egovframework.com.cop.brd.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cop.brd.entity.BbsMasterOptn;
import egovframework.com.cop.brd.repository.EgovBbsMasterOptnRepository;
import egovframework.com.cop.brd.repository.EgovBbsMasterRepository;
import egovframework.com.cop.brd.service.BbsMasterOptnVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EgovBbsMasterServiceImplTest {

    @Mock
    private EgovBbsMasterOptnRepository optnRepository;

    @Mock
    private EgovBbsMasterRepository masterRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private EgovBbsMasterServiceImpl service;

    @Test
    void selectBBSMasterOptnFindsOptionOnce() {
        String bbsId = "BBSMSTR_000000000001";
        BbsMasterOptn option = new BbsMasterOptn();
        option.setBbsId(bbsId);
        option.setAnswerAt("Y");
        option.setStsfdgAt("N");
        when(optnRepository.findById(bbsId)).thenReturn(Optional.of(option));

        BbsMasterOptnVO result = service.selectBBSMasterOptn(bbsId);

        assertThat(result.getBbsId()).isEqualTo(bbsId);
        assertThat(result.getAnswerAt()).isEqualTo("Y");
        assertThat(result.getStsfdgAt()).isEqualTo("N");
        verify(optnRepository).findById(bbsId);
    }

    @Test
    void selectBBSMasterOptnReturnsNullWhenOptionDoesNotExist() {
        String bbsId = "BBSMSTR_000000000001";
        when(optnRepository.findById(bbsId)).thenReturn(Optional.empty());

        BbsMasterOptnVO result = service.selectBBSMasterOptn(bbsId);

        assertThat(result).isNull();
        verify(optnRepository).findById(bbsId);
    }
}
