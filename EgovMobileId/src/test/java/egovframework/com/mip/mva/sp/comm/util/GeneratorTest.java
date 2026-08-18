package egovframework.com.mip.mva.sp.comm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Generator의 거래코드/Nonce 타임스탬프가 24시간제(HH)로 기록되는지 검증한다.
 *
 * <p>12시간제(hh)를 쓰면 오후 시각의 시(hour)가 오전과 같아진다(예: 14시 → "02"). 그 결과
 * 거래코드의 시간 해석이 틀리고, 타임스탬프 접두 문자열로 정렬하면 시간 순서가 뒤집힌다
 * (13시 → "01" 이 09시 → "09" 보다 앞선다).</p>
 */
class GeneratorTest {

	/** 기준 시각 생성. 시(hour)는 24시간제로 지정한다. */
	private static Date at(int hourOfDay, int minute, int second) {
		Calendar cal = Calendar.getInstance(Locale.KOREA);
		cal.clear();
		cal.set(2024, Calendar.JUNE, 3, hourOfDay, minute, second);
		cal.set(Calendar.MILLISECOND, 123);
		return cal.getTime();
	}

	@Test
	@DisplayName("거래코드: 오후 14시는 24시간제 '14'로 기록된다(12시간제면 '02')")
	void genTrxcode_afternoonUses24Hour() {
		// 거래코드 = yyyyMMddHHmmssSSS(17자리) + 난수 8자리. 시(hour) 자리는 index 8~9.
		String code = Generator.genTrxcode(at(14, 23, 45));
		assertEquals("14", code.substring(8, 10),
				"오후 2시가 거래코드에 '14'로 기록되어야 한다. '02'이면 12시간제(hh) 버그.");
		assertEquals(25, code.length(), "거래코드 길이 = 타임스탬프 17 + 난수 8");
	}

	@Test
	@DisplayName("거래코드: 타임스탬프 문자열 정렬이 시간 순서와 일치한다(09시 < 13시)")
	void genTrxcode_lexicographicOrderMatchesChronology() {
		String morningTs = Generator.genTrxcode(at(9, 0, 0)).substring(0, 17);
		String afternoonTs = Generator.genTrxcode(at(13, 0, 0)).substring(0, 17);
		assertTrue(afternoonTs.compareTo(morningTs) > 0,
				"13시 타임스탬프가 09시보다 사전순으로 커야 한다. 12시간제면 13시 '01' < 09시 '09'로 역전된다. "
						+ "morning=" + morningTs + " afternoon=" + afternoonTs);
	}

	@Test
	@DisplayName("Nonce: 오후 14시는 24시간제 '14'로 기록된다")
	void genNonce_afternoonUses24Hour() {
		// Nonce = yyyyMMddHHmmssSSSS(18자리) + 난수. 시(hour) 자리는 index 8~9.
		String nonce = Generator.genNonce(at(14, 5, 9));
		assertEquals("14", nonce.substring(8, 10),
				"오후 2시가 Nonce에 '14'로 기록되어야 한다.");
	}
}
