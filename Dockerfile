# Use OpenJDK as base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/PointerListener.jar app.jar

# Run the JAR file and allow env vars like BIND_IP
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
