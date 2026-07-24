package egovframework.com.uat.uia.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class AuthorizeTokenService {

    // 정확성/동시성: 기존에는 빈 생성 시점에 고정된 Date 필드와 스레드 비안전한
    // SimpleDateFormat 인스턴스 필드를 @Service(싱글턴)에서 공유했다.
    // - regDate 가 앱 기동 시각으로 고정되어 실제 발급 시각을 반영하지 못했고,
    // - 동시 요청 시 SimpleDateFormat 공유로 출력 손상/예외가 발생할 수 있었다.
    // 불변·스레드 안전한 DateTimeFormatter 로 대체하고 시각은 호출 시점에 계산한다.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RedisTemplate<String, AuthorizeToken> authRedisTemplate;
    private final RedisTemplate<String, String> tokenRedisTemplate;

    public AuthorizeTokenService(RedisTemplate<String, AuthorizeToken> authRedisTemplate, RedisTemplate<String, String> tokenRedisTemplate) {
        this.authRedisTemplate = authRedisTemplate;
        this.tokenRedisTemplate = tokenRedisTemplate;
    }

    @Cacheable(value="authRedis", key="#username")
    public AuthorizeToken findToken(String username) {
        return authRedisTemplate.opsForValue().get(username);
    }

    @CachePut(value="authRedis", key="#username")
    public void saveToken(String username, String tokenKey, String refreshToken, String expDate) {
        long now = System.currentTimeMillis();
        String regDate = LocalDateTime.now().format(DATE_FORMAT);
        String expDateStr = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(now + Long.parseLong(expDate)), ZoneId.systemDefault()).format(DATE_FORMAT);
        AuthorizeToken authorizeToken = new AuthorizeToken(
                username, tokenKey, refreshToken, regDate, expDateStr);
        authRedisTemplate.opsForValue().set(username, authorizeToken);
    }

    @CacheEvict(value="authRedis", key="#username")
    public void deleteToken(String username) {
        authRedisTemplate.delete(username);
    }

    @Cacheable(value="tokenRedis", key="#tokenKey")
    public String findTokenById(String tokenKey) {
        return tokenRedisTemplate.opsForValue().get(tokenKey);
    }

    @CachePut(value="tokenRedis", key="#tokenKey")
    public void saveTokenById(String tokenKey, String username) {
        tokenRedisTemplate.opsForValue().set(tokenKey, username);
    }

    @CacheEvict(value="tokenRedis", key="#tokenKey")
    public void deleteTokenById(String tokenKey) {
        tokenRedisTemplate.delete(tokenKey);
    }

}
