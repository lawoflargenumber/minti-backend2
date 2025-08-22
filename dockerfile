# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Gradle wrapper & 설정만 먼저 복사 → 의존성 캐시 극대화
COPY gradlew /workspace/gradlew
COPY gradle /workspace/gradle
COPY build.gradle settings.gradle /workspace/
RUN chmod +x ./gradlew

# (선택) 의존성 선캐시: 플러그인/디펜던시만 우선 내려받기
RUN ./gradlew --no-daemon -g /workspace/.gradle dependencies || true

# 소스 복사 후 실제 빌드
COPY src /workspace/src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 타임존 설정 (Asia/Seoul)
ENV TZ=Asia/Seoul
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata curl && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

# 비루트 유저
RUN useradd -ms /bin/bash spring
USER spring

# JAR 배포
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar /app/app.jar

# 기본 포트
EXPOSE 8080

# 컨테이너 튜닝 & Spring 프로필/환경변수
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Seoul" \
    SPRING_PROFILES_ACTIVE=local

# 헬스체크 (Actuator 열려있다는 가정)
HEALTHCHECK --interval=30s --timeout=3s --retries=5 CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]