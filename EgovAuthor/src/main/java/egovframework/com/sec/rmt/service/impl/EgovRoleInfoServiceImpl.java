package egovframework.com.sec.rmt.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.sec.rmt.entity.CmmnDetailCode;
import egovframework.com.sec.rmt.entity.QCmmnDetailCode;
import egovframework.com.sec.rmt.entity.QRoleInfo;
import egovframework.com.sec.rmt.entity.RoleInfo;
import egovframework.com.sec.rmt.repository.EgovRoleInfoRepository;
import egovframework.com.sec.rmt.service.EgovRoleInfoService;
import egovframework.com.sec.rmt.service.RoleInfoDTO;
import egovframework.com.sec.rmt.service.RoleInfoVO;
import egovframework.com.sec.rmt.util.EgovRoleInfoUtility;
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

@Service("rmtEgovRoleInfoService")
public class EgovRoleInfoServiceImpl extends EgovAbstractServiceImpl implements EgovRoleInfoService {

    private final EgovRoleInfoRepository repository;
    private final EgovIdGnrService idgenService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JPAQueryFactory queryFactory;

    public EgovRoleInfoServiceImpl(
            EgovRoleInfoRepository repository,
            @Qualifier("egovRoleIdGnrService") EgovIdGnrService idgenService,
            JPAQueryFactory queryFactory
    ) {
        this.repository = repository;
        this.idgenService = idgenService;
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<RoleInfoDTO> list(RoleInfoVO roleInfoVO) {
        Pageable pageable = PageRequest.of(roleInfoVO.getFirstIndex(), roleInfoVO.getRecordCountPerPage());
        String searchCondition = roleInfoVO.getSearchCondition();
        String searchKeyword = roleInfoVO.getSearchKeyword();

        QRoleInfo roleInfo = QRoleInfo.roleInfo;
        QCmmnDetailCode cmmnDetailCode = QCmmnDetailCode.cmmnDetailCode;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition) && searchKeyword != null && !searchKeyword.isEmpty()) {
            where.and(roleInfo.roleNm.contains(searchKeyword));
        }

        JPAQuery<Tuple> query = queryFactory
                .select(roleInfo, cmmnDetailCode)
                .from(roleInfo)
                .leftJoin(cmmnDetailCode)
                .on(roleInfo.roleTy.eq(cmmnDetailCode.cmmnDetailCodeId.code).
                        and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM029")))
                .where(where)
                .orderBy(roleInfo.roleCode.asc());

        List<Tuple> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(roleInfo.count())
                        .from(roleInfo)
                        .leftJoin(cmmnDetailCode)
                        .on(roleInfo.roleTy.eq(cmmnDetailCode.cmmnDetailCodeId.code).
                                and(cmmnDetailCode.cmmnDetailCodeId.codeId.eq("COM029")))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<RoleInfoDTO> content = results.stream().map(tuple -> {
            RoleInfo r = tuple.get(roleInfo);
            CmmnDetailCode cdc = tuple.get(cmmnDetailCode);

            String codeNm = "";
            codeNm = cdc != null && cdc.getCodeNm() != null ? cdc.getCodeNm() : "";

            return new RoleInfoDTO(
                    Objects.requireNonNull(r).getRoleCode(),
                    r.getRoleNm(),
                    r.getRolePttrn(),
                    r.getRoleDc(),
                    r.getRoleTy(),
                    codeNm,
                    r.getRoleSort(),
                    r.getRoleCreatDe()
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public RoleInfoVO detail(RoleInfoVO roleInfoVO, Map<String, String> userInfo) {
        requireAuthenticated(userInfo);
        String roleCode = roleInfoVO.getRoleCode();
        return repository.findById(roleCode).map(EgovRoleInfoUtility::roleEntityToVO).orElse(null);
    }

    @Transactional
    @Override
    public RoleInfoVO insert(RoleInfoVO roleInfoVO) {
        try {
            String roleTy = roleInfoVO.getRoleTy();
            if ("method".equals(roleTy)) {
                roleInfoVO.setRoleCode("mtd-".concat(idgenService.getNextStringId()));
                roleInfoVO.setRoleCreatDe(LocalDateTime.now().format(formatter));
            } else if ("pointcut".equals(roleTy)) {
                roleInfoVO.setRoleCode("pct-".concat(idgenService.getNextStringId()));
                roleInfoVO.setRoleCreatDe(LocalDateTime.now().format(formatter));
            } else {
                roleInfoVO.setRoleCode("web-".concat(idgenService.getNextStringId()));
                roleInfoVO.setRoleCreatDe(LocalDateTime.now().format(formatter));
            }

            RoleInfo roleInfo = EgovRoleInfoUtility.roleVOToEntity(roleInfoVO);
            return EgovRoleInfoUtility.roleEntityToVO(repository.save(roleInfo));
        //2026.02.28 KISA 보안취약점 조치
        } catch (FdlException ex) {
            leaveaTrace("fail.common.insert");
            return null;
        }
    }

    @Transactional
    @Override
    public RoleInfoVO update(RoleInfoVO roleInfoVO) {
        RoleInfo roleInfo = EgovRoleInfoUtility.roleVOToEntity(roleInfoVO);
        return repository.findById(roleInfo.getRoleCode())
                .map(result -> {
                    result.setRoleNm(roleInfo.getRoleNm());
                    result.setRolePttrn(roleInfo.getRolePttrn());
                    result.setRoleDc(roleInfo.getRoleDc());
                    result.setRoleTy(roleInfo.getRoleTy());
                    result.setRoleSort(roleInfo.getRoleSort());
                    result.setRoleCreatDe(LocalDateTime.now().format(formatter));
                    return repository.save(result);
                })
                .map(EgovRoleInfoUtility::roleEntityToVO).orElse(null);
    }

    @Transactional
    @Override
    public void delete(RoleInfoVO roleInfoVO, Map<String, String> userInfo) {
        requireAuthenticated(userInfo);
        String roleCode = roleInfoVO.getRoleCode();
        repository.deleteById(roleCode);
    }

    private void requireAuthenticated(Map<String, String> userInfo) {
        if (userInfo == null || ObjectUtils.isEmpty(userInfo.get("uniqId"))) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치 - 권한 마스터 데이터는 인증된 사용자만 접근
    }

}
