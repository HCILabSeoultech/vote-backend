# vote-backend/Dockerfile
FROM openjdk:17-slim
LABEL authors="kohuijae"

WORKDIR /app

# JAR 복사
COPY build/libs/vote-backend-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
