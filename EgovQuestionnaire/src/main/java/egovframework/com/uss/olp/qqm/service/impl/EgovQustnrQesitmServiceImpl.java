package egovframework.com.uss.olp.qqm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uss.olp.qqm.entity.*;
import egovframework.com.uss.olp.qqm.repository.EgovQustnrQesitmRepository;
import egovframework.com.uss.olp.qqm.service.EgovQustnrQesitmService;
import egovframework.com.uss.olp.qqm.service.QustnrQesitmDTO;
import egovframework.com.uss.olp.qqm.service.QustnrQesitmVO;
import egovframework.com.uss.olp.qqm.util.EgovQustnrQesitmUtility;
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

@Service("qqmEgovQustnrQesitmService")
public class EgovQustnrQesitmServiceImpl extends EgovAbstractServiceImpl implements EgovQustnrQesitmService {

    private final EgovQustnrQesitmRepository repository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovQustnrQesitmServiceImpl(
            EgovQustnrQesitmRepository repository,
            @Qualifier("egovQustnrQestnManageIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<QustnrQesitmDTO> list(QustnrQesitmVO qustnrQesitmVO) {
        Pageable pageable = PageRequest.of(qustnrQesitmVO.getFirstIndex(), qustnrQesitmVO.getRecordCountPerPage());
        String searchCondition = qustnrQesitmVO.getSearchCondition();
        String searchKeyword = qustnrQesitmVO.getSearchKeyword();

        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qestnrInfo.qustnrSj.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(qustnrQesitm.qestnCn.contains(searchKeyword));
        }

        List<Tuple> results = qustnrQesitmQuery()
                .where(where)
                .orderBy(qustnrQesitm.qustnrQesitmId.qestnrId.desc(),
                        qustnrQesitm.qustnrQesitmId.qustnrQesitmId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(qustnrQesitm.count())
                        .from(qustnrQesitm)
                        .leftJoin(qestnrInfo)
                        .on(qustnrQesitm.qustnrQesitmId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                        .leftJoin(userMaster)
                        .on(qustnrQesitm.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(cmmnDetailCode)
                        .on(qustnrQesitm.qestnTyCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                                .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM018")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<QustnrQesitmDTO> content = results.stream().map(tuple -> {

            QustnrQesitm qqm = tuple.get(qustnrQesitm);
            QestnrInfo qe = tuple.get(qestnrInfo);
            UserMaster user = tuple.get(userMaster);
            CmmnDetailCode code = tuple.get(cmmnDetailCode);

            String qustnrSj = qe != null && qe.getQustnrSj() != null ? qe.getQustnrSj() : "";
            String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";
            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new QustnrQesitmDTO(
                    Objects.requireNonNull(qqm).getQustnrQesitmId().getQustnrTmplatId(),
                    qqm.getQustnrQesitmId().getQestnrId(),
                    qqm.getQustnrQesitmId().getQustnrQesitmId(),
                    qustnrSj,
                    qqm.getQestnSn(),
                    qqm.getQestnTyCode(),
                    codeNm,
                    qqm.getQestnCn(),
                    qqm.getMxmmChoiseCo(),
                    qqm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qqm.getFrstRegisterId(),
                    qqm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qqm.getLastUpdusrId(),
                    userNm
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public QustnrQesitmDTO detail(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        QustnrQesitmId qustnrQesitmId = new QustnrQesitmId();
        qustnrQesitmId.setQustnrTmplatId(qustnrQesitmVO.getQustnrTmplatId());
        qustnrQesitmId.setQestnrId(qustnrQesitmVO.getQestnrId());
        qustnrQesitmId.setQustnrQesitmId(qustnrQesitmVO.getQustnrQesitmId());
        QustnrQesitm question = repository.findById(qustnrQesitmId).orElse(null);
        if (question == null) {
            return null;
        }
        assertOwner(question.getFrstRegisterId(), userInfo);

        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        Tuple tuple = qustnrQesitmQuery().where(qustnrQesitm.qustnrQesitmId.qustnrQesitmId.eq(qustnrQesitmVO.getQustnrQesitmId())).fetchOne();

        QustnrQesitm qqm = tuple.get(qustnrQesitm);
        QestnrInfo qe = tuple.get(qestnrInfo);
        UserMaster user = tuple.get(userMaster);
        CmmnDetailCode code = tuple.get(cmmnDetailCode);

        String qustnrSj = qe != null && qe.getQustnrSj() != null ? qe.getQustnrSj() : "";
        String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";
        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

        return new QustnrQesitmDTO(
                Objects.requireNonNull(qqm).getQustnrQesitmId().getQustnrTmplatId(),
                qqm.getQustnrQesitmId().getQestnrId(),
                qqm.getQustnrQesitmId().getQustnrQesitmId(),
                qustnrSj,
                qqm.getQestnSn(),
                qqm.getQestnTyCode(),
                codeNm,
                qqm.getQestnCn(),
                qqm.getMxmmChoiseCo(),
                qqm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qqm.getFrstRegisterId(),
                qqm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qqm.getLastUpdusrId(),
                userNm
        );
    }

    @Transactional
    @Override
    public QustnrQesitmVO insert(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        try {
            String qustnrQesitmId = idgenService.getNextStringId();
            qustnrQesitmVO.setQustnrQesitmId(qustnrQesitmId);

            QustnrQesitm qustnrQesitm = EgovQustnrQesitmUtility.qustnrQesitmVOToEntity(qustnrQesitmVO);
            qustnrQesitm.setFrstRegistPnttm(LocalDateTime.now());
            qustnrQesitm.setFrstRegisterId(userInfo.get("uniqId"));
            qustnrQesitm.setLastUpdtPnttm(LocalDateTime.now());
            qustnrQesitm.setLastUpdusrId(userInfo.get("uniqId"));

            return EgovQustnrQesitmUtility.qustnrQesitmEntityToVO(repository.save(qustnrQesitm));
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public QustnrQesitmVO update(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        QustnrQesitmId qustnrQesitmId = new QustnrQesitmId();
        qustnrQesitmId.setQustnrTmplatId(qustnrQesitmVO.getQustnrTmplatId());
        qustnrQesitmId.setQestnrId(qustnrQesitmVO.getQestnrId());
        qustnrQesitmId.setQustnrQesitmId(qustnrQesitmVO.getQustnrQesitmId());

        return repository.findById(qustnrQesitmId)
                .map(item -> {
                    assertOwner(item.getFrstRegisterId(), userInfo);
                    return updateItem(item, qustnrQesitmVO, userInfo.get("uniqId"));
                })
                .map(repository::save)
                .map(EgovQustnrQesitmUtility::qustnrQesitmEntityToVO)
                .orElse(null);
    }

    @Transactional
    @Override
    public boolean delete(QustnrQesitmVO qustnrQesitmVO, Map<String, String> userInfo) {
        QustnrQesitmId qustnrQesitmId = new QustnrQesitmId();
        qustnrQesitmId.setQustnrTmplatId(qustnrQesitmVO.getQustnrTmplatId());
        qustnrQesitmId.setQestnrId(qustnrQesitmVO.getQestnrId());
        qustnrQesitmId.setQustnrQesitmId(qustnrQesitmVO.getQustnrQesitmId());

        return repository.findById(qustnrQesitmId)
                .map(result -> {
                    assertOwner(result.getFrstRegisterId(), userInfo);
                    repository.delete(result);
                    return true;
                })
                .orElse(false);
    }

    private QustnrQesitm updateItem(QustnrQesitm qustnrQesitm, QustnrQesitmVO qustnrQesitmVO, String uniqId) {
        qustnrQesitm.setQestnSn(qustnrQesitmVO.getQestnSn());
        qustnrQesitm.setQestnTyCode(qustnrQesitmVO.getQestnTyCode());
        qustnrQesitm.setQestnCn(qustnrQesitmVO.getQestnCn());
        qustnrQesitm.setMxmmChoiseCo(qustnrQesitmVO.getMxmmChoiseCo());
        qustnrQesitm.setLastUpdtPnttm(LocalDateTime.now());
        qustnrQesitm.setLastUpdusrId(uniqId);
        return qustnrQesitm;
    }

    @Override
    public List<QustnrQesitmDTO> qustnrQusitmList(QustnrQesitmVO qustnrQesitmVO) {
        String qustnrTmplatId = qustnrQesitmVO.getQustnrTmplatId();
        String qestnrId = qustnrQesitmVO.getQestnrId();

        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        List<Tuple> results = qustnrQesitmQuery()
                .where(qustnrQesitm.qustnrQesitmId.qustnrTmplatId.eq(qustnrTmplatId)
                        .and(qustnrQesitm.qustnrQesitmId.qestnrId.eq(qestnrId)))
                .orderBy(qustnrQesitm.qestnSn.asc())
                .fetch();

        List<QustnrQesitmDTO> content = results.stream().map(tuple -> {

            QustnrQesitm qqm = tuple.get(qustnrQesitm);
            QestnrInfo qe = tuple.get(qestnrInfo);
            UserMaster user = tuple.get(userMaster);
            CmmnDetailCode code = tuple.get(cmmnDetailCode);

            String qustnrSj = qe != null && qe.getQustnrSj() != null ? qe.getQustnrSj() : "";
            String codeNm = code != null && code.getCodeNm() != null ? code.getCodeNm() : "";
            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new QustnrQesitmDTO(
                    Objects.requireNonNull(qqm).getQustnrQesitmId().getQustnrTmplatId(),
                    qqm.getQustnrQesitmId().getQestnrId(),
                    qqm.getQustnrQesitmId().getQustnrQesitmId(),
                    qustnrSj,
                    qqm.getQestnSn(),
                    qqm.getQestnTyCode(),
                    codeNm,
                    qqm.getQestnCn(),
                    qqm.getMxmmChoiseCo(),
                    qqm.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qqm.getFrstRegisterId(),
                    qqm.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    qqm.getLastUpdusrId(),
                    userNm
            );
        }).collect(Collectors.toList());

        return content;
    }

    private JPAQuery<Tuple> qustnrQesitmQuery(){

        QQustnrQesitm qustnrQesitm = QQustnrQesitm.qustnrQesitm;
        QQestnrInfo qestnrInfo = QQestnrInfo.qestnrInfo;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        return queryFactory
                .select(qustnrQesitm, qestnrInfo, userMaster, cmmnDetailCode)
                .from(qustnrQesitm)
                .leftJoin(qestnrInfo)
                .on(qustnrQesitm.qustnrQesitmId.qestnrId.eq(qestnrInfo.qestnrInfoId.qestnrId))
                .leftJoin(userMaster)
                .on(qustnrQesitm.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(cmmnDetailCode)
                .on(qustnrQesitm.qestnTyCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                        .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM018")));
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

}
