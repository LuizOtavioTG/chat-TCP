# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia apenas os POMs primeiro (melhor cache)
COPY pom.xml .
COPY common/pom.xml common/
COPY client/pom.xml client/
COPY server/pom.xml server/

# Resolve dependências offline
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Agora copia o código fonte
COPY common/src common/src
COPY client/src client/src
COPY server/src server/src

# Compila só o módulo server (e dependências)
RUN mvn -pl server -am -q -e -B -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=5000
COPY --from=build /app/server/target/server.jar /app/server.jar
EXPOSE 5000
CMD ["java", "-jar", "/app/server.jar"]
