package egovframework.com.ext.ops.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BbsDTO {

    private Long nttId;
    private String bbsId;
    private String nttNo;
    private String nttSj;
    private String nttCn;
    private String answerAt;
    private String parntscttNo;
    private String answerLc;
    private String sortOrdr;
    private String rdcnt;
    private String useAt;
    private String ntceBgnde;
    private String ntceEndde;
    private String ntcrId;
    private String ntcrNm;
    @JsonIgnore
    private String password;
    private String atchFileId;
    private String noticeAt;
    private String sjBoldAt;
    private String secretAt;
    private String frstRegistPnttm;
    private String frstRegisterId;
    private String frstRegisterNm;
    private String lastUpdtPnttm;
    private String lastUpdusrId;
    private String bbsNm;
    private String bbsIntrcn;
    private String bbsTyCode;
    private String replyPosblAt;
    private String fileAtchPosblAt;
    private Integer atchPosblFileNumber;

}
