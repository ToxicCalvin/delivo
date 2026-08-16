# multistage build Dockerfile for a Java 17 Spring Boot application
# stage 1: Build Stage
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY model/pom.xml model/
COPY service/pom.xml service/
COPY delivo-ai-service/pom.xml delivo-ai-service/

RUN mvn dependency:go-offline -B

COPY . .

RUN mvn clean package -DskipTests

# stage 2: Run Stage
# This time we only need a very small Java 17 runtime environment, no need for the bulky Maven
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/service/target/service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
