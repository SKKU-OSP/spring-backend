# ============================
# 1단계: Build stage (Gradle 빌드)
# ============================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 먼저 Gradle 설정/의존성만 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./
# 만약 multi-module이면 필요에 따라 settings.gradle.kts, build.gradle.kts 등 맞춰서 복사
# (프로젝트 구조에 맞게 조정해줘야 함. 예: build.gradle.kts 쓰면 그 파일 이름으로)

# 의존성만 먼저 받아놓기 (캐시용, 빌드 빨라짐)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# 이제 실제 소스 전체 복사
COPY . .

# 실제 jar 빌드
RUN ./gradlew clean bootJar --no-daemon

# ============================
# 2단계: Runtime stage (얇은 JRE로 실행)
# ============================
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# builder 단계에서 생성된 JAR 복사
# build/libs/*.jar 중 실제 스프링 부트 fat jar 하나만 카피
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너에서 노출할 포트
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
