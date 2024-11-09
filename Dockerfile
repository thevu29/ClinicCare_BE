# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Install curl for downloading wait-for-it
RUN apt-get update && apt-get install -y curl netcat-traditional && rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Download wait-for-it script
RUN curl -o wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && chmod +x wait-for-it.sh

# Package the application using Maven
RUN ./mvnw clean package -DskipTests

# Create startup script within Dockerfile
RUN echo '#!/bin/bash\n\
./wait-for-it.sh db:3306 -t 60\n\
java -jar target/ClinicCare-0.0.1-SNAPSHOT.jar' > startup.sh && chmod +x startup.sh

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/ClinicCare-0.0.1-SNAPSHOT.jar", "./startup.sh"]