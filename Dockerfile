# Base image as dependency for other modules
FROM gradle:6.9-jdk11

WORKDIR /app

COPY src src
COPY build.gradle .

RUN gradle publishToMavenLocal --no-daemon
