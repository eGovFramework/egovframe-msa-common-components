package egovframework.com.uss.olp.qri.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QustnrRspnsResultVOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void qustnrItemListIsRequired() {
        QustnrRspnsResultVO vo = new QustnrRspnsResultVO();

        assertThat(validator.validateProperty(vo, "qustnrItemList")).isNotEmpty();

        vo.setQustnrItemList(new String[0]);

        assertThat(validator.validateProperty(vo, "qustnrItemList")).isNotEmpty();
    }
}
