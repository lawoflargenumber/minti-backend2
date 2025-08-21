# webflux-gateway

Spring WebFlux 기반의 논블로킹 중계 서버입니다. (인증/JWT 제외, 추후 확장 용이)

## 요구사항
- Java 21
- MongoDB, PostgreSQL
- FastAPI 서버 (SSE 지원)

## 실행
```bash
./gradlew bootRun
```

## 환경변수
- SPRING_DATA_MONGODB_URI
- SPRING_R2DBC_URL, R2DBC_USERNAME, R2DBC_PASSWORD
- FASTAPI_BASE_URL
- UPLOAD_DIR
- CORS_ALLOWED_ORIGINS