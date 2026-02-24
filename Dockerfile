# ===============================
# Stage 1: Build the application
# ===============================

# Use Maven image with OpenJDK 17 (Temurin build)
# This image contains Maven + full JDK needed to compile the project
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory inside container
WORKDIR /app

# Copy only Maven wrapper and pom first
# This allows Docker to cache dependencies if source code changes
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download all dependencies (cached layer if pom.xml unchanged)
RUN ./mvnw dependency:go-offline

# Copy application source code
COPY src src

# Build the Spring Boot fat jar
# Skip tests for faster Docker builds (remove -DskipTests if needed)
RUN ./mvnw clean package -DskipTests



# ===============================
# Stage 2: Runtime environment
# ===============================

# Use lightweight OpenJDK runtime (no compiler, smaller image)
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the generated jar from build stage
COPY --from=build /app/target/mercato-backend-v0.0.1-SNAPSHOT.jar .

# Expose application port
EXPOSE 6099

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/mercato-backend-v0.0.1-SNAPSHOT.jar"]