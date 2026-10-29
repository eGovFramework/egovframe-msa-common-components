# 설문조사

설문 문항,답변 등록 및 설문 조사를 실시할 수 있는 컴포넌트

## 서비스 상태 (현재 모듈 기준)

| 항목 | 내용 |
|------|------|
| **역할** | 설문 템플릿·설문·질문·항목·응답자·설문응시 |
| **애플리케이션명** | `EgovQuestionnaire` → Eureka `EGOVQUESTIONNAIRE` |
| **포트** | `0`(랜덤). **Gateway** `http://localhost:9000/uss/olp/...` |
| **Config Server** | 사용 |
| **Eureka** | 등록 |

## 프로젝트 구성

``` text
  EgovQuestionnaire
    ├ /src/main
    │   ├ java
    │   │   └ egovframework.com
    │   │       ├ config
    │   │       ├ filter
    │   │       ├ pagination
    │   │       ├ olp
    │   │       │  ├ qim
    │   │       │  ├ qmc
    │   │       │  ├ qqm
    │   │       │  ├ qri
    │   │       │  ├ qrm
    │   │       │  └ qtm
    │   │       └ EgovQuestionnaireApplication
    │   │
    │   └ resources
    │       ├ messages.egovframework.com
    │       ├ static
    │       ├ templates.egovframework.com.uss.olp
    │       │   ├ qim
    │       │   ├ qmc
    │       │   ├ qqm
    │       │   ├ qri
    │       │   ├ qrm
    │       │   └ qtm
    │       └ application.yml
    └ pom.xml
```

- 항목관리 (`/com/uss/olp/qim`)
- 설문관리 (`/com/uss/olp/qmc`)
- 질문관리 (`/com/uss/olp/qqm`)
- 설문조사 (`/com/uss/olp/qri`)
- 응답자관리 (`/com/uss/olp/qrm`)
- 설문템플릿관리 (`/com/uss/olp/qtm`)

## 화면 구성

 - 설문 등록은 "업무사용자" 권한을 가진 계정으로 '설문템플릿관리' > '설문관리' > '질문관리' > '항목관리' 순으로 등록
 - 설문 참여는 "일반사용자" 권한을 가진 계정으로 '설문조사' 에서 등록
 - 응답자관리는 등록/수정을 제공하지 않고 설문 참여 시 작성한 정보를 목록형태로 확인할 수 있다.   

### 1. 설문템플릿관리

  1. 설문 템플릿 목록
  ![Tmplat_list](https://github.com/user-attachments/assets/f1668e34-0699-4c6c-98ca-45eca51aed72)   
  <br/>
  *설문템플릿의 경우 템플릿으로서의 기능은 현재 제공하고있지 않음
  
### 2. 설문관리

  1. 설문관리 등록   
    ![qmc_insert](https://github.com/user-attachments/assets/4b4b59c3-ed84-469c-aeef-20f20e3a7348)   
    <br/><br/>
     - 등록된 설문으로 설문 질문과 항목을 연관하여 생성
     - 설문기간의 경우 시작일이 '오늘'보다 빠를 수 없고 종료일은 '시작일'보다 빠를 수 없음
     - 수정 시 '템플릿 유형'은 변경 불가능   
    <br/>
  2. 설문관리 상세
    ![qmc_detail](https://github.com/user-attachments/assets/cccc9aad-dbd3-4d87-a995-8568b11d19bb)   
    <br/><br/>
  3. 설문관리 목록
     - 등록한 설문들에 대한 목록이 출력되는 페이지
      ![qmc_list](https://github.com/user-attachments/assets/c67ef833-ff40-4cd5-9fdf-df64d77dd740)   
       <br/><br/>
     - 연관된 설문 문항이 있는 경우 버튼 클릭시 모달창 오픈   
        ![qim_list](https://github.com/user-attachments/assets/8e7bb5bd-1145-412d-9b81-95bd4baa2cc5)   
       <br/><br/>
     - 등록된 설문조사를 참여한 사람이 있는 경우 '응답자 정보'에 표시   
        ![qrm_list](https://github.com/user-attachments/assets/3b8239be-d88f-463a-924f-91ae67bad646)   
       <br/><br/>
     - 통계는 설문조사에 대한 결과 표시   
        ![result_list](https://github.com/user-attachments/assets/1f14e117-3b80-4597-b87d-b6f0b3cccdd5)   

### 3. 질문관리

### 4. 항목관리

### 5. 설문조사

### 6. 응답자관리



## 유의사항

- 본 모듈 `pom.xml` 기준 **Java 17**.
- `EgovQuestionnaire`는 중복 의존성 선언 없이 Maven 3.10.0의 POM 모델 검증을 통과합니다.

## 참조
