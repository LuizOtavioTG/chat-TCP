# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY client/pom.xml client/
COPY server/pom.xml server/

RUN mvn -q -e -B -DskipTests dependency:go-offline

COPY common/src common/src
COPY client/src client/src
COPY server/src server/src

# Compila só o módulo client (e dependências)
RUN mvn -pl client -am -q -e -B -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV HOST=server
ENV PORT=5000
ENV USERNAME=
COPY --from=build /app/client/target/client.jar /app/client.jar
CMD ["java", "-jar", "/app/client.jar"]
