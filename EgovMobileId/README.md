# 모바일 신분증 (EgovMobileId)

## 서비스 상태 (현재 모듈 기준)

| 항목 | 내용 |
|------|------|
| **역할** | 모바일 신분증 **SP(Verifier)** 서버 — QR-MPM·Direct-Mode, VP 검증 |
| **애플리케이션명** | `EgovMobileId` → Eureka `EGOVMOBILEID` |
| **포트** | **`9991` 고정** (`verifyConfig.json` 의 `sp.serverDomain` 과 일치 필요) |
| **Config Server** | 본 모듈 `application.yml` 에는 미연결. 설정은 주로 **로컬 YAML + `verifyConfig.json`·DID·Wallet** |
| **Eureka** | 등록 (웹 UI는 Gateway `/mip/**` 경유 가능) |
| **특이사항** | 모바일 앱 → SP는 **Gateway를 거치지 않고 9991로 직접** 접속. `/mip/profile`, `/mip/vp` 등은 JWT 대신 SP측 필터 정책 적용 |

---

## 개요

표준프레임워크는 모바일 신분증 검증 모듈을 표준프레임워크를 적용한 개발에 활용할 수 있도록 예제를 제공한다.

모바일 신분증이란 자신의 스마트폰에 안전하게 저장된 신분증으로서, 이용자는 신분 증명이 필요한 모든 곳에서 모바일 신분증을 편리하게 사용할 수 있다.

표준프레임워크가 지원하는 모바일 신분증은 총 3종으로서, 모두 검증 모듈 v3.0이 지원하는 신분증들이다. 3종의 신분증은 아래와 같다.
- 모바일 운전면허증
- 모바일 국가보훈등록증
- 모바일 재외국민신원확인증

표준프레임워크의 모바일 신분증 컴포넌트 활용시 장점은 3가지가 있다.
1. 자기 정보 결정권 강화
	- 자신의 신원정보를 자신의 스마트폰 안에 보유 함으로써, 신원 증명시 필요한 정보만 골라서 제공할 수 있다.
2. 블록체인 기반 분산 ID
	- 블록체인을 통해 자신이 제공하는 신원정보의 진위여부가 확인된다.
3. 표준프레임워크 컴포넌트 상호간 호환
	- KRDS가 적용된 MSA 기반의 표준프레임워크 v4.3 공통컴포넌트 26종의 일부로서, 컴포넌트들 간 상호 호환성을 지원한다.

## 연계서비스 이용 절차

연계서비스 이용 절차는 모바일 신분증 개발지원센터를 통해 진행한다. 구체적인 이용 절차는 아래 링크를 통해 확인한다.
- 모바일 신분증 개발지원센터 : https://dev.mobileid.go.kr/mip/dfs/dfsmain.do
- 모바일 신분증 연계서비스 이용 절차 : https://dev.mobileid.go.kr/mip/dfs/apiuse/apiusestep.do

연계서비스 이용 절차는 다음과 같다.
1. 개발 사전 절차
	1. 연계서비스 신청 접수
	2. 이용 승인
	3. 개발용 Wallet & DID 등록
	4. 테스트 신분증 발급 및 테스트 App 설치
2. 개발
	1. SP 서버 개발 및 테스트
3. 개발 사후 절차
	1. 시나리오 영상 제출 및 운영 승인
	2. 운영용 Wallet & DID 등록
	3. 운영 개시

## 환경 정보 및 설정

### 1. 환경 정보

| 프로그램 명 | 버전 명   |
| :----- | :----- |
| Java   | 17 (본 저장소 `pom.xml` 기준). 검증모듈 요구사항은 별도 확인 |
| Spring Boot | 3.5.6 |
| Spring Cloud | 2025.0.0 |
| 검증모듈(Spring Boot) | 3.0 |

검증모듈은 다음 링크에서 다운받을 수 있다

https://dev.mobileid.go.kr/mip/dfs/downapi/useguidedown.do

### 2. 환경 설정

#### 1) Java

- Java 8 이상 사용 (Java 8 사용 권장)

**SSL 오류 대응**

- OpenJDK 사용 시 Maven 통신 시 SSL 관련 오류가 발생할 수 있음. 이 경우 Oracle JDK의 cacerts(/jre/lib/security/cacerts)파일을 OpenJDK에 복사하여 해결.

#### 2) 방화벽

SP 서버(EgovMobileId)가 사용하는 Port로 연결되는 모든 외부 IP는 열려있어야 한다.

QR-MPM / Direct-Mode는 사용자 핸드폰의 모바일 신분증 앱(Client)에서 SP 서버로 바로 요청을 하므로, Client의 IP주소와 Port를 미리 알 수 없기 때문이다.

따라서 아래와 같은 방화벽 설정이 있어야 한다.
 - Windows의 경우 다음과 같은 인바운드 방화벽을 오픈해야 한다.
	- 프로토콜 종류 : TCP
	- 로컬 포트 : 9991 (또는 귀사가 수정한 SP서버 - EgovMobileId - 의 Port)
	- 원격 포트 : 모든 포트
 - macOS의 경우 "시스템 설정 → 네트워크 → 방화벽" 설정이 활성화되어 있다면,
 	- "옵션"을 클릭하여 하단에 "다운로드한 소프트웨어가 들어오는 연결을 수신하는 것을 자동으로 허용"이 활성화되어 있어야 한다.

자세한 내용은 https://dev.mobileid.go.kr/mip/dfs/useguide/mdGuide.do?guide=spdevguide 에 들어가서 "1.3 방화벽 설정"의 내용을 확인한다.

#### 3) 포트포워드

내부망에서 테스트시 블록체인 및 모바일 신분증 앱과의 연동을 위해 포트포워드 설정이 필요하다.

|    구분    |      내부 IP      |     외부 IP      |              포트              |
| :------: | :-------------: | :------------: | :--------------------------: |
|  환경 정보   | 192.168.XXX.123 | 67.890.123.456 |   EgovMobileId의 포트 : 9991    |
| 포트포워드 설정 | 192.168.XXX.123 | 67.890.123.456 | 내부 포트 : 9991<br>외부 포트 : 9991 |

만약 내부망에 서버를 띄우고, 핸드폰이 내부망과 연결된 Wifi를 사용중이라면, 포트포워드를 하지 않아도 오류가 발생하지 않는다. 통신이 내부망을 벗어나지 않기 떄문이다. 따라서 정확한 테스트를 원한다면, 핸드폰의 wifi를 사용하지 않는 상태로 테스트 해야 한다.

EgovMobileId 서비스의 포트는 ./src/main/resources/application.yml 파일에 설정되어 있다.
```yaml
server:
  port: 9991 # verifyConfig.json에서 sp.serverDomain에 설정한 Port와 동일하게 설정할 것
```

**QR 촬영시 핸드폰 앱과 SP 서버간 통신 내역조차 없는 오류 대응**

다수의 포트포워드 규칙이 존재함으로 인해 하나의 포트가 다수의 내부 IP와 연결되어 있다면 오류가 발생한다. 따라서 테스트를 진행하는 하나의 내부 IP 주소를 제외한 다른 IP 주소의 포트포워드 규칙은 비활성화 해야 한다.

하지만 당연하게도, 포트포워드 규칙에 적용된 포트가 겹치지 않는다면 문제 없다.

#### 4) DID와 / Wallet / verifyConfig.json

DID와 Wallet 파일은 아래 절차를 통해 이미 생성했을 것이다.
- 연계서비스 이용 절차 - 개발 사전 절차 - 개발용 Wallet & DID 등록
	- DID와 Wallet 생성 가이드 : https://dev.mobileid.go.kr/mip/dfs/useguide/mdGuide.do?guide=didcreateguide

verifyConfig.json 파일은 아래 링크를 통해 생성한다.
- http://mvadev.mobileid.go.kr:8080/static/html/comm/verifyConfig.html 또는 http://mvadev.mobileid.go.kr:8180/static/html/comm/verifyConfig.html
- 위 링크가 수시로 변한다. 최신 링크는 모바일 신분증 개발지원센터의 "SP 서버 테스트 가이드"를 확인한다.
	- https://dev.mobileid.go.kr/mip/dfs/useguide/mdGuide.do?guide=sptestguide

위 세 파일 모두 프로젝트 루트 디렉터리에 추가한다.

각 파일의 경로 설정은 다음과 같다
- ./src/main/resources/application.yml 파일에는 verifyConfig.json 파일의 경로가 설정되어 있다.
```yaml
app:
  verify-file-path: ./EgovMobileId/verifyConfig.json # MobileId - configBean이 verify-file-path를 사용함.
```
- ./verifyConfig.json 파일에는 DID와, Wallet 파일의 경로가 설정되어 있다.
	- Wallet 파일 경로 :  keymanagerPath
	- DID와 파일 경로 :  didFilePath
```json
    "didWalletFile": {
        "keymanagerPath": "./example_op.wallet",
        "keymanagerPassword": "raon1234",
        "signKeyId": "example.sp",
        "encryptKeyId": "example.sp.rsa",
        "didFilePath": "./example_op.did"
```

**오류 대응 : configBean Bean 생성 실패 / verifyManager Bean 생성 실패**

테스트시 위와 같이 경로 설정으로 상대경로를 활용하면, 사용하는 IDE에 따라 파일을 못 읽는 오류가 발생한다. 이 경우 다음과 같은 로그가 나타날 수 있다.
- configBean Bean 생성 실패 오류 로그
```
Error creating bean with name 'app2AppService' (중략) Unsatisfied dependency expressed through constructor parameter 0 (중략)
Error creating bean with name 'directService' (중략) Unsatisfied dependency expressed through constructor parameter 0 (중략)
Error creating bean with name 'configBean' (중략) Invocation of init method failed (중략)
```
- verifyManager Bean 생성 실패 오류 로그
```
Error creating bean with name 'app2AppService' (중략) Unsatisfied dependency expressed through constructor parameter 0 (중략)
Error creating bean with name 'directService' (중략) Unsatisfied dependency expressed through constructor parameter 2 (중략)
Error creating bean with name 'configBean' (중략) Invocation of init method failed (중략)
Error creating bean with name 'mipDidVpService' (중략) Unsatisfied dependency expressed through constructor parameter 0 (중략)
Error creating bean with name 'verifyManager' (중략) Invocation of init method failed (중략)
```
각 오류별 주요 원인은 다음과 같다.
- configBean Bean 생성 실패 오류 : configBean Bean 생성시 application.yml에 설정된 verifyConfig.json 파일의 경로를 못 찾음
- verifyManager Bean 생성 실패 오류 로그 : verifyManager Bean 생성시 verifyConfig.json에 설정된 DID, Wallet 파일의 경로를 못 찾음
파일 경로를 못 찾는 이유는 각 IDE 별로 MSA의 경우 프로젝트를 관리하는 기본 체계가 다르기 때문이다. 아래는 Eclipse와 IntelliJ에 적합한 상대경로 설정이다.
- Eclipse
	- verifyConfig.json 파일 경로 설정 : `./verifyConfig.json`
	- DID 파일 경로 설정 : `./{DID 파일명}.did`
	- Wallet 파일 경로 설정 : `./{Wallet 파일명}.did`
- IntelliJ
	- verifyConfig.json 파일 경로 설정 : `./EgovMobileId/verifyConfig.json`
	- DID 파일 경로 설정 : `./EgovMobileId/{DID 파일명}.did`
	- Wallet 파일 경로 설정 : `./EgovMobileId/{Wallet 파일명}.did`

하지만 **절대경로를 활용할 것을 권장**한다.

## 용어 정리

- DID
	- 사용자의 유일한 식별 값
- VC(Verifiable Claims)
	- 증명 가능한 정보를 뜻하며, 사용자 개인정보가 담김 증명서
	- 신분증의 주체를 식별할 수 있는 정보(이름, 소속부처, 직급 정보 등)
- VP(Verifiable Presentation)
	- 여러개의 VC를 모아놓은 것
- 블록체인 노드
	- DID, VC검증 정보가 공유되고 있는 블록체인 노드(서버)
- 프로파일(Profile)
	- 검증에 필요한 정보를 담고 있는 문서(JSON) 데이터
	- 해당 정보는 제출 받고자 하는 \[VC Type, 허용되는 Issuer, 필수 제출 개인정보, 암호화 처리 정보, CallbackURL] 을 담고 있음
- Wallet
	- 개인키를 담고 있는 파일 형태의 암호화 지갑
	- DID의 개인키를 보관하고 있어 SDK 연동시 반드시 필요
- CLI
	- Command Line Interface로 명령창에서 실행 가능한 툴
	- Wallet 생성과 DID 생성 목적으로 사용
- SP(또는 Verifier)
	- Service Provider의 약자로 DID, VC를 검증하려는 서버
	- SP 서버 또는 Verifier라고도 함
- Service_Code
	- 연계될 서비스에 대한 코드
	- 포함 정보 : VC제출 목록, 정책정보 등

## 아키텍처 개요

표준프레임워크가 적용한 모바일 신분증 아키텍쳐는 다음과 같다.

### VP(신원정보) 검증 방식 : 라이브러리 방식

![image-01-egovmobileid-architecture-library](https://github.com/user-attachments/assets/751763bc-0724-4682-bbb7-2c2489496044)

VP 검증 방식에 대한 아키텍처는 **블록체인과 통신하고 VP를 검증하는 주체가 누구인가**에 관한 것이다.

표준프레임워크는 라이브러리 방식을 채택했다.

라이브러리 방식의 아키텍처에 대한 구체적인 설명은 아래와 같다.
- 라이브러리 방식은 **SP 서버가** 블록체인과 직접 통신한다.
- SP 서버 내에 검증 라이브러리를 탑재한다.
- SP 서버가 내부 라이브러리를 통해 블록체인과 통신하여 사용자가 제출한 VP를 검증한다.
- 장점 : SP 서버 외에 별도의 서버가 필요하지 않다.
- 단점 : Java 7 이상이 필요하다(**Java 8 권장**).

라이브러리 방식을 위해 적용된 라이브러리(연계용 모듈)의 위치는 다음과 같다.
- ./libs/
	- com/raonsecure/license/
		- RSLicenseSDK_jdk16
	- com/raonsecure/omnione/
		- OmniEnt-SDK-Core
		- OmniEnt-SDK-ServerCore
		- OmniEnt-SDK-Verifier

참고) 데몬 방식
- 데몬 방식은 SP 서버 대신 **데몬 서버가** 블록체인과 통신한다.
- 데몬 방식은 SP 서버 밖에 데몬 서버를 둔다.
- 데몬 서버가 블록체인과 통신하여 사용자가 제출한 VP를 검증한다.
- 장점 : SP 서버는 데몬 서버와 API 방식으로 통신을 하므로 개발 언어에 영향받지 않는다. 
- 단점 : SP 서버 외에 데몬 서버를 별도로 둬야 한다.

### 인터페이스 및 통신 방식 : QR-MPM / Direct-Mode

![image-02-egovmobileid-architecture-qrmpm-direct](https://github.com/user-attachments/assets/ac4e381d-fc39-4a48-9ccf-e290fd1e5f4a)

인터페이스 방식에 대한 아키텍처는 사용자가 VP를 제출하기 위해 모바일 신분증 앱의 **어떠한 인터페이스(기능)를 사용하는지**, 그리고 사용자의 모바일 신분증 앱이 SP서버로 데이터를 발송할 때 **SP 서버가 데이터를 직접 수신하는지**에 관한 것이다.

표준프레임워크는 QR-MPM / Direct-Mode를 선택했다.
- QR-MPM 인터페이스는 사용자의 모바일 신분증 앱의 **QR 촬영 기능**을 사용하는 방식이다.
- Direct-Mode 통신 방식은 모바일 신분증 앱이 발송하는 데이터를 **SP 서버가 직접** 수신하는 주체가 되는 방식이다.

참고) Push 인터페이스, Proxy-Mode 통신 방식

Push 인터페이스는 사용자의 모바일 신분증 앱의 **Push 알림 기능**을 사용하는 방식이다.

Proxy-Mode 통신 방식은 SP 서버와 모바일 신분증 앱 사이에 **중개서버인 Proxy 서버를 거쳐** 양자가 데이터를 송수신하는 방식이다.

## 아키텍처에 상세 및 비즈니스 로직

라이브러리 방식 및 QR-MPM Direct-Mode 방식을 적용한 아키텍처의 구체적인 데이터 흐름은 아래와 같다.

### 1. QR 표출

① QR 생성 요청, ② QR 표출

- 사용자
	- 사용자가 SP Device(PC Browser)에서 "QR정보요청" 버튼 클릭
- Client : SP Device(PC Browser)
	- SP Device가 SP 서버에게 QR 생성 요청
	- qrmpm.js - fnQrInfoReq()
		- 요청 URI : **/mip/start** => QrmpmController.java - start(t510)
		- QR 생성
	- QR 표출
- SP 서버
	- 이력을 생성한 후, QR에 담을 내용을 발송
	- QrmpmController.java - start(t510)
		- 이력관리용 trxList 객체 (또는 DB 활용시 TB_TRX_INFO 테이블)에 새로운 trxInfo 추가
		- QR에 필요한 m200 메시지를 담은 t510을 생성
		- return t510

관련 코드는 다음과 같다.

(README에서는 로직 이해가 용이하도록 코드를 편집했다. 실제 소스코드는 예외처리 등이 적용되었다.)
- QrmpmController.java
```java
@RestController
public class QrmpmController {

	/**
	 * QR-MPM 시작
	 * 
	 * @MethodName start
	 * @param t510 QR-MPM 정보
	 * @return {"result": true, data: QR-MPM 정보 + Base64로 인코딩된 M200 메시지}
	 * @throws SpException
	 */
	@PostMapping(value = "/mip/start")
	public MipApiDataVO start(@RequestBody T510VO t510) throws SpException {
		
		T510VO data = qrmpmService.start(t510);
		
		MipApiDataVO mipApiData = new MipApiDataVO();
		
		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(data)));
		
		return mipApiData;
	}

}
```

- QrmpmServiceImpl.java
```java
@Service("qrmpmService")
public class QrmpmServiceImpl implements QrmpmService {

	/**
	 * QR-MPM 시작
	 * 
	 * @MethodName start
	 * @param t510 QR-MPM 정보
	 * @return QR-MPM 정보 + Base64로 인코딩된 M200 메시지
	 * @throws SpException
	 */
	@Override
	public T510VO start(T510VO t510) throws SpException {
		
		String mode = t510.getMode();
		String svcCode = t510.getSvcCode();
		
		TrxInfoVO trxInfo = new TrxInfoVO();
		
		trxInfo.setMode(mode);
		trxInfo.setSvcCode(svcCode);
		
		M200VO m200 = null;
		
		// QR에 필요한 m200 메시지를 생성
		m200 = this.directStart(trxInfo);
		
		String m200Str = ConfigBean.gson.toJson(m200);
		String m200Base64 = Base64Util.encode(m200Str);
		
		// Qm200 메시지를 담은 t510을 생성
		t510.setM200Base64(m200Base64);
		
		return t510;
	}


	/**
	 * QR-MPM 시작(Direct 모드)
	 * 
	 * @MethodName directStart
	 * @param trxInfo
	 * @return
	 * @throws SpException
	 */
	private M200VO directStart(TrxInfoVO trxInfo) throws SpException {
		M200VO m200 = null;
		
		
		String spServerDomain = configBean.getVerifyConfig().getSp().getServerDomain();
		String spBiImageUrl = configBean.getVerifyConfig().getSp().getBiImageUrl();
		Boolean isCi = configBean.getVerifyConfig().getSp().getIsCi();
		Boolean isTelno = configBean.getVerifyConfig().getSp().getIsTelno();
		String trxcode = Generator.genTrxcode();
		String mode = trxInfo.getMode();
		
		trxInfo.setTrxcode(trxcode);
		
		// 이력관리용 trxList 객체 (또는 DB 활용시 TB_TRX_INFO 테이블)에 새로운 trxInfo 추가
		trxInfoService.registTrxInfo(trxInfo);
		
		m200 = new M200VO();

		// QR에 필요한 m200 메시지를 생성
		m200.setTrxcode(trxcode);
		m200.setMode(mode);
		m200.setImage(spBiImageUrl);
		m200.setCi(isCi);
		m200.setTelno(isTelno);
		m200.setHost(spServerDomain);
		
		return m200;
	}

}
```
### 2. Profile 검증

③ QR 촬영, ④ Profile 요청, ⑤ Profile 생성, ⑥ Profile 검증

- 사용자
	- 사용자가 모바일 신분증 앱으로 QR 촬영(QR-MPM 인터페이스)
- Client : 모바일 신분증 앱
	- 모바일 신분증 앱이 SP 서버에게 SP 서버의 Profile을 요청
	- 이 때 SP 서버가 요청을 직접 수신(Direct-Mode 통신 방식)
	- 요청 URI : **/mip/profile** => MipController.java - getProfile(m310)
	- SP 서버로부터 받은 profile을 검증
- SP 서버
	- SP 서버의 Profile을 생성 및 검증한 후 모바일 신분증 앱에게 제출
	- MipController.java - getProfile(m310)
		- 모바일 신분증 앱이 보낸 m310 메시지가 정상인지 Enum을 통해 확인
		- m310에 담긴 trxcode로 trxInfo를 조회 -> trxInfo의 stsCode(상태코드) 값과 verifyResult(검증여부) 값과 service 값이 정상인지 확인
		- verifyConfig.json 파일의 데이터 중 profile에 담길 데이터 추출
		- 블록체인을 통해 SP 서버의 profile 생성(라이브러리 방식)
		- 생성한 profile을 핸드폰 앱이 보낸 m310 메시지에 set
		- return m310

관련 코드는 다음과 같다.

(README에서는 로직 이해가 용이하도록 코드를 편집했다. 실제 소스코드는 예외처리 등이 적용되었다.)
- MipController.java
```java
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/mip")
public class MipController {

	/**
	 * Profile 요청
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 M310 메시지"}
	 * @return {"result": true, "data": "Base64로 인코딩된 M310 메시지"}
	 * @throws SpException
	 */
	@PostMapping("/profile")
	public MipApiDataVO getProfile(@RequestBody MipApiDataVO mipApiData) throws SpException {

		M310VO m310 = null;

		String data = Base64Util.decode(mipApiData.getData());
		
		m310 = ConfigBean.gson.fromJson(data, M310VO.class);

		m310 = directService.getProfile(m310);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(m310)));

		return mipApiData;
	}
```

- DirectServiceImpl.java
```java
@Service("directService")
public class DirectServiceImpl implements DirectService {

	/**
	 * Profile 요청
	 * 
	 * @MethodName getProfile
	 * @param m310 M310 메시지
	 * @return M310 메시지 + Profile
	 * @throws SpException
	 */
	@Override
	public M310VO getProfile(M310VO m310) throws SpException {
		
		// 모바일 신분증 앱이 보낸 m310 메시지가 정상인지 Enum을 통해 확인
		this.validateM310(m310);
		
		// m310에 담긴 trxcode로 trxInfo를 조회
		String trxcode = m310.getTrxcode();
		TrxInfoVO trxInfo = trxInfoService.getTrxInfo(trxcode);

		// trxInfo의 stsCode(상태코드) 값과 verifyResult(검증여부) 값과 service 값이 정상인지 확인
		if (!TrxStsCodeEnum.SERCIVE_REQ.getVal().equals(trxInfo.getTrxStsCode())) {
			throw new SpException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "stsCode != 0001");
		}
		if ("Y".equals(trxInfo.getVpVerifyResult())) {
			throw new SpException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "verifyResult == Y");
		}
		ServiceVO service = services.get(trxInfo.getSvcCode());
		if (ObjectUtils.isEmpty(service)) {
			throw new SpException(MipErrorEnum.SP_CONFIG_ERROR, trxcode, "servie does not exist");
		}
		
		Integer presentType = service.getPresentType();
		String profile = "";
		
		// 블록체인을 통해 SP 서버의 profile 생성(라이브러리 방식)
		// 이력 수정
		profile = mipDidVpService.getProfile(trxInfo);
		
		// 생성한 profile을 핸드폰 앱이 보낸 m310 메시지에 set
		m310.setProfile(profile);

		return m310;
	}

}
```

### 3. VP 검증

⑦ VP 생성, ⑧ VP 검증 요청, ⑨ VP 검증

- Client : 모바일 신분증 앱
	- 모바일 신분증 앱이 SP 서버에게 제출할 VP를 생성하고 제출
	- 이 때 SP 서버가 요청을 직접 수신(Direct-Mode 통신 방식)
	- URI : **/mip/vp** => MipController.java - verifyVP(m400)
- SP 서버
	- SP 서버는 받은 VP를 검증하고, 모바일 신분증 앱에게 검증 결과를 발송
	- MipController.java - verifyVP(m400)
		- 모바일 신분증 앱이 보낸 m400 메시지가 정상인지 Enum을 통해 확인
		- m400에 담긴 trxcode로 trxInfo를 조회 -> trxInfo의 stsCode(상태코드) 값과 verifyResult(검증여부) 값이 정상인지 확인
		- m400에 담긴 VP가 정상인지 Enum을 통해 확인
		- 블록체인을 통해 VP 검증(라이브러리 방식)
		- VP 검증 결과를 Boolean Type으로 result에 set
		- return result

관련 코드는 다음과 같다.

(README에서는 로직 이해가 용이하도록 코드를 편집했다. 실제 소스코드는 예외처리 등이 적용되었다.)
- MipController.java
```java
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/mip")
public class MipController {

	/**
	 * VP 검증
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 M400 메시지"}
	 * @return {"result": true}
	 * @throws SpException
	 */
	@PostMapping("/vp")
	public MipApiDataVO verifyVP(@RequestBody MipApiDataVO mipApiData) throws SpException {

		M400VO m400 = null;

		String data = Base64Util.decode(mipApiData.getData());

		m400 = ConfigBean.gson.fromJson(data, M400VO.class);

		Boolean result = directService.verifyVP(m400);

		mipApiData.setResult(result);

		return mipApiData;
	}

}
```

- DirectServiceImpl.java
```java
@Service("directService")
public class DirectServiceImpl implements DirectService {

	/**
	 * VP 검증
	 * 
	 * @MethodName verifyVP
	 * @param m400 M400메시지
	 * @return 검증 결과
	 * @throws SpException
	 */
	@Override
	public Boolean verifyVP(M400VO m400) throws SpException {
		Boolean result = false;

		// 모바일 신분증 앱이 보낸 m400 메시지가 정상인지 Enum을 통해 확인
		this.validateM400(m400);
		
		// m400에 담긴 trxcode로 trxInfo를 조회
		String trxcode = m400.getTrxcode();
		TrxInfoVO trxInfo = trxInfoService.getTrxInfo(trxcode);
		
		// trxInfo의 stsCode(상태코드) 값과 verifyResult(검증여부) 값이 정상인지 확인
		if (!TrxStsCodeEnum.PROFILE_REQ.getVal().equals(trxInfo.getTrxStsCode())) {
			throw new SpException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "stsCode != 0002");
		}
		if ("Y".equals(trxInfo.getVpVerifyResult())) {
			throw new SpException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "verifyResult == Y");
		}
		
		// m400에 담긴 VP가 정상인지 Enum을 통해 확인
		VP vp = m400.getVp();
		this.validateVp(trxInfo, vp);

		Integer presentType = vp.getPresentType();

		// 블록체인을 통해 VP 검증(라이브러리 방식)
		// VP 검증 결과를 Boolean Type으로 result에 set
		// 이력 수정
		result = mipDidVpService.verifyVP(trxInfo, vp);

		return result;
	}

}
```

### 4. Privacy 조회 (결과 조회)

- 사용자
	- 사용자가 SP Device(PC Browser)에서 "결과조회" 버튼 클릭
- Client : SP Device(PC Browser)
	- SP Device가 SP 서버에게 trxInfo를 요청
	- common.js - fnGetTrxsts()
		- URI : **/mip/trxsts** => MipController.java - getTxsts(trxcode)
		- 화면 "거래상태" 표에 trxInfo 데이터를 출력
	- privacy.js - getPrivacy(trxinfo)
		- SP Device가 SP 서버에게 VC에 담긴 Privacy 조회를 요청
		- URI : **/mip/privacy/{license}** => MipController.java - getPrivacy(license, trxInfo)
		- 화면 "개인정보" 표에 LicenseVO 데이터를 출력
- SP 서버
	- 거래정보(trxInfo)와 개인정보(privacy)를 조회하고 이를 SP Device에게 발송
	- MipController.java - getTxsts(trxcode)
		- SP Device가 보낸 trxcode를 활용하여 trxInfo를 조회
		- return trxInfo
	- MipController.java - getPrivacy(license, trxInfo)
		- SP Device가 보낸 trxInfo에 담긴 VP를 라이브러리 방식으로 블록체인을 통해 복호화하여 privacy 목록을 추출
		- privacy 목록을 license에 해당하는 LicenseVO의 각 필드와 매핑
		- return map.put(LicenseVO)

관련 코드는 다음과 같다.

(README에서는 로직 이해가 용이하도록 코드를 편집했다. 실제 소스코드는 예외처리 등이 적용되었다.)
- MipController.java
```java
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/mip")
public class MipController {

	/**
	 * 거래상태 조회
	 * 
	 * @param mipApiData {"data": "Base64로 인코딩된 TrxInfoVO"}
	 * @return {"result": true, "data": "Base64로 인코딩된 TrxInfoVO"}
	 * @throws SpException
	 */
	@PostMapping(value = "/trxsts")
	public MipApiDataVO getTrxsts(@RequestBody MipApiDataVO mipApiData) throws SpException {
		
		TrxInfoVO trxInfo = null;
		String data = Base64Util.decode(mipApiData.getData());
		trxInfo = ConfigBean.gson.fromJson(data, TrxInfoVO.class);
		
		// SP Device가 보낸 trxcode를 활용하여 trxInfo를 조회
		trxInfo = trxInfoService.getTrxInfo(trxInfo.getTrxcode());
		
		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(trxInfo)));
		
		return mipApiData;
	}

	/**
	 * Privacy 조회
	 * 
	 * @param mipApiData {"data": "base64로 인코딩된 TrxInfo"}
	 * @return {"result": true, "data": "base64로 인코딩된 HashMap 객체"}
	 * @throws SpException
	 */
	@PostMapping(value = "/privacy/{license}")
	public MipApiDataVO getPrivacy(@PathVariable("license") String license, @RequestBody MipApiDataVO mipApiData) throws SpException {
		
		String data = Base64Util.decode(mipApiData.getData());
		TrxInfoVO trxInfo = ConfigBean.gson.fromJson(data, TrxInfoVO.class);
		
		// SP Device가 보낸 trxInfo에 담긴 VP를 라이브러리 방식으로 블록체인을 통해 복호화하여 privacy 목록을 추출
		List<Unprotected> privacy = mipDidVpService.getPrivacy(trxInfo.getTrxcode());

		// privacy 목록을 license에 해당하는 LicenseVO의 각 필드와 매핑
		Map<String, Object> map = mipDidVpService.mapPrivacy(license, privacy);
		
		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(map)));

		return mipApiData;
	}

}
```

- MipDidVpServiceImpl.java
```java
@Service("mipDidVpService")
public class MipDidVpServiceImpl implements MipDidVpService {

	/**
	 * Privacy 조회
	 * 
	 * @MethodName getPrivacy
	 * @param trxcode 거래코드
	 * @return Privacy 목록
	 * @throws SpException
	 */
	@Override
	public List<Unprotected> getPrivacy(String trxcode) throws SpException {
		TrxInfoVO trxInfo = trxInfoService.getTrxInfo(trxcode);

		VP vp = null;
		vp = ConfigBean.gson.fromJson(trxInfo.getVp(), VP.class);
		
		VCVerifyProfileResult vCVerifyProfileResult = new VCVerifyProfileResult();

		vCVerifyProfileResult.setEncryptType(vp.getEncryptType());
		vCVerifyProfileResult.setKeyType(vp.getKeyType());
		vCVerifyProfileResult.setType(vp.getType());
		vCVerifyProfileResult.setData(vp.getData());
		vCVerifyProfileResult.setAuthType(vp.getAuthType());
		vCVerifyProfileResult.setDid(vp.getDid());
		vCVerifyProfileResult.setNonce(vp.getNonce());
		
		// SP Device가 보낸 trxInfo에 담긴 VP를 라이브러리 방식으로 블록체인을 통해 추출
		String vpData = verifyManager.getVPData(vCVerifyProfileResult);
		
		// VP를 라이브러리 방식으로 블록체인을 통해 복호화하여 privacy 목록을 추출
		List<Unprotected> privacyList = verifyManager.getPrivacy(vpData);

		return privacyList;
	}
	
	/**
	 * Privacy를 LicenseVO로 매핑
	 * 
	 * @MethodName mapPrivacy
	 * @param license 라이선스명
	 * @param privacy Privacy 목록
	 * @return LicenseVO로 매핑된 privacy 목록
	 * @throws SpException
	 */
	@Override
	public Map<String, Object> mapPrivacy(String license, List<Unprotected> privacy) throws SpException {
		
		String licenseName = "";
		Object licenseVO;
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// privacy 목록을 license에 해당하는 LicenseVO의 각 필드와 매핑
		switch (license) {
		case "driver":
			licenseName = "driver";
			licenseVO = new LicenseDriverVO().mapFromList(privacy);
			break;
		case "honor":
			licenseName = "honor";
			licenseVO = new LicenseHonorVO().mapFromList(privacy);
			break;
		case "expat":
			licenseName = "expat";
			licenseVO = new LicenseExpatVO().mapFromList(privacy);
			break;
		default:
			licenseName = null;
			licenseVO = null;
			break;
		}
		
		map.put("licenseName", licenseName);
		map.put("licenseVO", licenseVO);
			
		return map;
	}

}
```

## DB 사용 여부 설정

개발시 테스트의 편의성을 위해 db를 대체한 ListHolder를 사용하는 버전으로 배포했다. DB를 적용하려면 다음 내용을 수정하면 된다.

- ./src/main/java/
	- egovframework.com.mip.mva.sp.comm.service.impl.TrxInfoLocalServiceImpl
		- `@Service("trxInfoService")` 어노테이션을 주석 처리
	- egovframework.com.mip.mva.sp.comm.service.impl.TrxInfoServiceImpl
		- `@Service("trxInfoService")` 어노테이션의 주석을 해제
	- egovframework.com.mip.mva.sp.config.db.SpDataSource
		- `@Configuration` 어노테이션의 주석을 해제
- ./schema/
    - 각 db별 DDL sql 파일을 참고하여 테이블 생성
- ./verifyConfig.json
	- "db" 환경정보 추가
	- verifyConfig.json 파일 생성은 다음 링크 참고 : http://mvadev.mobileid.go.kr:8180/static/html/comm/main.html

DB 환경정보 설정을 verifyConfig.json 파일에 하는 방법 외에도, 다른 서비스에서 설정하는 방식처럼 ConfigServer를 활용하는 방법도 있다. 이 경우 SpDataSource를 활용하지 않으므로, SpDataSource 내 `@Configuration` 어노테이션의 주석처리는 유지해야 한다.

## Filter 설정

### 1. GatewayServer의 필터

MSA 공통컴포넌트는 보안 및 URL 관리를 위해 Gateway에서 라우팅을 관리한다. GatewayServer의 application.yml 파일에는 다음과 같은 라우팅 설정이 있다.
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: EgovMobileId
          uri: lb://EGOVMOBILEID
          predicates:
            - Path=/mip/**
          filters:
            - RewritePath=/mip/(?<segment>.*), /mip/${segment}
            - name: Auth
```

이를 간략하게 설명하면 다음과 같다.
- 클라이언트의 요청 경로가 `/mip`로 시작하는 경우 필터(AuthGatewayFilterFactory)를 통해 JWT 기반 인증 진행
- 그 인증을 통과한 경우에만 Header에 아래 4가지 속성 값을 set한 후 EGOVMOBILEID 서비스(로드 런싱을 통해 선택된 인스턴스)로 요청을 전달
	- **X-CODE-ID**, X-USER-ID, X-USER-NM, X-UNIQ-ID

Gateway에 대한 자세한 설명은 [GatewayServer의 README](../GatewayServer/README.md)를 참고한다.

위 필터 규칙(`/mip`로 시작)을 MobileId에 적용하기 위해, 검증모듈 원본과 달리 EgovMobileId 내 몇몇 컨트롤러 메서드의 URI 매핑을 수정했다. 수정한 내역은 다음과 같다.
- src/main/java
	- egovframework.com.mip.mva.sp.comm.web.CommViewController
		- main 메서드 : `/main` -> `/mip/main`
	- egovframework.com.mip.mva.sp.qrmpm.web.QrmpmController
		- start 메서드 : `/qrmpm/start` -> `/mip/start`
	- egovframework.com.mip.mva.sp.qrmpm.web.QrmpmViewController
		- qrmpmView 메서드 : `/qrmpm/qrmpmView` -> `/mip/main/{license}`

**오류 대응 : 404 오류**

특정 메서드에서 404 오류가 발생한다면, 그 원인은 해당 URI와 매칭되는 라우팅 규칙이 없기 때문일 수 있다.  만약 표준프레임워크가 사용하는 것 이외의 메서드를 더 사용하고자 한다면, 다음 중 하나의 조치가 필요하다.
- 그 매서드와 매핑된 URI가 `/mip`로 시작하도록 수정(**권장**)
- GatewayServer의 application.yml의 routes 설정에서 EgovMobileId의 `predicates - Path` 부분을 커스텀하여 해당 메서드가 라우팅되도록 설정

### 2. EgovMobileId의 필터

Client로부터 받는 모든 Request는 GatewayServer의 JWT 필터를 거쳐 로드 밸런싱 된 후, EgovMobileId의 필터를 통해 추가적인 보안 검증("**X-CODE-ID**" 값 체크)을 거쳐야 한다. 그 구체적인 역할은 src.main.java.egovframework.com.filter.AuthorizeFilter 클래스의 doFilterInternal 메서드가 수행한다.
```java
public class AuthorizeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    
        String secretCodeId = request.getHeader("X-CODE-ID");
        
        if (!request.getRequestURI().startsWith("/mip/profile") &&
            !request.getRequestURI().startsWith("/mip/vp")
        ) {
            String SECRET_CODE = "-WzAnecnlNewSEQwDgJ2BQ";
            if (ObjectUtils.isEmpty(secretCodeId) || !SECRET_CODE.equals(secretCodeId)) {
                log.warn("##### Access Denied: Unauthorized Access Attempt");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                String ERROR_PAGE = "/error/403.html";
                response.sendRedirect(request.getContextPath() + ERROR_PAGE);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
```

하지만 EgovMobileId는 사용자 PC와의 일반적인 통신뿐만 아니라 사용자의 모바일 신분증 앱과도 통신을 한다. 사용자의 모바일 신분증 앱은 verifyConfig.json의 `sp - serverDomain`의 URL 값을 활용해 SP 서버(EgovMobileId)와 직접 연결된다. 따라서, **사용자의 모바일 신분증 앱과의 통신은 GatewayServer의 필터(JWT)와 로드 밸런싱을 거치지 않는다.**
```json
{
    "sp": {
        "serverDomain": "http://61.253.112.177:9991",
    },
}
```

결론적으로, profile 검증(`/mip/profile`)과 vp 검증(`/mip/vp`)은 GatewayServer를 거치지 않고 바로 EgovMobileId와 연결되므로,  "X-CODE-ID" 값 체크만 거치면 된다.

하지만 이 경우 모바일 신분증 앱은 Header에 X-CODE-ID 값을 설정할 수 없다. 따라서 SP 서버는 모바일 신분증 앱과 통신하기 위해 반드시 두 URI에 대해 인증 예외를 설정해야 한다. 그 예외 설정은 위 AuthorizeFilter 코드를 살펴보면 확인 가능하다.

**오류 대응 : 403 오류**

특정 메서드에서 403 오류가 발생한다면, 그 원인은 EgovMobileId에서 진행하는 보안 검증(X-CODE-ID" 값 체크)에 실패했기 때문일 확률이 높다. 따라서 모바일 신분증 앱과 통신하는 메서드가 존재하는 경우, 보안 검증 예외 대상에 그 메서드와 매핑된 URI를 추가해야 한다.

EgovMobileId 내 AuthorizeFilter 클래스를 다음과 같이 수정한다.
```java
		// 수정 전
		if (!request.getRequestURI().startsWith("/mip/profile") &&
		    !request.getRequestURI().startsWith("/mip/vp")
		) {}
		
		// 수정 후
		if (!request.getRequestURI().startsWith("/mip/profile") &&
		    !request.getRequestURI().startsWith("/mip/vp") &&
		    !request.getRequestURI().startsWith("{보안 체크 예외 URI}")
		) {}
```

## 테스트

- main 페이지로 접속하여 "일반" 사용자로 로그인한다.
	- ID : USER
	- PW : rhdxhd12

![image-11-egovmobileid-web-login](https://github.com/user-attachments/assets/5b0a2a02-89ce-4e56-b144-0af4c35184f2)

- 사이드 메뉴에서 "모바일신분증"을 클릭 후, 테스트 하려는 신분증을 선택한다.

![image-12-egovmobileid-web-choose-license](https://github.com/user-attachments/assets/0b2872bc-1ef5-43b1-a77e-d8ab339a2c16)

- "QR정보요청" 버튼을 클릭한다.

![image-13-egovmobileid-web-click-request_qr_btn](https://github.com/user-attachments/assets/545cc17b-e670-4e0b-8d15-78af1ceed0fd)

- QR 이미지가 생성되는 것을 확인한다.

![image-14-egovmobileid-web-qr-image](https://github.com/user-attachments/assets/08347b0b-03af-4c51-9c43-d62dc4cac169)

- 모바일 신분증 어플에서 좌측 상단의 설정 버튼을 클릭한다.

<img width="400" alt="image-15-egovmobileid-mobile-setting" src="https://github.com/user-attachments/assets/93fa16af-096d-4874-bfe6-38633229533e" />

- "내 신분증 관리"를 클릭한다.

<img width="400" alt="image-16-egovmobileid-mobile-management" src="https://github.com/user-attachments/assets/d9138e19-1876-4ddc-be48-2588732025b9" />

- 대표신분증을 테스트 대상 신분증에 맞춰 변경한다.

<img width="400" alt="image-17-egovmobileid-mobile-represent" src="https://github.com/user-attachments/assets/dc7813c5-7067-46ae-a328-12655c426858" />

- 모바일 신분증 앱의 시작화면으로 돌아와서, "QR 촬영" 버튼을 클릭하고, PC Browser에 생성된 QR을 촬영한다.

<img width="400" alt="image-18-egovmobileid-mobile-picture-qr" src="https://github.com/user-attachments/assets/73ddc612-cdee-4836-9d0a-68a16168c85a" />

- "정보 보내기" 버튼을 클릭하고, 인증을 진행한다. 

<img width="400" alt="image-19-egovmobileid-mobile-verify" src="https://github.com/user-attachments/assets/76caed82-d77a-4db5-a038-e1e439f4ab89" />

- 아래 화면이 뜬다면, 정상적으로 처리가 된 것이다.

<img width="400" alt="image-20-egovmobileid-mobile-result" src="https://github.com/user-attachments/assets/64a8389e-515b-4ca1-ad3a-5be959a85538" />

- PC로 돌아와서, "결과조회" 버튼을 클릭한다.

![image-21-egovmobileid-web-inquiry_result_btn](https://github.com/user-attachments/assets/fdb7d5f4-6fe7-4c46-8236-77f2ab3633c2)

- 화면을 스크롤하여 아래로 내리면, "거래상태"와 "개인정보"를 확인할 수 있다.

![image-22-egovmobileid-web-result](https://github.com/user-attachments/assets/2ac10306-24d8-4ae8-9c02-b7164184a1b1)
