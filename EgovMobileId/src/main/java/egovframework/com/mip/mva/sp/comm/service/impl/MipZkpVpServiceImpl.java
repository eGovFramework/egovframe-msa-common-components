package egovframework.com.mip.mva.sp.comm.service.impl;

import com.raonsecure.omnione.core.data.iw.profile.CommonProfile;
import com.raonsecure.omnione.core.data.iw.profile.result.VCVerifyProfileResult;
import com.raonsecure.omnione.core.data.rest.ResultJson;
import com.raonsecure.omnione.core.data.rest.ResultProfile;
import egovframework.com.mip.mva.sp.comm.enums.MipErrorEnum;
import egovframework.com.mip.mva.sp.comm.enums.TrxStsCodeEnum;
import egovframework.com.mip.mva.sp.comm.exception.SpException;
import egovframework.com.mip.mva.sp.comm.service.MipZkpVpService;
import egovframework.com.mip.mva.sp.comm.service.TrxInfoService;
import egovframework.com.mip.mva.sp.comm.util.Base64Util;
import egovframework.com.mip.mva.sp.comm.util.VerifyManager;
import egovframework.com.mip.mva.sp.comm.vo.TrxInfoVO;
import egovframework.com.mip.mva.sp.comm.vo.VP;
import egovframework.com.mip.mva.sp.config.ConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Project 모바일 운전면허증 서비스 구축 사업
 * @PackageName mip.mva.sp.comm.service.impl
 * @FileName MipZkpVpServiceImpl.java
 * @Author Min Gi Ju
 * @Date 2022. 6. 3.
 * @Description VP 검증(영지식) ServiceImpl
 * 
 * <pre>
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2024. 5. 28.    민기주           최초생성
 * </pre>
 */
@Service("mipZkpVpService")
public class MipZkpVpServiceImpl implements MipZkpVpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MipZkpVpServiceImpl.class);

	/** 검증 Manager */
	private final VerifyManager verifyManager;
	/** 거래정보 Service */
	private final TrxInfoService trxInfoService;

	/**
	 * 생성자
	 * 
	 * @param verifyManager 검증 Manager
	 * @param trxInfoService 서비스 Service
	 * @param trxInfoService 거래정보 Service
	 */
	public MipZkpVpServiceImpl(VerifyManager verifyManager, TrxInfoService trxInfoService) {
		this.verifyManager = verifyManager;
		this.trxInfoService = trxInfoService;
	}

	/**
	 * Profile 요청
	 * 
	 * @MethodName getProfile
	 * @param trxInfo 거래정보
	 * @return Base64로 인코딩된 Profile
	 * @throws SpException
	 */
	@Override
	public String getProfile(TrxInfoVO trxInfo) throws SpException {
		ResultProfile resultProfile = null;

		String trxcode = trxInfo.getTrxcode();
		String svcCode = trxInfo.getSvcCode();

		try {
			resultProfile = verifyManager.profileZkp(svcCode);

			CommonProfile commonProfile = ConfigBean.gson.fromJson(Base64Util.decode(resultProfile.getProfileBase64()), CommonProfile.class);

			String nonce = commonProfile.getProfile().getProofRequest().getNonce().toString();

			TrxInfoVO trxInfoNew = new TrxInfoVO();

			trxInfoNew.setTrxcode(trxcode);
			trxInfoNew.setTrxStsCode(TrxStsCodeEnum.PROFILE_REQ.getVal());
			trxInfoNew.setZkpNonce(nonce);

			trxInfoService.modifyTrxInfo(trxInfoNew);
		} catch (SpException e) {
			e.setTrxcode(trxcode);

			throw e;
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return resultProfile.getProfileBase64();
	}

	/**
	 * VP 검증
	 * 
	 * @MethodName verifyVP
	 * @param trxInfo 거래정보
	 * @param vp VP 정보
	 * @return 검증 결과
	 * @throws SpException
	 */
	@Override
	public Boolean verifyVP(TrxInfoVO trxInfo, VP vp) throws SpException {
		Boolean result = false;

		String trxcode = trxInfo.getTrxcode();
		String profileNonce = trxInfo.getZkpNonce();

		try {
			TrxInfoVO trxInfoNew = new TrxInfoVO();

			trxInfoNew.setTrxcode(trxcode);
			trxInfoNew.setTrxStsCode(TrxStsCodeEnum.VERIFY_REQ.getVal());

			trxInfoService.modifyTrxInfo(trxInfoNew);

			// VP 검증 Start
			VCVerifyProfileResult vCVerifyProfileResult = new VCVerifyProfileResult();

			vCVerifyProfileResult.setEncryptType(vp.getEncryptType());
			vCVerifyProfileResult.setKeyType(vp.getKeyType());
			vCVerifyProfileResult.setType(vp.getType());
			vCVerifyProfileResult.setData(vp.getData());
			vCVerifyProfileResult.setAuthType(vp.getAuthType());
			vCVerifyProfileResult.setDid(vp.getDid());
			vCVerifyProfileResult.setZkpNonce(vp.getZkpNonce());

			ResultJson resultJson = verifyManager.verifyZkp(trxInfo.getSvcCode(), vCVerifyProfileResult);

			if (resultJson == null || !resultJson.isResult()) {
				return result;
			} else {
				result = true;
			}
			// VP 검증 End

			// Nonce 위변조 확인 Start
			LOGGER.debug("profileNonce : {}, vpNonce : {}", profileNonce, vp.getZkpNonce());

			if (vp.getZkpNonce().indexOf(profileNonce) == -1) {
				throw new SpException(MipErrorEnum.SP_MISMATCHING_NONCE, trxcode);
			}
			// Nonce 위변조 확인 End

			trxInfoNew.setTrxStsCode(TrxStsCodeEnum.VERIFY_COM.getVal());
			trxInfoNew.setVpVerifyResult(result ? "Y" : "N");
			trxInfoNew.setVp(ConfigBean.gson.toJson(vp));

			trxInfoService.modifyTrxInfo(trxInfoNew);
		} catch (SpException e) {
			e.setTrxcode(trxcode);

			throw e;
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getMessage());
		}

		return result;
	}

	/**
	 * VP data 조회
	 * 
	 * @MethodName getVPData
	 * @param vp VP
	 * @throws SpException
	 */
	@Override
	public String getVPData(VP vp) throws SpException {
		VCVerifyProfileResult vCVerifyProfileResult = new VCVerifyProfileResult();

		vCVerifyProfileResult.setEncryptType(vp.getEncryptType());
		vCVerifyProfileResult.setKeyType(vp.getKeyType());
		vCVerifyProfileResult.setType(vp.getType());
		vCVerifyProfileResult.setData(vp.getData());
		vCVerifyProfileResult.setAuthType(vp.getAuthType());
		vCVerifyProfileResult.setDid(vp.getDid());
		vCVerifyProfileResult.setNonce(vp.getNonce());

		String data = verifyManager.getVPData(vCVerifyProfileResult);

		return data;
	}

}
