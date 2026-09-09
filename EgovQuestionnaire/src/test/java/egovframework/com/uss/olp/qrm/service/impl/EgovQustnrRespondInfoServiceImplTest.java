package egovframework.com.uss.olp.qrm.service.impl;

import egovframework.com.uss.olp.qrm.service.EgovQustnrRespondInfoService;
import egovframework.com.uss.olp.qrm.service.QustnrRespondInfoDTO;
import egovframework.com.uss.olp.qrm.service.QustnrRespondInfoVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EgovQustnrRespondInfoServiceImplTest {

    @Autowired
    private EgovQustnrRespondInfoService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listReturnsSeparateSexAndOccupationCodeNames() {
        String templateId = "TEST_CODE_ALIAS";
        String questionnaireId = "TEST_CODE_ALIAS";
        String respondentId = "TEST_CODE_ALIAS";
        jdbcTemplate.update("INSERT INTO COMTNQUSTNRTMPLAT "
                + "(QUSTNR_TMPLAT_ID, QUSTNR_TMPLAT_TY) VALUES (?, ?)", templateId, "1");
        jdbcTemplate.update("INSERT INTO COMTNQESTNRINFO "
                + "(QUSTNR_TMPLAT_ID, QESTNR_ID, QUSTNR_SJ) VALUES (?, ?, ?)",
                templateId, questionnaireId, "별칭 검증 설문");
        jdbcTemplate.update("INSERT INTO COMTNQUSTNRRESPONDINFO "
                + "(QUSTNR_TMPLAT_ID, QESTNR_ID, QUSTNR_RESPOND_ID, SEXDSTN_CODE, OCCP_TY_CODE, "
                + "RESPOND_NM, FRST_REGIST_PNTTM, LAST_UPDT_PNTTM) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                templateId, questionnaireId, respondentId, "M", "1", "별칭 검증 응답자");

        QustnrRespondInfoVO vo = new QustnrRespondInfoVO();
        vo.setFirstIndex(0);
        vo.setRecordCountPerPage(10);

        Page<QustnrRespondInfoDTO> result = service.list(vo);

        assertThat(result.getContent())
                .filteredOn(item -> respondentId.equals(item.getQustnrRespondId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getSexdstnNm()).isEqualTo("남자");
                    assertThat(item.getOccpTyNm()).isEqualTo("학생");
                });
    }
}
