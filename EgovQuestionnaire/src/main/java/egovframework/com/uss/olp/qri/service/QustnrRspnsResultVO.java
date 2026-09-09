package egovframework.com.uss.olp.qri.service;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egovframe.rte.ptl.reactive.validation.EgovNullCheck;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QustnrRspnsResultVO extends EgovDefaultVO implements Serializable {

    private static final long serialVersionUID = 4557856668675562522L;

    private String qustnrTmplatId;

    private String qestnrId;

    private String qustnrIemId;

    private String qustnrQesitmId;

    private String qustnrRspnsResultId;

    private String respondAnswerCn;

    private String etcAnswerCn;

    @EgovNullCheck(message="{comUssOlpQri.regist.respondNm}{common.required.msg}")
    private String respondNm;

    @NotEmpty(message="{comUssOlpQri.regist.qustnrIem}{common.required.msg}")
    private String[] qustnrItemList;

    private String frstRegistPnttm;

    private String frstRegisterId;

    private String lastUpdtPnttm;

    private String lastUpdusrId;

    private String qustnrRespondId;

    @EgovNullCheck(message="{comUssOlpQnn.regist.sexdstnCode}{common.required.msg}")
    private String sexdstnCode;

    @EgovNullCheck(message="{comUssOlpQnn.regist.occpTyCode}{common.required.msg}")
    private String occpTyCode;

}
