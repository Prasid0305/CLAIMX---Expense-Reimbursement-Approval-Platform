# CLAIMX – Expense Claim Management System

## Index
- Overview
- Tech Stack
- Project Structure
- Setup Instructions
- Authentication Flow
- API Modules
- Claim Example Response
- Author

---

## Overview
CLAIMX is a backend application for managing employee expense claims.  
Employees can create and submit claims, while managers, admins, and finance teams can review and track them.

---

## Tech Stack
- Backend - Java 17
- Framework - SpringBoot
- Database - PostgreSQL
- Security - Spring Security (JWT)
- OMR - Spring Data JPA (Hibernate)
- Build Tool - Gradle
- API Testing - Swagger, Postman
- Testing - JUnit, Mockito, Integration Testing
- Database visualizer - pgAdmin


---

## Project Structure
```text
src.main.java.com.company.claimx
│
├── annotation
├── aspect
├── config
├── constants
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
└── ClaimxApplication
```
```text

src.main.resources
│
├── application.yml
├── data.sql
└── schema.sql

```

```text
src.test.java.com.company.claimx
│
├── integration
├── service
└── ClaimxApplicationTests


```

```text

src.test.resources
│
└── application-test.yml

```


---

## Setup Instructions
1. Clone the repository - https://github.com/Prasid0305/CLAIMX---Expense-Reimbursement-Approval-Platform.git

2. Installation requirements:
    - Java 17
    - PostgreSQL
    - Gradel
    - Intellij IDEA


3. Create PostgreSql database
   login to Postgres :
    ```text

    CREATE DATABASE claimx;

    ```



4. Configure database in `application.yml`
   ```text
   spring
    application:
        name: claimx
    datasource:
        url: jdbc:postgresql://localhost:5432/claimx
        username: claimx_user
        password: claimx_password
        driver-class-name: org.postgresql.Driver

    jpa:
        hibernate:
        ddl-auto: update 
        show-sql: true 
    ```
5. Run the application:
    ```text

     ./gradlew bootRun

    ```


6. API documentation link:
   ### Swagger:
        http://localhost:8080/swagger-ui/index.html#/

7. Sample claim response

        ```json
        {
        "claimId": 2,
        "claimNumber": "CLM-2026-01019",
        "employeeId": 5,
        "employeeCode": "EMP001",
        "employeeName": "Prasid",
        "managerId": 3,
        "managerName": "Venkat",
        "title": "testing",
        "totalAmount": 25000,
        "status": "PAID",
        "reviewedDate": "2026-03-23T14:35:21.06644",
        "reviewComment": "this claim is valid hence will be approved",
        "createdAt": "2026-03-23T14:33:21.518617",
        "submittedAt": "2026-03-23T14:34:00.481637",
        "items": [
            {
            "itemId": 5,
            "claimId": 2,
            "expenseDate": "2024-01-15",
            "category": "TRAVEL",
            "amount": 8000,
            "description": "Flight to Mumbai"
            },
            {
            "itemId": 6,
            "claimId": 2,
            "expenseDate": "2024-01-16",
            "category": "ACCOMMODATION",
            "amount": 12000,
            "description": "Hotel for 3 nights"
            },
            {
            "itemId": 7,
            "claimId": 2,
            "expenseDate": "2024-01-17",
            "category": "FOOD",
            "amount": 3500,
            "description": "Business dinner"
            },
            {
            "itemId": 8,
            "claimId": 2,
            "expenseDate": "2024-01-18",
            "category": "TRAVEL",
            "amount": 1500,
            "description": "Local taxi"
            }
        ]
        }


        ```




---
## Author

    Prasid Gowda H S
    Software Engineer Apprentice
github: https://github.com/Prasid0305






  



   






