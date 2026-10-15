FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY registry/mvnw registry/mvnw
COPY registry/.mvn registry/.mvn
COPY registry/pom.xml registry/pom.xml
COPY registry/src registry/src

WORKDIR /workspace/registry
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/registry/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]

