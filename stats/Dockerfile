FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY stats/mvnw stats/mvnw
COPY stats/.mvn stats/.mvn
COPY stats/pom.xml stats/pom.xml
COPY stats/src stats/src

WORKDIR /workspace/stats
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/stats/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

