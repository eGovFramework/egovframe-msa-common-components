package egovframework.com.security;

import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class GatewayInternalAuthSupport {

    public static final String HEADER_TIMESTAMP = "X-GATEWAY-TIMESTAMP";
    public static final String HEADER_SIGNATURE = "X-GATEWAY-SIGNATURE";

    private static final String[] TRUST_HEADERS = {
            "X-CODE-ID", "X-USER-ID", "X-USER-NM", "X-UNIQ-ID",
            HEADER_TIMESTAMP, HEADER_SIGNATURE
    };

    private GatewayInternalAuthSupport() {
    }

    public static void stripTrustHeaders(HttpHeaders headers) {
        for (String header : TRUST_HEADERS) {
            headers.remove(header);
        }
    }

    public static String buildPayload(String timestamp, String userId, String uniqId, String path) {
        return nullToEmpty(timestamp) + "|" + nullToEmpty(userId) + "|" + nullToEmpty(uniqId) + "|" + nullToEmpty(path);
    }

    public static String sign(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    public static boolean verifySignature(String secret, String payload, String signature) throws Exception {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        String expected = sign(secret, payload);
        return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
