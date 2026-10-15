FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY matches/mvnw matches/mvnw
COPY matches/.mvn matches/.mvn
COPY matches/pom.xml matches/pom.xml
COPY matches/src matches/src

WORKDIR /workspace/matches
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/matches/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

