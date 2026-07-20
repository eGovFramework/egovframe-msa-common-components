package egovframework.com.uss.olp.qqm.service.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qqm.entity.QQustnrIem;
import egovframework.com.uss.olp.qqm.entity.QQustnrRspnsResult;
import egovframework.com.uss.olp.qqm.entity.QestnrInfo;
import egovframework.com.uss.olp.qqm.entity.QestnrInfoId;
import egovframework.com.uss.olp.qqm.entity.QustnrRspnsResult;
import egovframework.com.uss.olp.qqm.repository.EgovQestnrInfoRepository;
import egovframework.com.uss.olp.qqm.repository.EgovQustnrRspnsResultRepository;
import egovframework.com.uss.olp.qqm.service.EgovQustnrRspnsResultService;
import egovframework.com.uss.olp.qqm.service.QustnrQesitmVO;
import egovframework.com.uss.olp.qqm.service.QustnrRspnsResultESStatsDTO;
import egovframework.com.uss.olp.qqm.service.QustnrRspnsResultMCStatsDTO;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("qqmEgovQustnrRspnsResultService")
@RequiredArgsConstructor
public class EgovQustnrRspnsResultServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrRspnsResultService {

    private final EgovQustnrRspnsResultRepository repository;
    private final EgovQestnrInfoRepository qestnrInfoRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<QustnrRspnsResultMCStatsDTO> qustnrRspnsResultMCStats(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        assertQestnrOwner(qustnrQesitmVO.getQustnrTmplatId(), qustnrQesitmVO.getQestnrId(), userInfo);

        QQustnrIem qustnrIem = QQustnrIem.qustnrIem;
        QQustnrRspnsResult qustnrRspnsResult = QQustnrRspnsResult.qustnrRspnsResult;

        NumberExpression<Long> iemCount = qustnrRspnsResult.qustnrIemId.count();
        NumberTemplate<Long> percentageExpr = Expressions.numberTemplate(Long.class,
                "CASE WHEN ({1}) = 0 THEN 0 ELSE ROUND((100.0 * {0}) / ({1})) END",
                qustnrRspnsResult.qustnrIemId.count(),
                JPAExpressions.select(qustnrRspnsResult.count())
                        .from(qustnrRspnsResult)
                        .where(qustnrRspnsResult.qustnrRspnsResultId.qustnrQesitmId.eq(qustnrIem.qustnrIemId.qustnrQesitmId))
        );

        return queryFactory
                .select(Projections.constructor(
                        QustnrRspnsResultMCStatsDTO.class,
                        qustnrIem.qustnrIemId.qustnrTmplatId,
                        qustnrIem.qustnrIemId.qestnrId,
                        qustnrIem.qustnrIemId.qustnrQesitmId,
                        qustnrIem.qustnrIemId.qustnrIemId,
                        qustnrIem.iemCn,
                        iemCount,
                        percentageExpr
                ))
                .from(qustnrIem)
                .leftJoin(qustnrRspnsResult)
                .on(qustnrIem.qustnrIemId.qustnrIemId.eq(qustnrRspnsResult.qustnrIemId))
                .where(qustnrIem.qustnrIemId.qustnrTmplatId.eq(qustnrQesitmVO.getQustnrTmplatId())
                        .and(qustnrIem.qustnrIemId.qestnrId.eq(qustnrQesitmVO.getQestnrId())))
                .groupBy(qustnrIem.qustnrIemId.qustnrTmplatId,
                        qustnrIem.qustnrIemId.qestnrId,
                        qustnrIem.qustnrIemId.qustnrQesitmId,
                        qustnrIem.qustnrIemId.qustnrIemId,
                        qustnrRspnsResult.qustnrRspnsResultId.qustnrQesitmId,
                        qustnrIem.iemCn)
                .fetch();
    }

    @Override
    public List<QustnrRspnsResultESStatsDTO> qustnrRspnsResultESStats(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        assertQestnrOwner(qustnrQesitmVO.getQustnrTmplatId(), qustnrQesitmVO.getQestnrId(), userInfo);

        QQustnrRspnsResult qustnrRspnsResult = QQustnrRspnsResult.qustnrRspnsResult;

        List<QustnrRspnsResult> results = queryFactory
                .select(qustnrRspnsResult)
                .from(qustnrRspnsResult)
                .where(qustnrRspnsResult.qustnrRspnsResultId.qustnrTmplatId.eq(qustnrQesitmVO.getQustnrTmplatId())
                        .and(qustnrRspnsResult.qustnrRspnsResultId.qestnrId.eq(qustnrQesitmVO.getQestnrId()))
                        .and(qustnrRspnsResult.qustnrRspnsResultId.isNull().or(qustnrRspnsResult.qustnrIemId.eq(""))))
                .fetch();

        return results.stream().map(qrr -> new QustnrRspnsResultESStatsDTO(
                qrr.getQustnrRspnsResultId().getQustnrTmplatId(),
                qrr.getQustnrRspnsResultId().getQestnrId(),
                qrr.getQustnrRspnsResultId().getQustnrQesitmId(),
                qrr.getQustnrIemId(),
                qrr.getRespondAnswerCn(),
                qrr.getEtcAnswerCn(),
                qrr.getRespondNm()
        )).collect(Collectors.toList());
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

}
