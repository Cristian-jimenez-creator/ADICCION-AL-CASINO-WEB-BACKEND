FROM maven:3.10.0-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
COPY game-content game-content
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/simulador-backend-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/game-content ./game-content
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
