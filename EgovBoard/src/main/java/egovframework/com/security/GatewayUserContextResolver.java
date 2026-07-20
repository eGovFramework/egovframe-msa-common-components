package egovframework.com.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 서명 검증 후에만 X-USER-* 헤더에서 사용자 정보를 추출한다.
 */
@Component
@RequiredArgsConstructor
public class GatewayUserContextResolver {

    private final GatewayInternalAuthVerifier gatewayInternalAuthVerifier;
    private final EgovEnvCryptoServiceImpl egovEnvCryptoService;

    public Map<String, String> resolve(HttpServletRequest request) {
        if (!gatewayInternalAuthVerifier.verify(request)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String encryptUserId = request.getHeader("X-USER-ID");
        String encryptUserNm = request.getHeader("X-USER-NM");
        String encryptUniqId = request.getHeader("X-UNIQ-ID");

        if (ObjectUtils.isEmpty(encryptUserId) || ObjectUtils.isEmpty(encryptUniqId)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", decryptHeaderValue(encryptUserId));
        userInfo.put("userName", decryptHeaderValue(encryptUserNm));
        userInfo.put("uniqId", decryptHeaderValue(encryptUniqId));
        return userInfo;
    }

    private String decryptHeaderValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return "";
        }
        try {
            return egovEnvCryptoService.decrypt(value);
        } catch (Exception e) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
    }
}
