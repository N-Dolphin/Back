# Back
<div align="center">

# 🐬 NDolphin
유저 매칭과 실시간 채팅을 위한 고가용성 메시징 서버

[//]: # (https://www.ndolophin.com/)

[2025/02/13] : 서버 비용문제로 인스턴스 종료하였습니다

[![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/)
[![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)](https://www.nginx.com/)
[![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)

</div>

## ⚡️ 주요 기능

- `사용자 인증`: JWT와 SMTP, Kakao OAuth2.0을 통한 보안 강화
- `유저 매칭` : PostGis의 거리 계산 쿼리를 이용한 일정 범위 내 유저 추천
- `유저 필터링` : 한번 매칭된 유저 혹은 싫어요를 누른 유저를 추천에서 제외
- `실시간 채팅`: WebSocket(STOMP)과 RabbitMQ를 활용한 실시간 메시징
- `무중단 배포`: Blue/Green 배포 전략을 통한 서비스 안정성 확보
- `이미지 업로드`: AWS S3를 활용한 프로필 이미지 관리
- `채팅 메세지 관리`: 채팅 메세지는 NOSQL에 따로 저장

[//]: # (- `실시간 알림`: Redis를 활용한 효율적인 실시간 알림 처리)

## 🏗️ 시스템 아키텍처

<div align="center">
<img src="/src/main/resources/static/아키텍쳐설계도.png" alt="system-architecture">
</div>


###주요 엔티티

-UserEntity: 사용자 인증 정보 저장 (이메일, 비밀번호, OAuth 정보)
-Profile: 사용자 프로필 정보 (이름, 소개, 성별, 나이, 위치)
-ProfileImage: S3에 저장된 사용자 프로필 이미지 관리
-AreaFilter: 지리 데이터를 활용한 위치 기반 필터링
-SearchFilter: 매칭 사용자 검색 조건 (성별, 나이 범위, 검색 거리)
-Swipe: 프로필 간 상호작용 기록 (좋아요, 싫어요)
-ChatRoom: 매칭된 두 사용자 간의 대화방
-ChatRoomParticipant: 채팅방 참가자 정보와 읽은 시간 추적
-ChatMessage: MongoDB에 저장되는 채팅 메시지 (텍스트, 파일 등)

###주요 관계

-User-Profile: 사용자는 하나의 프로필을 가짐
-Profile-ProfileImage: 프로필은 여러 이미지를 가질 수 있음
-Profile-Swipe: 사용자 간 좋아요/싫어요 관계
-Profile-ChatRoomParticipant: 사용자는 여러 채팅방에 참여할 수 있음
-ChatRoom-ChatMessage: 채팅방은 여러 메시지를 포함함

## 🛠 Tech Stack

### Infrastructure
![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

### Backend
![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Interceptor](https://img.shields.io/badge/Spring_Interceptor-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![SMTP](https://img.shields.io/badge/SMTP-005FF9?style=for-the-badge&logo=gmail&logoColor=white)
![Kakao OAuth](https://img.shields.io/badge/Kakao_OAuth-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white)

### Database
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

## 📝 API Documentation
```http
http://{server-url}:8080/swagger-ui.html
http://{server-url}:8081/swagger-ui.html
```

## 💡 상세 기능

### 채팅
- 실시간 1:1 채팅
- 채팅방 목록 조회
- 페이징으로 메세지 조회
- 읽지 않은 메시지 카운트
- 이미지 전송 기능
- 채팅방 나가기

### 유저 매칭
- 프로필 추천 (거리 기반)
- 좋아요/싫어요 기능
- 매칭 시 채팅방 자동 생성

### 프로필
- 프로필 이미지 관리
- 기본 정보 설정 (나이, 성별, 주소)
- 관심사 설정
- 자기소개 관리

