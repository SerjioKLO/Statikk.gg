FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy parent POM for Maven multi-module resolution
COPY pom.xml .

# Copy service files
COPY excel-parser/mvnw excel-parser/mvnw
COPY excel-parser/.mvn excel-parser/.mvn
COPY excel-parser/pom.xml excel-parser/pom.xml
COPY excel-parser/src excel-parser/src

WORKDIR /workspace/excel-parser
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/excel-parser/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

