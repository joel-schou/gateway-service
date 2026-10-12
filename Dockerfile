FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gateway-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9003
ENTRYPOINT ["java", "-jar", "app.jar"]