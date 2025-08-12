# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-alpine

# Add maintainer info
LABEL maintainer="CampusCon Team"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Set application's JAR file
ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar

# Add the application's JAR to the container
COPY ${JAR_FILE} app.jar

# Set up environment variables
# Use the production profile inside containers
ENV SPRING_PROFILES_ACTIVE=production
ENV TZ=Asia/Kolkata

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
