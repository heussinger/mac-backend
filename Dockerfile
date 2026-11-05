FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/mac-backend-*.jar app.jar
COPY application.example.yml config/application.yml
EXPOSE 8080
# Datenbankzugang über DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
ENTRYPOINT ["java", "-jar", "app.jar"]
