# One build for both apps, as they share the API modules; docker-compose.yml picks the target (emulator, client)

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
# cache mount: Maven downloads the dependencies only once, not on every build
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre AS emulator
COPY --from=build /build/emulator/target/emulator-*-exec.jar /app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app.jar"]

FROM eclipse-temurin:21-jre AS client
COPY --from=build /build/client/target/client-*.jar /app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app.jar"]
