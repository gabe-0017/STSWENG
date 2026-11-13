# Multi-stage build: same image locally, in CI and on Render.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Download dependencies first so they are cached between builds
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
USER app

# Render's free instance has 512 MB; let the JVM use most of it
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
