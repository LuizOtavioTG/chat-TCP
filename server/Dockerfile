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
ENV PORT=5000
COPY --from=build /app/target/server-jar-with-dependencies.jar /app/server.jar
EXPOSE 5000
CMD ["java", "-jar", "/app/server.jar"]