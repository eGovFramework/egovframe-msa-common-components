package egovframework.com.uss.olp.qim.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import egovframework.com.uss.olp.qim.entity.QestnrInfo;
import egovframework.com.uss.olp.qim.entity.QestnrInfoId;
import egovframework.com.uss.olp.qim.entity.QustnrQesitm;
import egovframework.com.uss.olp.qim.entity.QustnrQesitmId;
import egovframework.com.uss.olp.qim.repository.EgovQestnrInfoRepository;
import egovframework.com.uss.olp.qim.repository.EgovQustnrQesitmRepository;
import egovframework.com.uss.olp.qim.service.EgovQustnrItemService;
import egovframework.com.uss.olp.qim.service.QustnrIemVO;
import egovframework.com.uss.olp.qmc.entity.QustnrTmplat;
import egovframework.com.uss.olp.qmc.repository.EgovQustnrTmplatRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Transactional
@Slf4j
class EgovQustnrItemServiceImplTest {

	@Autowired
	private EgovQustnrItemService egovQustnrItemService;

	@Autowired
	private EgovQustnrQesitmRepository egovQustnrQesitmRepository;

	@Autowired
	private EgovQestnrInfoRepository egovQestnrInfoRepository;

	@Autowired
	private EgovQustnrTmplatRepository egovQustnrTmplatRepository;

	@Test
	void insert() {
		// given
		LocalDateTime now = LocalDateTime.now();
		String now2 = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String qustnrBgnde = now.format(dateFormatter);
		String qustnrEndde = now.plusDays(1).format(dateFormatter);
		String testId = "TEST_" + now2;
		String testText = "test 이백행_" + now2;
		String qustnrTmplatId = testId;
		String qestnrId = testId;
		String qustnrQesitmId = testId;
		String uniqId = testId;

		QustnrTmplat template = new QustnrTmplat();
		template.setQustnrTmplatId(qustnrTmplatId);
		template.setQustnrTmplatTy("1");
		template.setQustnrTmplatDc(testText + "_설문 템플릿 설명");
		template.setQustnrTmplatPathNm("test");
		template.setFrstRegistPnttm(now);
		template.setFrstRegisterId(uniqId);
		template.setLastUpdtPnttm(now);
		template.setLastUpdusrId(uniqId);
		egovQustnrTmplatRepository.save(template);

		QestnrInfoId surveyId = new QestnrInfoId();
		surveyId.setQustnrTmplatId(qustnrTmplatId);
		surveyId.setQestnrId(qestnrId);

		QestnrInfo survey = new QestnrInfo();
		survey.setQestnrInfoId(surveyId);
		survey.setQustnrSj(testText + "_설문 제목");
		survey.setQustnrPurps(testText + "_설문 목적");
		survey.setQustnrWritingGuidanceCn(testText + "_설문 작성 안내");
		survey.setQustnrTrget(testText + "_설문 대상");
		survey.setQustnrBgnde(qustnrBgnde);
		survey.setQustnrEndde(qustnrEndde);
		survey.setFrstRegistPnttm(now);
		survey.setFrstRegisterId(uniqId);
		survey.setLastUpdtPnttm(now);
		survey.setLastUpdusrId(uniqId);
		egovQestnrInfoRepository.save(survey);

		QustnrQesitmId parentId = new QustnrQesitmId();
		parentId.setQustnrTmplatId(qustnrTmplatId);
		parentId.setQestnrId(qestnrId);
		parentId.setQustnrQesitmId(qustnrQesitmId);

		QustnrQesitm parent = new QustnrQesitm();
		parent.setQustnrQesitmId(parentId);
		parent.setQestnSn("1");
		parent.setQestnTyCode("1");
		parent.setQestnCn(testText + "_설문 문항 내용");
		parent.setMxmmChoiseCo("1");
		parent.setFrstRegistPnttm(now);
		parent.setFrstRegisterId(uniqId);
		parent.setLastUpdtPnttm(now);
		parent.setLastUpdusrId(uniqId);
		egovQustnrQesitmRepository.save(parent);

		QustnrIemVO qustnrIemVO = new QustnrIemVO();
		qustnrIemVO.setQustnrTmplatId(qustnrTmplatId);
		qustnrIemVO.setQestnrId(qestnrId);
		qustnrIemVO.setQustnrQesitmId(qustnrQesitmId);
		qustnrIemVO.setIemSn("999");
		qustnrIemVO.setIemCn(testText + "_설문 항목 내용");
		qustnrIemVO.setEtcAnswerAt("N");

		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("uniqId", uniqId);

		// when
		QustnrIemVO result = egovQustnrItemService.insert(qustnrIemVO, userInfo);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getQustnrIemId()).isNotBlank();
		assertThat(result.getIemSn()).isEqualTo(qustnrIemVO.getIemSn());
		assertThat(result.getIemCn()).isEqualTo(qustnrIemVO.getIemCn());
		assertThat(result.getEtcAnswerAt()).isEqualTo(qustnrIemVO.getEtcAnswerAt());
		assertThat(result.getFrstRegisterId()).isEqualTo(userInfo.get("uniqId"));
		assertThat(result.getLastUpdusrId()).isEqualTo(userInfo.get("uniqId"));
		assertThat(result.getFrstRegistPnttm()).isNotNull();
		assertThat(result.getLastUpdtPnttm()).isNotNull();

		log.debug("qustnrIemId={}", result.getQustnrIemId());
		log.debug("iemCn={}", result.getIemCn());
	}
}
