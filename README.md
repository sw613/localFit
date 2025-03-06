# LocalFit: 서울시 공공체육시설 기반 운동 모임 플랫폼
![메인1](https://github.com/user-attachments/assets/3816a50e-538c-4b9d-817a-05e23709cacc)
![메인2](https://github.com/user-attachments/assets/9160a448-6d73-4444-9bd8-1da88cdc593e)



# 프로젝트 주제
- **서울시 공공체육시설 데이터를 활용하여 운동 모임을 쉽게 찾고 참여할 수 있는 커뮤니티 플랫폼**


# 프로젝트 목표
- 기획 의도
    - 가까운 지역에서 운동 모임을 찾기 어려운 문제 해결
    - 사설 체육시설 이용 시 발생하는 비용 부담 완화
    - 지역 기반의 운동 커뮤니티 형성을 통한 체육시설 이용 활성화
      
- 기대 효과
    - 누구나 쉽게 **운동 모임을 개설하고 참여 가능**
    - **지역 주민 간 네트워킹 강화**, 커뮤니티 활성화
    - **공공체육시설 이용률 증가**, 유휴 공간의 효과적인 활용



# 개발 환경
![기술스택](https://github.com/user-attachments/assets/26891ea5-58dd-4759-885b-47a1e4f1199f)



# 주요 기능
- 유저
    - **일반 회원가입 및 로그인** (Spring Security 적용)
    - **OAuth 로그인 지원** (구글)
    - **마이페이지** (회원 정보 수정)
    - **관리자 페이지** (유저 관리, 모임 관리, 피드 관리)
      
- 시설
    - **서울시 공공체육시설 목록 조회** (공공 API 활용)
    - **시설별 상세 정보 제공** (시설명, 장소명 등)
      
- 지도
    - **시설 위치 지도 조회** (카카오맵 API 연동)
      
- 운동 모임
    - **시설별 운동 모임 개설/수정/삭제 (CRUD)**
    - **모임별 가입 신청자 목록 확인**
    - **모임 가입 신청 및 승인/거절**
      
- 채팅방
    - **모임 가입 시 자동 채팅방 생성**
    - **채팅방 목록 조회**
    - **실시간 채팅 지원**
    - **채팅방 나가기**
    - **방장 권한 추가** (채팅방 삭제, 유저 강제 퇴장)
      
- 라운지(SNS형 피드)
    - **운동 관련 피드 업로드 (CRUD)**
    - **피드 상세 페이지 제공**
    - **피드에 댓글, 대댓글 작성 가능**
    - **피드 추천(좋아요) 기능**
    - **팔로우/팔로잉 기능**
    - **유저 프로필 수정 가능**
      
- 검색
    - **해시태그 기반 검색 기능**
    - **시설 검색 자동완성 기능**


# Wireframe

![와이어프레임](https://github.com/user-attachments/assets/01d20060-087b-459c-ae30-228549a009ef)



# ERD

![ERD](https://github.com/user-attachments/assets/ef0e7fd8-e162-4d64-9f74-e09ab8680267)

