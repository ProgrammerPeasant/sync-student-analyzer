FROM gradle:8.5-jdk21 as builder
WORKDIR /app
COPY . .
RUN gradle :file-storage-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/file-storage-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]