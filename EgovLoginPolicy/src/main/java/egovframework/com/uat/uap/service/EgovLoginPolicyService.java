package egovframework.com.uat.uap.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface EgovLoginPolicyService {

    Page<LoginPolicyDTO> list(LoginPolicyVO loginPolicyVO);

    LoginPolicyVO detail(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo);

    LoginPolicyVO insert(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo);

    LoginPolicyVO update(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo);

    void delete(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo);

}
