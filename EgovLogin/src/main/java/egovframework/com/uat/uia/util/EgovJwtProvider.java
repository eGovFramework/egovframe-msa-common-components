package egovframework.com.uat.uia.util;

import egovframework.com.uat.uia.service.LoginVO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

@Configuration
@Getter
public class EgovJwtProvider {

    @Value("${org.egovframe.crypto.algorithm}")
    private String cryptoAlgorithm;

    @Value("${token.accessSecret}")
    private String accessSecret;

    @Value("${token.refreshSecret}")
    private String refreshSecret;

    @Value("${token.accessExpiration}")
    private String accessExpiration;

    @Value("${token.refreshExpiration}")
    private String refreshExpiration;

    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    public EgovJwtProvider(@Qualifier("egovEnvCryptoService") EgovEnvCryptoServiceImpl egovEnvCryptoService) {
        this.egovEnvCryptoService = egovEnvCryptoService;
    }

    public SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(LoginVO loginVO) {
        SecretKey key = getSigningKey(accessSecret);
        Claims claims = createClaims(loginVO, accessExpiration);
        return Jwts.builder().claims(claims).signWith(key).compact();
    }

    public String createRefreshToken(LoginVO loginVO) {
        SecretKey key = getSigningKey(refreshSecret);
        Claims claims = createClaims(loginVO, refreshExpiration);
        return Jwts.builder().claims(claims).signWith(key).compact();
    }

    public Claims createClaims(LoginVO loginVO, String expiration) {
        ClaimsBuilder builder = Jwts.claims()
                .subject("Token")
                .add("userId", ObjectUtils.isEmpty(loginVO.getUserId()) ? "" : encrypt(loginVO.getUserId()))
                .add("userNm", ObjectUtils.isEmpty(loginVO.getName()) ? "" : encrypt(loginVO.getName()))
                .add("uniqId", ObjectUtils.isEmpty(loginVO.getUniqId()) ? "" : encrypt(loginVO.getUniqId()))
                .add("authLs", ObjectUtils.isEmpty(loginVO.getAuthorList()) ? "" : encrypt(loginVO.getAuthorList()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Long.parseLong(expiration)));

        return builder.build();
    }

    public int accessValidateToken(String token) {
        try {
            accessExtractClaims(token);
            return 200;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return 400;
        } catch (ExpiredJwtException e) {
            return 401;
        }
    }

    public int refreshValidateToken(String token) {
        try {
            refreshExtractClaims(token);
            return 200;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return 400;
        } catch (ExpiredJwtException e) {
            return 401;
        }
    }

    public Claims accessExtractClaims(String token) {
        SecretKey key = getSigningKey(accessSecret);
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Claims refreshExtractClaims(String token) {
        SecretKey key = getSigningKey(refreshSecret);
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extractUserId(String token) {
        return ObjectUtils.isEmpty(refreshExtractClaims(token).get("userId")) ?
                "" : refreshExtractClaims(token).get("userId").toString();
    }

    public String extractUserNm(String token) {
        return ObjectUtils.isEmpty(refreshExtractClaims(token).get("userNm")) ?
                "" : refreshExtractClaims(token).get("userNm").toString();
    }

    public String extractUniqId(String token) {
        return ObjectUtils.isEmpty(refreshExtractClaims(token).get("uniqId")) ?
                "" : refreshExtractClaims(token).get("uniqId").toString();
    }

    public String extractAuthId(String token) {
        return ObjectUtils.isEmpty(refreshExtractClaims(token).get("authId")) ?
                "" : refreshExtractClaims(token).get("authId").toString();
    }

    public String encrypt(String s) {
        return egovEnvCryptoService.encrypt(s);
    }

    public String decrypt(String s) {
        return egovEnvCryptoService.decrypt(s);
    }

    public String generateHash(String data) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(cryptoAlgorithm);
            // 2026.02.28 KISA 보안취약점 조치
            messageDigest.update(generateSalt());
            byte[] bytes = messageDigest.digest(data.getBytes(StandardCharsets.UTF_8));
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            sb.setLength(0);
        }
        return sb.toString();
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public ResponseCookie createCookie(String tokenName, String tokenValue, long tokenMaxAge) {
        return ResponseCookie.from(tokenName, tokenValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(tokenMaxAge + 10)
                .sameSite("Strict")
                .build();
    }

    public String getCookie(HttpServletRequest request, String cookieName) {
        String cookieValue = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    cookieValue = cookie.getValue();
                }
            }
        }
        return cookieValue;
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

}
