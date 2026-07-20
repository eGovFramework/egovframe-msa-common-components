package egovframework.com.uss.olp.qrm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qrm.entity.*;
import egovframework.com.uss.olp.qrm.repository.EgovQustnrRespondInfoRepository;
import egovframework.com.uss.olp.qrm.service.EgovQustnrRespondInfoService;
import egovframework.com.uss.olp.qrm.service.QustnrRespondInfoDTO;
import egovframework.com.uss.olp.qrm.service.QustnrRespondInfoVO;
import egovframework.com.uss.olp.qrm.util.EgovQustnrRespondInfoUtility;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("qrmEgovQustnrRespondInfoService")
public class EgovQustnrRespondInfoServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrRespondInfoService {

    private final EgovQustnrRespondInfoRepository repository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQustnrRespondInfoServiceImpl(
            EgovQustnrRespondInfoRepository repository,
            @Qualifier("qustnrRespondManageIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory) {
        this.repository = repository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QustnrRespondInfoDTO> list(QustnrRespondInfoVO qustnrRespondInfoVO) {
        Pageable pageable = PageRequest.of(qustnrRespondInfoVO.getFirstIndex(), qustnrRespondInfoVO.getRecordCountPerPage());
        String searchCondition = qustnrRespondInfoVO.getSearchCondition();
        String searchKeyword = qustnrRespondInfoVO.getSearchKeyword();

        QQustnrRespondInfo qustnrRespondInfo = QQustnrRespondInfo.qustnrRespondInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QCmmnDetailCode sexCode = QCmmnDetailCode.cmmnDetailCode;
        QCmmnDetailCode tyCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qustnrRespondInfo.respondNm.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(userMaster.userNm.contains(searchKeyword));
        }

        List<Tuple> results = qustnrRespondInfoQuery()
                .where(where)
                .orderBy(qustnrRespondInfo.frstRegistPnttm.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long total = Optional.ofNullable(
                queryFactory
                    .select(qustnrRespondInfo.count())
                    .from(qustnrRespondInfo)
                    .leftJoin(userMaster)
                    .on(qustnrRespondInfo.frstRegisterId.eq(userMaster.esntlId))
                    .leftJoin(qestnrInfo)
                    .on(qustnrRespondInfo.qustnrRespondInfoId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                    .leftJoin(sexCode)
                    .on(qustnrRespondInfo.sexdstnCode.eq(sexCode.cmmnDetailCodeId.code)
                            .and(sexCode.cmmnDetailCodeId.codeId.eq("COM014")))
                    .leftJoin(tyCode)
                    .on(qustnrRespondInfo.occpTyCode.eq(tyCode.cmmnDetailCodeId.code)
                            .and(tyCode.cmmnDetailCodeId.codeId.eq("COM034")))
                    .where(where)
                    .fetchOne()
        ).orElse(0L);

        List<QustnrRespondInfoDTO> content = results.stream().map(tuple -> {

            QustnrRespondInfo qrs = tuple.get(qustnrRespondInfo);
            QestnrInfo qe = tuple.get(qestnrInfo);
            UserMaster user = tuple.get(userMaster);
            CmmnDetailCode sexTyCode = tuple.get(sexCode);
            CmmnDetailCode occpTyCode = tuple.get(tyCode);

            String qustnrSj = qe != null && qe.getQustnrSj() != null ? qe.getQustnrSj() : "";
            String sexCodeNm = sexTyCode != null && sexTyCode.getCodeNm() != null ? sexTyCode.getCodeNm() : "";
            String occpTyCodeNm = occpTyCode != null && occpTyCode.getCodeNm() != null ? occpTyCode.getCodeNm() : "";
            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new QustnrRespondInfoDTO(
                    Objects.requireNonNull(qrs).getQustnrRespondInfoId().getQustnrTmplatId(),
                    qrs.getQustnrRespondInfoId().getQestnrId(),
                    qustnrSj,
                    qrs.getQustnrRespondInfoId().getQustnrRespondId(),
                    qrs.getSexdstnCode(),
                    sexCodeNm,
                    qrs.getOccpTyCode(),
                    occpTyCodeNm,
                    qrs.getRespondNm(),
                    qrs.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qrs.getFrstRegisterId(),
                    qrs.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qrs.getLastUpdusrId(),
                    userNm
            );
        }).collect(Collectors.toList());
        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public QustnrRespondInfoDTO detail(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo) {
        QustnrRespondInfoId qustnrRespondInfoId = new QustnrRespondInfoId();
        qustnrRespondInfoId.setQustnrTmplatId(qustnrRespondInfoVO.getQustnrTmplatId());
        qustnrRespondInfoId.setQestnrId(qustnrRespondInfoVO.getQestnrId());
        qustnrRespondInfoId.setQustnrRespondId(qustnrRespondInfoVO.getQustnrRespondId());
        QustnrRespondInfo respondent = repository.findById(qustnrRespondInfoId).orElse(null);
        if (respondent == null) {
            return null;
        }
        assertOwner(respondent.getFrstRegisterId(), userInfo);

        QQustnrRespondInfo qustnrRespondInfo = QQustnrRespondInfo.qustnrRespondInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QCmmnDetailCode sexCode = QCmmnDetailCode.cmmnDetailCode;
        QCmmnDetailCode tyCode = QCmmnDetailCode.cmmnDetailCode;

        Tuple tuple = qustnrRespondInfoQuery()
                .where(qustnrRespondInfo.qustnrRespondInfoId.qustnrRespondId.eq(qustnrRespondInfoVO.getQustnrRespondId()))
                .fetchOne();

        QustnrRespondInfo qrs = tuple.get(qustnrRespondInfo);
        QestnrInfo qe = tuple.get(qestnrInfo);
        UserMaster user = tuple.get(userMaster);
        CmmnDetailCode sexTyCode = tuple.get(sexCode);
        CmmnDetailCode occpTyCode = tuple.get(tyCode);

        String qustnrSj = qe != null && qe.getQustnrSj() != null ? qe.getQustnrSj() : "";
        String sexCodeNm = sexTyCode != null && sexTyCode.getCodeNm() != null ? sexTyCode.getCodeNm() : "";
        String occpTyCodeNm = occpTyCode != null && occpTyCode.getCodeNm() != null ? occpTyCode.getCodeNm() : "";
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

        return new QustnrRespondInfoDTO(
                Objects.requireNonNull(qrs).getQustnrRespondInfoId().getQustnrTmplatId(),
                qrs.getQustnrRespondInfoId().getQestnrId(),
                qustnrSj,
                qrs.getQustnrRespondInfoId().getQustnrRespondId(),
                qrs.getSexdstnCode(),
                sexCodeNm,
                qrs.getOccpTyCode(),
                occpTyCodeNm,
                qrs.getRespondNm(),
                qrs.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qrs.getFrstRegisterId(),
                qrs.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qrs.getLastUpdusrId(),
                userNm
        );

    }

    @Transactional
    @Override
    public QustnrRespondInfoVO insert(QustnrRespondInfoVO qustnrRespondInfoVO) {
        try {
            String qustnrRespondId = idgenService.getNextStringId();
            qustnrRespondInfoVO.setQustnrRespondId(qustnrRespondId);

            QustnrRespondInfo qustnrRespondInfo = EgovQustnrRespondInfoUtility.qustnrRespondInfoVOTOEntity(qustnrRespondInfoVO);
            qustnrRespondInfo.setFrstRegistPnttm(LocalDateTime.now());
            qustnrRespondInfo.setLastUpdtPnttm(LocalDateTime.now());

            return EgovQustnrRespondInfoUtility.qustnrRespondInfoEntityToVO(repository.save(qustnrRespondInfo));

        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public QustnrRespondInfoVO update(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo) {
        QustnrRespondInfoId qustnrRespondInfoId = new QustnrRespondInfoId();
        qustnrRespondInfoId.setQustnrTmplatId(qustnrRespondInfoVO.getQustnrTmplatId());
        qustnrRespondInfoId.setQestnrId(qustnrRespondInfoVO.getQestnrId());
        qustnrRespondInfoId.setQustnrRespondId(qustnrRespondInfoVO.getQustnrRespondId());

        return repository.findById(qustnrRespondInfoId)
                .map(item -> {
                    assertOwner(item.getFrstRegisterId(), userInfo);
                    return updateItem(item, qustnrRespondInfoVO, userInfo.get("uniqId"));
                })
                .map(repository::save)
                .map(EgovQustnrRespondInfoUtility::qustnrRespondInfoEntityToVO)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean delete(QustnrRespondInfoVO qustnrRespondInfoVO, Map<String, String> userInfo) {
        QustnrRespondInfoId qustnrRespondInfoId = new QustnrRespondInfoId();
        qustnrRespondInfoId.setQustnrTmplatId(qustnrRespondInfoVO.getQustnrTmplatId());
        qustnrRespondInfoId.setQestnrId(qustnrRespondInfoVO.getQestnrId());
        qustnrRespondInfoId.setQustnrRespondId(qustnrRespondInfoVO.getQustnrRespondId());

        return repository.findById(qustnrRespondInfoId)
                .map(result -> {
                    assertOwner(result.getFrstRegisterId(), userInfo);
                    repository.delete(result);
                    return true;
                })
                .orElse(false);
    }

    private QustnrRespondInfo updateItem(QustnrRespondInfo qustnrRespondInfo, QustnrRespondInfoVO qustnrRespondInfoVO, String uniqId) {
        qustnrRespondInfo.setSexdstnCode(qustnrRespondInfoVO.getSexdstnCode());
        qustnrRespondInfo.setOccpTyCode(qustnrRespondInfoVO.getOccpTyCode());
        qustnrRespondInfo.setRespondNm(qustnrRespondInfoVO.getRespondNm());
        qustnrRespondInfo.setLastUpdtPnttm(LocalDateTime.now());
        qustnrRespondInfo.setLastUpdusrId(uniqId);
        return qustnrRespondInfo;
    }

    private void assertOwner(String frstRegisterId, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치
        if (!Objects.equals(uniqId, frstRegisterId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

    private JPAQuery<Tuple> qustnrRespondInfoQuery(){
        QQustnrRespondInfo qustnrRespondInfo = QQustnrRespondInfo.qustnrRespondInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QCmmnDetailCode sexCode = QCmmnDetailCode.cmmnDetailCode;
        QCmmnDetailCode tyCode = QCmmnDetailCode.cmmnDetailCode;

        return queryFactory
                .select(qustnrRespondInfo,userMaster,qestnrInfo,sexCode,tyCode)
                .from(qustnrRespondInfo)
                .leftJoin(userMaster)
                .on(qustnrRespondInfo.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qestnrInfo)
                .on(qustnrRespondInfo.qustnrRespondInfoId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                .leftJoin(sexCode)
                .on(qustnrRespondInfo.sexdstnCode.eq(sexCode.cmmnDetailCodeId.code)
                        .and(sexCode.cmmnDetailCodeId.codeId.eq("COM014")))
                .leftJoin(tyCode)
                .on(qustnrRespondInfo.occpTyCode.eq(tyCode.cmmnDetailCodeId.code)
                        .and(tyCode.cmmnDetailCodeId.codeId.eq("COM034")));
    }

}
