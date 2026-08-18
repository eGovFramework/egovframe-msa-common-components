package egovframework.com.mip.mva.sp.comm.util;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Project 모바일 운전면허증 서비스 구축 사업
 * @PackageName mip.mva.sp.comm.util
 * @FileName Generator.java
 * @Author Min Gi Ju
 * @Date 2022. 6. 3.
 * @Description 난수 생성 Util
 * 
 * <pre>
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2024. 5. 28.    민기주           최초생성
 * </pre>
 */
public class Generator {

	/**
	 * 거래코드 생성 - 현재시간 yyyyMMddHHmmssSSS(24시간제) + 시큐어난수 (8자리)
	 *
	 * @MethodName genTrxcode
	 * @return 거래코드
	 */
	public static String genTrxcode() {
		return genTrxcode(new Date());
	}

	/**
	 * 거래코드 생성(기준 시각 주입 - 테스트용). 시(hour)는 24시간제(HH)로 표기한다.
	 * 12시간제(hh)를 쓰면 오후 시각이 오전과 같은 시 값이 되어 거래코드의 시간 순서·해석이 어긋난다.
	 *
	 * @MethodName genTrxcode
	 * @param today 기준 시각
	 * @return 거래코드
	 */
	static String genTrxcode(Date today) {
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
		String second = secRandom(4); // 4자리 생성하고 hex code로 표현되므로 8개 자리가 나옴

		String first = formater.format(today);
		String result = first + second;

		return result;
	}

	/**
	 * Nonce 생성
	 *
	 * @MethodName genNonce
	 * @return Nonce
	 */
	public static String genNonce() {
		return genNonce(new Date());
	}

	/**
	 * Nonce 생성(기준 시각 주입 - 테스트용). 시(hour)는 24시간제(HH)로 표기한다.
	 *
	 * @MethodName genNonce
	 * @param today 기준 시각
	 * @return Nonce
	 */
	static String genNonce(Date today) {
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmssSSSS", Locale.KOREA);

		String first = formater.format(today);
		String second = secRandom(11); // 16진수 11개 자릿수 -> 스트링 -> 바이트배열 22바이트 + 18바이트 = 40바이트
		String result = first + second;

		return result;
	}

	/**
	 * 난수 생성
	 * 
	 * @MethodName SecRandom
	 * @param genNum
	 * @return 난수
	 */
	public static String secRandom(int genNum) {
		SecureRandom random = new SecureRandom();

		byte bytes[] = new byte[genNum];

		random.nextBytes(bytes);

		return bytesToHexString(bytes);
	}

	/** Base Hex Chars */
	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	/**
	 * Byte Array to Hex String
	 * 
	 * @MethodName bytesToHexString
	 * @param bytes Byte Array
	 * @return Hex String
	 */
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		char[] hexChars = new char[bytes.length * 2];

		for (int i = 0; i < bytes.length; i++) {
			int value = bytes[i] & 0xff;

			hexChars[i * 2] = HEX_CHARS[value >>> 4];
			hexChars[i * 2 + 1] = HEX_CHARS[value & 0x0f];
		}

		return new String(hexChars);
	}

}
