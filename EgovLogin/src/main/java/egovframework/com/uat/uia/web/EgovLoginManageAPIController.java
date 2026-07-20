package egovframework.com.uat.uia.web;

import egovframework.com.uat.uia.service.*;
import egovframework.com.uat.uia.token.AuthorizeToken;
import egovframework.com.uat.uia.token.AuthorizeTokenService;
import egovframework.com.uat.uia.util.EgovClientIP;
import egovframework.com.uat.uia.util.EgovJwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.boot.security.bean.EgovReloadableFilterInvocationSecurityMetadataSource;
import org.egovframe.boot.security.userdetails.util.EgovUserDetailsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("uiaEgovLoginManageAPIController")
@RequestMapping("/uat/uia")
@RequiredArgsConstructor
@Slf4j
public class EgovLoginManageAPIController {

    @Value("${egov.login.lock}")
    private String lock;

    @Value("${egov.login.lockCount}")
    private String lockCount;

    private final EgovLoginManageService service;
    private final EgovJwtProvider jwtProvider;
    private final ReloadableResourceBundleMessageSource messageSource;
    private final EgovReloadableFilterInvocationSecurityMetadataSource securityMetadataSource;
    private final AuthenticationManager authenticationManager;
    private final AuthorizeTokenService authorizeTokenService;

    /**
     * 로그인 실패 응답 메시지를 생성하는 헬퍼 메소드
     * 
     * @param errorMessage 오류 메시지
     * @return ResponseEntity with login failure message
     */
    private ResponseEntity<Map<String, Object>> createLoginFailureResponse(String errorMessage) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", "loginFailure");
        message.put("userInfo", "");
        message.put("userId", "");
        message.put("accessToken", "");
        message.put("refreshToken", "");
        message.put("errors", errorMessage);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/actionLogin")
    public ResponseEntity<?> actionLogin(@RequestBody LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) {
        log.debug("### EgovLoginManageAPIController.actionLogin() Start... ");

        if (ObjectUtils.isEmpty(loginVO)) {
            return ResponseEntity.ok(messageSource.getMessage("fail.common.login", null, request.getLocale()));
        }

        Map<String, Object> message = new HashMap<>();
        Map<String, Object> incorrect = loginIncorrect(loginVO, request);
        if (!incorrect.isEmpty()) {
            return ResponseEntity.ok(incorrect);
        }

        LoginDTO loginDTO = service.actionLogin(loginVO);
        if (ObjectUtils.isEmpty(loginDTO)) {
            message.put("status", "loginFailure");
            message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
            return ResponseEntity.ok(message);
        } else {
            try {
                // 1. Spring Security를 통한 권한 처리
                Authentication authentication;
                try {
                    authentication = new UsernamePasswordAuthenticationToken(loginDTO.getId(), loginDTO.getPassword());
                    // SecurityContextHolder 설정
                    SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authentication));
                } catch (AuthenticationException e) {
                    log.error("Spring Security authentication error: {}", e.getMessage(), e);
                    return createLoginFailureResponse("권한 처리 중 오류가 발생했습니다. (Spring Security Authentication Error: " + e.getMessage() + ")");
                }

                // 권한에 해당하는 Role 정보를 문자열 형태로 설정
                List<Map.Entry<String, String>> rolePatternList = EgovUserDetailsHelper.getRoleAndPatternList();
                List<String> authorList = EgovUserDetailsHelper.getAuthorities();
                String accessiblePatterns = EgovUserDetailsHelper.getAccessiblePatterns(rolePatternList, authorList);

                // SecurityContextHolder 삭제
                new SecurityContextLogoutHandler().logout(request, response, authentication);

                LoginVO dtoToVo = new LoginVO();
                dtoToVo.setUserId(loginDTO.getId());
                dtoToVo.setName(loginDTO.getName());
                dtoToVo.setUniqId(loginDTO.getUniqId());
                dtoToVo.setAuthorList(accessiblePatterns);

                // 2. 토큰 생성
                String accessToken;
                String refreshToken;
                try {
                    accessToken = jwtProvider.createAccessToken(dtoToVo);
                    refreshToken = jwtProvider.createRefreshToken(dtoToVo);
                } catch (JwtException e) {
                    log.error("JWT token creation error: {}", e.getMessage(), e);
                    return createLoginFailureResponse("토큰 생성 중 오류가 발생했습니다. (Token Creation Error: " + e.getMessage() + ")");
                } catch (IllegalArgumentException e) {
                    log.error("JWT token creation argument error: {}", e.getMessage(), e);
                    return createLoginFailureResponse("토큰 생성 중 오류가 발생했습니다. (Token Creation Argument Error: " + e.getMessage() + ")");
                } catch (Exception e) {
                    log.error("Unexpected error during token creation: {}", e.getMessage(), e);
                    return createLoginFailureResponse("토큰 생성 중 예기치 않은 오류가 발생했습니다. (Unexpected Token Creation Error: " + e.getMessage() + ")");
                }

                // 3. Redis에 토큰 저장
                try {
                    // Delete old token data of redis
                    AuthorizeToken oldAuthToken = authorizeTokenService.findToken(dtoToVo.getUserId());
                    if (!ObjectUtils.isEmpty(oldAuthToken)) {
                        authorizeTokenService.deleteTokenById(oldAuthToken.getTokenKey());
                        authorizeTokenService.deleteToken(dtoToVo.getUserId());
                    }

                    // Save token data of redis
                    String tokenKey = jwtProvider.generateHash(accessToken);
                    authorizeTokenService.saveTokenById(tokenKey, dtoToVo.getUserId());
                    authorizeTokenService.saveToken(dtoToVo.getUserId(), tokenKey, refreshToken, jwtProvider.getRefreshExpiration());
                } catch (DataAccessResourceFailureException e) {
                    log.error("Redis connection failure error: {}", e.getMessage(), e);
                    return createLoginFailureResponse("Redis 연결 오류가 발생했습니다. (Redis Connection Error: " + e.getMessage() + ")");
                } catch (DataAccessException e) {
                    log.error("Redis data access error: {}", e.getMessage(), e);
                    return createLoginFailureResponse("Redis 토큰 저장 중 오류가 발생했습니다. (Redis Data Access Error: " + e.getMessage() + ")");
                } catch (Exception e) {
                    log.error("Unexpected error during Redis token storage: {}", e.getMessage(), e);
                    return createLoginFailureResponse("Redis 토큰 저장 중 예기치 않은 오류가 발생했습니다. (Unexpected Redis Error: " + e.getMessage() + ")");
                }

                // Create access token cookie
                long accessTokenMaxAge = Long.parseLong(jwtProvider.getAccessExpiration()) / 1000 + 60;
                ResponseCookie accessTokenCookie = jwtProvider.createCookie("accessToken", accessToken, accessTokenMaxAge);
                response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

                message.put("status", "loginSuccess");
                message.put("userInfo", loginDTO.getName() + "(" + loginDTO.getId() + ")");
                message.put("userId", loginDTO.getId());
                message.put("accessToken", accessToken);
                message.put("refreshToken", refreshToken);
                message.put("errors", "");

                return ResponseEntity.ok(message);

            // 2026.02.28 KISA 보안취약점 조치
            } catch (NumberFormatException e) {
                log.error("Login process error (invalid token expiration): {}", e.getMessage(), e);
                return createLoginFailureResponse("로그인 처리 중 오류가 발생했습니다. (Invalid Token Expiration: " + e.getMessage() + ")");
            } catch (NullPointerException e) {
                log.error("Login process error (null value): {}", e.getMessage(), e);
                return createLoginFailureResponse("로그인 처리 중 오류가 발생했습니다. (Null Value Error: " + e.getMessage() + ")");
            } catch (Exception e) {
                log.error("Login process error (Unexpected error): {}", e.getMessage(), e);
                return createLoginFailureResponse("로그인 처리 중 예기치 않은 오류가 발생했습니다. (Unexpected Login Error: " + e.getMessage() + ")");
            }
        }
    }

    public Map<String, Object> loginIncorrect(LoginVO loginVO, HttpServletRequest request) {
        Map<String, Object> message = new HashMap<>();

        if (!Boolean.parseBoolean(this.lock)) {
            return message;
        }
        String clientIp = EgovClientIP.getClientIp();
        LoginPolicyVO loginPolicyVO = new LoginPolicyVO();
        loginPolicyVO.setEmployerId(loginVO.getUserId());
        loginPolicyVO = service.loginPolicy(loginPolicyVO);

        if (!ObjectUtils.isEmpty(loginPolicyVO)) {
            if ("Y".equals(loginPolicyVO.getLmttAt()) && clientIp.equals(loginPolicyVO.getIpInfo())) {
                message.put("status", "loginFailure");
                message.put("errors", messageSource.getMessage("fail.common.login.ip", null, request.getLocale()));
                return message;
            }
        }

        LoginIncorrectVO loginIncorrectVO = service.loginIncorrectList(loginVO);
        if (ObjectUtils.isEmpty(loginIncorrectVO)) {
            message.put("status", "loginFailure");
            message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
            return message;
        }

        String incorrectCode = service.loginIncorrectProcess(loginVO, loginIncorrectVO, lockCount);
        if (!"E".equals(incorrectCode)) {
            if ("L".equals(incorrectCode)) {
                message.put("status", "loginFailure");
                message.put("errors", messageSource.getMessage("fail.common.loginIncorrect",
                        new Object[]{lockCount, request.getLocale()}, request.getLocale()));
            } else if ("C".equals(incorrectCode)) {
                message.put("status", "loginFailure");
                message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
            }
        }

        return message;
    }

    @GetMapping("/validateRefreshToken")
    @ResponseBody
    public Mono<Boolean> validateRefreshToken(@RequestHeader String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = jwtProvider.decrypt(jwtProvider.extractUserId(refreshToken));
            if (ObjectUtils.isEmpty(username)) {
                jwtProvider.deleteCookie(request, response, "accessToken");
                jwtProvider.deleteCookie(request, response, "refreshToken");
                return Mono.just(false);
            }
            
            // Redis에서 저장된 토큰 정보를 가져와 비교
            AuthorizeToken authToken = authorizeTokenService.findToken(username);
            if (!ObjectUtils.isEmpty(authToken) && authToken.getRefreshToken().equals(refreshToken)) {
                return Mono.just(true);
            } else {
                // 토큰이 일치하지 않으면 Redis에서 삭제
                if (!ObjectUtils.isEmpty(authToken)) {
                    authorizeTokenService.deleteTokenById(authToken.getTokenKey());
                    authorizeTokenService.deleteToken(username);
                }
                jwtProvider.deleteCookie(request, response, "accessToken");
                jwtProvider.deleteCookie(request, response, "refreshToken");
                return Mono.just(false);
            }

        // 2026.02.28 KISA 보안취약점 조치
        } catch (JwtException e) {
            log.error("validateRefreshToken JWT error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        } catch (IllegalArgumentException e) {
            log.error("validateRefreshToken argument error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        } catch (NullPointerException e) {
            log.error("validateRefreshToken null value error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        } catch (DataAccessResourceFailureException e) {
            log.error("validateRefreshToken Redis connection error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        } catch (DataAccessException e) {
            log.error("validateRefreshToken Redis data access error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        } catch (Exception e) {
            log.error("validateRefreshToken unexpected error: {}", e.getMessage(), e);
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            return Mono.just(false);
        }
    }

    @GetMapping("/recreateAccessToken")
    @ResponseBody
    public ResponseEntity<?> recreateAccessToken(@RequestHeader String refreshToken) {
        try {
            // refreshToken 유효성 검증
            String username = jwtProvider.decrypt(jwtProvider.extractUserId(refreshToken));
            if (ObjectUtils.isEmpty(username)) {
                return ResponseEntity.badRequest().body("Invalid refresh token");
            }
            
            // Redis에서 저장된 토큰 정보를 가져와 비교
            AuthorizeToken authToken = authorizeTokenService.findToken(username);
            if (ObjectUtils.isEmpty(authToken) || !authToken.getRefreshToken().equals(refreshToken)) {
                return ResponseEntity.badRequest().body("Refresh token not found or invalid");
            }
            
            // 새로운 accessToken 생성
            Claims claims = Jwts.claims()
                    .subject("Token")
                    .add("userId", jwtProvider.extractUserId(refreshToken))
                    .add("userNm", jwtProvider.extractUserNm(refreshToken))
                    .add("uniqId", jwtProvider.extractUniqId(refreshToken))
                    .add("authId", jwtProvider.extractAuthId(refreshToken))
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + Long.parseLong(jwtProvider.getAccessExpiration())))
                    .build();

            // 2026.07.13 KISA 보안취약점 조치 - accessToken은 accessSecret으로 서명
            SecretKey key = jwtProvider.getSigningKey(jwtProvider.getAccessSecret());
            String newAccessToken = Jwts.builder().claims(claims).signWith(key).compact();
            
            // Redis에 새로운 accessToken의 해시값 업데이트
            String oldTokenKey = authToken.getTokenKey();
            String newTokenKey = jwtProvider.generateHash(newAccessToken);
            
            // 이전 tokenKey 삭제 및 새로운 tokenKey 저장
            authorizeTokenService.deleteTokenById(oldTokenKey);
            authorizeTokenService.saveTokenById(newTokenKey, username);
            authorizeTokenService.saveToken(username, newTokenKey, refreshToken, jwtProvider.getRefreshExpiration());
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        // 2026.02.28 KISA 보안취약점 조치
        } catch (JwtException e) {
            log.error("recreateAccessToken JWT error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid or expired refresh token");
        } catch (NumberFormatException e) {
            log.error("recreateAccessToken token expiration parse error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid token expiration configuration");
        } catch (IllegalArgumentException e) {
            log.error("recreateAccessToken argument error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid token format or argument");
        } catch (DataAccessResourceFailureException e) {
            log.error("recreateAccessToken Redis connection error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Token service temporarily unavailable");
        } catch (DataAccessException e) {
            log.error("recreateAccessToken Redis data access error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update token in storage");
        } catch (NullPointerException e) {
            log.error("recreateAccessToken null value error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid token or missing required data");
        } catch (Exception e) {
            log.error("recreateAccessToken unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to recreate access token");
        }
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadSecurityMetadata() {
        //EgovUserDetailsHelper.reloadSecurityMetadata(securityMetadataSource);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/actionLogout")
    public ResponseEntity<?> actionLogout(@RequestHeader(value = "refreshToken", required = false) String refreshToken, 
                                          HttpServletRequest request, HttpServletResponse response) {
        log.debug("##### EgovLoginManageAPIController logout started #####");

        Map<String, Object> message = new HashMap<>();
        
        try {
            // refreshToken이 제공된 경우 Redis에서 토큰 정보 삭제
            if (!ObjectUtils.isEmpty(refreshToken)) {
                String username = jwtProvider.decrypt(jwtProvider.extractUserId(refreshToken));
                if (!ObjectUtils.isEmpty(username)) {
                    AuthorizeToken authToken = authorizeTokenService.findToken(username);
                    if (!ObjectUtils.isEmpty(authToken)) {
                        // Redis에서 토큰 정보 삭제
                        authorizeTokenService.deleteTokenById(authToken.getTokenKey());
                        authorizeTokenService.deleteToken(username);
                        log.debug("Deleted token from Redis for user: {}", username);
                    }
                }
            }
            
            // 쿠키 삭제
            jwtProvider.deleteCookie(request, response, "accessToken");
            jwtProvider.deleteCookie(request, response, "refreshToken");
            
            message.put("status", "success");
            message.put("error", "");

        // 2026.02.28 KISA 보안취약점 조치
        } catch (JwtException e) {
            log.error("actionLogout JWT error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        } catch (IllegalArgumentException e) {
            log.error("actionLogout argument error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        } catch (NullPointerException e) {
            log.error("actionLogout null value error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        } catch (DataAccessResourceFailureException e) {
            log.error("actionLogout Redis connection error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        } catch (DataAccessException e) {
            log.error("actionLogout Redis data access error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        } catch (Exception e) {
            log.error("actionLogout unexpected error: {}", e.getMessage(), e);
            message.put("status", "error");
            message.put("error", "Logout failed");
        }

        return ResponseEntity.ok(message);
    }

    @GetMapping("/loginFailure")
    public ResponseEntity<?> loginFailure(HttpServletRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", "loginFailure");
        message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
        return ResponseEntity.ok(message);
    }

    @PostMapping("/accessDenied")
    public ResponseEntity<?> accessDenied(HttpServletRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", "loginFailure");
        message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
        return ResponseEntity.ok(message);
    }

    @PostMapping("/consurentExpired")
    public ResponseEntity<?> consurentExpired(HttpServletRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", "loginFailure");
        message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
        return ResponseEntity.ok(message);
    }

    @GetMapping("/defaultTarget")
    public String defaultTarget() {
        return "redirect:/";
    }

    @PostMapping("/csrfAccessDenied")
    public ResponseEntity<?> csrfAccessDenied(HttpServletRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", "loginFailure");
        message.put("errors", messageSource.getMessage("fail.common.login", null, request.getLocale()));
        return ResponseEntity.ok(message);
    }

}
