package egovframework.com.ext.ops.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class BoardVectorVO extends EgovDefaultVO implements Serializable {

    private static final long serialVersionUID = 2369702238824949925L;

    /** 게시물 아이디 */
    private Long nttId;

    /** 게시판 아이디 */
    private String bbsId;

    /** 게시물 번호 */
    private String nttNo;

    /** 게시물 제목 */
    private String nttSj;

    /** 게시물 내용 */
    private String nttCn;

    /** 사용여부 */
    private String useAt;

    /** 게시시작일 */
    private String ntceBgnde;

    /** 게시종료일 */
    private String ntceEndde;

    /** 게시자 아이디 */
    private String ntcrId;

    /** 게시자명 */
    private String ntcrNm;

    /** 패스워드 */
    @JsonIgnore
    private String password;

    /** 게시물 첨부파일 아이디 */
    private String atchFileId;

    /** 검색시작일 */
    private String searchBgnDe = "";

    /** 검색조건 */
    private String searchCnd = "";

    /** 검색종료일 */
    private String searchEndDe = "";

    /** 검색단어 */
    private String searchWrd = "";

    /** 정렬순서(DESC,ASC) */
    private long sortOrdr = 0L;

    /** 레코드 번호 */
    private int rowNo = 0;

    /** 최초등록자 아이디 */
    private String frstRegisterId;

    /** 최초 등록자명 */
    private String frstRegisterNm = "";

    /** 최초 수정시점 */
    private String frstRegistPnttm;

    /** 최종수정자 아이디 */
    private String lastUpdusrId;

    /** 최종 수정자명 */
    private String lastUpdusrNm = "";

    /** 최종 수정시점 */
    private String lastUpdtPnttm;

    /** 유효여부 */
    private String isExpired = "N";

    /** 상위 정렬 순서 */
    private String parntsSortOrdr = "";

    /** 상위 답변 위치 */
    private String parntsReplyLc = "";

    /** 게시판 유형코드 */
    private String bbsTyCode = "";

    /** 게시판 속성코드 */
    private String bbsAttrbCode = "";

    /** 게시판 명 */
    private String bbsNm = "";

    /** 파일첨부가능여부 */
    private String fileAtchPosblAt = "";

    /** 첨부가능파일숫자 */
    private int posblAtchFileNumber = 0;

    /** 답장가능여부 */
    private String replyPosblAt = "";

    /** 조회 수 증가 여부 */
    private boolean plusCount = false;

    /** 익명등록 여부 */
    private String anonymousAt = "";

    /** 하위 페이지 인덱스 (댓글 및 만족도 조사 여부 확인용) */
    private String subPageIndex = "";

    /** 게시글 댓글개수 */
    private String commentCo = "";

    /** 볼드체 여부 */
    private String sjBoldAt;

    /** 공지 여부 */
    private String noticeAt;

    /** 비밀글 여부 */
    private String secretAt;

    /** 유사도 점수 */
    private double score;

    /** 임베딩 값 */
    private float[] bbsArticleVector;

}
