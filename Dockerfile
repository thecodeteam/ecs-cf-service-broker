FROM gradle:4.10-jdk8 as GradleBuilder

COPY --chown=gradle:gradle build.gradle /home/gradle/src/build.gradle
COPY --chown=gradle:gradle src /home/gradle/src/src

WORKDIR /home/gradle/src

ENV GRADLE_USER_HOME=/home/gradle


RUN gradle clean assemble  --stacktrace

# =====================================================================================
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY --from=GradleBuilder /home/gradle/src/build/libs/ecs-cf-service-broker-*.jar app.jar
