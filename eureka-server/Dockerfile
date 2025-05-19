FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .

RUN ./gradlew :eureka-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/eureka-server/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
