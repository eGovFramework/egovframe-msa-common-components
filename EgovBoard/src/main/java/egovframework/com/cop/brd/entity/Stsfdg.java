package egovframework.com.cop.brd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "brdStsfdg")
@Table(name = "COMTNSTSFDG")
public class Stsfdg {

    /**
     * 만족도 번호
     */
    @Id
    @Column(name = "STSFDG_NO")
    private String stsfdgNo = "";

    /**
     * 게시판 ID
     */
    @Column(name = "BBS_ID")
    private String bbsId = "";

    /**
     * 게시물 번호
     */
    @Column(name = "NTT_ID")
    private long nttId = 0L;

    /**
     * 작성자 ID
     */
    @Column(name = "WRTER_ID")
    private String wrterId = "";

    /**
     * 작성자명
     */
    @Column(name = "WRTER_NM")
    private String wrterNm = "";

    /**
     * 패스워드
     */
    @Column(name = "PASSWORD")
    private String password = "";

    /**
     * 만족도 내용
     */
    @Column(name = "STSFDG_CN")
    private String stsfdgCn = "";

    /**
     * 만족도
     */
    @Column(name = "STSFDG")
    private int stsfdg = 0;

    /**
     * 사용 여부
     */
    @Column(name = "USE_AT")
    private String useAt = "";

    /**
     * 최초등록자 아이디
     */
    @Column(name = "FRST_REGISTER_ID")
    private String frstRegisterId = "";

    /**
     * 최초등록시점
     */
    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    /**
     * 최종수정자 아이디
     */
    @Column(name = "LAST_UPDUSR_ID")
    private String lastUpdusrId = "";

    /**
     * 최종수정시점
     */
    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdusrPnttm;
}
