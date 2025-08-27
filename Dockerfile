FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
    
COPY . .
    
# --no-daemon prevents Gradle from starting a background daemon, making builds more predictable in CI/CD or container environments.
RUN gradle bootJar --no-daemon
    
# Using multi stage build to decouple build and runtime stages
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
    
COPY --from=builder /app/build/libs/*.jar app.jar
    
EXPOSE 8080
    
ENTRYPOINT ["java", "-jar", "app.jar"]
    