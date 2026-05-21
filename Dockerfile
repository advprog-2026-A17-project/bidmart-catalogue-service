FROM gradle:8.14.4-jdk21 AS builder

WORKDIR /workspace
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY src src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/build/libs/bidmart-catalogue-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
