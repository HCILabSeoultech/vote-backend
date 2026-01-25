# VoteY - 투표 기반 SNS 플랫폼

### 💡 프로젝트 소개
VoteY는 투표를 중심으로 의견을 공유하고, 결과를 심층적으로 분석할 수 있는 SNS 플랫폼입니다.  
사용자는 텍스트, 이미지, 영상 기반 투표를 생성하고 참여할 수 있으며, 댓글·좋아요·공유·팔로우·알림 기능 등 다양한 SNS 요소를 경험할 수 있습니다.  
또한 성별, 연령대, 지역별 통계 분석을 통해 의미 있는 인사이트를 제공하고, 생성형 AI를 활용해 당일 뉴스를 기반으로 오늘의 이슈 및 투표를 생성합니다.

---

### 🛠️ 기술 스택

### Backend & Language
![Java](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?logo=springboot&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate&logoColor=white)

### Database & Cache
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-AWS%20RDS-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)

### Infra & DevOps
![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS-S3-569A31?logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?logo=githubactions&logoColor=white)

### AI & Others
![FastAPI](https://img.shields.io/badge/FastAPI-Generative%20AI-009688?logo=fastapi&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-85EA2D?logo=swagger&logoColor=black)
![Notion](https://img.shields.io/badge/Notion-Team%20Collab-000000?logo=notion&logoColor=white)

---

### ⚙️ 시스템 아키텍처
<img width="1248" height="832" alt="Gemini_Generated_Image_ql0zp9ql0zp9ql0z" src="https://github.com/user-attachments/assets/f61a4c7e-4e68-4e1c-b9db-81ee70404cd4" />

---

### 프로젝트 구조
```
/src
  /main/java/project/votebackend
    ├─ application
    ├─ client
    ├─ config
    ├─ controller
    ├─ domain
    ├─ dto
    ├─ exception
    ├─ repository
    ├─ scheduler
    ├─ security
    ├─ service
    ├─ type
    └─ util
  /test/java/project/votebackend
```

---

### 🔑 ERD
<img width="1116" height="953" alt="스크린샷 2025-09-25 오후 3 51 31" src="https://github.com/user-attachments/assets/a847f086-7996-4b91-905b-491963d2812d" />

---

## 🚀 주요 기능

### 투표 기능
- 텍스트, 이미지, 영상 기반 투표 생성
- 사용자 참여 투표, 투표 결과 즉시 확인
- 투표 결과에 대한 성별, 연령대, 지역별 통계 분석

### 소셜 기능
- 댓글, 대댓글, 좋아요, 북마크, 공유
- 팔로우/팔로워
- 실시간 알림 서비스

### 인기 탭
- 좋아요 수 / 댓글 수 / 투표 수 기준으로 인기 게시물 제공
- 당일 트렌드를 반영한 실시간 인기 투표 노출

### AI 기반 투표 생성
- 네이버 뉴스 기반으로 카테고리별 인기 있는 주제 선정
- 생성형 AI가 자동으로 투표 및 오늘의 이슈 생성
- FastAPI 서버를 통해 Spring과 연동
- 앱 내 AI 추천 투표 제공

| 기능 | 화면 |
|------|------|
| **홈 탭** | <img width="259" height="556" alt="스크린샷 2025-09-25 오후 3 31 28" src="https://github.com/user-attachments/assets/c81e7e30-4631-4e43-8b12-324192eca5e1" /> |
| **통계 분석** |  <img width="264" height="544" alt="스크린샷 2025-09-25 오후 3 31 37" src="https://github.com/user-attachments/assets/0e8b3fe9-f6d7-449d-873d-d680c1a64d48" /> |
| **인기 탭** | <img width="261" height="557" alt="스크린샷 2025-09-25 오후 3 31 46" src="https://github.com/user-attachments/assets/0f8ea564-8a9e-45d8-baf3-3d0a18e9cabd" /> |
| **글작성 탭** | <img width="261" height="550" alt="스크린샷 2025-09-25 오후 3 31 52" src="https://github.com/user-attachments/assets/8b317fc3-af73-46a8-9ede-fc4e4f004ccf" /> |
| **이슈 탭** |  <img width="266" height="553" alt="스크린샷 2025-09-25 오후 3 31 58" src="https://github.com/user-attachments/assets/6c6ff8de-21fb-4c2c-affd-d5d998e0f72b" /> |
| **마이페이지** |<img width="264" height="553" alt="스크린샷 2025-09-25 오후 3 32 04" src="https://github.com/user-attachments/assets/f4db5ca6-3abc-41f7-bdbe-aef9eb86477f" /> |
