FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY player/mvnw player/mvnw
COPY player/.mvn player/.mvn
COPY player/pom.xml player/pom.xml
COPY player/src player/src

WORKDIR /workspace/player
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/player/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

