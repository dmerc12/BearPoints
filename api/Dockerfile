# Build stage
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

# Copy maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copy source code
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies and build the application
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
