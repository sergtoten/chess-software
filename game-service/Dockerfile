# ---------- Etapa 1: compilar (Maven + JDK 27) ----------
FROM maven:3-eclipse-temurin-27 AS build
WORKDIR /app
# Primero solo el pom: si no cambia, Docker reutiliza la capa con las dependencias descargadas
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
# Compila, pasa los tests (si alguno falla, no hay imagen) y copia las dependencias a target/lib
RUN mvn -B package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

# ---------- Etapa 2: ejecutar (solo JRE 27, sin Maven ni código fuente) ----------
FROM eclipse-temurin:27-jre
WORKDIR /app
COPY --from=build /app/target/game-service-1.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/lib ./lib
EXPOSE 8080
CMD ["java", "-cp", "app.jar:lib/*", "software.ulpgc.chess.Main"]
