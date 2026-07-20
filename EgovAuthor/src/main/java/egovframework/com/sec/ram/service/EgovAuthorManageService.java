package egovframework.com.sec.ram.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovAuthorManageService {

    Page<AuthorInfoVO> list(AuthorInfoVO authorInfoVO);

    AuthorInfoVO detail(AuthorInfoVO authorInfoVO, Map<String, String> userInfo);

    AuthorInfoVO insert(AuthorInfoVO authorInfoVO);

    AuthorInfoVO update(AuthorInfoVO authorInfoVO);

    boolean delete(AuthorInfoVO authorInfoVO, Map<String, String> userInfo);

}
