FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/wikipedia_race-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "com.example.App"]