# Spring Boot Customer Analysis Application

This is a Spring Boot application for customers analysis, integrated with PostgreSQL, Kafka, and Zookeeper. A MailHog container is present to simulate mail sending. 
The application exposes REST APIs secured with JWT authentication and provides a Swagger UI for interactive API exploration.

---

## Table of Contents

- Getting Started
- Running the Application
- Swagger UI & Authentication
- API Endpoints
    - Authentication
    - Customer Management
- Preconfigured Data
- Notes

---

## Getting Started

### Prerequisites

- Docker
- Docker Compose

Clone this repository and navigate to the project root:

```bash
git clone <repository-url>
cd ROOT_PROJECT
```

---

## Running the Application

Start the application using Docker Compose:

```bash
docker-compose up --build
```

This will start:

- PostgreSQL (port 5432) with database customerdb
- Zookeeper (port 2181)
- Kafka (port 9092)
- Spring Boot Application (port 8080)
- MailHog (port 1025 for STMP, 8025 for user interface)

Logs are persisted in the local logs/ folder.
Data are persisted in the local postgres_data/ folder.

To stop the application:

```bash
docker-compose down
```

---

## Swagger UI & Authentication

Access the Swagger UI at:  
http://localhost:8080/swagger-ui/index.html

### Authenticating on Swagger

1. Click the Authorize button on Swagger UI
2. Enter a JWT token in the following format:  
   Bearer <JWT_TOKEN>

### How to get a JWT Token

Use the /auth/login endpoint with one of the preconfigured users

Request Example:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response Example:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

Copy the token value and use it in Swagger's Authorize dialog.

---

## API Endpoints

### Authentication (/auth)

- Method: POST
- Endpoint: /auth/login
- Description: Authenticate a user and retrieve a JWT token

---

### Customer Service management (/api/customerservice)

Authorization:
- ADMIN – full access
- OPERATOR – can upload file

1. **Upload new CSV**
- Endpoint: POST /api/customerservice/upload
- Roles: ADMIN, OPERATOR
- Description: Uploads a CSV file containing customer services. The CSV is processed and stored in the database. Any duplicate or invalid rows are ignored and logged.
- Request: Multipart file to upload
- Headers required: 

\t - Content-Type: multipart/form-data
\t - Authorization: Bearer <token>

- Response Example:  
\t - 200 OK: File processed successfully
\t - 400 Bad Request: File missing or empty


2. **Generate Customer Service Report**
- Endpoint: GET /api/customerservice/summary
- Roles: ADMIN
- Description: Generates an aggregated Excel report containing the following statistics on customer services:

\t 1.Total active services grouped by service_type
\t 2.Average spending per customer (customer_id)
\t 3.List of customers with more than one expired service (status = EXPIRED)
\t 4.List of customers with services expiring within the next 15 days

- Headers required:

\t - Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
\t - Authorization: Bearer <token>

- Response Example:  

\t - 200 OK: Report generated successfully

\t\t - Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

\t\t - Content-Disposition: attachment; filename=customer_report_<timestamp>.xlsx

\t\t - Body: Excel file in .xlsx format

#### Notes: 
in "Documents" folder an exported Postman collection is present to provide test option outside swagger

## Preconfigured Data

### Customer Service

- Database contains 10 sample customers service.
- In "Documents" folder a example_customers.csv is present and can be used to test upload endpoint.

### Users

- Username: admin  
  Password: admin123  
  Role: ADMIN  
  Permissions: full access (upload, generate report)

- Username: operator  
  Password: operator123  
  Role: OPERATOR  
  Permissions: upload

---

