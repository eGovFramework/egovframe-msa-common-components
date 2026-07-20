package egovframework.com.sec.rgm.service;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface EgovAuthorGroupService {

    Page<AuthorGroupDTO> list(AuthorGroupVO authorGroupVO);

    List<AuthorInfoVO> authorInfoList();

    AuthorGroupVO insert(AuthorGroupVO authorGroupVO);

    AuthorGroupVO update(AuthorGroupVO authorGroupVO);

    void delete(AuthorGroupVO authorGroupVO, Map<String, String> userInfo);

}
