FROM gradle:6.8.0-jdk8 as GradleBuilder

COPY --chown=gradle:gradle build.gradle /home/gradle/src/ecs-cf-service-broker/build.gradle
COPY --chown=gradle:gradle src /home/gradle/src/ecs-cf-service-broker/src

WORKDIR /home/gradle/src/ecs-cf-service-broker

ENV GRADLE_USER_HOME=/home/gradle

RUN gradle clean assemble

# =====================================================================================
FROM openjdk:8-jre-slim
VOLUME /tmp
COPY --from=GradleBuilder /home/gradle/src/ecs-cf-service-broker/build/libs/ecs-cf-service-broker*.jar app.jar
