# One image, one service: builds the React frontend, bakes it into the Spring Boot jar,
# and serves the site and the API from a single port.
# Build context = repository root (frontend/ and backend/ side by side).

# ---- 1) Frontend: React + Vite -> frontend/dist
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
# Dependencies first so they are cached between builds
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- 2) Backend: Gradle, with the built frontend placed in Spring's static folder
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app/backend
COPY backend/gradlew backend/settings.gradle backend/build.gradle ./
COPY backend/gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null
COPY backend/src src
COPY --from=frontend /app/frontend/dist src/main/resources/static
RUN ./gradlew bootJar -x test --no-daemon

# ---- 3) Runtime: just the JRE and the jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /app/backend/build/libs/*-SNAPSHOT.jar app.jar
USER app

# Render's free instance has 512 MB; let the JVM use most of it
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
