package egovframework.com.cop.cmy.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cop.cmy.entity.*;
import egovframework.com.cop.cmy.repository.EgovCommunityRepository;
import egovframework.com.cop.cmy.repository.EgovCommunityUserRepository;
import egovframework.com.cop.cmy.service.CommunityDTO;
import egovframework.com.cop.cmy.service.CommunityUserVO;
import egovframework.com.cop.cmy.service.CommunityVO;
import egovframework.com.cop.cmy.service.EgovCommunityService;
import egovframework.com.cop.cmy.util.EgovCommunityUtility;
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

@Service("cmyEgovCommunityServiceImpl")
public class EgovCommunityServiceImpl extends EgovAbstractServiceImpl implements EgovCommunityService {

    private final EgovCommunityRepository repository;
    private final EgovCommunityUserRepository userRepository;
    private final EgovIdGnrService idgenService;
    private final JPAQueryFactory queryFactory;

    public EgovCommunityServiceImpl(
            EgovCommunityRepository repository,
            EgovCommunityUserRepository userRepository,
            @Qualifier("egovCmmntyIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<CommunityDTO> list(CommunityVO communityVO) {
        Pageable pageable = PageRequest.of(communityVO.getFirstIndex(), communityVO.getRecordCountPerPage());
        String searchCondition = communityVO.getSearchCondition();
        String searchKeyword = communityVO.getSearchKeyword();

        QCmmnty cmmnty = QCmmnty.cmmnty;
        QUserMaster userMaster = QUserMaster.userMaster;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition)) {
            if (searchKeyword == null || searchKeyword.isEmpty()) {
            } else {
                where.and(cmmnty.cmmntyNm.contains(searchKeyword));;
            }
        }

        List<Tuple> results = queryFactory
                .select(cmmnty,userMaster,cmmnDetailCode)
                .from(cmmnty)
                .leftJoin(userMaster)
                .on(cmmnty.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(cmmnDetailCode)
                .on(cmmnty.registSeCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                        .and(cmmnDetailCode.useAt.eq("Y"))
                        .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM001")))
                .where(where)
                .orderBy(cmmnty.frstRegistPnttm.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long total = Optional.ofNullable(
                queryFactory
                        .select(cmmnty.count())
                        .from(cmmnty)
                        .leftJoin(userMaster)
                        .on(cmmnty.frstRegisterId.eq(userMaster.esntlId))
                        .leftJoin(cmmnDetailCode)
                        .on(cmmnty.registSeCode.eq(cmmnDetailCode.cmmnDetailCodeId.code)
                                .and(cmmnDetailCode.useAt.eq("Y"))
                                .and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM001")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<CommunityDTO> content = results.stream().map(tuple -> {

            Cmmnty c = tuple.get(cmmnty);
            UserMaster user = tuple.get(userMaster);

            String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";

            return new CommunityDTO(
                    Objects.requireNonNull(c).getCmmntyId(),
                    c.getCmmntyNm(),
                    c.getCmmntyIntrcn(),
                    c.getUseAt(),
                    c.getRegistSeCode(),
                    c.getTmplatId(),
                    c.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    c.getFrstRegisterId(),
                    c.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    c.getLastUpdusrId(),
                    userNm,
                    ""
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public CommunityDTO detail(CommunityVO communityVO, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            return null;
        }

        CmmntyUserId memberId = new CmmntyUserId();
        memberId.setCmmntyId(communityVO.getCmmntyId());
        memberId.setEmplyrId(uniqId);
        Optional<CmmntyUser> member = userRepository.findById(memberId);
        if (member.isEmpty() || !"Y".equals(member.get().getUseAt())) {
            return null;
        }

        QCmmnty cmmnty = QCmmnty.cmmnty;
        QUserMaster userMaster = QUserMaster.userMaster;
        QTmplatInfo tmplatInfo = QTmplatInfo.tmplatInfo;

        Tuple tuple = queryFactory
                .select(cmmnty,userMaster,tmplatInfo)
                .from(cmmnty)
                .leftJoin(userMaster)
                .on(cmmnty.frstRegisterId.eq(userMaster.esntlId))
                .leftJoin(tmplatInfo)
                .on(cmmnty.tmplatId.eq(tmplatInfo.tmplatId))
                .where(cmmnty.cmmntyId.eq(communityVO.getCmmntyId()))
                .fetchOne();

        if (tuple == null) {
            return null;
        }

        Cmmnty c = Objects.requireNonNull(tuple.get(cmmnty));
        UserMaster user = tuple.get(userMaster);
        TmplatInfo tmplat = tuple.get(tmplatInfo);

        String userNm = user != null && user.getUserNm() != null ? user.getUserNm() : "";
        String tmplatNm = tmplat != null && tmplat.getTmplatNm() != null ? tmplat.getTmplatNm() : "";

        return new CommunityDTO(
                Objects.requireNonNull(c).getCmmntyId(),
                c.getCmmntyNm(),
                c.getCmmntyIntrcn(),
                c.getUseAt(),
                c.getRegistSeCode(),
                c.getTmplatId(),
                c.getFrstRegistPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                c.getFrstRegisterId(),
                c.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                c.getLastUpdusrId(),
                userNm,
                tmplatNm
        );
    }

    @Transactional
    @Override
    public CommunityVO insert(CommunityVO communityVO, Map<String, String> userInfo) {
        try {
            String cmmntyId = idgenService.getNextStringId();

            communityVO.setCmmntyId(cmmntyId);
            communityVO.setRegistSeCode("REGC02");
            communityVO.setFrstRegistPnttm(LocalDateTime.now());
            communityVO.setFrstRegisterId(userInfo.get("uniqId"));
            communityVO.setLastUpdtPnttm(LocalDateTime.now());
            communityVO.setLastUpdusrId(userInfo.get("uniqId"));
            Cmmnty cmmnty = repository.save(EgovCommunityUtility.communityVOToEntity(communityVO));

            CommunityUserVO communityUserVO = getCommunityUserVO(cmmntyId, userInfo.get("uniqId"));
            userRepository.save(EgovCommunityUtility.communityUsereVOToEntity(communityUserVO));

            return EgovCommunityUtility.cmmntyEntityToVO(cmmnty);
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public CommunityVO update(CommunityVO communityVO, Map<String, String> userInfo) {
        String uniqId = userInfo != null ? userInfo.get("uniqId") : null;
        if (ObjectUtils.isEmpty(uniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        return repository.findById(communityVO.getCmmntyId())
                .map(result -> {
                    CmmntyUserId memberId = new CmmntyUserId();
                    memberId.setCmmntyId(communityVO.getCmmntyId());
                    memberId.setEmplyrId(uniqId);
                    boolean isManager = userRepository.findById(memberId)
                            .filter(member -> "Y".equals(member.getUseAt()))
                            .filter(member -> "Y".equals(member.getMngrAt()))
                            .isPresent();
                    if (!Objects.equals(uniqId, result.getFrstRegisterId()) && !isManager) {
                        throw new IllegalStateException("수정 권한이 없습니다.");
                    }
                    result.setCmmntyNm(communityVO.getCmmntyNm());
                    result.setCmmntyIntrcn(communityVO.getCmmntyIntrcn());
                    result.setLastUpdtPnttm(LocalDateTime.now());
                    result.setLastUpdusrId(uniqId);
                    return repository.save(result);
                })
                .map(EgovCommunityUtility::cmmntyEntityToVO).orElse(null);
    }

    private CommunityUserVO getCommunityUserVO(String cmmntyId, String uniqId) {
        CommunityUserVO communityUserVO = new CommunityUserVO();
        communityUserVO.setCmmntyId(cmmntyId);
        communityUserVO.setEmplyrId(uniqId);
        communityUserVO.setMngrAt("Y");
        communityUserVO.setMberSttus("P");
        communityUserVO.setSbscrbDe(LocalDateTime.now());
        communityUserVO.setUseAt("Y");
        communityUserVO.setFrstRegistPnttm(LocalDateTime.now());
        communityUserVO.setFrstRegisterId(uniqId);
        communityUserVO.setLastUpdtPnttm(LocalDateTime.now());
        communityUserVO.setLastUpdusrId(uniqId);
        return communityUserVO;
    }

}
