package egovframework.com.uat.uia.service.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.uat.uia.entity.*;
import egovframework.com.uat.uia.repository.EgovEmployMemberRepository;
import egovframework.com.uat.uia.repository.EgovEnterpriseMemberRepository;
import egovframework.com.uat.uia.repository.EgovGeneralMemberRepository;
import egovframework.com.uat.uia.repository.EgovLoginPolicyRepository;
import egovframework.com.uat.uia.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service("egovLoginManageService")
@RequiredArgsConstructor
@Slf4j
public class EgovLoginManageServiceImpl extends EgovAbstractServiceImpl implements EgovLoginManageService {

    private final EgovGeneralMemberRepository genRepository; // 일반회원
    private final EgovEnterpriseMemberRepository entRepository; // 기업회원
    private final EgovEmployMemberRepository empRepository; // 업무사용자
	private final EgovLoginPolicyRepository loginPolicyRepository; // 로그인정책관리
    private final JPAQueryFactory queryFactory;

    @Override
    public LoginDTO actionLogin(LoginVO loginVO) {
        String userId = loginVO.getUserId();
        String userSe = loginVO.getUserSe();
        String encPassword = encryptPassword(loginVO.getUserPw(), loginVO.getUserId());
        // encPassword(암호화된 비밀번호)는 로그로 남기지 않는다 (CWE-532).
        log.debug("userId = {}, userSe = {}", userId, userSe);

        QGnrlMber gnrlMber = QGnrlMber.gnrlMber;
        QEntrprsMber entrprsMber = QEntrprsMber.entrprsMber;
        QEmplyrInfo emplyrInfo = QEmplyrInfo.emplyrInfo;
        QEmplyrscrtyestbs emplyrscrtyestbs = QEmplyrscrtyestbs.emplyrscrtyestbs;

        Tuple tuple = null;
        Emplyrscrtyestbs es = null;
        String authorCode = "";

        switch (userSe) {
            case "GNR":
                tuple = queryFactory
                        .select(gnrlMber, emplyrscrtyestbs)
                        .from(gnrlMber)
                        .innerJoin(emplyrscrtyestbs)
                        .on(gnrlMber.esntlId.eq(emplyrscrtyestbs.scrtyDtrmnTrgetId))
                        .where(gnrlMber.mberId.eq(userId)
                                .and(gnrlMber.password.eq(encPassword))
                                .and(gnrlMber.mberStus.eq("P")))
                        .fetchOne();
                if (tuple == null) {
                    return null;
                }
                GnrlMber gm = tuple.get(gnrlMber);
                es = tuple.get(emplyrscrtyestbs);
                authorCode = es != null && es.getAuthorCode() != null ? es.getAuthorCode() : "";

                return new LoginDTO(
                        Objects.requireNonNull(gm).getMberId(),
                        gm.getMberNm(),
                        gm.getPassword(),
                        gm.getIhidNum(),
                        gm.getMberEmailAdres(),
                        "GNR",
                        "",
                        gm.getEsntlId(),
                        "",
                        authorCode
                );
            case "ENT":
                tuple = queryFactory
                        .select(entrprsMber,emplyrscrtyestbs)
                        .from(entrprsMber)
                        .innerJoin(emplyrscrtyestbs)
                        .on(entrprsMber.esntlId.eq(emplyrscrtyestbs.scrtyDtrmnTrgetId))
                        .where(entrprsMber.entrprsMberId.eq(loginVO.getUserId())
                                .and(entrprsMber.entrprsMberPassword.eq(encPassword))
                                .and(entrprsMber.entrprsMberStus.eq("P")))
                        .fetchOne();
                if (tuple == null) {
                    return null;
                }
                EntrprsMber em = tuple.get(entrprsMber);
                es = tuple.get(emplyrscrtyestbs);
                authorCode = es != null && es.getAuthorCode() != null ? es.getAuthorCode() : "";
                return new LoginDTO(
                        Objects.requireNonNull(em).getEntrprsMberId(),
                        em.getCmpnyNm(),
                        em.getEntrprsMberPassword(),
                        em.getBizrno(),
                        em.getApplcntEmailAdres(),
                        "ENT",
                        "",
                        em.getEsntlId(),
                        "",
                        authorCode
                );
            case "USR":
                tuple = queryFactory
                        .select(emplyrInfo,emplyrscrtyestbs)
                        .from(emplyrInfo)
                        .innerJoin(emplyrscrtyestbs)
                        .on(emplyrInfo.esntlId.eq(emplyrscrtyestbs.scrtyDtrmnTrgetId))
                        .where(emplyrInfo.emplyrId.eq(loginVO.getUserId())
                                .and(emplyrInfo.password.eq(encPassword))
                                .and(emplyrInfo.emplyrStusCode.eq("P")))
                        .fetchOne();
                if (tuple == null) {
                    return null;
                }
                EmplyrInfo ei = tuple.get(emplyrInfo);
                es = tuple.get(emplyrscrtyestbs);
                authorCode = es != null && es.getAuthorCode() != null ? es.getAuthorCode() : "";
                return new LoginDTO(
                        Objects.requireNonNull(ei).getEmplyrId(),
                        ei.getUserNm(),
                        ei.getPassword(),
                        ei.getIhidNum(),
                        ei.getEmailAdres(),
                        "USR",
                        ei.getOrgnztId(),
                        ei.getEsntlId(),
                        "",
                        authorCode
                );
            default:
                return null;
        }
    }

    @Override
    public LoginPolicyVO loginPolicy(LoginPolicyVO loginPolicyVO) {
        LoginPolicy loginPolicy = loginPolicyRepository.findById(loginPolicyVO.getEmployerId()).orElse(null);
        if (!ObjectUtils.isEmpty(loginPolicy)) {
            loginPolicyVO.setEmployerId(loginPolicy.getEmployerId());
            loginPolicyVO.setLmttAt(loginPolicy.getLmttAt());
            loginPolicyVO.setIpInfo(loginPolicy.getIpInfo());
        }
        return loginPolicyVO;
    }

    @Override
    public LoginIncorrectVO loginIncorrectList(LoginVO loginVO) {
        String userId = loginVO.getUserId();
        String userSe = loginVO.getUserSe();

        if (ObjectUtils.isEmpty(userId) || ObjectUtils.isEmpty(userSe)) {
            return null;
        }

        switch (userSe) {
            case "GNR": // 일반회원
                return getLoginInfo(genRepository::findById, userId, result -> new LoginIncorrectVO(
                        result.getMberId(), result.getPassword(), result.getMberNm(), userSe,
                        result.getEsntlId(), getLockAt(result.getLockAt()), getLockCnt(result.getLockCnt())
                ));
            case "ENT": // 기업회원
                return getLoginInfo(entRepository::findById, userId, result -> new LoginIncorrectVO(
                        result.getEntrprsMberId(), result.getEntrprsMberPassword(), result.getCmpnyNm(), userSe,
                        result.getEsntlId(), getLockAt(result.getLockAt()), getLockCnt(result.getLockCnt())
                ));
            case "USR": // 업무사용자
                return getLoginInfo(empRepository::findById, userId, result -> new LoginIncorrectVO(
                        result.getEmplyrId(), result.getPassword(), result.getUserNm(), userSe,
                        result.getEsntlId(), getLockAt(result.getLockAt()), getLockCnt(result.getLockCnt())
                ));
            default:
                return null;
        }
    }

    private <T> LoginIncorrectVO getLoginInfo(Function<String, Optional<T>> findByIdFunction, String userId, Function<T, LoginIncorrectVO> mapper) {
        return findByIdFunction.apply(userId)
                .map(mapper)
                .orElse(null);
    }

    private String getLockAt(String lockAt) {
        return ObjectUtils.isEmpty(lockAt) ? "N" : lockAt;
    }

    private int getLockCnt(Integer lockCnt) {
        return ObjectUtils.isEmpty(lockCnt) ? 0 : lockCnt;
    }

    @Override
    public String loginIncorrectProcess(LoginVO loginVO, LoginIncorrectVO loginIncorrectVO, String lockCount) {
        String processCode = "C";
        String userId = loginVO.getUserId();
        String userSe = loginVO.getUserSe();
        String encPassword = encryptPassword(loginVO.getUserPw(), loginVO.getUserId());
        String lockAt = getLockAt(loginIncorrectVO.getLockAt());
        int lockCnt = getLockCnt(loginIncorrectVO.getLockCnt());
        int lockConfigCnt = Integer.parseInt(lockCount);

        if (ObjectUtils.isEmpty(userId) || ObjectUtils.isEmpty(userSe)) {
            return processCode;
        }

        // 비밀번호가 맞는 경우
        if (loginIncorrectVO.getUserPw().equals(encPassword)) {
            saveLoginIncorrect(userId, userSe, "E", lockCnt);
            return "E";
        }

        // 계정이 잠겨있는 경우
        if ("Y".equals(lockAt)) {
            return "L";
        }

        // 실패 횟수가 잠금 임계값에 도달한 경우
        if (lockCnt + 1 >= lockConfigCnt) {
            saveLoginIncorrect(userId, userSe, "L", lockCnt);
            return "L";
        }

        // 일반적인 실패 처리
        saveLoginIncorrect(userId, userSe, "C", lockCnt);
        return "C";
    }

    private void saveLoginIncorrect(String userId, String userSe, String processCode, int lockCnt) {
        LocalDateTime now = LocalDateTime.now();
        switch (userSe) {
            case "GNR": // 일반회원
                genRepository.findById(userId).ifPresent(gnrlMber -> {
                    updateLockStatus(gnrlMber, processCode, lockCnt, now);
                    genRepository.save(gnrlMber);
                });
                break;
            case "ENT": // 기업회원
                entRepository.findById(userId).ifPresent(entrprsMber -> {
                    updateLockStatus(entrprsMber, processCode, lockCnt, now);
                    entRepository.save(entrprsMber);
                });
                break;
            case "USR": // 업무사용자
                empRepository.findById(userId).ifPresent(emplyrInfo -> {
                    updateLockStatus(emplyrInfo, processCode, lockCnt, now);
                    empRepository.save(emplyrInfo);
                });
                break;
            default:
                break;
        }
    }

    // 공통 업데이트 로직
    private void updateLockStatus(CommonEntity entity, String processCode, int lockCnt, LocalDateTime now) {
        switch (processCode) {
            case "E":
                entity.setLockAt(null);
                entity.setLockCnt(0);
                entity.setLockLastPnttm(null);
                break;
            case "L":
                entity.setLockAt("Y");
                entity.setLockCnt(lockCnt + 1);
                entity.setLockLastPnttm(now);
                break;
            case "C":
                entity.setLockCnt(lockCnt + 1);
                entity.setLockLastPnttm(now);
                break;
            default:
                break;
        }
    }

    private String encryptPassword(String key, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(md.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.debug("##### EgovLoginManageServiceImpl NoSuchAlgorithmException >>> {}", e.getMessage());
            return "0";
        }
    }

}
