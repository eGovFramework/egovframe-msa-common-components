package egovframework.com.sec.rgm.service.impl;

import egovframework.com.sec.rgm.service.AuthorGroupDTO;
import egovframework.com.sec.rgm.service.AuthorGroupVO;
import egovframework.com.sec.rgm.service.EgovAuthorGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EgovAuthorGroupServiceImplTest {

    @Autowired
    private EgovAuthorGroupService service;

    @Test
    void listAppliesSearchConditionToTotalCount() {
        AuthorGroupVO authorGroupVO = new AuthorGroupVO();
        authorGroupVO.setFirstIndex(0);
        authorGroupVO.setRecordCountPerPage(10);
        authorGroupVO.setSearchCondition("1");
        authorGroupVO.setSearchKeyword("__NON_EXISTENT_USER_ID__");

        Page<AuthorGroupDTO> result = service.list(authorGroupVO);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
