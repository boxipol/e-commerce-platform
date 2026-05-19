
# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# copy full monorepo (important for multi-module build)
COPY . .
# build ONLY user-service module
RUN mvn clean package -DskipTests -pl :gateway-service -am

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/gateway-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]