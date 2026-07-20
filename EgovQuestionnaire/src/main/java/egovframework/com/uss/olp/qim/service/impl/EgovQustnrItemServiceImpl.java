package egovframework.com.uss.olp.qim.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qim.entity.*;
import egovframework.com.uss.olp.qim.repository.EgovQusntrItemRepository;
import egovframework.com.uss.olp.qim.service.EgovQustnrItemService;
import egovframework.com.uss.olp.qim.service.QustnrIemDTO;
import egovframework.com.uss.olp.qim.service.QustnrIemVO;
import egovframework.com.uss.olp.qim.util.EgovQusntrItemUtility;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("qimEgovQustnrItemService")
public class EgovQustnrItemServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrItemService {

    private final EgovQusntrItemRepository repository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQustnrItemServiceImpl(
            EgovQusntrItemRepository repository,
            @Qualifier("egovQustnrItemManageIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QustnrIemDTO> list(QustnrIemVO qustnrIemVO) {
        Pageable pageable = PageRequest.of(qustnrIemVO.getFirstIndex(), qustnrIemVO.getRecordCountPerPage());
        String searchCondition = qustnrIemVO.getSearchCondition();
        String searchKeyword = qustnrIemVO.getSearchKeyword();

        QQustnrIem qustnrIem = QQustnrIem.qustnrIem;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qustnrIem.iemCn.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(userMaster.userNm.contains(searchKeyword));
        }

        JPAQuery<Tuple> query =  queryFactory.select(qustnrIem, userMaster,qustnrQesitm,qestnrInfo)
                .from(qustnrIem)
                .leftJoin(userMaster)
                .on(qustnrIem.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qustnrQesitm)
                .on(qustnrIem.qustnrIemId.qustnrQesitmId.eq(qustnrQesitm.qustnrQesitmId.qustnrQesitmId))
                .leftJoin(qestnrInfo)
                .on(qustnrIem.qustnrIemId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                .where(where)
                .orderBy(qustnrIem.qustnrIemId.qestnrId.desc(),qustnrIem.qustnrIemId.qustnrQesitmId.desc(),qustnrIem.qustnrIemId.qustnrIemId.desc());

        List<Tuple> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory.select(qustnrIem.count())
                        .from(qustnrIem)
                        .leftJoin(userMaster)
                        .on(qustnrIem.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(qustnrQesitm)
                        .on(qustnrIem.qustnrIemId.qustnrQesitmId.eq(qustnrQesitm.qustnrQesitmId.qustnrQesitmId))
                        .leftJoin(qestnrInfo)
                        .on(qustnrIem.qustnrIemId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<QustnrIemDTO> content = results.stream().map(tuple -> {

            QustnrIem qim = tuple.get(qustnrIem);
            UserMaster user = tuple.get(userMaster);
            QustnrQesitm qqm = tuple.get(qustnrQesitm);
            QestnrInfo qi = tuple.get(qestnrInfo);

            String qustnrSj =  qi != null && qi.getQustnrSj() != null ? qi.getQustnrSj() : "";
            String qestnCn = qqm != null && qqm.getQestnCn() != null ? qqm.getQestnCn() : "";
            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new QustnrIemDTO(
                    Objects.requireNonNull(qim).getQustnrIemId().getQustnrTmplatId(),
                    qim.getQustnrIemId().getQestnrId(),
                    qustnrSj,
                    qim.getQustnrIemId().getQustnrQesitmId(),
                    qestnCn,
                    qim.getQustnrIemId().getQustnrIemId(),
                    qim.getIemSn(),
                    qim.getIemCn(),
                    qim.getEtcAnswerAt(),
                    qim.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qim.getFrstRegisterId(),
                    qim.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qim.getLastUpdusrId(),
                    userNm
            );

        }).collect(Collectors.toList());
        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public QustnrIemDTO detail(QustnrIemVO qustnrIemVO, Map<String, String> userInfo) {
        QustnrIemId qustnrIemId = new QustnrIemId();
        qustnrIemId.setQustnrTmplatId(qustnrIemVO.getQustnrTmplatId());
        qustnrIemId.setQestnrId(qustnrIemVO.getQestnrId());
        qustnrIemId.setQustnrQesitmId(qustnrIemVO.getQustnrQesitmId());
        qustnrIemId.setQustnrIemId(qustnrIemVO.getQustnrIemId());
        QustnrIem item = repository.findById(qustnrIemId).orElse(null);
        if (item == null) {
            return null;
        }
        assertOwner(item.getFrstRegisterId(), userInfo);

        QQustnrIem qustnrIem = QQustnrIem.qustnrIem;
        QUserMaster userMaster = QUserMaster.userMaster;
        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;

        Tuple query = queryFactory
                .select(qustnrIem,userMaster,qustnrQesitm, qestnrInfo)
                .from(qustnrIem)
                .leftJoin(userMaster)
                .on(qustnrIem.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(qustnrQesitm)
                .on(qustnrIem.qustnrIemId.qustnrQesitmId.eq(qustnrQesitm.qustnrQesitmId.qustnrQesitmId))
                .leftJoin(qestnrInfo)
                .on(qustnrIem.qustnrIemId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                .where(qustnrIem.qustnrIemId.qustnrIemId.eq(qustnrIemVO.getQustnrIemId()))
                .fetchOne();

        if (query == null) return null;

        QustnrIem qim = query.get(qustnrIem);
        UserMaster user = query.get(userMaster);
        QustnrQesitm qqm = query.get(qustnrQesitm);
        QestnrInfo qi = query.get(qestnrInfo);

        String qustnrSj =  qi != null && qi.getQustnrSj() != null ? qi.getQustnrSj() : "";
        String qestnCn = qqm != null && qqm.getQestnCn() != null ? qqm.getQestnCn() : "";
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

        return new QustnrIemDTO(
                Objects.requireNonNull(qim).getQustnrIemId().getQustnrTmplatId(),
                qim.getQustnrIemId().getQestnrId(),
                qustnrSj,
                qim.getQustnrIemId().getQustnrQesitmId(),
                qestnCn,
                qim.getQustnrIemId().getQustnrIemId(),
                qim.getIemSn(),
                qim.getIemCn(),
                qim.getEtcAnswerAt(),
                qim.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qim.getFrstRegisterId(),
                qim.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qim.getLastUpdusrId(),
                userNm
        );
    }

    @Transactional
    @Override
    public QustnrIemVO insert(QustnrIemVO qustnrIemVO, Map<String, String> userInfo) {
        try {
            String itemId = idgenService.getNextStringId();
            qustnrIemVO.setQustnrIemId(itemId);
            QustnrIem qustnrIem = EgovQusntrItemUtility.qustnrIemVOToEntity(qustnrIemVO);
            qustnrIem.setFrstRegistPnttm(LocalDateTime.now());
            qustnrIem.setFrstRegisterId(userInfo.get("uniqId"));
            qustnrIem.setLastUpdtPnttm(LocalDateTime.now());
            qustnrIem.setLastUpdusrId(userInfo.get("uniqId"));
            return EgovQusntrItemUtility.qustnrIemEntityToVO(repository.save(qustnrIem));
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex){
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public QustnrIemVO update(QustnrIemVO qustnrIemVO, Map<String, String> userInfo) {
        QustnrIemId qustnrIemId = new QustnrIemId();
        qustnrIemId.setQustnrTmplatId(qustnrIemVO.getQustnrTmplatId());
        qustnrIemId.setQestnrId(qustnrIemVO.getQestnrId());
        qustnrIemId.setQustnrQesitmId(qustnrIemVO.getQustnrQesitmId());
        qustnrIemId.setQustnrIemId(qustnrIemVO.getQustnrIemId());

        return repository.findById(qustnrIemId)
                .map(existingItem -> {
                    assertOwner(existingItem.getFrstRegisterId(), userInfo);
                    return updateExistingItem(existingItem, qustnrIemVO, userInfo.get("uniqId"));
                })
                .map(repository::save)
                .map(EgovQusntrItemUtility::qustnrIemEntityToVO)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean delete(QustnrIemVO qustnrIemVO, Map<String, String> userInfo) {
        QustnrIemId qustnrIemId = new QustnrIemId();
        qustnrIemId.setQustnrTmplatId(qustnrIemVO.getQustnrTmplatId());
        qustnrIemId.setQestnrId(qustnrIemVO.getQestnrId());
        qustnrIemId.setQustnrQesitmId(qustnrIemVO.getQustnrQesitmId());
        qustnrIemId.setQustnrIemId(qustnrIemVO.getQustnrIemId());

        return repository.findById(qustnrIemId)
                .map(result -> {
                    assertOwner(result.getFrstRegisterId(), userInfo);
                    repository.delete(result);
                    return true;
                })
                .orElse(false);
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

    private QustnrIem updateExistingItem(QustnrIem existingItem, QustnrIemVO qustnrIemVO, String uniqId) {
        existingItem.setIemSn(qustnrIemVO.getIemSn());
        existingItem.setIemCn(qustnrIemVO.getIemCn());
        existingItem.setLastUpdtPnttm(LocalDateTime.now());
        existingItem.setLastUpdusrId(uniqId);
        return existingItem;
    }

}
