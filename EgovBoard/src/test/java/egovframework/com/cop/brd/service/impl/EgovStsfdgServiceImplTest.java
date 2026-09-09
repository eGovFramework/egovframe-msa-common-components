package egovframework.com.cop.brd.service.impl;

import egovframework.com.cop.brd.repository.EgovStsfdgRepository;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EgovStsfdgServiceImplTest {

    @Mock
    private EgovStsfdgRepository egovStsfdgRepository;

    @Mock
    private EgovIdGnrService idgenServiceStsfdgNo;

    private EgovStsfdgServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EgovStsfdgServiceImpl(egovStsfdgRepository, idgenServiceStsfdgNo);
    }

    @Test
    void deleteStsfdgThrowsWhenRowMissing() {
        when(egovStsfdgRepository.findById("STSFDG_00000000999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteStsfdg("STSFDG_00000000999",
                Map.of("uniqId", "USRCNFRM_00000000000")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("만족도를 찾을 수 없습니다.");
    }
}
