package egovframework.com.sec.rgm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.sec.rgm.entity.*;
import egovframework.com.sec.rgm.repository.EgovAuthorGroupRepository;
import egovframework.com.sec.rgm.repository.EgovAuthorInfoRepository;
import egovframework.com.sec.rgm.service.AuthorGroupDTO;
import egovframework.com.sec.rgm.service.AuthorGroupVO;
import egovframework.com.sec.rgm.service.AuthorInfoVO;
import egovframework.com.sec.rgm.service.EgovAuthorGroupService;
import egovframework.com.sec.rgm.util.EgovAuthorGroupUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("rgmEgovAuthorGroupService")
@RequiredArgsConstructor
public class EgovAuthorGroupServiceImpl extends EgovAbstractServiceImpl implements EgovAuthorGroupService {

    private final EgovAuthorGroupRepository repository;
    private final EgovAuthorInfoRepository authorInfoRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuthorGroupDTO> list(AuthorGroupVO authorGroupVO) {
        Pageable pageable = PageRequest.of(authorGroupVO.getFirstIndex(), authorGroupVO.getRecordCountPerPage());
        String searchCondition = authorGroupVO.getSearchCondition();
        String searchKeyword = authorGroupVO.getSearchKeyword();

        QUserMaster user = QUserMaster.userMaster;
        QEmplyrscrtyestbs auth = QEmplyrscrtyestbs.emplyrscrtyestbs;
        QCmmnDetailCode code = QCmmnDetailCode.cmmnDetailCode;

        JPAQuery<Tuple> query = queryFactory
                .select(user, auth, code)  // 모든 엔티티 객체 통째로 select
                .from(user)
                .leftJoin(auth).on(user.esntlId.eq(auth.scrtyDtrmnTrgetId))
                .leftJoin(code).on(
                    code.cmmnDetailCodeId.code.eq(
                        Expressions.stringTemplate(
                                "(CASE WHEN {0} = 'GNR' THEN 'USR01' WHEN {0} = 'ENT' THEN 'USR02' ELSE 'USR03' END)",
                                user.userSe
                        )
                    )
                    .and(code.cmmnDetailCodeId.codeId.eq("COM012"))
                    .and(code.useAt.eq("Y"))
                );

        // 검색 조건
        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(user.userId.contains(searchKeyword));
        } else if ("2".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(user.userNm.contains(searchKeyword));
        } else if ("3".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(user.groupId.eq(searchKeyword));
        }

        query.where(where);
        query.orderBy(code.cmmnDetailCodeId.code.asc());

        // 페이징 적용
        List<Tuple> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리
        long total = Optional.ofNullable(
                queryFactory
                .select(user.count())  // 모든 엔티티 객체 통째로 select
                .from(user)
                .leftJoin(auth).on(user.esntlId.eq(auth.scrtyDtrmnTrgetId))
                .leftJoin(code).on(
                        code.cmmnDetailCodeId.code.eq(
                                        Expressions.stringTemplate(
                                                "(CASE WHEN {0} = 'GNR' THEN 'USR01' WHEN {0} = 'ENT' THEN 'USR02' ELSE 'USR03' END)",
                                                user.userSe
                                        )
                                )
                                .and(code.cmmnDetailCodeId.codeId.eq("COM012"))
                                .and(code.useAt.eq("Y"))
                )
                .fetchOne()
        ).orElse(0L);

            // Tuple → DTO 매핑
            List<AuthorGroupDTO> content = results.stream().map(tuple -> {
                UserMaster u = tuple.get(user);
                Emplyrscrtyestbs a = tuple.get(auth);
                CmmnDetailCode c = tuple.get(code);

                String mappedCode;
                switch (Objects.requireNonNull(u).getUserSe()) {
                    case "GNR":
                        mappedCode = "USR01";
                        break;
                    case "ENT":
                        mappedCode = "USR02";
                        break;
                    default:
                        mappedCode = "USR03";
                        break;
                }

                return new AuthorGroupDTO(
                        u.getUserId(),
                        u.getUserNm(),
                        u.getGroupId(),
                        mappedCode,
                        c != null ? c.getCodeNm() : null,
                        a != null ? a.getAuthorCode() : null,
                        a != null && a.getScrtyDtrmnTrgetId() != null ? "Y" : "N",
                        u.getEsntlId()
                );
            }).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, total);

    }

    @Override
    public List<AuthorInfoVO> authorInfoList() {
        return authorInfoRepository.findAll().stream().map(EgovAuthorGroupUtility::authorInfoEntityToVO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AuthorGroupVO insert(AuthorGroupVO authorGroupVO) {
        Emplyrscrtyestbs emplyrscrtyestbs = EgovAuthorGroupUtility.authorGroupVOToEntity(authorGroupVO);
        return EgovAuthorGroupUtility.authorGroupEntityToVO(repository.save(emplyrscrtyestbs));
    }

    @Transactional
    @Override
    public AuthorGroupVO update(AuthorGroupVO authorGroupVO) {
        Emplyrscrtyestbs emplyrscrtyestbs = EgovAuthorGroupUtility.authorGroupVOToEntity(authorGroupVO);
        return repository.findById(emplyrscrtyestbs.getScrtyDtrmnTrgetId())
                .map(result -> {
                    result.setMberTyCode(emplyrscrtyestbs.getMberTyCode());
                    result.setAuthorCode(emplyrscrtyestbs.getAuthorCode());
                    return repository.save(result);
                })
                .map(EgovAuthorGroupUtility::authorGroupEntityToVO).orElse(null);
    }

    @Transactional
    @Override
    public void delete(AuthorGroupVO authorGroupVO, Map<String, String> userInfo) {
        requireAuthenticated(userInfo);
        String scrtyDtrmnTrgetId = authorGroupVO.getScrtyDtrmnTrgetId();
        repository.deleteById(scrtyDtrmnTrgetId);
    }

    private void requireAuthenticated(Map<String, String> userInfo) {
        if (userInfo == null || ObjectUtils.isEmpty(userInfo.get("uniqId"))) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치 - 권한 그룹 매핑은 인증된 사용자만 삭제
    }

}
