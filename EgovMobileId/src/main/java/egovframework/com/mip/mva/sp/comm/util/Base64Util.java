package egovframework.com.mip.mva.sp.comm.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @Project 모바일 운전면허증 서비스 구축 사업
 * @PackageName mip.mva.sp.comm.util
 * @FileName Base64Util.java
 * @Author Min Gi Ju
 * @Date 2022. 6. 3.
 * @Description Base64 Util
 * 
 * <pre>
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2024. 5. 28.    민기주           최초생성
 * </pre>
 */
public class Base64Util {

	/**
	 * String to Base64 String
	 * 
	 * @MethodName encode
	 * @param text String
	 * @return Base64 String
	 */
	public static String encode(String text) {
		return Base64.getUrlEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Byte to Base64 String
	 * 
	 * @MethodName encode
	 * @param data Byte 배열
	 * @return Base64 String
	 */
	public static String encode(byte[] data) {
		return Base64.getUrlEncoder().encodeToString(data);
	}

	/**
	 * Base64 String to String
	 * 
	 * @MethodName decode
	 * @param text Base64 String
	 * @return String
	 */
	public static String decode(String text) {
		return new String(Base64.getUrlDecoder().decode(text), StandardCharsets.UTF_8);
	}

	/**
	 * Base64 String to byte
	 *
	 * @MethodName decodeToByte
	 * @param text Base64 byte
	 * @return byte[]
	 */
	public static byte[] decodeToByte(String text) {
		return Base64.getUrlDecoder().decode(text);
	}

}
