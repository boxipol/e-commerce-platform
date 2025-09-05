# docker build -f microservice1/Dockerfile -t microservice-1-app .

# Step 1: Use Maven to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . app/microservice1
COPY ../pom.xml .
WORKDIR app/microservice1

RUN mvn clean package -DskipTests

# Step 2: Use a lightweight JDK 17 image to run the app
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY /microservice1/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]