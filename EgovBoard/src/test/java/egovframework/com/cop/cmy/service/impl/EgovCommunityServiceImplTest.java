package egovframework.com.cop.cmy.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.querydsl.jpa.impl.JPAQueryFactory;

import egovframework.com.cop.cmy.entity.Cmmnty;
import egovframework.com.cop.cmy.repository.EgovCommunityRepository;
import egovframework.com.cop.cmy.repository.EgovCommunityUserRepository;
import egovframework.com.cop.cmy.service.CommunityVO;

@ExtendWith(MockitoExtension.class)
class EgovCommunityServiceImplTest {

    @Mock
    EgovCommunityRepository repository;

    @Mock
    EgovCommunityUserRepository userRepository;

    @Mock
    EgovIdGnrService idgenService;

    @Mock
    JPAQueryFactory queryFactory;

    @InjectMocks
    EgovCommunityServiceImpl service;

    @Test
    void updateRejectsNonMember() {
        CommunityVO request = new CommunityVO();
        request.setCmmntyId("CMMNTY_TEST");
        Cmmnty community = new Cmmnty();
        community.setCmmntyId("CMMNTY_TEST");
        community.setFrstRegisterId("OWNER");
        when(repository.findById("CMMNTY_TEST")).thenReturn(Optional.of(community));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(request, Map.of("uniqId", "NON_MEMBER")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("수정 권한이 없습니다.");
        verify(repository, never()).save(any());
    }
}
