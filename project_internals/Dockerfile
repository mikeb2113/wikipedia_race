FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy main app jar
COPY target/wikipedia_race-1.0-SNAPSHOT.jar app.jar

# Copy all dependency jars (including DuckDB)
COPY target/dependency ./dependency

# Use -cp to include app.jar + all deps
ENTRYPOINT ["java", "-cp", "app.jar:dependency/*", "com.example.server.App"]

#FROM eclipse-temurin:21-jre
#WORKDIR /app

#COPY target/wikipedia_race-1.0-SNAPSHOT.jar app.jar

#ENTRYPOINT ["java", "-jar", "app.jar"]