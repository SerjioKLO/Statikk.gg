FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY api-gateway/mvnw api-gateway/mvnw
COPY api-gateway/.mvn api-gateway/.mvn
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

WORKDIR /workspace/api-gateway
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

