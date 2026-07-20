package egovframework.com.cop.brd.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egovframe.rte.ptl.reactive.validation.EgovNullCheck;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StsfdgVO extends EgovDefaultVO implements Serializable {
    private String stsfdgNo = "";
    private String bbsId = "";
    private long nttId = 0L;
    private String wrterId = "";
    private String wrterNm = "";
    @JsonIgnore
    private String password = "";
    @EgovNullCheck(message = "{comCopBbs.boardMasterVO.detail.option2}{common.required.msg}")
    private String stsfdgCn = "";
    private int stsfdg = 0;
    private String useAt = "";
    private String frstRegisterId = "";
    private LocalDateTime frstRegistPnttm;
    private String lastUpdusrId = "";
    private LocalDateTime lastUpdusrPnttm;
}
