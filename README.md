# Clinic Booking Backend

This is the backend service for a clinic booking website, built using Spring Boot. It provides APIs for managing user authentication, doctor profiles, appointment scheduling, and other core functionalities needed for the booking system.

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Firebase Configuration](#firebase-configuration)
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

### Database Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `spring.datasource.url`      | Database connection URL                            | `jdbc:mysql://localhost:3306/clinic`|
| `spring.datasource.username` | Database username                                  | `root`                              |
| `spring.datasource.password` | Database password                                  | `password`                          |
| `spring.datasource.driver-class-name` | Database driver class name                    | `com.mysql.cj.jdbc.Driver`          |

### API Key
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `openai.api-key`             | API key for OpenAI API                      | `your-api-key`                      |

### Mail Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `spring.mail.username`       | Email username                                     | `your-email@gmail.com`              |
| `spring.mail.password`       | Email password                                     | `your-password`                     |

### JWT Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `jwt.secret`                 | Secret key for JWT                                 | `your-secret-key`                   |
| `jwt.expiration`             | JWT expiration time in milliseconds                | `3600000`                           |
| `jwt.refreshExpiration`      | JWT refresh token expiration in milliseconds       | `7200000`                           |

### OAuth2 Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `spring.security.oauth2.client.registration.google.client-id` | Google OAuth2 Client ID            | `your-client-id.apps.googleusercontent.com` |
| `spring.security.oauth2.client.registration.google.client-secret` | Google OAuth2 Client Secret      | `your-client-secret`               |

### Frontend Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `frontend.url`               | Frontend application URL                          | `http://localhost:3000`             |

### VNPay Configuration
| Variable                     | Description                                         | Example Value                       |
|------------------------------|-----------------------------------------------------|-------------------------------------|
| `vnp_TmnCode`                | VNPay terminal code                                | `your-tmn-code`                     |
| `vnp_HashSecret`             | VNPay hash secret key                              | `your-hash-secret`                  |
| `vnp_PayUrl`                 | VNPay payment URL                                  | `https://pay.vnpay.vn`              |
| `vnp_ReturnUrl`              | VNPay return URL                                   | `http://localhost:3000/vnpay-return`|

---

## Firebase Configuration
Create project in https://console.firebase.google.com/u/0/ and create file serviceAccountKey.json

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
Below is the database schema diagram:

![Database Schema](/database_diagram.png)

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

