package egovframework.com.uss.olp.qmc.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qmc.entity.*;
import egovframework.com.uss.olp.qmc.repository.*;
import egovframework.com.uss.olp.qmc.service.EgovQestnrInfoService;
import egovframework.com.uss.olp.qmc.service.QestnrInfoDTO;
import egovframework.com.uss.olp.qmc.service.QestnrInfoVO;
import egovframework.com.uss.olp.qmc.util.EgovQestnrInfoUtility;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("qmcEgovQestnrInfoService")
public class EgovQestnrInfoServiceImpl extends EgovAbstractServiceImpl implements EgovQestnrInfoService {

    private final EgovQestnrInfoRepository repository;
    private final EgovQustnrQesitmRepository qustnrQesitmRepository;
    private final EgovQustnrIemRepository qustnrIemRepository;
    private final EgovQustnrRespondInfoRepository qustnrRespondInfoRepository;
    private final EgovQustnrRspnsResultRepository qustnrRspnsResultRepository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQestnrInfoServiceImpl(
            EgovQestnrInfoRepository repository,
            EgovQustnrQesitmRepository qustnrQesitmRepository,
            EgovQustnrIemRepository qustnrIemRepository,
            EgovQustnrRespondInfoRepository qustnrRespondInfoRepository,
            EgovQustnrRspnsResultRepository qustnrRspnsResultRepository,
            @Qualifier("egovQustnrManageIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.qustnrQesitmRepository = qustnrQesitmRepository;
        this.qustnrIemRepository = qustnrIemRepository;
        this.qustnrRespondInfoRepository = qustnrRespondInfoRepository;
        this.qustnrRspnsResultRepository = qustnrRspnsResultRepository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QestnrInfoDTO> list(QestnrInfoVO qestnrInfoVO) {
        Pageable pageable = PageRequest.of(qestnrInfoVO.getFirstIndex(), qestnrInfoVO.getRecordCountPerPage());
        String searchCondition = qestnrInfoVO.getSearchCondition();
        String searchKeyword = qestnrInfoVO.getSearchKeyword();

        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQustnrTmplat qustnrTmplat = QQustnrTmplat.qustnrTmplat;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qestnrInfo.qustnrSj.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(userMaster.userNm.contains(searchKeyword));
        }

        List<Tuple> query = queryFactory.select(qestnrInfo,userMaster,qustnrTmplat,cmmnDetailCode)
                .from(qestnrInfo)
                .leftJoin(userMaster)
                .on(qestnrInfo.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qustnrTmplat)
                .on(qestnrInfo.qestnrInfoId.qustnrTmplatId.eq(qustnrTmplat.qustnrTmplatId))
                .leftJoin(cmmnDetailCode)
                .on(qestnrInfo.qustnrTrget.eq(cmmnDetailCode.cmmnDetailCodeId.code).and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM034")))
                .where(where)
                .orderBy(qestnrInfo.qestnrInfoId.qestnrId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory.select(qestnrInfo.count())
                        .from(qestnrInfo)
                        .leftJoin(userMaster)
                        .on(qestnrInfo.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(qustnrTmplat)
                        .on(qestnrInfo.qestnrInfoId.qustnrTmplatId.eq(qustnrTmplat.qustnrTmplatId))
                        .leftJoin(cmmnDetailCode)
                        .on(qestnrInfo.qustnrTrget.eq(cmmnDetailCode.cmmnDetailCodeId.code).and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM034")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<QestnrInfoDTO> content = query.stream().map(tuple -> {

            QestnrInfo qe = tuple.get(qestnrInfo);
            UserMaster user = tuple.get(userMaster);
            QustnrTmplat qtm = tuple.get(qustnrTmplat);
            CmmnDetailCode code = tuple.get(cmmnDetailCode);

            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";
            String qustnrTmplatTy = qtm != null && qtm.getQustnrTmplatTy() != null ? qtm.getQustnrTmplatTy() : "";
            String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";

            return new QestnrInfoDTO(
                    Objects.requireNonNull(qe).getQestnrInfoId().getQustnrTmplatId(),
                    qe.getQestnrInfoId().getQestnrId(),
                    qe.getQustnrSj(),
                    qe.getQustnrPurps(),
                    qe.getQustnrWritingGuidanceCn(),
                    qe.getQustnrTrget(),
                    formatDateString(qe.getQustnrBgnde()),
                    formatDateString(qe.getQustnrEndde()),
                    qe.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qe.getFrstRegisterId(),
                    qe.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qe.getLastUpdusrId(),
                    userNm,
                    qustnrTmplatTy,
                    codeNm
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content,pageable,total);
    }

    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    @Override
    public QestnrInfoDTO detail(QestnrInfoVO qestnrInfoVO) {

        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQustnrTmplat qustnrTmplat = QQustnrTmplat.qustnrTmplat;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;
        Tuple query = queryFactory.select(qestnrInfo,userMaster,qustnrTmplat,cmmnDetailCode)
                .from(qestnrInfo)
                .leftJoin(userMaster)
                .on(qestnrInfo.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qustnrTmplat)
                .on(qestnrInfo.qestnrInfoId.qustnrTmplatId.eq(qustnrTmplat.qustnrTmplatId))
                .leftJoin(cmmnDetailCode)
                .on(qestnrInfo.qustnrTrget.eq(cmmnDetailCode.cmmnDetailCodeId.code).and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM034")))
                .where(qestnrInfo.qestnrInfoId.qestnrId.eq(qestnrInfoVO.getQestnrId()))
                .fetchOne();

        QestnrInfo qe = query.get(qestnrInfo);
        UserMaster user = query.get(userMaster);
        QustnrTmplat qtm = query.get(qustnrTmplat);
        CmmnDetailCode code = query.get(cmmnDetailCode);

        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";
        String qustnrTmplatTy = qtm != null && qtm.getQustnrTmplatTy() != null ? qtm.getQustnrTmplatTy() : "";
        String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";


        return new QestnrInfoDTO(
                Objects.requireNonNull(qe).getQestnrInfoId().getQustnrTmplatId(),
                qe.getQestnrInfoId().getQestnrId(),
                qe.getQustnrSj(),
                qe.getQustnrPurps(),
                qe.getQustnrWritingGuidanceCn(),
                qe.getQustnrTrget(),
                formatDateString(qe.getQustnrBgnde()),
                formatDateString(qe.getQustnrEndde()),
                qe.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qe.getFrstRegisterId(),
                qe.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qe.getLastUpdusrId(),
                userNm,
                qustnrTmplatTy,
                codeNm
        );
    }

    @Transactional
    @Override
    public QestnrInfoVO insert(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo) {
        try {
            String qestnrId = idgenService.getNextStringId();
            qestnrInfoVO.setQestnrId(qestnrId);

            QestnrInfo qestnrInfo = EgovQestnrInfoUtility.qestnrInfoVOToEntiry(qestnrInfoVO);
            qestnrInfo.setQustnrBgnde(qestnrInfo.getQustnrBgnde().replace("-", ""));
            qestnrInfo.setQustnrEndde(qestnrInfo.getQustnrEndde().replace("-", ""));
            qestnrInfo.setFrstRegistPnttm(LocalDateTime.now());
            qestnrInfo.setFrstRegisterId(userInfo.get("uniqId"));
            qestnrInfo.setLastUpdtPnttm(LocalDateTime.now());
            qestnrInfo.setLastUpdusrId(userInfo.get("uniqId"));
            return EgovQestnrInfoUtility.quesnrInfoEntityToVO(repository.save(qestnrInfo));
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public QestnrInfoVO update(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo) {
        QestnrInfoId qestnrInfoId = new QestnrInfoId();
        qestnrInfoId.setQustnrTmplatId(qestnrInfoVO.getQustnrTmplatId());
        qestnrInfoId.setQestnrId(qestnrInfoVO.getQestnrId());

        return repository.findById(qestnrInfoId)
                .map(item -> updateItem(item, qestnrInfoVO, userInfo.get("uniqId")))
                .map(repository::save)
                .map(EgovQestnrInfoUtility::quesnrInfoEntityToVO)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean delete(QestnrInfoVO qestnrInfoVO, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String qustnrTmplatId = qestnrInfoVO.getQustnrTmplatId();
        String qestnrId = qestnrInfoVO.getQestnrId();

        QestnrInfoId qestnrInfoId = new QestnrInfoId();
        qestnrInfoId.setQustnrTmplatId(qustnrTmplatId);
        qestnrInfoId.setQestnrId(qestnrId);

        return repository.findById(qestnrInfoId)
                .map(result -> {
                    // 2026.07.13 KISA 보안취약점 조치
                    if (!Objects.equals(uniqId, result.getFrstRegisterId())) {
                        throw new IllegalStateException("권한이 없습니다.");
                    }
                    qustnrRespondInfoRepository.deleteByQustnrRespondInfoIdQustnrTmplatIdAndQustnrRespondInfoIdQestnrId(qustnrTmplatId, qestnrId);
                    qustnrRspnsResultRepository.deleteByQustnrRspnsResultIdQustnrTmplatIdAndQustnrRspnsResultIdQestnrId(qustnrTmplatId, qestnrId);
                    qustnrIemRepository.deleteByQustnrIemIdQustnrTmplatIdAndQustnrIemIdQestnrId(qustnrTmplatId, qestnrId);
                    qustnrQesitmRepository.deleteByQustnrQesitmIdQustnrTmplatIdAndQustnrQesitmIdQestnrId(qustnrTmplatId, qestnrId);
                    repository.delete(result);
                    return true;
                })
                .orElse(false);
    }

    private QestnrInfo updateItem(QestnrInfo qestnrInfo, QestnrInfoVO qestnrInfoVO, String uniqId) {
        qestnrInfo.setQustnrSj(qestnrInfoVO.getQustnrSj());
        qestnrInfo.setQustnrPurps(qestnrInfoVO.getQustnrPurps());
        qestnrInfo.setQustnrWritingGuidanceCn(qestnrInfoVO.getQustnrWritingGuidanceCn());
        qestnrInfo.setQustnrTrget(qestnrInfoVO.getQustnrTrget());
        qestnrInfo.setQustnrBgnde(qestnrInfoVO.getQustnrBgnde());
        qestnrInfo.setQustnrEndde(qestnrInfoVO.getQustnrEndde());
        qestnrInfo.setLastUpdtPnttm(LocalDateTime.now());
        qestnrInfo.setLastUpdusrId(uniqId);
        return qestnrInfo;
    }

}
