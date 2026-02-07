FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/salle-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]