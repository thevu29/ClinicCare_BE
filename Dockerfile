# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Install curl for downloading wait-for-it
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl netcat-traditional && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src/

# Download wait-for-it script
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /app/
RUN chmod +x wait-for-it.sh

# Build the application
RUN ./mvnw clean package -DskipTests

# Create startup script
RUN echo '#!/bin/bash\n\
./wait-for-it.sh db:3306 -t 60\n\
java -jar target/ClinicCare-0.0.1-SNAPSHOT.jar' > startup.sh && \
    chmod +x startup.sh

EXPOSE 8080

CMD ["./startup.sh"]