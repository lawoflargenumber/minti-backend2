# ---------- Build ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY gradlew /workspace/gradlew
COPY gradle /workspace/gradle
COPY build.gradle settings.gradle /workspace/
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon -g /workspace/.gradle help || true
COPY src /workspace/src
RUN ./gradlew --no-daemon -g /workspace/.gradle clean bootJar -x test

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# 타임존 설정 (alpine)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

COPY --from=build /workspace/build/libs/*.jar /app/app.jar
RUN adduser -D -h /home/spring spring && \
    chown -R spring:spring /app
USER spring

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Seoul" \
    SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
