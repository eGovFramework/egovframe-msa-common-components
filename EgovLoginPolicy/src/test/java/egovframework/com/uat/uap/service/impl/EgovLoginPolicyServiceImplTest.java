package egovframework.com.uat.uap.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.uat.uap.service.EgovLoginPolicyService;
import egovframework.com.uat.uap.service.LoginPolicyDTO;
import egovframework.com.uat.uap.service.LoginPolicyVO;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Transactional
@Slf4j
class EgovLoginPolicyServiceImplTest {

	@Autowired
	EgovLoginPolicyService egovLoginPolicyService;

	@Test
	void insert() {
		// given
		LoginPolicyVO loginPolicyVO = new LoginPolicyVO();

		// `EMPLYR_ID` varchar(20) NOT NULL COMMENT '업무사용자ID',
//		loginPolicyVO.setEmployerId("TEST1");
		loginPolicyVO.setFirstIndex(0);
		loginPolicyVO.setRecordCountPerPage(1);
		Page<LoginPolicyDTO> list = egovLoginPolicyService.list(loginPolicyVO);
		for (LoginPolicyDTO result : list) {
			loginPolicyVO.setEmployerId(result.getUserId());
		}

		// `IP_INFO` varchar(23) NOT NULL COMMENT 'IP정보',
		loginPolicyVO.setIpInfo("127.0.0.1");

		// `DPLCT_PERM_AT` char(1) NOT NULL COMMENT '중복허용여부',
		loginPolicyVO.setDplctPermAt("N");

		// `LMTT_AT` char(1) NOT NULL COMMENT '제한여부',
		loginPolicyVO.setLmttAt("Y");

		Map<String, String> userInfo = new HashMap<>();

		// when
		LoginPolicyVO result = egovLoginPolicyService.insert(loginPolicyVO, userInfo);

		// then
		assertThat(result).isNotNull();

		assertThat(result.getEmployerId()).isEqualTo(loginPolicyVO.getEmployerId());

		log.debug("result, loginPolicyVO");
		log.debug("getEmployerId={}, {}", result.getEmployerId(), loginPolicyVO.getEmployerId());

		log.debug("getFrstRegisterId={}, {}", result.getFrstRegisterId(), loginPolicyVO.getFrstRegisterId());
		log.debug("getFrstRegisterPnttm={}, {}", result.getFrstRegisterPnttm(), loginPolicyVO.getFrstRegisterPnttm());
	}

}
