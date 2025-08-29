# ----------- Build stage -----------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -q -B -DskipTests -e -Dstyle.color=never dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests -e -Dstyle.color=never package

# ----------- Runtime stage -----------
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Configure timezone and locale
ENV TZ=UTC

# Minimal runtime dependencies
RUN apk add --no-cache curl bash

WORKDIR /app

# Expose application port
EXPOSE 8080

# Copy built JAR from builder stage
ARG JAR_NAME=backend-0.0.1-SNAPSHOT.jar
COPY --from=builder /workspace/target/${JAR_NAME} app.jar

# Java optimization flags for container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Dfile.encoding=UTF-8"

# Spring profile
ENV SPRING_PROFILES_ACTIVE=production

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1

# Ensure permissions
RUN chown -R appuser:appgroup /app
USER appuser

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
