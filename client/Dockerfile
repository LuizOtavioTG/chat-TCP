# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV HOST=server
ENV PORT=5000
ENV USERNAME=
COPY --from=build /app/target/client-jar-with-dependencies.jar /app/client.jar
CMD ["java", "-jar", "/app/client.jar"]