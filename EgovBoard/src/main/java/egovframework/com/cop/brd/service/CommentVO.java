package egovframework.com.cop.brd.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egovframe.rte.ptl.reactive.validation.EgovNullCheck;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO extends EgovDefaultVO implements Serializable {

    private Long nttId;

    private String bbsId;

    private Long answerNo;

    private String wrterId;

    private String wrterNm;

    @EgovNullCheck(message = "{comCopBbs.boardMasterVO.detail.option2}{common.required.msg}")
    private String answer;

    private String useAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime frstRegistPnttm;

    private String frstRegisterId;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdtPnttm;

    private String lastUpdusrId;

    @JsonIgnore
    private String password;

}
