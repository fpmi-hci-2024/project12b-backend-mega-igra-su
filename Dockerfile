FROM openjdk:17-jdk-slim

WORKDIR /appы

COPY target/app-0.0.1-SNAPSHOT.jar app-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "app-0.0.1-SNAPSHOT.jar"]
