package egovframework.com.mip.mva.sp.comm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base64Util 이 JVM 기본 charset 과 무관하게 UTF-8 로 인코딩/디코딩하는지 검증한다.
 *
 * <p>수정 전에는 {@code text.getBytes()} 와 {@code new String(byte[])} 가 기본 charset 에 의존해,
 * 서버 기본 charset 이 UTF-8 이 아니면(예: US-ASCII·EUC-KR·MS949) 한글이 깨졌다(CWE-176).
 * 이 테스트는 UTF-8 계약을 고정하며, 기본 charset 이 UTF-8 이 아닌 JVM 에서 수정 전 코드로 실행하면
 * 실패한다. (로컬에서 {@code -Dfile.encoding=US-ASCII} 로 판별 확인)</p>
 */
class Base64UtilTest {

    @Test
    void encode_한글을_UTF8_URL_Base64로_고정한다() {
        // "가"(U+AC00) = UTF-8 바이트 EA B0 80 → URL-safe Base64 "6rCA".
        // 기본 charset 이 US-ASCII 면 '가'가 '?'(0x3F)로 치환돼 다른 값이 된다.
        assertEquals("6rCA", Base64Util.encode("가"));
    }

    @Test
    void decode_UTF8_Base64를_한글로_복원한다() {
        assertEquals("가", Base64Util.decode("6rCA"));
    }

    @Test
    void encodeDecode_한글영문기호_왕복이_보존된다() {
        String original = "홍길동-Gil.Dong_2026!";
        assertEquals(original, Base64Util.decode(Base64Util.encode(original)));
    }
}
