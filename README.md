# Minti - Spring WebFlux 서버

## 📌 개요
마케터를 위한 AI 에이전트 프로젝트 "Minti"의 보안 서버입니다.
Spring WebFlux 기반의 리액티브 웹 애플리케이션으로, AI 채팅, 프로모션 기획, 디자인 생성 등의 기능을 제공합니다.

## 🛠 기술 스택
- **Backend**: Java 17, Spring Boot 3.3.2, Spring WebFlux
- **Security**: JWT 인증, Azure OAuth2, Spring Security
- **Database**: MongoDB (Reactive), PostgreSQL (R2DBC)
- **Cloud Storage**: Azure Blob Storage
- **Infrastructure**: Docker, Kubernetes, Azure Container Registry
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Gradle
- **External Integration**: FastAPI 서버 연동
- **DevOps**: Github Actions

## ✨ 주요 기능
- **JWT 기반 인증**: Azure OAuth2를 통한 사용자 인증 및 JWT 토큰 발급
- **실시간 스트리밍 채팅**: Server-Sent Events(SSE)를 통한 실시간 AI 채팅
- **프로모션 기획 관리**: AI 기반 마케팅 캠페인 생성 및 관리
- **디자인 업로드 및 생성**: 브랜드/카테고리별 디자인 소재 관리
- **파일 업로드**: Azure Blob Storage 연동 파일 저장
- **FastAPI 연동**: 외부 AI 서버와의 비동기 통신
- **사이드바 네비게이션**: 사용자별 맞춤 메뉴 제공

## 📂 프로젝트 구조
```
src/
├── main/
│   ├── java/com/example/gateway/
│   │   ├── api/                          # REST API 컨트롤러
│   │   │   ├── auth/                     # 인증 관련 API
│   │   │   │   └── AuthController.java  # JWT 인증 처리
│   │   │   ├── chat/                     # 채팅 관련 API
│   │   │   │   └── ChatController.java  # 실시간 채팅 처리
│   │   │   ├── design/                   # 디자인 관련 API
│   │   │   │   └── DesignUploadController.java # 디자인 업로드/생성
│   │   │   ├── plan/                     # 프로모션 기획 API
│   │   │   │   └── PlanController.java  # 프로모션 계획 관리
│   │   │   ├── sidebar/                  # 사이드바 API
│   │   │   │   └── SidebarController.java # 네비게이션 메뉴
│   │   │   ├── board/                    # 게시판 API
│   │   │   │   └── BoardController.java # 게시판 기능
│   │   │   ├── dto/                      # 데이터 전송 객체
│   │   │   └── CommonExceptionHandler.java # 전역 예외 처리
│   │   ├── application/                  # 비즈니스 로직
│   │   │   ├── auth/                     # 인증 서비스
│   │   │   │   ├── AuthServiceAzure.java # Azure OAuth2 인증
│   │   │   │   ├── AuthServiceStub.java  # 테스트용 인증
│   │   │   │   └── JwtService.java       # JWT 토큰 처리
│   │   │   ├── chat/                     # 채팅 서비스
│   │   │   │   └── ChatService.java     # 채팅 비즈니스 로직
│   │   │   ├── design/                   # 디자인 서비스
│   │   │   │   └── DesignService.java   # 디자인 생성/관리
│   │   │   ├── plan/                     # 프로모션 서비스
│   │   │   │   └── PlanService.java     # 프로모션 계획 처리
│   │   │   ├── upload/                   # 업로드 서비스
│   │   │   │   └── UploadService.java   # 파일 업로드 처리
│   │   │   └── sidebar/                  # 사이드바 서비스
│   │   │       └── SidebarService.java  # 메뉴 관리
│   │   ├── config/                       # 설정 클래스
│   │   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   │   ├── JwtAuthFilter.java       # JWT 인증 필터
│   │   │   ├── WebConfig.java           # 웹 설정 (CORS 등)
│   │   │   ├── AzureStorageConfig.java  # Azure Storage 설정
│   │   │   └── MongoTimeConvertersConfig.java # MongoDB 시간 변환
│   │   ├── domain/                       # 도메인 모델
│   │   │   ├── user/                     # 사용자 도메인
│   │   │   │   └── User.java            # 사용자 엔티티
│   │   │   ├── chat/                     # 채팅 도메인
│   │   │   │   ├── Chat.java            # 채팅방 엔티티
│   │   │   │   └── ChatMessage.java     # 채팅 메시지 엔티티
│   │   │   ├── plan/                     # 프로모션 도메인
│   │   │   │   └── Plan.java            # 프로모션 계획 엔티티
│   │   │   └── upload/                   # 업로드 도메인
│   │   │       └── Upload.java          # 파일 업로드 엔티티
│   │   ├── infra/                        # 인프라스트럭처 계층
│   │   │   ├── fastapi/                  # FastAPI 연동
│   │   │   │   └── FastApiClient.java   # 외부 AI 서버 클라이언트
│   │   │   ├── mongo/                    # MongoDB 리포지토리
│   │   │   │   ├── ChatRepository.java  # 채팅 데이터 접근
│   │   │   │   ├── MessageRepository.java # 메시지 데이터 접근
│   │   │   │   ├── PlanRepository.java  # 프로모션 데이터 접근
│   │   │   │   └── UploadRepository.java # 업로드 데이터 접근
│   │   │   └── postgres/                 # PostgreSQL 리포지토리
│   │   │       └── UserRepository.java  # 사용자 데이터 접근
│   │   └── WebfluxGatewayApplication.java # 메인 애플리케이션 클래스
│   └── resources/
│       └── application.yml               # 애플리케이션 설정
k8s/                                      # Kubernetes 배포 설정
├── configmap.yml                         # 환경 변수 설정
├── deployment.yml                        # 애플리케이션 배포
└── service.yml                           # 서비스 노출
```
