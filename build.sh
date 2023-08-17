#!/bin/bash
./gradlew publishToMavenLocal
docker build -t yti-spring-security:latest .
