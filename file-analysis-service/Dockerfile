FROM gradle:8.5-jdk21 as builder
WORKDIR /app
COPY . .
RUN gradle :file-analysis-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/file-analysis-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]