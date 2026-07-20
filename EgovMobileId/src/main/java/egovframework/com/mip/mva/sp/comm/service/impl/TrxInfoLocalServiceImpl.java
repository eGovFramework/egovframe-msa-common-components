package egovframework.com.mip.mva.sp.comm.service.impl;

import egovframework.com.mip.mva.sp.comm.enums.MipErrorEnum;
import egovframework.com.mip.mva.sp.comm.exception.SpException;
import egovframework.com.mip.mva.sp.comm.service.TrxInfoService;
import egovframework.com.mip.mva.sp.comm.vo.TrxInfoVO;
import egovframework.com.mip.mva.sp.comm.vo.TrxListHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * TrxInfoService의 구현체 중 DB를 사용하지 않는 버전  (DB 사용 버전은 TrxServiceImpl를 참고)
 * @author 표준프레임워크 박성완
 * @since 2025.02.13.
 * @version 4.3
 * <pre>
 *
 * ==================================================
 * DATE            AUTHOR          NOTE
 * ==================================================
 * 2025.02.13.     박성완           표준프레임워크 v4.3 - 최초생성
 * </pre>
 */
@Service("trxInfoService")
public class TrxInfoLocalServiceImpl implements TrxInfoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrxInfoLocalServiceImpl.class);
	
    private final List<TrxInfoVO> trxList;
	
	public TrxInfoLocalServiceImpl(TrxListHolder trxListHolder) {
		//멀티 쓰레드 동기화 문제 방지
	    this.trxList = Collections.synchronizedList(trxListHolder.getTrxList());
	}
	
	/**
	 * 거래정보 조회
	 * 
	 * @MethodName getTrxInfo
	 * @param trxcode 거래코드
	 * @return 거래정보
	 * @throws SpException
	 */
	@Override
	public TrxInfoVO getTrxInfo(String trxcode) throws SpException {
		LOGGER.debug("Start getTrxInfo(String trxcode) | trxcode : {}", trxcode);
		
		TrxInfoVO trxInfo = null;
		
		int index = searchIndex(trxcode);
		
		if(index >= 0) {
			trxInfo = trxList.get(index);
		} else {
			throw new SpException(MipErrorEnum.SP_TRXCODE_NOT_FOUND, trxcode);
		}

		LOGGER.debug("End getTrxInfo(String trxcode) | trxInfo : {}", trxInfo.toString());
		LOGGER.info(this.toString());
		
		return trxInfo;
	}

	/**
	 * 거래정보 등록
	 * 
	 * @MethodName registTrxInfo
	 * @param trxInfo 거래정보
	 * @throws SpException
	 */
	@Override
	public void registTrxInfo(TrxInfoVO trxInfo) throws SpException {
		LOGGER.debug("Start registTrxInfo(TrxInfoVO trxInfo) | ------ trxInfo : {}", trxInfo.toString());
		
		try {
			if(trxInfo.getVpVerifyResult() == null) {
				trxInfo.setVpVerifyResult("N");
			}
			if(trxInfo.getTrxStsCode() == null) {
				trxInfo.setTrxStsCode("0001");
			}
			if(trxInfo.getRegDt() == null) {
				trxInfo.setRegDt(getSysDate());
			}
			
			// null 체크 후 기본 값 설정 (유틸 메서드 활용)
			setDefaultIfNullOrEmpty(trxInfo.getVpVerifyResult(), trxInfo::setVpVerifyResult, "N");
			setDefaultIfNullOrEmpty(trxInfo.getTrxStsCode(), trxInfo::setTrxStsCode, "0001");
			setDefaultIfNullOrEmpty(trxInfo.getRegDt(), trxInfo::setRegDt, getSysDate());

			trxList.add(trxInfo);

			LOGGER.debug("End registTrxInfo(TrxInfoVO trxInfo) | registed trxInfo : {}", trxInfo.toString());
			LOGGER.info(this.toString());
			
		}catch (Exception e) {
			throw new SpException(MipErrorEnum.SP_TRXLIST_ERROR, trxInfo.getTrxcode(), "trxInfo insert error");
		}
	}
	
	/**
	 * 거래정보 수정
	 *
	 * @MethodName modifyTrxInfo
	 * @param trxInfo 거래정보
	 * @throws SpException
	 */
	@Override
	public void modifyTrxInfo(TrxInfoVO trxInfo) throws SpException {
		LOGGER.debug("Start modifyTrxInfo(TrxInfoVO trxInfo) | trxInfo : {}", trxInfo.toString());

		try {
			int index = searchIndex(trxInfo.getTrxcode());
			TrxInfoVO targetTrxInfo = trxList.get(index);

			LOGGER.debug("targetTrxInfo which is not yet Modified | targetTrxInfo : {}", targetTrxInfo.toString());

			String sysdate = getSysDate();

			String trxStsCode = trxInfo.getTrxStsCode();
			//trxStsCode
			targetTrxInfo.setTrxStsCode(trxStsCode);

			switch(trxStsCode) {
			case "0002" :
				//profileSendDt
				targetTrxInfo.setProfileSendDt(sysdate);
			case "0003" :
				//vpReceptDt
				targetTrxInfo.setVpReceptDt(sysdate);
			case "0004" :
				//imgSendDt
				targetTrxInfo.setImgSendDt(sysdate);
				break;
			};

	        // nonce, zkpNonce, vp, errorCn : null과 empty 체크 후 설정
			setIfNotNullOrNotEmpty(trxInfo.getNonce(), targetTrxInfo::setNonce);
			setIfNotNullOrNotEmpty(trxInfo.getZkpNonce(), targetTrxInfo::setZkpNonce);
			setIfNotNullOrNotEmpty(trxInfo.getVp(), targetTrxInfo::setVp);
			setIfNotNullOrNotEmpty(trxInfo.getErrorCn(), targetTrxInfo::setErrorCn);

			//vpVerifyResult
			String vpVerifyResult = trxInfo.getVpVerifyResult();
	        targetTrxInfo.setVpVerifyResult(
	        		( vpVerifyResult == null || vpVerifyResult.isEmpty() )
	        			? "N"
	        			: vpVerifyResult
	        );

			//udtDt
			targetTrxInfo.setUdtDt(sysdate);

			//update 실행
			//trxList.set(index, targetTrxInfo);

			LOGGER.debug("End modifyTrxInfo(TrxInfoVO trxInfo)");
			LOGGER.debug("targetTrxInfo which is Modified | ------- targetTrxInfo : {}", targetTrxInfo.toString());
			LOGGER.info(this.toString());
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.SP_TRXLIST_ERROR, trxInfo.getTrxcode(), "trxInfo update error");
		}
	}
	
	/**
	 * 거래정보 삭제
	 * 
	 * @MethodName removeTrxInfo
	 * @param trxcode 거래코드
	 * @throws SpException
	 */
	@Override
	public void removeTrxInfo(String trxcode) throws SpException {
		LOGGER.debug("Start removeTrxInfo(String trxcode) | trxcode : {}", trxcode);
		
		try {
			int index = searchIndex(trxcode);
			LOGGER.debug("index to remove : {} | trxInfo to remove : {}", index, trxList.get(index).toString());
			trxList.remove(index);
			LOGGER.info(this.toString());
		} catch (Exception e) {
			throw new SpException(MipErrorEnum.SP_TRXLIST_ERROR, trxcode, "trxInfo delete error");
		}
	}
	/**
	 * 유틸 메서드 : trxList에서 trxcod로 index 조회
	 * 
	 * @MethodName searchIndex
	 * @param trxcode 거래코드
	 * @return index trxList의 index
	 * @throws
	 */
	private int searchIndex(String trxcode) {
		int index = -1;
		for (int i = 0; i < trxList.size(); i++) {
		    if (trxcode.equals(trxList.get(i).getTrxcode())) {
		    	index = i;
		        break;
		    }
		}
		if (index == -1) {
			LOGGER.error("trxcode와 일치하는 trxList의 항목을 찾지 못했습니다.");
		} else {
			LOGGER.debug("trxcode와 일치하는 trxList의 항목을 찾았습니다.");
		}
		return index;
	}
	
	/**
	 * 유틸 메서드 : param1의 값이 null이거나 empty일 경우 param3을 set
	 * 
	 * @MethodName setDefaultIfNullOrEmpty
	 * @param value 값을 확인할 변수
	 * @param setter 실행할 setter 메서드
	 * @param defaultValue setter 메서드가 set할 값
	 * @throws
	 */
	private void setDefaultIfNullOrEmpty(String value, 
            						  	 java.util.function.Consumer<String> setter, 
            						  	 String defaultValue) {
		if (value == null || value.isEmpty()) {
			setter.accept(defaultValue);
		}
	}
	
	/**
	 * 유틸 메서드: 출력 형식에 맞춘 sysdate
	 * 
	 * @MethodName getSysDate
	 * @param 
	 * @return 현재 시각
	 * @throws
	 */
	private String getSysDate() {
		// 출력 형식 예: 2025-02-06 13:34:58.938
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        // 현재 시각
        LocalDateTime now = LocalDateTime.now();
        // 현재 시각을 출력 형식에 맞춤
        String formattedTime = now.format(formatter);
        
        return formattedTime;
	}
	
	/**
	 * 유틸 메서드: param1의 값이 null이 아니거나 empty가 아닐 경우 set
	 * 
	 * @MethodName setIfNotNullOrNotEmpty
	 * @param value 값을 확인할 변수
	 * @param setter 실행할 setter 메서드
	 * @param defaultValue setter 메서드가 set할 값
	 * @throws
	 */
	private void setIfNotNullOrNotEmpty(String value,
										java.util.function.Consumer<String> setter) {
	    if (value != null && !value.isEmpty()) {
	        setter.accept(value);
	    }
	}
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("trxInfo 목록 - List<TrxInfoVO> trxList").append(System.lineSeparator());
        for (int i = 0; i < trxList.size(); i++) {
            sb.append("[trxList.get(").append(""+i).append(")] ")
              .append(trxList.get(i).toString())
              .append(System.lineSeparator());
        }
       
        return sb.toString();
    }
	
}
