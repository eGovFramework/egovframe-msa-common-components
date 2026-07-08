package egovframework.com.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class GatewayInternalAuthVerifier {

    private static final long MAX_SKEW_MS = 60_000L;

    @Value("${gateway.internalSecret:${token.accessSecret:}}")
    private String internalSecret;

    public boolean verify(HttpServletRequest request) {
        if (ObjectUtils.isEmpty(internalSecret)) {
            log.warn("Gateway internal secret is not configured");
            return false;
        }

        String timestamp = request.getHeader(GatewayInternalAuthSupport.HEADER_TIMESTAMP);
        String signature = request.getHeader(GatewayInternalAuthSupport.HEADER_SIGNATURE);

        if (ObjectUtils.isEmpty(timestamp) || ObjectUtils.isEmpty(signature)) {
            return false;
        }

        try {
            long requestTime = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() - requestTime) > MAX_SKEW_MS) {
                return false;
            }

            String payload = GatewayInternalAuthSupport.buildPayload(
                    timestamp,
                    request.getHeader("X-USER-ID"),
                    request.getHeader("X-UNIQ-ID"),
                    request.getRequestURI());

            return GatewayInternalAuthSupport.verifySignature(internalSecret, payload, signature);
        } catch (Exception e) {
            log.debug("Gateway signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
