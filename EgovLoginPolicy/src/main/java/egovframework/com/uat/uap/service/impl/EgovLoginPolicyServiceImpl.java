package egovframework.com.uat.uap.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uat.uap.entity.LoginPolicy;
import egovframework.com.uat.uap.entity.QLoginPolicy;
import egovframework.com.uat.uap.entity.QUserMaster;
import egovframework.com.uat.uap.entity.UserMaster;
import egovframework.com.uat.uap.repository.EgovLoginPolicyRepository;
import egovframework.com.uat.uap.service.EgovLoginPolicyService;
import egovframework.com.uat.uap.service.LoginPolicyDTO;
import egovframework.com.uat.uap.service.LoginPolicyVO;
import egovframework.com.uat.uap.util.EgovLoginPolicyUtility;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
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

@Service("uapEgovLoginPolicyServiceImpl")
@RequiredArgsConstructor
public class EgovLoginPolicyServiceImpl extends EgovAbstractServiceImpl implements EgovLoginPolicyService {

    private final EgovLoginPolicyRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LoginPolicyDTO> list(LoginPolicyVO loginPolicyVO) {
        Pageable pageable = PageRequest.of(loginPolicyVO.getFirstIndex(), loginPolicyVO.getRecordCountPerPage());
        String searchCondition = loginPolicyVO.getSearchCondition();
        String searchKeyword = loginPolicyVO.getSearchKeyword();

        QUserMaster userMaster = QUserMaster.userMaster;
        QLoginPolicy loginPolicy = QLoginPolicy.loginPolicy;

        BooleanBuilder where = new BooleanBuilder();
        if ("1".equals(searchCondition)) {
            if (searchKeyword == null || searchKeyword.isEmpty()) {
            } else {
                where.and(userMaster.userNm.contains(searchKeyword));;
            }
        }

        List<Tuple> results = queryFactory
                .select(userMaster, loginPolicy)
                .from(userMaster)
                .leftJoin(loginPolicy)
                .on(userMaster.userId.eq(loginPolicy.employerId))
                .where(where)
                .orderBy(userMaster.userId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(userMaster.count())
                        .from(userMaster)
                        .leftJoin(loginPolicy)
                        .on(userMaster.userId.eq(loginPolicy.employerId))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        List<LoginPolicyDTO> content = results.stream().map(tuple -> {

            UserMaster user = tuple.get(userMaster);
            LoginPolicy lp = tuple.get(loginPolicy);

            String employerId = "N";
            String ipInfo = "";
            String dplctPermAt = "";
            String lmttAt = "";
            String lastUpdusrId = "";
            String lastUpdtPnttm = "";

            if (lp != null) {
                employerId = lp.getEmployerId() == null ? "N" : "Y";
                ipInfo = lp.getIpInfo() != null && !lp.getIpInfo().isEmpty() ? lp.getIpInfo() : "";
                dplctPermAt = lp.getDplctPermAt() != null && !lp.getDplctPermAt().isEmpty() ? lp.getDplctPermAt() : "";
                lmttAt = lp.getLmttAt() != null && !lp.getLmttAt().isEmpty() ? lp.getLmttAt() : "";
                lastUpdusrId = lp.getLastUpdusrId() != null && !lp.getLastUpdusrId().isEmpty() ? lp.getLastUpdusrId() : "";
                lastUpdtPnttm = lp.getLastUpdtPnttm() != null
                        ? lp.getLastUpdtPnttm().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "";
            }
            return new LoginPolicyDTO(
                    Objects.requireNonNull(user).getUserId(),
                    user.getUserNm(),
                    user.getUserSe(),
                    ipInfo,
                    dplctPermAt,
                    lmttAt,
                    lastUpdusrId,
                    lastUpdtPnttm,
                    employerId
            );
        }).collect(Collectors.toList());
        return new PageImpl<>(content,pageable,total);
    }

    @Override
    public LoginPolicyVO detail(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo) {
        assertLoginPolicyOwner(loginPolicyVO.getEmployerId(), userInfo);
        String employerId = loginPolicyVO.getEmployerId();
        return this.repository.findById(employerId).map(EgovLoginPolicyUtility::entityToVO)
                .orElse(new LoginPolicyVO(
                        employerId,
                        "",
                        "",
                        "",
                        "",
                        LocalDateTime.now(),
                        "",
                        LocalDateTime.now(),
                        "",
                        "")
                );
    }

    @Transactional
    @Override
    public LoginPolicyVO insert(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo) {
        LoginPolicy loginPolicy = EgovLoginPolicyUtility.vOToEntity(loginPolicyVO);
        loginPolicy.setFrstRegisterId(userInfo.get("uniqId"));
        loginPolicy.setFrstRegisterPnttm(LocalDateTime.now());
        loginPolicy.setLastUpdusrId(userInfo.get("uniqId"));
        loginPolicy.setLastUpdtPnttm(LocalDateTime.now());
        return EgovLoginPolicyUtility.entityToVO(this.repository.save(loginPolicy));
    }

    @Transactional
    @Override
    public LoginPolicyVO update(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo) {
        assertLoginPolicyOwner(loginPolicyVO.getEmployerId(), userInfo);
        LoginPolicy loginPolicy = EgovLoginPolicyUtility.vOToEntity(loginPolicyVO);
        return this.repository.findById(loginPolicyVO.getEmployerId())
                .map(result -> {
                    result.setIpInfo(loginPolicy.getIpInfo());
                    result.setLmttAt(loginPolicy.getLmttAt());
                    result.setLastUpdusrId(userInfo.get("uniqId"));
                    result.setLastUpdtPnttm(LocalDateTime.now());
                    return this.repository.save(result);
                })
                .map(EgovLoginPolicyUtility::entityToVO).orElse(loginPolicyVO);
    }

    @Transactional
    @Override
    public void delete(LoginPolicyVO loginPolicyVO, Map<String, String> userInfo) {
        assertLoginPolicyOwner(loginPolicyVO.getEmployerId(), userInfo);
        String employerId = loginPolicyVO.getEmployerId();
        this.repository.deleteById(employerId);
    }

    private void assertLoginPolicyOwner(String employerId, Map<String, String> userInfo) {
        String userId = userInfo != null ? userInfo.get("userId") : null;
        if (ObjectUtils.isEmpty(userId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 2026.07.13 KISA 보안취약점 조치
        if (!Objects.equals(userId, employerId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

}
