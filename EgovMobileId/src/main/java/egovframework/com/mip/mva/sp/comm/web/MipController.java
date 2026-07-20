package egovframework.com.mip.mva.sp.comm.web;

import com.google.gson.JsonSyntaxException;
import com.raonsecure.omnione.core.data.iw.Unprotected;
import egovframework.com.mip.mva.sp.comm.enums.MipErrorEnum;
import egovframework.com.mip.mva.sp.comm.enums.TrxStsCodeEnum;
import egovframework.com.mip.mva.sp.comm.exception.SpException;
import egovframework.com.mip.mva.sp.comm.service.DirectService;
import egovframework.com.mip.mva.sp.comm.service.MipDidVpService;
import egovframework.com.mip.mva.sp.comm.service.TrxInfoService;
import egovframework.com.mip.mva.sp.comm.util.Base64Util;
import egovframework.com.mip.mva.sp.comm.vo.*;
import egovframework.com.mip.mva.sp.config.ConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project 모바일 운전면허증 서비스 구축 사업
 * @PackageName mip.mva.sp.comm.web
 * @FileName MipController.java
 * @Author Min Gi Ju
 * @Date 2022. 6. 3.
 * @Description MIP 검증 Controller
 * 
 *              <pre>
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2024. 5. 28.    민기주           최초생성
 * 2025.02.13.     박성완           표준프레임워크 v4.3 - getPrivacy 메서드 수정
 *              </pre>
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/mip")
public class MipController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MipController.class);

	/** Direct Service */
	private final DirectService directService;
	/** 거래정보 Service */
	private final TrxInfoService trxInfoService;
	/** VP 검증 Service */
	private final MipDidVpService mipDidVpService;

	/**
	 * 생성자
	 * 
	 * @param directService  Direct Service
	 * @param trxInfoService 거래정보 Service
	 */
	public MipController(DirectService directService, TrxInfoService trxInfoService, MipDidVpService mipDidVpService) {
		this.directService = directService;
		this.trxInfoService = trxInfoService;
		this.mipDidVpService = mipDidVpService;
	}

	/**
	 * Profile 요청
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 M310 메시지"}
	 * @return {"result": true, "data": "Base64로 인코딩된 M310 메시지"}
	 * @throws SpException
	 */
	@PostMapping("/profile")
	public MipApiDataVO getProfile(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("Profile 요청!");

		M310VO m310 = null;

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			m310 = ConfigBean.gson.fromJson(data, M310VO.class);

			m310 = directService.getProfile(m310);

			mipApiData.setResult(true);
			mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(m310)));
		} catch (JsonSyntaxException e) {
			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m310");
		} catch (SpException e) {
			String trxcode = ObjectUtils.isEmpty(m310) ? null : m310.getTrxcode();

			e.setTrxcode(trxcode);

			throw e;
		} catch (Exception e) {
			String trxcode = ObjectUtils.isEmpty(m310) ? null : m310.getTrxcode();

			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * BI 이미지 요청
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 M320 메시지"}
	 * @return {"result": true, "data": "Base64로 인코딩된 Image"}
	 * @throws SpException
	 */
	@PostMapping("/image")
	public MipApiDataVO getImage(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("BI Image 요청!");

		M320VO m320 = null;

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			m320 = ConfigBean.gson.fromJson(data, M320VO.class);

			String image = directService.getImage(m320);

			mipApiData.setResult(true);
			mipApiData.setData(image);
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			String trxcode = ObjectUtils.isEmpty(m320) ? null : m320.getTrxcode();

			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "m320");
		} catch (Exception e) {
			String trxcode = ObjectUtils.isEmpty(m320) ? null : m320.getTrxcode();

			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * VP 검증
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 M400 메시지"}
	 * @return {"result": true}
	 * @throws SpException
	 */
	@PostMapping("/vp")
	public MipApiDataVO verifyVP(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("VP 검증!");

		M400VO m400 = null;

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			m400 = ConfigBean.gson.fromJson(data, M400VO.class);

			Boolean result = directService.verifyVP(m400);

			mipApiData.setResult(result);
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			String trxcode = ObjectUtils.isEmpty(m400) ? null : m400.getTrxcode();

			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "m400");
		} catch (Exception e) {
			String trxcode = ObjectUtils.isEmpty(m400) ? null : m400.getTrxcode();

			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * 오류 전송
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 오류 메시지"}
	 * @return {"result": true}
	 * @throws SpException
	 */
	@PostMapping("/error")
	public MipApiDataVO error(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("오류 전송!");

		M900VO m900 = null;

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			m900 = ConfigBean.gson.fromJson(data, M900VO.class);

			directService.sendError(m900);

			mipApiData.setResult(true);
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			String trxcode = ObjectUtils.isEmpty(m900) ? null : m900.getTrxcode();

			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "m900");
		} catch (Exception e) {
			String trxcode = ObjectUtils.isEmpty(m900) ? null : m900.getTrxcode();

			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * 거래상태 조회
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 TrxInfoVO"}
	 * @return {"result": true, "data": "Base64로 인코딩된 TrxInfoVO"}
	 * @throws SpException
	 */
	@PostMapping(value = "/trxsts")
	public MipApiDataVO getTrxsts(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("거래상태 조회!");

		TrxInfoVO trxInfo = null;

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			trxInfo = ConfigBean.gson.fromJson(data, TrxInfoVO.class);

			trxInfo = trxInfoService.getTrxInfo(trxInfo.getTrxcode());

			mipApiData.setResult(true);
			mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(trxInfo)));
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			String trxcode = ObjectUtils.isEmpty(trxInfo) ? null : trxInfo.getTrxcode();

			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "trxInfo");
		} catch (Exception e) {
			String trxcode = ObjectUtils.isEmpty(trxInfo) ? null : trxInfo.getTrxcode();

			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * VP 재검증 - 부인방지
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 서비스코드 & VP"}
	 * @return {"result": true}
	 * @throws SpException
	 */
	@PostMapping(value = "/revp")
	public MipApiDataVO reVerifyVP(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("VP 재검증!");

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			Map<String, String> dataMap = ConfigBean.gson.fromJson(data, HashMap.class);

			VP vp = ConfigBean.gson.fromJson(dataMap.get("vp"), VP.class);

			Boolean result = directService.reVerifyVP(vp);

			mipApiData.setResult(result);
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "vp");
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, null, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * VP data 조회
	 * 
	 * @param mipApiData {"data": "base64로 인코딩된 VP"}
	 * @return {"result": true, "data": "base64로 인코딩된 VP data"}
	 * @throws SpException
	 */
	@PostMapping(value = "/vpdata")
	public MipApiDataVO getVPData(@RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("VP data 조회!");

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			Map<String, Object> dataMap = ConfigBean.gson.fromJson(data, HashMap.class);

			VP vp = ConfigBean.gson.fromJson((String) dataMap.get("vp"), VP.class);

			String vpData = directService.getVPData(vp);

			mipApiData.setResult(true);
			mipApiData.setData(Base64Util.encode(vpData));
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "vp");
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, null, e.getMessage());
		}

		return mipApiData;
	}

	/**
	 * Privacy 조회
	 * 
	 * @param mipApiData {"data": "base64로 인코딩된 TrxInfo"}
	 * @return {"result": true, "data": "base64로 인코딩된 HashMap 객체"}
	 * @throws SpException
	 * 
	 * getPrivacy 메서드 수정(2025.02.13 박성완)
	 *   - 메서드 설명 중 param : "VP"로 잘못 기재된 것을 "TrxInfo"로 수정
	 *   - 메서드 설명 중 return : "VP data"에서 "HashMap"으로 수정
	 *   - URL : PathVariable로 "/{license}" 추가
	 *   - return값 : LicenseVO와 license명이 담긴 HashMap으로 수정
	 */
	@PostMapping(value = "/privacy/{license}")
	public MipApiDataVO getPrivacy(@PathVariable("license") String license, @RequestBody MipApiDataVO mipApiData) throws SpException {
		LOGGER.debug("Privacy 조회!");

		try {
			String data = Base64Util.decode(mipApiData.getData());

			LOGGER.debug("data : {}", data);

			TrxInfoVO trxInfo = ConfigBean.gson.fromJson(data, TrxInfoVO.class);
			if (trxInfo == null || ObjectUtils.isEmpty(trxInfo.getTrxcode())) {
				throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "privacy");
			}

			TrxInfoVO storedTrxInfo = trxInfoService.getTrxInfo(trxInfo.getTrxcode());
			if (!TrxStsCodeEnum.VERIFY_COM.getVal().equals(storedTrxInfo.getTrxStsCode())) {
				throw new SpException(MipErrorEnum.SP_TRXINFO_NOT_FOUND, trxInfo.getTrxcode(), "VP verification not completed");
			}
			if (ObjectUtils.isEmpty(storedTrxInfo.getVp())) {
				throw new SpException(MipErrorEnum.SP_TRXINFO_NOT_FOUND, trxInfo.getTrxcode(), "VP not found");
			}

			List<Unprotected> privacy = mipDidVpService.getPrivacy(storedTrxInfo.getTrxcode());
			//수정 전 원본(2025.02.13 박성완)
			//mipApiData.setResult(true);
			//mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(privacy)));
			
			//return값 : LicenseVO와 license명이 담긴 HashMap으로 수정(2025.02.13 박성완)
			LOGGER.debug("trxInfo : {}", trxInfo);
			Map<String, Object> map = mipDidVpService.mapPrivacy(license, privacy);
			mipApiData.setResult(true);
			mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(map)));
		} catch (SpException e) {
			throw e;
		} catch (JsonSyntaxException e) {
			throw new SpException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "privacy");
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, null, e.getMessage());
		}

		return mipApiData;
	}

}
