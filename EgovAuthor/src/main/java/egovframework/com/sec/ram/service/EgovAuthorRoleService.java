package egovframework.com.sec.ram.service;

import org.springframework.data.domain.Page;

public interface EgovAuthorRoleService {

    Page<RoleInfoDTO> list(AuthorRoleRelatedVO authorRoleRelatedVO);

    AuthorRoleRelatedVO insert(AuthorRoleRelatedVO authorRoleRelatedVO);

    boolean delete(AuthorRoleRelatedVO authorRoleRelatedVO);

    boolean updateRoleInfoList(String[] authorCodes, String[] roleCodes, String[] regYns, AuthorRoleRelatedVO authorRoleRelatedVO);

}
