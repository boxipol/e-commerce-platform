# syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy full source (needed for multi-module resolution)
COPY . .
# Persist Maven repo across builds
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -pl :gateway-service -am -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/gateway-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]