FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/springboot-camel-restdsl-api-0.0.1-SNAPSHOT.jar ./springboot-camel-restdsl-api-0.0.1-SNAPSHOT.jar
COPY entrypoint.sh ./entrypoint.sh
