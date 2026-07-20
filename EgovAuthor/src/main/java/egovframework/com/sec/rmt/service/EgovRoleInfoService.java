package egovframework.com.sec.rmt.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovRoleInfoService {

    Page<RoleInfoDTO> list(RoleInfoVO roleInfoVO);

    RoleInfoVO detail(RoleInfoVO roleInfoVO, Map<String, String> userInfo);

    RoleInfoVO insert(RoleInfoVO roleInfoVO);

    RoleInfoVO update(RoleInfoVO roleInfoVO);

    void delete(RoleInfoVO roleInfoVO, Map<String, String> userInfo);

}
