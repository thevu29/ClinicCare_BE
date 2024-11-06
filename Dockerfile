# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Grant execute permissions for the Maven wrapper
RUN chmod +x ./mvnw

# Package the application using Maven
RUN ./mvnw clean package -DskipTests

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/ClinicCare-0.0.1-SNAPSHOT.jar"]