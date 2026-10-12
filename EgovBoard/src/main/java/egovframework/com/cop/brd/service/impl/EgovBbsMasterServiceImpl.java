package egovframework.com.cop.brd.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cop.brd.entity.*;
import egovframework.com.cop.brd.repository.EgovBbsMasterOptnRepository;
import egovframework.com.cop.brd.repository.EgovBbsMasterRepository;
import egovframework.com.cop.brd.service.BbsMasterDTO;
import egovframework.com.cop.brd.service.BbsMasterOptnVO;
import egovframework.com.cop.brd.service.BbsMasterVO;
import egovframework.com.cop.brd.service.EgovBbsMasterService;
import egovframework.com.cop.brd.util.EgovBoardUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service("brdEgovBbsMasterService")
@RequiredArgsConstructor
public class EgovBbsMasterServiceImpl extends EgovAbstractServiceImpl implements EgovBbsMasterService {

    private final EgovBbsMasterOptnRepository optnRepository;
    private final EgovBbsMasterRepository masterRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public BbsMasterDTO detail(BbsMasterVO bbsMasterVO) {

        QBbsMaster bbsMaster = QBbsMaster.bbsMaster;
        QBbsMasterOptn masterOptn = QBbsMasterOptn.bbsMasterOptn;
        QTmplatInfo tmplatInfo = QTmplatInfo.tmplatInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        Tuple tuple = queryFactory
                .select(bbsMaster, masterOptn, tmplatInfo, userMaster, cmmnDetailCode)
                .from(bbsMaster)
                .leftJoin(masterOptn)
                .on(bbsMaster.bbsId.eq(masterOptn.bbsId))
                .leftJoin(tmplatInfo)
                .on(bbsMaster.tmplatId.eq(tmplatInfo.tmplatId))
                .leftJoin(userMaster)
                .on(bbsMaster.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(cmmnDetailCode)
                .on(bbsMaster.bbsTyCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                        .and(cmmnDetailCode.useAt.eq("Y"))
                        .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM101")))
                .where(bbsMaster.bbsId.eq(bbsMasterVO.getBbsId()))
                .fetchOne();

        BbsMaster bm = tuple.get(bbsMaster);
        BbsMasterOptn bmop = tuple.get(masterOptn);
        TmplatInfo tmplat = tuple.get(tmplatInfo);
        CmmnDetailCode code = tuple.get(cmmnDetailCode);
        UserMaster user = tuple.get(userMaster);

        String answerAt = bmop != null && bmop.getAnswerAt() != null ? bmop.getAnswerAt() : "";
        String stsfdgAt = bmop != null && bmop.getStsfdgAt() != null ? bmop.getStsfdgAt() : "";
        String tmplatNm = tmplat != null && tmplat.getTmplatNm() != null ? tmplat.getTmplatNm() : "";
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";
        String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";

        return new BbsMasterDTO(
                Objects.requireNonNull(bm).getBbsId(),
                bm.getBbsNm(),
                bm.getBbsIntrcn(),
                bm.getBbsTyCode(),
                bm.getReplyPosblAt(),
                bm.getFileAtchPosblAt(),
                bm.getAtchPosblFileNumber(),
                bm.getAtchPosblFileSize(),
                bm.getUseAt(),
                bm.getTmplatId(),
                bm.getCmmntyId(),
                bm.getFrstRegisterId(),
                bm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                bm.getLastUpdusrId(),
                bm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                bm.getBlogId(),
                bm.getBlogAt(),
                answerAt,
                stsfdgAt,
                tmplatNm,
                userNm,
                codeNm
        );
    }

    @Override
    public BbsMasterOptnVO selectBBSMasterOptn(String bbsId) {
        return optnRepository.findById(bbsId)
                .map(EgovBoardUtility::bbsmasteroptnEntityToVO)
                .orElse(null);
    }

    @Override
    public boolean isBbsMasterOptnAccessible(String bbsId, String uniqId) {
        if (bbsId == null || bbsId.isBlank() || uniqId == null || uniqId.isBlank()) {
            return false;
        }
        return masterRepository.findById(bbsId)
                .map(bm -> "Y".equals(bm.getUseAt()))
                .orElse(false);
    }

}
