# 표준프레임워크 MSA 공통컴포넌트
![java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=JAVA&logoColor=white)
![Spring_boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![html5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![javascript](https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![jQuery](https://img.shields.io/badge/jquery-%230769AD.svg?style=for-the-badge&logo=jquery&logoColor=white)

## 프로젝트 소개

해당 프로젝트는 Spring Boot, Spring Data JPA, Spring Cloud를 이용하여 MSA 형식으로 제작되어 기존 공통컴포넌트 23종 + 신규 3종(모바일신분증 3종) 으로 구성되어있다.

### 프로젝트 환경

프로젝트에서 사용된 환경 프로그램 정보는 다음과 같다.

| 프로그램 명 | 버전 명                                  |
| :----- |:--------------------------------------|
| Java   | 17 (각 모듈 `pom.xml` 기준) |
| Spring Boot | 3.5.6                                |
| Spring Cloud | 2025.0.0                              |
| Docker Desktop | 4.39.0 |
| Open Search | 2.15.0 |
| Python | 3.11.5 (Embedding 용 Model export 시 사용) |

### 프로젝트 구성

```
  egovframe-msa-common-components (멀티 모듈 Maven)
    ├ ConfigServer      … 중앙 설정 (native, classpath:/config)
    ├ EurekaServer      … 서비스 디스커버리
    ├ GatewayServer     … API 게이트웨이 (기본 포트 9000)
    ├ EgovMain         … 포털 메인 레이아웃·메뉴
    ├ EgovLogin        … 로그인·JWT 쿠키 발급 (Redis 사용)
    ├ EgovLoginPolicy  … 로그인 정책(IP 등)
    ├ EgovAuthor       … 권한·롤·그룹
    ├ EgovBoard        … 게시판·협업 (RabbitMQ: 검색 연동 시)
    ├ EgovCmmnCode     … 공통코드
    ├ EgovQuestionnaire … 설문
    ├ EgovSearch       … OpenSearch·통합검색 (고정 포트 9992)
    └ EgovMobileId     … 모바일 신분증 SP (고정 포트 9991, 외부 앱 직접 통신)
```
※ OpenSearch·RabbitMQ용 `docker-compose`는 `EgovSearch/docker-compose/` 에 있음. 검색 설정 디렉터리 `EgovSearch-Config`는 별도 준비.

### 공통컴포넌트 목록

- `EgovLogin` : 로그인·JWT 발급
- `EgovLoginPolicy` : 로그인정책관리
- `EgovAuthor` : 보안 (7종)
  - 권한관리
  - 권한그룹관리
  - 그룹관리
  - 롤관리
  - `EgovMobileId` : 모바일신분증(모바일운전면허증, 국가보훈등록증, 재외국민신원확인증)
- `EgovBoard` : 협업 (7종)
  - 블로그관리
  - 게시판관리
  - 커뮤니티관리
  - 게시판(방명록)
  - 게시판(통합게시판)
  - 댓글관리
  - 만족도조사
- `EgovQuestionnaire` : 사용자지원(6종)
  - 설문관리
  - 설문조사
  - 설문템플릿관리
  - 응답자관리
  - 질문관리
  - 항목관리
- `EgovCmmnCode` : 시스템관리 (3종)
  - 공통분류코드
  - 공통상세코드
  - 공통코드
- `EgovSearch` : 시스템/서비스연계 (1종)
  - OpenSearch 검색엔진
 
## 프로젝트 구동 방법
0. docker-compose/mysql 실행 `docker-compose up -d`
1. EurekaService 실행
2. ConfigServer
   - `ConfigServer/src/main/resources/config/application-local.yml`
   - DB, Token 설정 확인
   - 샘플 DB : ([공통컴포넌트 테이블 정보](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:com:v4.1:init_table#:~:text=%EC%9A%B4%EC%A0%84%EB%A9%B4%ED%97%88%EC%A6%9D%20SP%20%EA%B1%B0%EB%9E%98%EC%A0%95%EB%B3%B4-,%ED%85%8C%EC%9D%B4%EB%B8%94/%EC%B4%88%EA%B8%B0%EB%8D%B0%EC%9D%B4%ED%84%B0%20%EC%83%9D%EC%84%B1%20%EC%8A%A4%ED%81%AC%EB%A6%BD%ED%8A%B8,-%EA%B3%B5%ED%86%B5%EC%BB%B4%ED%8F%AC%EB%84%8C%ED%8A%B8%EB%8A%94%20%EB%B0%B0%ED%8F%AC%ED%8C%8C%EC%9D%BC%EC%9D%84%20%ED%86%B5%ED%95%B4))
3. GatewayServer 실행 (기본 포트 :9000)
4. EgovMain 실행 (메인 레이아웃)
5. EgovLogin 실행 (로그인 후 인증토큰이 있어야 컴포넌트 사용 가능)
6. 필요한 컴포넌트 실행

## 화면 구성

### KRDS (대한민국정부 디자인 시스템)

본 프로젝트에는 KRDS Component Kit 1.0.3 ver을 적용  
KRDS의 컴포넌트 일부를 사용하였으며 용도에 따라 패턴 등을 추가하여 사용하고있다.

#### Custom resource

- CSS (`src/main/resources/static/css/egovframe.css`) : layout pattern 등
- JavaScript (`src/main/resources/static/js/egovframework/common.js`) : nullCheck, 키보드 접근성 등
- Image (`src/main/resources/static/img/egovframework`) : EgovFramework 로고 등

※ 추가로 사용한 리소스의 사용법은 제공하지않으며, 디자인에 관련된 자세한 사항은 KRDS를 확인.

### 화면

#### 1. 메인 화면

![메인레이아웃 화면](https://github.com/user-attachments/assets/36b0c623-f957-4586-a8bd-e9502828e52b)   
※ 1,2,3 번의 경우 KRDS의 컴포넌트 사용

1. 헤더(Header)
2. 사이드 네비게이션(Side-Navigation)
3. 푸터(Footer)
4. 콘텐츠 영역 : 공통컴포넌트 프로젝트가 표시되는 영역
   - 프로젝트 실행 후 페이지 표시까지 약 5초 소요
   - 로그인 컴포넌트가 실행된 경우 기본 첫 화면은 로그인화면 (로그인 인증 후 다른 컴포넌트 이용가능)  


#### 2. 로그인 화면
- 로그인 Token 관리를 위해 Redis가 구성되어야합니다.</br>
- [Redis 구성방법 참조](EgovLogin/README.md/#3-redis-구성방법---docker-이미지를-이용)

![로그인](https://github.com/user-attachments/assets/460b906e-ebe5-42d7-ad82-d0d9a6a17b14)   
- DB에 저장된 USER 정보를 이용해 로그인

- 로그인 성공   
![로그인 성공 화면](https://github.com/user-attachments/assets/0e001527-a628-45aa-a273-9e826ab2dd40)   

- 로그인 실패 시 다른 컴포넌트 페이지 접근 불가 (인증 토큰 부재)

- Token 발급 확인      
  - AccessToken과 RefreshToken이 발급된 상태   
    - AccessToken : 개발자모드(F12) → Application → Storage → Cookies 에서 확인
    - RefreshToken : [Redis 구성방법 참조](EgovLogin/README.md/#3-redis-구성방법---docker-이미지를-이용)을 참조하여 redis-cli를 통해 redis 내부에 저장된 값 확인
   
      

- 접근권한이 없는 페이지에 접근한 경우   
![403error](https://github.com/user-attachments/assets/ea7fdfa9-56ec-4ac5-9050-49aa9add8012)   
로그인은 성공하였으나 권한이 부여되지 않은 경로인 경우

- 프로젝트가 서버에 올라가지 못한 경우
![Image](https://github.com/user-attachments/assets/a5a0113d-dd20-4f15-bfb5-a45673f11d31)   
  - 서비스가 실행되지 않은 상태에서 접근한 경우 나타나는 에러
  - 서비스를 실행 후 5 ~ 10초가 지나도 실행되지 않는 경우 서비스가 에러없이 제대로 실행되었는지 확인 필요

## 참조

1. [KRDS](https://www.krds.go.kr/)
2. [Spring](https://spring.io/)
3. [공통컴포넌트 테이블 구성 정보](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:com:v4.3:init_table)
