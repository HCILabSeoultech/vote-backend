# vote-backend/Dockerfile
FROM eclipse-temurin:17-jdk-jammy
LABEL authors="kohuijae"

WORKDIR /app

# JAR 복사
COPY build/libs/vote-backend-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
