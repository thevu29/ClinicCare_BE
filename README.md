# Clinic Booking Backend

This is the backend service for a clinic booking website, built using Spring Boot. It provides APIs for managing user authentication, doctor profiles, appointment scheduling, and other core functionalities needed for the booking system.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Database Schema](#database-schema)
- [License](#license)

---

## Features
- User authentication and authorization (OAuth2 with Google Login)
- Doctor profile management
- Appointment scheduling and filtering
- Patient and doctor dashboards
- Integration with a MySQL database
- RESTful APIs for frontend communication

---

## Tech Stack
- **Backend Framework**: Spring Boot
- **Database**: MySQL
- **Authentication**: Spring Security with OAuth2 (Google)
- **Build Tool**: Maven
- **Containerization**: Docker

---

## Getting Started

### Prerequisites
Ensure you have the following installed on your system:
- Java 21 or higher
- Maven
- Docker (for containerized deployment)
- MySQL database

### Clone the Repository
```bash
git clone https://github.com/thevu29/ClinicCare_BE.git
```

### Build the Project
Run the following command to build the project:
```bash
mvn clean install
```

---

## Environment Variables
Create an `application.properties` file based on application-example.properties or set the following environment variables to configure the application:

| Variable                 | Description                              | Example Value                 |
|--------------------------|------------------------------------------|-------------------------------|
| `spring.datasource.url`  | Database connection URL                 | `jdbc:postgresql://localhost:5432/clinic` |
| `spring.datasource.username` | Database username                       | `postgres`                    |
| `spring.datasource.password` | Database password                       | `password`                    |
| `spring.security.oauth2.client.registration.google.client-id`| Google OAuth2 Client ID                 | `your-client-id.apps.googleusercontent.com` |
| `spring.security.oauth2.client.registration.google.client-secret` | Google OAuth2 Client Secret         | `your-client-secret`          |
| `openai.api-key` | OpenAI API Key | `your-key` |
| `spring.mail.username` | Email | `your-email` |
| `spring.mail.password` | Email Passowrd | `your-email-password` |

---

## Running the Application

### With Docker
Use Docker Compose to start the application and its dependencies:
```bash
docker-compose up --build
```

### Without Docker
1. Start your MySQL database.
2. Run the Spring Boot application:
```bash
mvn spring-boot:run
```

---

## Database Schema


---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

