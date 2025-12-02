FROM eclipse-temurin:21-jre

WORKDIR /project_internals/app

COPY /project_internals/target/wikipedia_race-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "com.example.App"]

#arg 4 com.example.
#arg 3 app