package egovframework.com.cop.brd.service;

import egovframework.com.cop.bbs.service.EgovDefaultVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BbsMasterOptnVO extends EgovDefaultVO implements Serializable {

    private static final long serialVersionUID = 8989516990850924438L;

    private String bbsId;

    private String answerAt;

    private String stsfdgAt;

    private LocalDateTime frstRegistPnttm;

    private String frstRegisterId;

    private LocalDateTime lastUpdtPnttm;

    private String lastUpdusrId;

}
