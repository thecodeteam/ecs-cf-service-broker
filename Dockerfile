FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/ecs-cf-service-broker-1.2.0-RELEASE.jar app.jar