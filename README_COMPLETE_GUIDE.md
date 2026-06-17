# Employee Service Record Management - Complete Project Guide

> A full-stack web application for managing employee service records with CRUD operations, PDF file upload, and bilingual (English/Tamil) PDF report generation.

---

## Technology Stack

| Layer        | Technology                    | Version   | Purpose                                      |
|------------- |-------------------------------|-----------|----------------------------------------------|
| **Frontend** | Angular                       | 21        | Single Page Application (SPA) framework      |
| **Frontend** | TypeScript                    | 5.9       | Type-safe JavaScript superset                |
| **Frontend** | SCSS                          | -         | CSS preprocessor for styling                 |
| **Frontend** | RxJS                          | 7.8       | Reactive programming (HTTP calls, Observables) |
| **Backend**  | Java                          | 17        | Programming language                         |
| **Backend**  | Spring Boot                   | 3.5.15    | REST API framework                           |
| **Backend**  | Spring Data JPA               | -         | ORM for database operations                  |
| **Backend**  | Hibernate                     | -         | JPA implementation                           |
| **Backend**  | Apache PDFBox                 | 2.0.27    | PDF report generation                        |
| **Backend**  | Maven                         | -         | Build tool & dependency management           |
| **Database** | PostgreSQL                    | 15+       | Relational database                          |
| **Tools**    | Eclipse IDE                   | -         | Backend Java development                     |
| **Tools**    | VS Code                       | -         | Frontend Angular development                 |
| **Tools**    | Node.js                       | 20+       | JavaScript runtime for Angular CLI           |
| **Tools**    | Angular CLI                   | 21        | Angular project scaffolding & dev server     |
| **Tools**    | npm                           | 11+       | Node package manager                         |

---

## Features

- Employee CRUD (Create, Read, Update, Delete)
- Appointment Order PDF file upload (max 2MB)
- Bilingual PDF report generation (English + Tamil)
- Search employees by code or name
- Form validation (frontend + backend)
- Responsive UI with clean design
- RESTful API architecture
- Global exception handling

---

## Project Architecture

```
Employee Service Record Management/
│
├── employee-backend/                    ← Spring Boot REST API (Eclipse)
│   └── employee-backend/
│       ├── pom.xml                      ← Maven dependencies
│       └── src/main/
│           ├── java/com/employee/backend/
│           │   ├── EmployeeBackendApplication.java    ← Main class
│           │   ├── controller/
│           │   │   └── EmployeeController.java        ← REST endpoints
│           │   ├── dto/
│           │   │   ├── ApiResponse.java               ← Generic response wrapper
│           │   │   ├── EmployeeRequestDTO.java        ← Input validation
│           │   │   └── EmployeeResponseDTO.java       ← Output format
│           │   ├── entity/
│           │   │   └── Employee.java                  ← Database table mapping
│           │   ├── exception/
│           │   │   ├── GlobalExceptionHandler.java    ← Centralized error handling
│           │   │   ├── EmployeeNotFoundException.java
│           │   │   ├── DuplicateEmployeeCodeException.java
│           │   │   └── InvalidFileException.java
│           │   ├── repository/
│           │   │   └── EmployeeRepository.java        ← Database queries
│           │   └── service/
│           │       ├── EmployeeService.java           ← Service interface
│           │       ├── EmployeeReportService.java     ← PDF generation
│           │       ├── FileStorageService.java        ← File upload handling
│           │       └── impl/
│           │           └── EmployeeServiceImpl.java   ← Business logic
│           └── resources/
│               ├── application.properties             ← App configuration
│               └── fonts/
│                   ├── NotoSansTamil-Regular.ttf      ← Tamil font for PDF title
│                   └── LATHA.TTF                      ← Tamil font for PDF body
│
├── employee-frontend/                   ← Angular SPA (VS Code)
│   ├── package.json                     ← npm dependencies
│   ├── angular.json                     ← Angular CLI config
│   └── src/
│       ├── main.ts                      ← Bootstrap entry point
│       ├── index.html                   ← Root HTML page
│       ├── styles.scss                  ← Global styles
│       └── app/
│           ├── app.ts                   ← Root component
│           ├── app.html                 ← Root template (header + router-outlet)
│           ├── app.scss                 ← Root styles
│           ├── app.routes.ts            ← Route definitions
│           ├── app.config.ts            ← App providers configuration
│           ├── services/
│           │   └── employee.ts          ← HTTP service + Employee interface
│           └── employee/
│               ├── employee-list/       ← List all employees + search
│               ├── employee-create/     ← Create employee form
│               ├── employee-edit/       ← Edit employee form
│               ├── employee-view/       ← View employee details (read-only)
│               └── employee-report/     ← View + download PDF report
│
└── README_COMPLETE_GUIDE.md             ← This file
```

---

## PART 1: DATABASE (PostgreSQL)

### Step 1.1: Install PostgreSQL

Download and install PostgreSQL from the official website. During installation, set the password for the `postgres` user (we use `Sakthi@123` in this project).

### Step 1.2: Create the Database

Open **pgAdmin** or **psql** terminal and run:

```sql
CREATE DATABASE employee_service_db;
```

### Step 1.3: Database Table (For Understanding)

Spring Boot will auto-create this table via Hibernate (`spring.jpa.hibernate.ddl-auto=update`), but here is the equivalent SQL query for your understanding:

```sql
CREATE TABLE employees (
    id                          BIGSERIAL PRIMARY KEY,
    employee_code               VARCHAR(50)   NOT NULL UNIQUE,
    employee_name_english       VARCHAR(200)  NOT NULL,
    employee_name_tamil         VARCHAR(200)  NOT NULL,
    designation                 VARCHAR(100)  NOT NULL,
    department                  VARCHAR(100)  NOT NULL,
    date_of_joining             DATE          NOT NULL,
    mobile_number               VARCHAR(20),
    email                       VARCHAR(200),
    remarks                     VARCHAR(1000),
    appointment_order_path      VARCHAR(500),
    appointment_order_file_name VARCHAR(255),
    created_at                  TIMESTAMP,
    updated_at                  TIMESTAMP,

    CONSTRAINT uk_employee_code UNIQUE (employee_code)
);
```

**Table columns explained:**

| Column                       | Type          | Description                                      |
|-----------------------------|---------------|--------------------------------------------------|
| `id`                        | BIGSERIAL     | Auto-increment primary key                       |
| `employee_code`             | VARCHAR(50)   | Unique employee identifier (e.g., "EMP001")      |
| `employee_name_english`     | VARCHAR(200)  | Employee name in English                         |
| `employee_name_tamil`       | VARCHAR(200)  | Employee name in Tamil                           |
| `designation`               | VARCHAR(100)  | Job title (e.g., Junior Assistant, Manager)      |
| `department`                | VARCHAR(100)  | Department name                                  |
| `date_of_joining`           | DATE          | When the employee joined                         |
| `mobile_number`             | VARCHAR(20)   | 10-digit mobile number                           |
| `email`                     | VARCHAR(200)  | Email address                                    |
| `remarks`                   | VARCHAR(1000) | Additional notes                                 |
| `appointment_order_path`    | VARCHAR(500)  | File path of uploaded appointment order PDF      |
| `appointment_order_file_name` | VARCHAR(255)| Original filename of uploaded PDF                |
| `created_at`                | TIMESTAMP     | Record creation timestamp                        |
| `updated_at`                | TIMESTAMP     | Last update timestamp                            |

### Step 1.4: Sample Insert Query (For Testing)

```sql
INSERT INTO employees (
    employee_code, employee_name_english, employee_name_tamil,
    designation, department, date_of_joining,
    mobile_number, email, remarks, created_at, updated_at
) VALUES (
    'EMP001', 'Sakthi Kousik', 'சக்தி கௌசிக்',
    'Developer', 'IT Department', '2024-01-15',
    '9876543210', 'sakthi@example.com', 'First employee record',
    NOW(), NOW()
);
```

---

## PART 2: BACKEND - Spring Boot (Eclipse IDE)

### Step 2.1: Create Spring Boot Project

1. Go to **https://start.spring.io/**
2. Configure:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.5.15
   - **Group:** `com.employee`
   - **Artifact:** `employee-backend`
   - **Packaging:** Jar
   - **Java:** 17
3. Add dependencies:
   - `Spring Web`
   - `Spring Data JPA`
   - `Spring Boot Validation` (search "Validation")
   - `PostgreSQL Driver`
   - `Lombok`
4. Click **GENERATE** and download the ZIP
5. Extract and open in **Eclipse IDE** → File → Import → Existing Maven Projects

### Step 2.2: Add PDFBox Dependency

Open `pom.xml` and add inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.27</version>
</dependency>
```

### Step 2.3: Complete `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.15</version>
        <relativePath/>
    </parent>
    <groupId>com.employee</groupId>
    <artifactId>employee-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.27</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2.4: `application.properties`

**File:** `src/main/resources/application.properties`

```properties
# --- Server ---
server.port=8080

# --- PostgreSQL datasource ---
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_service_db
spring.datasource.username=postgres
spring.datasource.password=Sakthi@123
spring.datasource.driver-class-name=org.postgresql.Driver

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# --- File upload (we will use this later) ---
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
file.upload-dir=uploads
```

### Step 2.5: Main Application Class

**File:** `src/main/java/com/employee/backend/EmployeeBackendApplication.java`

```java
package com.employee.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeBackendApplication.class, args);
    }
}
```

### Step 2.6: Entity Class (Database Table Mapping)

**File:** `src/main/java/com/employee/backend/entity/Employee.java`

```java
package com.employee.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "employee_code")
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(name = "employee_name_english", nullable = false, length = 200)
    private String employeeNameEnglish;

    @Column(name = "employee_name_tamil", nullable = false, length = 200)
    private String employeeNameTamil;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "appointment_order_path", length = 500)
    private String appointmentOrderPath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "appointment_order_file_name", length = 255)
    private String appointmentOrderFileName;

    // Constructors
    public Employee() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeNameEnglish() {
        return employeeNameEnglish;
    }

    public void setEmployeeNameEnglish(String employeeNameEnglish) {
        this.employeeNameEnglish = employeeNameEnglish;
    }

    public String getEmployeeNameTamil() {
        return employeeNameTamil;
    }

    public void setEmployeeNameTamil(String employeeNameTamil) {
        this.employeeNameTamil = employeeNameTamil;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAppointmentOrderPath() {
        return appointmentOrderPath;
    }

    public void setAppointmentOrderPath(String appointmentOrderPath) {
        this.appointmentOrderPath = appointmentOrderPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAppointmentOrderFileName() {
        return appointmentOrderFileName;
    }

    public void setAppointmentOrderFileName(String appointmentOrderFileName) {
        this.appointmentOrderFileName = appointmentOrderFileName;
    }
}
```

### Step 2.7: DTOs (Data Transfer Objects)

#### ApiResponse.java (Generic Response Wrapper)

**File:** `src/main/java/com/employee/backend/dto/ApiResponse.java`

```java
package com.employee.backend.dto;

public class ApiResponse<T> {

    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
```

#### EmployeeRequestDTO.java (Input with Validation)

**File:** `src/main/java/com/employee/backend/dto/EmployeeRequestDTO.java`

```java
package com.employee.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class EmployeeRequestDTO {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Employee name (English) is required")
    private String employeeNameEnglish;

    @NotBlank(message = "Employee name (Tamil) is required")
    private String employeeNameTamil;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Email(message = "Invalid email address")
    private String email;

    private String remarks;

    // Getters and Setters

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeNameEnglish() {
        return employeeNameEnglish;
    }

    public void setEmployeeNameEnglish(String employeeNameEnglish) {
        this.employeeNameEnglish = employeeNameEnglish;
    }

    public String getEmployeeNameTamil() {
        return employeeNameTamil;
    }

    public void setEmployeeNameTamil(String employeeNameTamil) {
        this.employeeNameTamil = employeeNameTamil;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
```

#### EmployeeResponseDTO.java (Output Format)

**File:** `src/main/java/com/employee/backend/dto/EmployeeResponseDTO.java`

```java
package com.employee.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponseDTO {

    private Long id;
    private String employeeCode;
    private String employeeNameEnglish;
    private String employeeNameTamil;
    private String designation;
    private String department;
    private LocalDate dateOfJoining;
    private String mobileNumber;
    private String email;
    private String remarks;
    private String appointmentOrderPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String appointmentOrderFileName;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeNameEnglish() {
        return employeeNameEnglish;
    }

    public void setEmployeeNameEnglish(String employeeNameEnglish) {
        this.employeeNameEnglish = employeeNameEnglish;
    }

    public String getEmployeeNameTamil() {
        return employeeNameTamil;
    }

    public void setEmployeeNameTamil(String employeeNameTamil) {
        this.employeeNameTamil = employeeNameTamil;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAppointmentOrderPath() {
        return appointmentOrderPath;
    }

    public void setAppointmentOrderPath(String appointmentOrderPath) {
        this.appointmentOrderPath = appointmentOrderPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAppointmentOrderFileName() {
        return appointmentOrderFileName;
    }

    public void setAppointmentOrderFileName(String appointmentOrderFileName) {
        this.appointmentOrderFileName = appointmentOrderFileName;
    }
}
```

### Step 2.8: Repository (Database Queries)

**File:** `src/main/java/com/employee/backend/repository/EmployeeRepository.java`

```java
package com.employee.backend.repository;

import com.employee.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);
}
```

### Step 2.9: Custom Exceptions

#### EmployeeNotFoundException.java

**File:** `src/main/java/com/employee/backend/exception/EmployeeNotFoundException.java`

```java
package com.employee.backend.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
```

#### DuplicateEmployeeCodeException.java

**File:** `src/main/java/com/employee/backend/exception/DuplicateEmployeeCodeException.java`

```java
package com.employee.backend.exception;

public class DuplicateEmployeeCodeException extends RuntimeException {

    public DuplicateEmployeeCodeException(String message) {
        super(message);
    }
}
```

#### InvalidFileException.java

**File:** `src/main/java/com/employee/backend/exception/InvalidFileException.java`

```java
package com.employee.backend.exception;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
```

#### GlobalExceptionHandler.java (Centralized Error Handling)

**File:** `src/main/java/com/employee/backend/exception/GlobalExceptionHandler.java`

```java
package com.employee.backend.exception;

import com.employee.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateEmployeeCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmployeeCode(DuplicateEmployeeCodeException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder("Validation failed: ");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            sb.append(fieldError.getField())
                    .append(" - ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        ApiResponse<Void> response = new ApiResponse<>(sb.toString(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(InvalidFileException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>("Unexpected error: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### Step 2.10: Service Layer

#### EmployeeService.java (Interface)

**File:** `src/main/java/com/employee/backend/service/EmployeeService.java`

```java
package com.employee.backend.service;

import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Long id);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO);

    EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName);

    void deleteEmployee(Long id);
}
```

#### EmployeeServiceImpl.java (Business Logic)

**File:** `src/main/java/com/employee/backend/service/impl/EmployeeServiceImpl.java`

```java
package com.employee.backend.service.impl;

import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;
import com.employee.backend.entity.Employee;
import com.employee.backend.exception.DuplicateEmployeeCodeException;
import com.employee.backend.exception.EmployeeNotFoundException;
import com.employee.backend.repository.EmployeeRepository;
import com.employee.backend.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) {
            throw new DuplicateEmployeeCodeException(
                "Employee code already exists: " + requestDTO.getEmployeeCode());
        }

        Employee employee = new Employee();
        mapRequestToEntity(requestDTO, employee);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        Employee saved = employeeRepository.save(employee);
        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                    "Employee not found with id: " + id));
        return mapEntityToResponse(employee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                    "Employee not found with id: " + id));

        if (!employee.getEmployeeCode().equals(requestDTO.getEmployeeCode())
                && employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) {
            throw new DuplicateEmployeeCodeException(
                "Employee code already exists: " + requestDTO.getEmployeeCode());
        }

        mapRequestToEntity(requestDTO, employee);
        employee.setUpdatedAt(LocalDateTime.now());

        Employee updated = employeeRepository.save(employee);
        return mapEntityToResponse(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                    "Employee not found with id: " + id));
        employeeRepository.deleteById(employee.getId());
    }

    @Override
    public EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                    "Employee not found with id: " + id));

        employee.setAppointmentOrderPath(filePath);
        employee.setAppointmentOrderFileName(fileName);
        employee.setUpdatedAt(LocalDateTime.now());

        Employee updated = employeeRepository.save(employee);
        return mapEntityToResponse(updated);
    }

    private void mapRequestToEntity(EmployeeRequestDTO requestDTO, Employee employee) {
        employee.setEmployeeCode(requestDTO.getEmployeeCode());
        employee.setEmployeeNameEnglish(requestDTO.getEmployeeNameEnglish());
        employee.setEmployeeNameTamil(requestDTO.getEmployeeNameTamil());
        employee.setDesignation(requestDTO.getDesignation());
        employee.setDepartment(requestDTO.getDepartment());
        employee.setDateOfJoining(requestDTO.getDateOfJoining());
        employee.setMobileNumber(requestDTO.getMobileNumber());
        employee.setEmail(requestDTO.getEmail());
        employee.setRemarks(requestDTO.getRemarks());
    }

    private EmployeeResponseDTO mapEntityToResponse(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setEmployeeNameEnglish(employee.getEmployeeNameEnglish());
        dto.setEmployeeNameTamil(employee.getEmployeeNameTamil());
        dto.setDesignation(employee.getDesignation());
        dto.setDepartment(employee.getDepartment());
        dto.setDateOfJoining(employee.getDateOfJoining());
        dto.setMobileNumber(employee.getMobileNumber());
        dto.setEmail(employee.getEmail());
        dto.setRemarks(employee.getRemarks());
        dto.setAppointmentOrderPath(employee.getAppointmentOrderPath());
        dto.setAppointmentOrderFileName(employee.getAppointmentOrderFileName());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        return dto;
    }
}
```

#### FileStorageService.java (File Upload Handling)

**File:** `src/main/java/com/employee/backend/service/FileStorageService.java`

```java
package com.employee.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + this.uploadDir, ex);
        }
    }

    public String saveAppointmentOrderFile(Long employeeId, MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        String fileName = "employee_" + employeeId + "_appointment" + fileExtension;

        Path targetLocation = this.uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation,
                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }
}
```

#### EmployeeReportService.java (PDF Generation with Tamil Support)

**File:** `src/main/java/com/employee/backend/service/EmployeeReportService.java`

```java
package com.employee.backend.service;

import com.employee.backend.entity.Employee;
import com.employee.backend.exception.EmployeeNotFoundException;
import com.employee.backend.repository.EmployeeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;

    private static final float MARGIN      = 50f;
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
    private static final float COL1_WIDTH  = 200f;
    private static final float COL2_WIDTH  = TABLE_WIDTH - COL1_WIDTH;
    private static final float ROW_HEIGHT  = 22f;
    private static final float CELL_PAD    = 5f;
    private static final int   NUM_ROWS    = 9;

    public EmployeeReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public byte[] generateEmployeeReport(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(
                    "Employee not found with id: " + id));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            Font awtTamil14 = loadAwtFont("fonts/NotoSansTamil-Regular.ttf", 14f);
            Font awtLatha11 = loadAwtFont("fonts/LATHA.TTF", 11f);

            String joiningDate = employee.getDateOfJoining() != null
                    ? employee.getDateOfJoining().toString() : "";

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = PAGE_HEIGHT - MARGIN;

                // English title
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Employee Service Record Report");
                cs.endText();
                y -= 26f;

                // Tamil title
                drawTamil(cs, "பணியாளர் சேவை விவர அறிக்கை", awtTamil14, MARGIN, y);
                y -= 30f;

                drawTableGrid(cs, y);

                String[][] rows = {
                    {"Employee Code",           safe(employee.getEmployeeCode()),        "en"},
                    {"Employee Name (English)", safe(employee.getEmployeeNameEnglish()), "en"},
                    {"Employee Name (Tamil)",   safe(employee.getEmployeeNameTamil()),   "ta"},
                    {"Designation",             safe(employee.getDesignation()),         "en"},
                    {"Department",              safe(employee.getDepartment()),          "en"},
                    {"Date of Joining",         joiningDate,                            "en"},
                    {"Mobile Number",           safe(employee.getMobileNumber()),        "en"},
                    {"Email",                   safe(employee.getEmail()),               "en"},
                    {"Remarks",                 safe(employee.getRemarks()),             "en"},
                };

                for (String[] r : rows) {
                    rowText(cs, r[0], r[1], "ta".equals(r[2]), awtLatha11, y);
                    y -= ROW_HEIGHT;
                }
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate employee report PDF", ex);
        }
    }

    private Font loadAwtFont(String classpathPath, float size) throws Exception {
        ClassPathResource res = new ClassPathResource(classpathPath);
        try (InputStream is = res.getInputStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
        }
    }

    private void drawTableGrid(PDPageContentStream cs, float firstRowBottom) throws IOException {
        float tableTop    = firstRowBottom + ROW_HEIGHT;
        float tableBottom = firstRowBottom - (NUM_ROWS - 1) * ROW_HEIGHT;
        float left        = MARGIN;
        float right       = MARGIN + TABLE_WIDTH;
        float divider     = MARGIN + COL1_WIDTH;

        cs.setLineWidth(0.5f);

        for (int i = 0; i <= NUM_ROWS; i++) {
            float lineY = tableTop - i * ROW_HEIGHT;
            cs.moveTo(left,  lineY);
            cs.lineTo(right, lineY);
        }

        cs.moveTo(left,     tableTop); cs.lineTo(left,     tableBottom);
        cs.moveTo(divider,  tableTop); cs.lineTo(divider,  tableBottom);
        cs.moveTo(right,    tableTop); cs.lineTo(right,    tableBottom);

        cs.stroke();
    }

    private void rowText(PDPageContentStream cs, String label, String value,
                         boolean valueTamil, Font tamilFont, float y) throws IOException {
        float textY = y + CELL_PAD + 2f;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(MARGIN + CELL_PAD, textY);
        cs.showText(label);
        cs.endText();

        if (valueTamil) {
            drawTamil(cs, value, tamilFont, MARGIN + COL1_WIDTH + CELL_PAD, textY);
        } else {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN + COL1_WIDTH + CELL_PAD, textY);
            cs.showText(value);
            cs.endText();
        }
    }

    private void drawTamil(PDPageContentStream cs, String text,
                            Font awtFont, float pdfX, float pdfY) throws IOException {
        if (text == null || text.isEmpty()) return;

        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        Shape outline = new TextLayout(text, awtFont, frc).getOutline(null);

        cs.setNonStrokingColor(0, 0, 0);

        PathIterator pi = outline.getPathIterator(null);
        float[] c = new float[6];
        float cx = 0, cy = 0;
        boolean hasPath = false;

        while (!pi.isDone()) {
            int type = pi.currentSegment(c);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    cs.moveTo(pdfX + c[0], pdfY - c[1]);
                    cx = c[0]; cy = c[1];
                    hasPath = true;
                    break;
                case PathIterator.SEG_LINETO:
                    cs.lineTo(pdfX + c[0], pdfY - c[1]);
                    cx = c[0]; cy = c[1];
                    break;
                case PathIterator.SEG_QUADTO: {
                    float qx = c[0], qy = c[1], ex = c[2], ey = c[3];
                    float bx1 = cx + 2f/3f*(qx-cx), by1 = cy + 2f/3f*(qy-cy);
                    float bx2 = ex + 2f/3f*(qx-ex), by2 = ey + 2f/3f*(qy-ey);
                    cs.curveTo(pdfX+bx1, pdfY-by1, pdfX+bx2, pdfY-by2, pdfX+ex, pdfY-ey);
                    cx = ex; cy = ey;
                    break;
                }
                case PathIterator.SEG_CUBICTO:
                    cs.curveTo(pdfX+c[0], pdfY-c[1], pdfX+c[2], pdfY-c[3],
                               pdfX+c[4], pdfY-c[5]);
                    cx = c[4]; cy = c[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    cs.closePath();
                    break;
            }
            pi.next();
        }

        if (hasPath) cs.fill();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
```

> **Note:** For Tamil font support, download `NotoSansTamil-Regular.ttf` from Google Fonts and `LATHA.TTF` from your Windows fonts folder (`C:\Windows\Fonts\LATHA.TTF`). Place them in `src/main/resources/fonts/`.

### Step 2.11: Controller (REST API Endpoints)

**File:** `src/main/java/com/employee/backend/controller/EmployeeController.java`

```java
package com.employee.backend.controller;

import com.employee.backend.dto.ApiResponse;
import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;
import com.employee.backend.exception.InvalidFileException;
import com.employee.backend.service.EmployeeReportService;
import com.employee.backend.service.EmployeeService;
import com.employee.backend.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;
    private final EmployeeReportService employeeReportService;

    public EmployeeController(EmployeeService employeeService,
                              FileStorageService fileStorageService,
                              EmployeeReportService employeeReportService) {
        this.employeeService = employeeService;
        this.fileStorageService = fileStorageService;
        this.employeeReportService = employeeReportService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {

        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO);
        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee created successfully", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees() {

        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        ApiResponse<List<EmployeeResponseDTO>> response =
                new ApiResponse<>("Employee list fetched successfully", employees);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(
            @PathVariable Long id) {

        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee fetched successfully", employee);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {

        EmployeeResponseDTO updated = employeeService.updateEmployee(id, requestDTO);
        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee updated successfully", updated);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> uploadAppointmentOrder(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        long maxSizeBytes = 2L * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException("File size must be <= 2 MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("Only PDF files are allowed");
        }

        String savedPath = fileStorageService.saveAppointmentOrderFile(id, file);

        EmployeeResponseDTO updated =
                employeeService.updateAppointmentOrder(id, savedPath, originalFileName);

        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Appointment order uploaded successfully", updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        ApiResponse<String> response =
                new ApiResponse<>("Employee deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<byte[]> generateEmployeeReport(@PathVariable Long id) {

        byte[] pdfBytes = employeeReportService.generateEmployeeReport(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "employee_report_" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
```

### Step 2.12: Run the Backend

In Eclipse:
1. Right-click `EmployeeBackendApplication.java`
2. Run As → Java Application
3. Backend starts at `http://localhost:8080`

### Backend API Endpoints Summary

| Method   | URL                             | Description                  | Request Body       |
|----------|--------------------------------|------------------------------|--------------------|
| `POST`   | `/api/employees/create`         | Create new employee          | JSON (EmployeeRequestDTO) |
| `GET`    | `/api/employees/list`           | Get all employees            | -                  |
| `GET`    | `/api/employees/{id}`           | Get employee by ID           | -                  |
| `PUT`    | `/api/employees/update/{id}`    | Update employee              | JSON (EmployeeRequestDTO) |
| `POST`   | `/api/employees/upload/{id}`    | Upload appointment order PDF | Multipart file     |
| `DELETE` | `/api/employees/delete/{id}`    | Delete employee              | -                  |
| `GET`    | `/api/employees/report/{id}`    | Download PDF report          | -                  |

---

## PART 3: FRONTEND - Angular (VS Code)

### Step 3.1: Prerequisites

Make sure you have these installed:

```bash
# Check Node.js version (need 20+)
node --version

# Check npm version
npm --version

# Install Angular CLI globally
npm install -g @angular/cli
```

### Step 3.2: Create Angular Project

Open terminal in VS Code and run:

```bash
# Create new Angular project
ng new employee-frontend --style=scss --ssr=true --routing=true

# Navigate into the project
cd employee-frontend

# Open in VS Code
code .
```

### Step 3.3: Generate Components and Service

Run these commands inside the `employee-frontend` folder:

```bash
# Generate the employee service
ng generate service services/employee

# Generate components
ng generate component employee/employee-list
ng generate component employee/employee-create
ng generate component employee/employee-edit
ng generate component employee/employee-view
ng generate component employee/employee-report
```

### Step 3.4: App Configuration

#### `src/app/app.config.ts`

```typescript
import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
  ],
};
```

#### `src/app/app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { EmployeeListComponent } from './employee/employee-list/employee-list';
import { EmployeeCreateComponent } from './employee/employee-create/employee-create';
import { EmployeeEditComponent } from './employee/employee-edit/employee-edit';
import { EmployeeViewComponent } from './employee/employee-view/employee-view';
import { EmployeeReportComponent } from './employee/employee-report/employee-report';

export const routes: Routes = [
  { path: '', redirectTo: 'employee/list', pathMatch: 'full' },
  { path: 'employee/list', component: EmployeeListComponent },
  { path: 'employee/create', component: EmployeeCreateComponent },
  { path: 'employee/edit/:id', component: EmployeeEditComponent },
  { path: 'employee/view/:id', component: EmployeeViewComponent },
  { path: 'employee/report/:id', component: EmployeeReportComponent },
];
```

### Step 3.5: Root Component

#### `src/app/app.ts`

```typescript
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  title = 'employee-frontend';
}
```

#### `src/app/app.html`

```html
<div class="app-container">
  <header class="header">
    <h1>Employee Service Record Management</h1>

    <nav class="nav-links">
      <a routerLink="/employee/list">Employee List</a>
      <a routerLink="/employee/create">Add Employee</a>
    </nav>
  </header>

  <main class="main-content">
    <router-outlet></router-outlet>
  </main>
</div>
```

#### `src/app/app.scss`

```scss
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

.app-container {
  width: 90%;
  margin: 0 auto;
}

.header {
  background-color: #1976d2;
  color: white;
  padding: 20px;
  margin-top: 20px;
  border-radius: 8px;
}

.header h1 {
  margin: 0 0 10px 0;
}

.nav-links a {
  color: white;
  text-decoration: none;
  margin-right: 20px;
  font-weight: bold;
}

.nav-links a:hover {
  text-decoration: underline;
}

.main-content {
  margin-top: 20px;
}
```

### Step 3.6: Employee Service (HTTP Calls + Interface)

#### `src/app/services/employee.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Employee {
  id?: number;
  employeeCode: string;
  employeeNameEnglish: string;
  employeeNameTamil: string;
  designation: string;
  department: string;
  dateOfJoining: string;
  mobileNumber: string;
  email: string;
  remarks: string;
  appointmentOrderFileName?: string;
}

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private baseUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  getAllEmployees(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/list`);
  }

  getEmployeeById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${this.baseUrl}/create`, employee);
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/update/${id}`, employee);
  }

  uploadAppointmentOrder(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}/upload/${id}`, formData);
  }

  deleteEmployee(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/delete/${id}`);
  }

  downloadEmployeeReport(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/report/${id}`, {
      responseType: 'blob',
    });
  }
}
```

### Step 3.7: Employee List Component

#### `src/app/employee/employee-list/employee-list.ts`

```typescript
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeListComponent implements OnInit {
  employees: any[] = [];
  searchTerm: string = '';
  successMessage: string = '';
  errorMessage: string = '';
  showDeleteButton = false;

  constructor(
    private employeeService: EmployeeService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    this.successMessage =
      navigation?.extras?.state?.['successMessage'] ||
      (typeof window !== 'undefined' ? window.history.state?.successMessage : '') ||
      '';

    this.loadEmployees();

    if (this.successMessage) {
      setTimeout(() => {
        this.successMessage = '';
        this.cdr.markForCheck();
      }, 3000);
    }
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe(
      (response: any) => {
        if (Array.isArray(response)) {
          this.employees = response;
        } else if (response && Array.isArray(response.data)) {
          this.employees = response.data;
        } else {
          this.employees = [];
        }
        this.errorMessage = '';
        this.cdr.markForCheck();
      },
      (error: any) => {
        this.errorMessage = 'Failed to load employees';
        this.employees = [];
        this.cdr.markForCheck();
      },
    );
  }

  goToReport(id: number | undefined): void {
    if (id == null) return;
    this.router.navigate(['/employee/report', id]);
  }

  get filteredEmployees(): any[] {
    const value = this.searchTerm.trim().toLowerCase();

    const sorted = [...this.employees].sort((a, b) =>
      (a.employeeCode || '').localeCompare(b.employeeCode || '', undefined, {
        numeric: true,
      }),
    );

    if (!value) {
      return sorted;
    }

    return sorted.filter(
      (employee: any) =>
        (employee.employeeCode || '').toLowerCase().includes(value) ||
        (employee.employeeNameEnglish || '').toLowerCase().includes(value) ||
        (employee.employeeNameTamil || '').toLowerCase().includes(value),
    );
  }

  goToAddEmployee(): void {
    this.router.navigate(['/employee/create']);
  }

  goToViewEmployee(id: number): void {
    this.router.navigate(['/employee/view', id]);
  }

  goToEditEmployee(id: number): void {
    this.router.navigate(['/employee/edit', id]);
  }

  deleteEmployee(id: number | undefined): void {
    if (id == null) return;
    if (!window.confirm('Are you sure you want to permanently delete this employee?'))
      return;
    this.employeeService.deleteEmployee(id).subscribe(
      () => {
        this.successMessage = 'Employee deleted successfully';
        this.loadEmployees();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 3000);
      },
      (error: any) => {
        this.errorMessage = 'Failed to delete employee';
        this.cdr.markForCheck();
      },
    );
  }
}
```

#### `src/app/employee/employee-list/employee-list.html`

```html
<div class="employee-list-container">
  <div class="page-header">
    <h2>Employee List</h2>
    <button class="add-btn" type="button" (click)="goToAddEmployee()">Add Employee</button>
  </div>

  <div class="search-box">
    <input
      type="text"
      [(ngModel)]="searchTerm"
      placeholder=" Search by Employee Name or Employee Code"
      class="search-input"
    />
  </div>

  <div *ngIf="successMessage" class="success-message">
    {{ successMessage }}
  </div>

  <div *ngIf="errorMessage" class="error-message">
    {{ errorMessage }}
  </div>

  <div *ngIf="filteredEmployees.length > 0; else noEmployees" class="table-wrapper">
    <table class="employee-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Employee Code</th>
          <th>Name English</th>
          <th>Name Tamil</th>
          <th>Designation</th>
          <th>Department</th>
          <th>Date of Joining</th>
          <th>Mobile</th>
          <th>Email</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let employee of filteredEmployees">
          <td>{{ employee.id }}</td>
          <td>{{ employee.employeeCode }}</td>
          <td>{{ employee.employeeNameEnglish }}</td>
          <td>{{ employee.employeeNameTamil }}</td>
          <td>{{ employee.designation }}</td>
          <td>{{ employee.department }}</td>
          <td>{{ employee.dateOfJoining }}</td>
          <td>{{ employee.mobileNumber }}</td>
          <td>{{ employee.email }}</td>
          <td class="actions">
            <button class="view-btn" type="button" (click)="goToViewEmployee(employee.id)">
              View
            </button>
            <button class="edit-btn" type="button" (click)="goToEditEmployee(employee.id)">
              Edit
            </button>
            <button class="report-btn" type="button" (click)="goToReport(employee.id)">
              Report
            </button>
            @if (showDeleteButton) {
              <button class="delete-btn" type="button" (click)="deleteEmployee(employee.id)">
                Delete
              </button>
            }
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <ng-template #noEmployees>
    <div class="no-data">No employees found.</div>
  </ng-template>
</div>
```

#### `src/app/employee/employee-list/employee-list.scss`

```scss
.employee-list-container {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #222;
}

.add-btn {
  background-color: #1976d2;
  color: #fff;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  background-color: #125ca1;
}

.search-box {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  max-width: 600px;
  padding: 10px 12px;
  border: 1px solid #bdbdbd;
  border-radius: 6px;
  font-size: 15px;
}

.search-input:focus {
  outline: none;
  border-color: #1976d2;
}

.success-message {
  color: #2e7d32;
  margin-bottom: 16px;
  font-weight: 500;
  background-color: #e8f5e9;
  border: 1px solid #c8e6c9;
  padding: 10px 12px;
  border-radius: 6px;
}

.error-message {
  color: #d32f2f;
  margin-bottom: 16px;
  font-weight: 500;
}

.table-wrapper {
  overflow-x: auto;
}

.employee-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.employee-table th,
.employee-table td {
  border: 1px solid #ddd;
  padding: 12px 10px;
  text-align: left;
  vertical-align: middle;
}

.employee-table th {
  background-color: #f5f5f5;
  font-weight: 600;
}

.actions {
  white-space: nowrap;
}

.view-btn,
.edit-btn,
.report-btn,
.delete-btn {
  border: none;
  padding: 7px 12px;
  border-radius: 5px;
  cursor: pointer;
  color: #fff;
  margin-right: 8px;
  font-size: 13px;
}

.view-btn { background-color: #2e7d32; }
.view-btn:hover { background-color: #1b5e20; }
.edit-btn { background-color: #f57c00; }
.edit-btn:hover { background-color: #e65100; }
.report-btn { background-color: #6f42c1; }
.report-btn:hover { background-color: #59359c; }
.delete-btn { background-color: #c62828; }
.delete-btn:hover { background-color: #a31515; }

.no-data {
  margin-top: 20px;
  font-size: 16px;
  color: #555;
}
```

### Step 3.8: Employee Create Component

#### `src/app/employee/employee-create/employee-create.ts`

```typescript
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Employee, EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-employee-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './employee-create.html',
  styleUrl: './employee-create.scss',
})
export class EmployeeCreateComponent implements OnInit {
  employeeForm!: FormGroup;
  selectedFile: File | null = null;
  successMessage: string = '';
  errorMessage: string = '';

  designations: string[] = [
    'Junior Assistant',
    'Senior Assistant',
    'Developer',
    'Senior Developer',
    'Manager',
    'Administrator',
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({
      employeeCode: ['', Validators.required],
      employeeNameEnglish: ['', Validators.required],
      employeeNameTamil: ['', Validators.required],
      designation: ['', Validators.required],
      department: ['', Validators.required],
      dateOfJoining: ['', Validators.required],
      mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      email: ['', [Validators.required, Validators.email]],
      remarks: [''],
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (file.type !== 'application/pdf') {
      this.errorMessage = 'Only PDF files are allowed.';
      this.selectedFile = null;
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = 'File size must be less than or equal to 2 MB.';
      this.selectedFile = null;
      return;
    }

    this.errorMessage = '';
    this.selectedFile = file;
  }

  onSubmit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    const employeeData: Employee = this.employeeForm.value;

    this.employeeService.createEmployee(employeeData).subscribe({
      next: (response: any) => {
        const created = response?.data ? response.data : response;

        if (this.selectedFile && created?.id) {
          this.uploadFile(created.id);
        } else {
          this.router.navigate(['/employee/list'], {
            state: { successMessage: 'Employee created successfully.' },
          });
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Failed to create employee. Employee code may already exist.';
        this.cdr.markForCheck();
      },
    });
  }

  private uploadFile(employeeId: number): void {
    this.employeeService.uploadAppointmentOrder(employeeId, this.selectedFile!).subscribe({
      next: () => {
        this.router.navigate(['/employee/list'], {
          state: { successMessage: 'Employee created successfully.' },
        });
      },
      error: () => {
        this.errorMessage = 'Employee saved, but file upload failed.';
        this.cdr.markForCheck();
      },
    });
  }

  goToList(): void {
    this.router.navigate(['/employee/list']);
  }

  get f() {
    return this.employeeForm.controls;
  }
}
```

#### `src/app/employee/employee-create/employee-create.html`

```html
<div class="form-container">
  <h2>Create Employee</h2>

  @if (successMessage) {
    <p class="success-message">{{ successMessage }}</p>
  }
  @if (errorMessage) {
    <p class="error-message">{{ errorMessage }}</p>
  }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      @if (f['employeeCode'].touched && f['employeeCode'].invalid) {
        <small class="error">Employee Code is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Employee Name English</label>
      <input type="text" formControlName="employeeNameEnglish" />
      @if (f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid) {
        <small class="error">Employee Name English is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Employee Name Tamil</label>
      <input type="text" formControlName="employeeNameTamil" />
      @if (f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid) {
        <small class="error">Employee Name Tamil is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <option value="">-- Select Designation --</option>
        @for (designation of designations; track designation) {
          <option [value]="designation">{{ designation }}</option>
        }
      </select>
      @if (f['designation'].touched && f['designation'].invalid) {
        <small class="error">Designation is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Department</label>
      <input type="text" formControlName="department" />
      @if (f['department'].touched && f['department'].invalid) {
        <small class="error">Department is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Date of Joining</label>
      <input type="date" formControlName="dateOfJoining" />
      @if (f['dateOfJoining'].touched && f['dateOfJoining'].invalid) {
        <small class="error">Date of Joining is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['required']) {
        <small class="error">Mobile Number is required.</small>
      }
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern']) {
        <small class="error">Mobile Number must be exactly 10 digits.</small>
      }
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      @if (f['email'].touched && f['email'].errors?.['required']) {
        <small class="error">Email is required.</small>
      }
      @if (f['email'].touched && f['email'].errors?.['email']) {
        <small class="error">Enter a valid email address.</small>
      }
    </div>

    <div class="form-group">
      <label>Remarks</label>
      <textarea formControlName="remarks" rows="4"></textarea>
    </div>

    <div class="form-group">
      <label>Appointment Order (PDF only)</label>
      <input type="file" accept="application/pdf" (change)="onFileChange($event)" />
    </div>

    <div class="button-group">
      <button type="submit">Save Employee</button>
      <button type="button" class="cancel-btn" (click)="goToList()">Cancel</button>
    </div>
  </form>
</div>
```

#### `src/app/employee/employee-create/employee-create.scss`

```scss
.form-container {
  max-width: 700px;
  margin: 20px auto;
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-container h2 {
  margin-bottom: 20px;
  color: #1976d2;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #cccccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
}

.button-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.button-group button {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 4px;
  cursor: pointer;
}

.button-group button:hover {
  background-color: #125ca1;
}

.cancel-btn {
  background-color: #757575 !important;
}

.cancel-btn:hover {
  background-color: #555 !important;
}

.error {
  color: red;
  display: block;
  margin-top: 4px;
  font-size: 13px;
}

.success-message {
  color: green;
  font-weight: bold;
  margin-bottom: 10px;
}

.error-message {
  color: red;
  font-weight: bold;
  margin-bottom: 10px;
}
```

### Step 3.9: Employee Edit Component

#### `src/app/employee/employee-edit/employee-edit.ts`

```typescript
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Employee, EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-employee-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './employee-edit.html',
  styleUrl: './employee-edit.scss',
})
export class EmployeeEditComponent implements OnInit {
  employeeForm!: FormGroup;
  employeeId!: number;
  selectedFile: File | null = null;
  currentAppointmentOrderFileName: string = '';
  successMessage: string = '';
  errorMessage: string = '';

  designations: string[] = [
    'Junior Assistant',
    'Senior Assistant',
    'Developer',
    'Senior Developer',
    'Manager',
    'Administrator',
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({
      employeeCode: ['', Validators.required],
      employeeNameEnglish: ['', Validators.required],
      employeeNameTamil: ['', Validators.required],
      designation: ['', Validators.required],
      department: ['', Validators.required],
      dateOfJoining: ['', Validators.required],
      mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      email: ['', [Validators.required, Validators.email]],
      remarks: [''],
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.employeeId = Number(id);
      this.loadEmployee(this.employeeId);
    } else {
      this.errorMessage = 'Employee ID not found in URL.';
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        const employee = response?.data ? response.data : response;

        this.employeeForm.patchValue({
          employeeCode: employee.employeeCode || '',
          employeeNameEnglish: employee.employeeNameEnglish || '',
          employeeNameTamil: employee.employeeNameTamil || '',
          designation: employee.designation || '',
          department: employee.department || '',
          dateOfJoining: employee.dateOfJoining || '',
          mobileNumber: employee.mobileNumber || '',
          email: employee.email || '',
          remarks: employee.remarks || '',
        });

        this.currentAppointmentOrderFileName = employee.appointmentOrderFileName || '';
        this.employeeForm.updateValueAndValidity();
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to load employee details.';
        this.cdr.detectChanges();
      },
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (file.type !== 'application/pdf') {
      this.errorMessage = 'Only PDF files are allowed.';
      this.selectedFile = null;
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = 'File size must be less than or equal to 2 MB.';
      this.selectedFile = null;
      return;
    }

    this.errorMessage = '';
    this.selectedFile = file;
  }

  onSubmit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    const employeeData: Employee = this.employeeForm.value;

    this.employeeService.updateEmployee(this.employeeId, employeeData).subscribe({
      next: (response: any) => {
        const updated = response?.data ? response.data : response;

        if (this.selectedFile && updated?.id) {
          this.uploadFile(updated.id);
        } else {
          this.router.navigate(['/employee/list'], {
            state: { successMessage: 'Employee updated successfully.' },
          });
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Failed to update employee.';
        this.cdr.markForCheck();
      },
    });
  }

  private uploadFile(employeeId: number): void {
    this.employeeService.uploadAppointmentOrder(employeeId, this.selectedFile!).subscribe({
      next: () => {
        this.router.navigate(['/employee/list'], {
          state: { successMessage: 'Employee updated successfully.' },
        });
      },
      error: () => {
        this.errorMessage = 'Employee saved, but file upload failed.';
        this.cdr.markForCheck();
      },
    });
  }

  goToList(): void {
    this.router.navigate(['/employee/list']);
  }

  get f() {
    return this.employeeForm.controls;
  }
}
```

#### `src/app/employee/employee-edit/employee-edit.html`

```html
<div class="form-container">
  <h2>Edit Employee</h2>

  @if (successMessage) {
    <p class="success-message">{{ successMessage }}</p>
  }
  @if (errorMessage) {
    <p class="error-message">{{ errorMessage }}</p>
  }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      @if (f['employeeCode'].touched && f['employeeCode'].invalid) {
        <small class="error">Employee Code is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Employee Name English</label>
      <input type="text" formControlName="employeeNameEnglish" />
      @if (f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid) {
        <small class="error">Employee Name English is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Employee Name Tamil</label>
      <input type="text" formControlName="employeeNameTamil" />
      @if (f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid) {
        <small class="error">Employee Name Tamil is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <option value="">-- Select Designation --</option>
        @for (designation of designations; track designation) {
          <option [value]="designation">{{ designation }}</option>
        }
      </select>
      @if (f['designation'].touched && f['designation'].invalid) {
        <small class="error">Designation is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Department</label>
      <input type="text" formControlName="department" />
      @if (f['department'].touched && f['department'].invalid) {
        <small class="error">Department is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Date of Joining</label>
      <input type="date" formControlName="dateOfJoining" />
      @if (f['dateOfJoining'].touched && f['dateOfJoining'].invalid) {
        <small class="error">Date of Joining is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['required']) {
        <small class="error">Mobile Number is required.</small>
      }
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern']) {
        <small class="error">Mobile Number must be exactly 10 digits.</small>
      }
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      @if (f['email'].touched && f['email'].errors?.['required']) {
        <small class="error">Email is required.</small>
      }
      @if (f['email'].touched && f['email'].errors?.['email']) {
        <small class="error">Enter a valid email address.</small>
      }
    </div>

    <div class="form-group">
      <label>Remarks</label>
      <textarea formControlName="remarks" rows="4"></textarea>
    </div>

    <div class="form-group">
      <label>Current Appointment Order</label>
      <p>{{ currentAppointmentOrderFileName || 'No file uploaded' }}</p>
    </div>

    <div class="form-group">
      <label>Replace Appointment Order (PDF only)</label>
      <input type="file" accept="application/pdf" (change)="onFileChange($event)" />
      <small>Select a new PDF only if you want to replace the existing file.</small>
    </div>

    <div class="button-group">
      <button type="submit">Update Employee</button>
      <button type="button" class="cancel-btn" (click)="goToList()">Cancel</button>
    </div>
  </form>
</div>
```

#### `src/app/employee/employee-edit/employee-edit.scss`

```scss
.form-container {
  max-width: 700px;
  margin: 20px auto;
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-container h2 {
  margin-bottom: 20px;
  color: #1976d2;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #cccccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
}

.form-group small {
  color: #666;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.button-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.button-group button {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 4px;
  cursor: pointer;
}

.button-group button:hover {
  background-color: #125ca1;
}

.cancel-btn {
  background-color: #757575 !important;
}

.cancel-btn:hover {
  background-color: #555 !important;
}

.error {
  color: red;
  display: block;
  margin-top: 4px;
  font-size: 13px;
}

.success-message {
  color: green;
  font-weight: bold;
  margin-bottom: 10px;
}

.error-message {
  color: red;
  font-weight: bold;
  margin-bottom: 10px;
}
```

### Step 3.10: Employee View Component

#### `src/app/employee/employee-view/employee-view.ts`

```typescript
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EmployeeService, Employee } from '../../services/employee';

@Component({
  selector: 'app-employee-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-view.html',
  styleUrl: './employee-view.scss',
})
export class EmployeeViewComponent implements OnInit {
  employee: Employee | null = null;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.loadEmployee(Number(id));
    } else {
      this.errorMessage = 'Employee ID not found in URL.';
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        if (response && response.data) {
          this.employee = response.data;
        } else {
          this.employee = response;
        }
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.employee = null;
        this.errorMessage = 'Failed to load employee details.';
        this.cdr.detectChanges();
      },
    });
  }
}
```

#### `src/app/employee/employee-view/employee-view.html`

```html
<div class="view-container">
  <h2>Employee Details</h2>

  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>

  <div *ngIf="employee" class="details-card">
    <p><strong>Employee Code:</strong> {{ employee?.employeeCode }}</p>
    <p><strong>Employee Name English:</strong> {{ employee?.employeeNameEnglish }}</p>
    <p><strong>Employee Name Tamil:</strong> {{ employee?.employeeNameTamil }}</p>
    <p><strong>Designation:</strong> {{ employee?.designation }}</p>
    <p><strong>Department:</strong> {{ employee?.department }}</p>
    <p><strong>Date of Joining:</strong> {{ employee?.dateOfJoining }}</p>
    <p><strong>Mobile Number:</strong> {{ employee?.mobileNumber }}</p>
    <p><strong>Email:</strong> {{ employee?.email }}</p>
    <p><strong>Remarks:</strong> {{ employee?.remarks || 'No remarks' }}</p>

    <p>
      <strong>Uploaded File Name:</strong>
      {{ employee?.appointmentOrderFileName || 'No file uploaded' }}
    </p>

    <div class="button-group">
      <a routerLink="/employee/list">Back to List</a>
    </div>
  </div>
</div>
```

#### `src/app/employee/employee-view/employee-view.scss`

```scss
.view-container {
  max-width: 700px;
  margin: 20px auto;
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.view-container h2 {
  color: #1976d2;
  margin-bottom: 20px;
}

.details-card p {
  margin: 10px 0;
  font-size: 16px;
  line-height: 1.5;
}

.button-group {
  margin-top: 20px;
}

.button-group a {
  display: inline-block;
  background-color: #1976d2;
  color: white;
  text-decoration: none;
  padding: 10px 16px;
  border-radius: 4px;
}

.button-group a:hover {
  background-color: #125ca1;
}

.error-message {
  color: red;
  font-weight: bold;
}
```

### Step 3.11: Employee Report Component

#### `src/app/employee/employee-report/employee-report.ts`

```typescript
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EmployeeService, Employee } from '../../services/employee';

@Component({
  selector: 'app-employee-report',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-report.html',
  styleUrl: './employee-report.scss',
})
export class EmployeeReportComponent implements OnInit {
  employee: Employee | null = null;
  errorMessage: string = '';
  downloading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.loadEmployee(Number(id));
    } else {
      this.errorMessage = 'Employee ID not found in URL.';
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        if (response && response.data) {
          this.employee = response.data;
        } else {
          this.employee = response;
        }
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: () => {
        this.employee = null;
        this.errorMessage = 'Failed to load employee details.';
        this.cdr.detectChanges();
      },
    });
  }

  downloadReport(): void {
    if (!this.employee?.id) return;

    this.downloading = true;
    this.employeeService.downloadEmployeeReport(this.employee.id).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `employee-report-${this.employee!.id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.downloading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to download PDF report.';
        this.downloading = false;
        this.cdr.detectChanges();
      },
    });
  }
}
```

#### `src/app/employee/employee-report/employee-report.html`

```html
<div class="report-container">
  <h2>Employee Service Record Report</h2>
  <p class="report-subtitle">பணியாளர் சேவை விவர அறிக்கை</p>

  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>

  <div *ngIf="employee" class="report-card">
    <div class="report-row">
      <span class="label">Employee Code</span>
      <span class="value">{{ employee.employeeCode }}</span>
    </div>
    <div class="report-row">
      <span class="label">Employee Name (English)</span>
      <span class="value">{{ employee.employeeNameEnglish }}</span>
    </div>
    <div class="report-row">
      <span class="label">Employee Name (Tamil)</span>
      <span class="value">{{ employee.employeeNameTamil }}</span>
    </div>
    <div class="report-row">
      <span class="label">Designation</span>
      <span class="value">{{ employee.designation }}</span>
    </div>
    <div class="report-row">
      <span class="label">Department</span>
      <span class="value">{{ employee.department }}</span>
    </div>
    <div class="report-row">
      <span class="label">Date of Joining</span>
      <span class="value">{{ employee.dateOfJoining }}</span>
    </div>
    <div class="report-row">
      <span class="label">Mobile Number</span>
      <span class="value">{{ employee.mobileNumber }}</span>
    </div>
    <div class="report-row">
      <span class="label">Email</span>
      <span class="value">{{ employee.email }}</span>
    </div>
    <div class="report-row">
      <span class="label">Remarks</span>
      <span class="value">{{ employee.remarks || 'No remarks' }}</span>
    </div>

    <div class="button-group">
      <button class="download-btn" type="button" (click)="downloadReport()" [disabled]="downloading">
        {{ downloading ? 'Downloading...' : 'Download PDF Report' }}
      </button>
      <a routerLink="/employee/list">Back to List</a>
    </div>
  </div>
</div>
```

#### `src/app/employee/employee-report/employee-report.scss`

```scss
.report-container {
  max-width: 700px;
  margin: 20px auto;
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.report-container h2 {
  color: #1976d2;
  margin-bottom: 4px;
}

.report-subtitle {
  color: #555;
  font-size: 15px;
  margin-bottom: 20px;
}

.report-card {
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}

.report-row {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
  line-height: 1.5;
}

.report-row:last-child {
  border-bottom: none;
}

.report-row:nth-child(even) {
  background-color: #f9f9f9;
}

.label {
  font-weight: 600;
  width: 220px;
  flex-shrink: 0;
  color: #333;
}

.value {
  color: #555;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #e0e0e0;
}

.download-btn {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.download-btn:hover:not(:disabled) {
  background-color: #125ca1;
}

.download-btn:disabled {
  background-color: #90caf9;
  cursor: not-allowed;
}

.button-group a {
  display: inline-block;
  background-color: #757575;
  color: white;
  text-decoration: none;
  padding: 10px 16px;
  border-radius: 4px;
  font-size: 14px;
}

.button-group a:hover {
  background-color: #555;
}

.error-message {
  color: red;
  font-weight: bold;
}
```

### Step 3.12: Run the Frontend

```bash
# Navigate to frontend folder
cd employee-frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend runs at `http://localhost:4200`

---

## PART 4: HOW TO RUN THE COMPLETE PROJECT

### Step-by-Step Execution Order

1. **Start PostgreSQL** - Make sure PostgreSQL service is running
2. **Create Database** - Run `CREATE DATABASE employee_service_db;` in pgAdmin/psql
3. **Start Backend** - Open Eclipse → Run `EmployeeBackendApplication.java` (runs on port 8080)
4. **Start Frontend** - Open VS Code terminal → `cd employee-frontend` → `npm start` (runs on port 4200)
5. **Open Browser** - Go to `http://localhost:4200`

### Verify Everything Works

1. Click "Add Employee" and fill in the form
2. Submit and verify it appears in the list
3. Click "View" to see details
4. Click "Edit" to modify details
5. Click "Report" to view and download the PDF report

---

## PART 5: DATA FLOW DIAGRAM

```
┌──────────────────┐     HTTP Request      ┌──────────────────┐     JPA/SQL      ┌──────────────┐
│                  │  ─────────────────►   │                  │  ──────────────►  │              │
│  Angular         │     JSON / Blob       │  Spring Boot     │     Entity       │  PostgreSQL  │
│  Frontend        │  ◄─────────────────   │  Backend         │  ◄──────────────  │  Database    │
│  (Port 4200)     │                       │  (Port 8080)     │                   │  (Port 5432) │
│                  │                       │                  │                   │              │
│  Components:     │                       │  Layers:         │                   │  Table:      │
│  - List          │                       │  - Controller    │                   │  - employees │
│  - Create        │                       │  - Service       │                   │              │
│  - Edit          │                       │  - Repository    │                   │              │
│  - View          │                       │  - Entity        │                   │              │
│  - Report        │                       │  - DTO           │                   │              │
│                  │                       │  - Exception     │                   │              │
└──────────────────┘                       └──────────────────┘                   └──────────────┘
```

---

## PART 6: COMMON TERMINAL COMMANDS REFERENCE

```bash
# === Angular CLI Commands ===
ng new project-name                    # Create new project
ng generate component path/name        # Generate component
ng generate service path/name          # Generate service
ng serve                               # Start dev server
ng build                               # Build for production

# === npm Commands ===
npm install                            # Install all dependencies
npm start                              # Start project (alias for ng serve)
npm run build                          # Build project

# === Maven Commands (in Eclipse terminal or cmd) ===
mvn clean install                      # Clean and build
mvn spring-boot:run                    # Run Spring Boot app

# === PostgreSQL Commands ===
psql -U postgres                       # Connect to PostgreSQL
\l                                     # List all databases
\c employee_service_db                 # Connect to database
\dt                                    # List all tables
SELECT * FROM employees;               # Query all employees
```

---

## PART 7: PACKAGE STRUCTURE SUMMARY

### Backend Package Structure (Java)

```
com.employee.backend
├── EmployeeBackendApplication.java     ← @SpringBootApplication (main)
├── controller
│   └── EmployeeController.java         ← @RestController, @RequestMapping
├── dto
│   ├── ApiResponse.java                ← Generic response wrapper
│   ├── EmployeeRequestDTO.java         ← @NotBlank, @Email, @Size validation
│   └── EmployeeResponseDTO.java        ← Response format
├── entity
│   └── Employee.java                   ← @Entity, @Table, @Column
├── exception
│   ├── GlobalExceptionHandler.java     ← @RestControllerAdvice
│   ├── EmployeeNotFoundException.java  ← extends RuntimeException
│   ├── DuplicateEmployeeCodeException.java
│   └── InvalidFileException.java
├── repository
│   └── EmployeeRepository.java         ← extends JpaRepository
└── service
    ├── EmployeeService.java            ← Interface
    ├── EmployeeReportService.java      ← @Service (PDFBox)
    ├── FileStorageService.java         ← @Service (file I/O)
    └── impl
        └── EmployeeServiceImpl.java    ← @Service, @Transactional
```

### Frontend File Structure (Angular)

```
src/app/
├── app.ts                              ← Root component (standalone)
├── app.html                            ← Header + router-outlet
├── app.scss                            ← Root styles
├── app.routes.ts                       ← Route definitions
├── app.config.ts                       ← Providers (HTTP, Router, Zoneless)
├── services/
│   └── employee.ts                     ← HttpClient service + Employee interface
└── employee/
    ├── employee-list/                  ← *ngFor table, search, sort
    ├── employee-create/                ← ReactiveFormsModule, FormBuilder
    ├── employee-edit/                  ← patchValue, file replace
    ├── employee-view/                  ← Read-only display
    └── employee-report/                ← Blob download for PDF
```

---

## Key Annotations Reference (Backend)

| Annotation                | Purpose                                          |
|--------------------------|--------------------------------------------------|
| `@SpringBootApplication` | Main application entry point                     |
| `@RestController`        | REST API controller (returns JSON)               |
| `@RequestMapping`        | Base URL path for controller                     |
| `@CrossOrigin`           | Allow cross-origin requests from Angular         |
| `@GetMapping`            | Handle HTTP GET                                  |
| `@PostMapping`           | Handle HTTP POST                                 |
| `@PutMapping`            | Handle HTTP PUT                                  |
| `@DeleteMapping`         | Handle HTTP DELETE                               |
| `@PathVariable`          | Extract value from URL path                      |
| `@RequestBody`           | Deserialize JSON request body                    |
| `@RequestParam`          | Extract query/form parameter                     |
| `@Valid`                 | Trigger bean validation                          |
| `@Entity`                | Mark class as JPA entity                         |
| `@Table`                 | Map to database table                            |
| `@Id`                    | Primary key                                      |
| `@GeneratedValue`        | Auto-increment strategy                          |
| `@Column`                | Map to database column                           |
| `@Service`               | Spring service bean                              |
| `@Transactional`         | Database transaction management                  |
| `@RestControllerAdvice`  | Global exception handler                         |
| `@ExceptionHandler`      | Handle specific exception type                   |
| `@NotBlank`              | Validation: not null and not empty               |
| `@NotNull`               | Validation: not null                             |
| `@Email`                 | Validation: valid email format                   |
| `@Size`                  | Validation: string length constraint             |
| `@Value`                 | Inject property from application.properties      |

---

## Key Angular Concepts Used (Frontend)

| Concept                     | Where Used                                       |
|----------------------------|--------------------------------------------------|
| Standalone Components      | All components use `standalone: true`             |
| Reactive Forms             | Create & Edit components (FormBuilder, FormGroup) |
| Template-driven Forms      | List component (ngModel for search)               |
| Router                     | app.routes.ts, RouterLink, Router.navigate()      |
| HttpClient                 | EmployeeService (GET, POST, PUT, DELETE)          |
| Observable/RxJS            | All HTTP calls return Observable                  |
| `@if` / `@for`            | Angular 21 control flow syntax                    |
| `*ngIf` / `*ngFor`        | Template directives                               |
| ChangeDetectorRef          | Manual change detection (Zoneless mode)           |
| FormData                   | File upload (multipart/form-data)                |
| Blob                       | PDF download handling                             |
| provideZonelessChangeDetection | Angular 21 performance optimization          |

---

*This README contains the complete source code and step-by-step instructions to build the Employee Service Record Management system from scratch. Use it as a reference guide for building in Eclipse (backend) and VS Code (frontend).*
