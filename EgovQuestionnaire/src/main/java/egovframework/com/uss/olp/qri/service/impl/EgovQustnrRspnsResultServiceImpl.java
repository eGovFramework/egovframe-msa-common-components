package egovframework.com.uss.olp.qri.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qri.entity.*;
import egovframework.com.uss.olp.qri.repository.EgovQestnrInfoRepository;
import egovframework.com.uss.olp.qri.repository.EgovQustnrRespondInfoRepository;
import egovframework.com.uss.olp.qri.repository.EgovQustnrRspnsResultRepository;
import egovframework.com.uss.olp.qri.service.*;
import egovframework.com.uss.olp.qri.util.EgovQustnrRspnsResultUtility;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("qriEgovQustnrRspnsResultService")
public class EgovQustnrRspnsResultServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrRspnsResultService {

    private final EgovQustnrRspnsResultRepository repository;
    private final EgovQustnrRespondInfoRepository qustnrRespondInfoRepository;
    private final EgovQestnrInfoRepository qestnrInfoRepository;
    private final EgovIdGnrService idgenService;
    private final EgovIdGnrService qustnrRespondInfoIdgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQustnrRspnsResultServiceImpl(
            EgovQustnrRspnsResultRepository repository,
            EgovQustnrRespondInfoRepository qustnrRespondInfoRepository,
            EgovQestnrInfoRepository qestnrInfoRepository,
            @Qualifier("qustnrRespondInfoIdGnrService") EgovIdGnrService idgenService,
            @Qualifier("qustnrRespondManageIdGnrService") EgovIdGnrService qustnrRespondInfoIdgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.qustnrRespondInfoRepository = qustnrRespondInfoRepository;
        this.qestnrInfoRepository = qestnrInfoRepository;
        this.idgenService = idgenService;
        this.qustnrRespondInfoIdgenService = qustnrRespondInfoIdgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QustnrRspnsResultDTO> list(QustnrRspnsResultVO qustnrRspnsResultVO) {
        Pageable pageable = PageRequest.of(qustnrRspnsResultVO.getFirstIndex(), qustnrRspnsResultVO.getPageSize());
        String searchCondition = qustnrRspnsResultVO.getSearchCondition();
        String searchKeyword = qustnrRspnsResultVO.getSearchKeyword();

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

        List<Tuple> results = queryFactory
                .select(qestnrInfo,userMaster,qustnrTmplat,cmmnDetailCode)
                .from(qestnrInfo)
                .leftJoin(userMaster)
                .on(qestnrInfo.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qustnrTmplat)
                .on(qestnrInfo.qestnrInfoId.qustnrTmplatId.eq(qustnrTmplat.qustnrTmplatId))
                .leftJoin(cmmnDetailCode)
                .on(qestnrInfo.qustnrTrget.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                        .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM034")))
                .where(where)
                .orderBy(qestnrInfo.qestnrInfoId.qestnrId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(qestnrInfo.count())
                        .from(qestnrInfo)
                        .leftJoin(userMaster)
                        .on(qestnrInfo.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(qustnrTmplat)
                        .on(qestnrInfo.qestnrInfoId.qustnrTmplatId.eq(qustnrTmplat.qustnrTmplatId))
                        .leftJoin(cmmnDetailCode)
                        .on(qestnrInfo.qustnrTrget.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                                .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM034")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);


        List<QustnrRspnsResultDTO> content = results.stream().map(tuple -> {

            QestnrInfo qe = tuple.get(qestnrInfo);
            UserMaster user = tuple.get(userMaster);
            QustnrTmplat qtm = tuple.get(qustnrTmplat);
            CmmnDetailCode code = tuple.get(cmmnDetailCode);

            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";
            String qustnrTmplayTy = qtm != null  && qtm.getQustnrTmplatTy() != null ? qtm.getQustnrTmplatTy(): "";
            String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";

            return new QustnrRspnsResultDTO(
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
                    qustnrTmplayTy,
                    codeNm
            );
        }).collect(Collectors.toList());
        return new PageImpl<>(content,pageable,total);
    }

    @Transactional
    @Override
    public boolean insert(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo) {
        try {
            // 설문응답자 저장
            QustnrRespondInfoVO qustnrRespondInfoVO = new QustnrRespondInfoVO();
            qustnrRespondInfoVO.setQustnrRespondId(qustnrRespondInfoIdgenService.getNextStringId());
            qustnrRespondInfoVO.setQustnrTmplatId(qustnrRspnsResultVO.getQustnrTmplatId());
            qustnrRespondInfoVO.setQestnrId(qustnrRspnsResultVO.getQestnrId());
            qustnrRespondInfoVO.setSexdstnCode(qustnrRspnsResultVO.getSexdstnCode());
            qustnrRespondInfoVO.setOccpTyCode(qustnrRspnsResultVO.getOccpTyCode());
            qustnrRespondInfoVO.setRespondNm(qustnrRspnsResultVO.getRespondNm());
            QustnrRespondInfo qustnrRespondInfo = EgovQustnrRspnsResultUtility.qustnrRespondInfoVOTOEntity(qustnrRespondInfoVO);
            qustnrRespondInfo.setFrstRegistPnttm(LocalDateTime.now());
            qustnrRespondInfo.setFrstRegisterId(userInfo.get("uniqId"));
            qustnrRespondInfo.setLastUpdtPnttm(LocalDateTime.now());
            qustnrRespondInfo.setLastUpdusrId(userInfo.get("uniqId"));

            qustnrRespondInfoRepository.save(qustnrRespondInfo);

            // 설문조사 저장
            String[] itemArray = qustnrRspnsResultVO.getQustnrItemList();
            for (String item : itemArray) {
                String[] itemList = item.split(",");
                if ("1".equals(itemList[0])) {
                    qustnrRspnsResultVO.setQustnrRspnsResultId(idgenService.getNextStringId());
                    qustnrRspnsResultVO.setQustnrQesitmId(itemList[1]);
                    qustnrRspnsResultVO.setQustnrIemId(itemList[2]);
                    qustnrRspnsResultVO.setRespondAnswerCn("");
                    qustnrRespondInfoVO.setRespondNm(qustnrRspnsResultVO.getRespondNm());
                } else {
                    qustnrRspnsResultVO.setQustnrRspnsResultId(idgenService.getNextStringId());
                    qustnrRspnsResultVO.setQustnrQesitmId(itemList[1]);
                    qustnrRspnsResultVO.setQustnrIemId("");
                    qustnrRspnsResultVO.setRespondAnswerCn(itemList[2]);
                }
                QustnrRspnsResult qustnrRspnsResult = EgovQustnrRspnsResultUtility.qustnrRspnsResultVOToEntity(qustnrRspnsResultVO);
                qustnrRspnsResult.setFrstRegistPnttm(LocalDateTime.now());
                qustnrRspnsResult.setFrstRegisterId(userInfo.get("uniqId"));
                qustnrRspnsResult.setLastUpdtPnttm(LocalDateTime.now());
                qustnrRspnsResult.setLastUpdusrId(userInfo.get("uniqId"));

                repository.save(qustnrRspnsResult);
            }
            return true;
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException e) {
            return false;
        }
    }

    @Override
    public List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo) {
        assertQestnrOwner(qustnrRspnsResultVO.getQustnrTmplatId(), qustnrRspnsResultVO.getQestnrId(), userInfo);

        QQustnrIem qustnrIem = QQustnrIem.qustnrIem;
        QQustnrRspnsResult qustnrRspnsResult = QQustnrRspnsResult.qustnrRspnsResult;

        NumberExpression<Long> iemCount = qustnrRspnsResult.qustnrIemId.count();
        NumberTemplate<Integer> percentageExpr = Expressions.numberTemplate(Integer.class,
                "CASE WHEN ({1}) = 0 THEN 0 ELSE ROUND((100.0 * {0}) / ({1})) END",
                qustnrRspnsResult.qustnrIemId.count(),
                JPAExpressions.select(qustnrRspnsResult.count())
                        .from(qustnrRspnsResult)
                        .where(qustnrRspnsResult.qustnrRspnsResultId.qustnrQesitmId.eq(qustnrIem.qustnrIemId.qustnrQesitmId))
        );

        List<Tuple> results = queryFactory
                .select(
                        qustnrIem.qustnrIemId.qustnrTmplatId,
                        qustnrIem.qustnrIemId.qestnrId,
                        qustnrIem.qustnrIemId.qustnrQesitmId,
                        qustnrIem.qustnrIemId.qustnrIemId,
                        qustnrIem.iemCn,
                        iemCount,
                        percentageExpr
                )
                .from(qustnrIem)
                .leftJoin(qustnrRspnsResult)
                .on(qustnrIem.qustnrIemId.qustnrIemId.eq(qustnrRspnsResult.qustnrIemId))
                .where(qustnrIem.qustnrIemId.qustnrTmplatId.eq(qustnrRspnsResultVO.getQustnrTmplatId())
                        .and(qustnrIem.qustnrIemId.qestnrId.eq(qustnrRspnsResultVO.getQestnrId())))
                .groupBy(qustnrIem.qustnrIemId.qustnrTmplatId,
                        qustnrIem.qustnrIemId.qestnrId,
                        qustnrIem.qustnrIemId.qustnrQesitmId,
                        qustnrIem.qustnrIemId.qustnrIemId,
                        qustnrRspnsResult.qustnrRspnsResultId.qustnrQesitmId,
                        qustnrIem.iemCn)
                .fetch();

        List<QustnrRspnsResultMCStatsDTO> content = results.stream().map(tuple -> {
            long count = tuple.get(iemCount) != null ? tuple.get(iemCount) : 0;
            long around = tuple.get(percentageExpr) != null ? tuple.get(percentageExpr) : 0;
            String iemCn = tuple.get(qustnrIem.iemCn) != null ? tuple.get(qustnrIem.iemCn) : "";

            return new QustnrRspnsResultMCStatsDTO(
                    tuple.get(0,String.class),
                    tuple.get(1,String.class),
                    tuple.get(2,String.class),
                    tuple.get(3,String.class),
                    iemCn,
                    count,
                    around
            );
        }).collect(Collectors.toList());
        return content;
    }

    @Override
    public List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats(QustnrRspnsResultVO qustnrRspnsResultVO, Map<String, String> userInfo) {
        assertQestnrOwner(qustnrRspnsResultVO.getQustnrTmplatId(), qustnrRspnsResultVO.getQestnrId(), userInfo);
        QQustnrRspnsResult qustnrRspnsResult = QQustnrRspnsResult.qustnrRspnsResult;

        List<QustnrRspnsResult> results = queryFactory
                .select(qustnrRspnsResult)
                .from(qustnrRspnsResult)
                .where(qustnrRspnsResult.qustnrRspnsResultId.qustnrTmplatId.eq(qustnrRspnsResultVO.getQustnrTmplatId())
                        .and(qustnrRspnsResult.qustnrRspnsResultId.qestnrId.eq(qustnrRspnsResultVO.getQestnrId()))
                        .and(qustnrRspnsResult.qustnrRspnsResultId.isNull().or(qustnrRspnsResult.qustnrIemId.eq(""))))
                .fetch();

        List<QustnrRspnsResultESStatsDTO> content = results.stream().map(qrr -> {
            return new QustnrRspnsResultESStatsDTO(
                    qrr.getQustnrRspnsResultId().getQustnrTmplatId(),
                    qrr.getQustnrRspnsResultId().getQestnrId(),
                    qrr.getQustnrRspnsResultId().getQustnrQesitmId(),
                    qrr.getQustnrIemId(),
                    qrr.getRespondAnswerCn(),
                    qrr.getEtcAnswerCn(),
                    qrr.getRespondNm()
            );
        }).collect(Collectors.toList());
        return content;
    }

    private void assertQestnrOwner(String qustnrTmplatId, String qestnrId, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        QestnrInfoId qestnrInfoId = new QestnrInfoId();
        qestnrInfoId.setQustnrTmplatId(qustnrTmplatId);
        qestnrInfoId.setQestnrId(qestnrId);
        QestnrInfo qestnrInfo = qestnrInfoRepository.findById(qestnrInfoId).orElse(null);
        if (qestnrInfo == null) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치
        if (!Objects.equals(uniqId, qestnrInfo.getFrstRegisterId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }
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
}
