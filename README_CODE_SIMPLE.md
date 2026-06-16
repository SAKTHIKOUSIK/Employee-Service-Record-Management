# README_CODE_SIMPLE.md

# Employee Service Record Management — Code Guide (Beginner-Friendly)

> Every line of code has **one short comment**. No walls of text.

---

## TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Application Flows](#3-application-flows)
4. [API Connection Map](#4-api-connection-map)
5. [BACKEND — Spring Boot](#5-backend--spring-boot)
6. [FRONTEND — Angular](#6-frontend--angular)

---

## 1. PROJECT OVERVIEW

Full-stack Employee Service Record Management System.

- **Backend:** Spring Boot 3 + Java 17 → REST API
- **Frontend:** Angular 21 (standalone components)
- **Database:** PostgreSQL (via Hibernate/JPA)
- **Features:** Create / View / Edit employees, upload PDF appointment orders, download Tamil+English PDF reports

---

## 2. TECHNOLOGY STACK

```
┌─────────────────────────────────────────────────────────┐
│  BROWSER (Angular 21)                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │  List    │ │  Create  │ │   Edit   │ │  Report  │  │
│  │Component │ │Component │ │Component │ │Component │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
│       └────────────┴────────────┴─────────────┘        │
│                    EmployeeService (HttpClient)         │
└──────────────────────────┬──────────────────────────────┘
                           │  HTTP REST (JSON / Blob)
                           ▼
┌─────────────────────────────────────────────────────────┐
│  Spring Boot (port 8080)                                │
│  EmployeeController → EmployeeServiceImpl               │
│  FileStorageService   EmployeeReportService (PDFBox)    │
│  EmployeeRepository (Spring Data JPA)                   │
└──────────────────────────┬──────────────────────────────┘
                           │  JDBC / Hibernate
                           ▼
┌─────────────────────────────────────────────────────────┐
│  PostgreSQL  →  employee_service_db  →  employees table │
└─────────────────────────────────────────────────────────┘
```

---

## 3. APPLICATION FLOWS

### Flow 1: Backend Startup
```
JVM starts main()
  → SpringApplication.run() boots everything
  → Finds all @RestController, @Service, @Repository beans
  → Reads application.properties → connects to PostgreSQL
  → Hibernate creates/updates "employees" table
  → Tomcat starts on port 8080 → app is READY
```

### Flow 2: Frontend Startup
```
Browser loads index.html
  → Loads Angular bundle (main.ts)
  → bootstrapApplication() sets up Router + HttpClient
  → Renders <app-root> → <router-outlet>
  → URL "/" redirects to /employee/list
  → EmployeeListComponent loads → calls GET /api/employees/list
  → Table renders with employee data
```

### Flow 3: Create Employee
```
Click "Add Employee" → navigate to /employee/create
  → User fills form → clicks "Save Employee"
  → Form validated → POST /api/employees/create
  → Backend checks for duplicate code → saves to DB
  → If PDF selected → POST /api/employees/upload/{id}
  → Navigate back to list with success message
```

### Flow 4: Edit Employee
```
Click "Edit" → navigate to /employee/edit/{id}
  → GET /api/employees/{id} → form pre-filled
  → User edits → clicks "Update Employee"
  → PUT /api/employees/update/{id}
  → If new PDF → upload replaces old file
  → Navigate back to list with success message
```

### Flow 5: Download PDF Report
```
Click "Report" → navigate to /employee/report/{id}
  → Employee details shown on screen
  → Click "Download PDF Report"
  → GET /api/employees/report/{id} (responseType: blob)
  → PDFBox generates PDF with Tamil + English text
  → Browser downloads "employee-report-{id}.pdf"
```

### Flow 6: Delete Employee (hidden behind flag)
```
Set showDeleteButton = true in employee-list.ts to reveal the Delete button
  → Click "Delete" on a table row → window.confirm() dialog appears
  → User confirms → EmployeeService.deleteEmployee(id)
  → DELETE /api/employees/delete/{id}
  → Backend: findById() → deleteById() → row removed from DB
  → HTTP 200 → list reloads → success message shown for 3 seconds
  → Set showDeleteButton = false before interview/demo to hide the button
```

---

## 4. API CONNECTION MAP

| Angular Method                     | HTTP   | Spring Endpoint              | Controller Method          |
| ---------------------------------- | ------ | ---------------------------- | -------------------------- |
| `getAllEmployees()`                | GET    | `/api/employees/list`        | `getAllEmployees()`        |
| `getEmployeeById(id)`              | GET    | `/api/employees/{id}`        | `getEmployeeById()`        |
| `createEmployee(data)`             | POST   | `/api/employees/create`      | `createEmployee()`         |
| `updateEmployee(id, data)`         | PUT    | `/api/employees/update/{id}` | `updateEmployee()`         |
| `uploadAppointmentOrder(id, file)` | POST   | `/api/employees/upload/{id}`  | `uploadAppointmentOrder()` |
| `downloadEmployeeReport(id)`       | GET    | `/api/employees/report/{id}`  | `generateEmployeeReport()` |
| `deleteEmployee(id)`               | DELETE | `/api/employees/delete/{id}`  | `deleteEmployee()`         |

---

## 5. BACKEND — SPRING BOOT

---

### FILE: pom.xml

**Purpose:** Maven project config — lists all libraries the project needs.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="...">

  <modelVersion>4.0.0</modelVersion>   <!-- Always 4.0.0 for Maven -->

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.15</version>          <!-- Inherit default Spring Boot settings -->
    <relativePath/>                    <!-- Download from Maven Central, not local disk -->
  </parent>

  <groupId>com.employee</groupId>       <!-- Your company/project namespace -->
  <artifactId>employee-backend</artifactId> <!-- Becomes the JAR filename -->
  <version>0.0.1-SNAPSHOT</version>    <!-- SNAPSHOT = still in development -->

  <properties>
    <java.version>17</java.version>    <!-- Compile with Java 17 -->
  </properties>

  <dependencies>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
      <!-- Adds JPA + Hibernate so we can use @Entity and save to DB -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
      <!-- Adds @NotBlank, @Email, @Size validators for DTOs -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <!-- Adds Spring MVC + embedded Tomcat for REST APIs -->
    </dependency>

    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>           <!-- PostgreSQL JDBC driver, only needed at runtime -->
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>        <!-- Lombok generates boilerplate (getters/setters) at compile time -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>             <!-- JUnit + Mockito for tests only -->
    </dependency>

    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
      <version>2.0.27</version>       <!-- PDF generation library (used for Tamil+English reports) -->
    </dependency>

  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <!-- Packages everything into one runnable JAR (fat JAR) -->
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <!-- Lombok not needed at runtime, exclude from JAR -->
            </exclude>
          </excludes>
        </configuration>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <!-- Configures how Java source is compiled -->
        <executions>
          <execution>
            <id>default-compile</id>
            <phase>compile</phase>
            <goals><goal>compile</goal></goals>
            <configuration>
              <annotationProcessorPaths>
                <path>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                  <!-- Run Lombok before compiling main sources -->
                </path>
              </annotationProcessorPaths>
            </configuration>
          </execution>
          <execution>
            <id>default-testCompile</id>
            <phase>test-compile</phase>
            <goals><goal>testCompile</goal></goals>
            <configuration>
              <annotationProcessorPaths>
                <path>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                  <!-- Run Lombok before compiling test sources -->
                </path>
              </annotationProcessorPaths>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

</project>
```

---

### FILE: application.properties

**Purpose:** All runtime settings in one place.

```properties
# ─── SERVER ───────────────────────────────────────────────
server.port=8080
# App listens on port 8080

# ─── POSTGRESQL ───────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_service_db
# Connect to local PostgreSQL database named "employee_service_db"

spring.datasource.username=postgres
# PostgreSQL username

spring.datasource.password=Sakthi@123
# PostgreSQL password (use env variables in production)

spring.datasource.driver-class-name=org.postgresql.Driver
# Use PostgreSQL JDBC driver

# ─── JPA / HIBERNATE ──────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
# Auto-create/update DB tables on startup based on @Entity classes

spring.jpa.show-sql=true
# Print all SQL queries to the console (for debugging)

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
# Tell Hibernate to generate PostgreSQL-compatible SQL

# ─── FILE UPLOAD ──────────────────────────────────────────
spring.servlet.multipart.max-file-size=2MB
# Reject uploaded files larger than 2MB at the server level

spring.servlet.multipart.max-request-size=2MB
# Reject the whole request if it exceeds 2MB

file.upload-dir=uploads
# Custom property: folder name where PDF files are saved on disk
```

---

### FILE: EmployeeBackendApplication.java

**Purpose:** The app's entry point — starts everything.

```java
package com.employee.backend; // This class belongs to the "backend" package

import org.springframework.boot.SpringApplication; // Used to start the Spring app
import org.springframework.boot.autoconfigure.SpringBootApplication; // Combines 3 annotations in one

@SpringBootApplication // Enables auto-config, component scan, and bean configuration
public class EmployeeBackendApplication {

    public static void main(String[] args) { // JVM calls this first when the JAR runs
        SpringApplication.run(EmployeeBackendApplication.class, args); // Starts Spring Boot, Tomcat, and all beans
    }
}
```

---

### FILE: entity/Employee.java

**Purpose:** Represents one row in the `employees` database table.

```java
package com.employee.backend.entity;

import jakarta.persistence.Column;          // Customizes how a field maps to a DB column
import jakarta.persistence.Entity;          // Marks this class as a DB table
import jakarta.persistence.GeneratedValue;  // Auto-generates the primary key
import jakarta.persistence.GenerationType;  // Strategy: use DB auto-increment
import jakarta.persistence.Id;             // Marks the primary key field
import jakarta.persistence.Table;          // Sets the table name and constraints
import jakarta.persistence.UniqueConstraint; // Adds a UNIQUE constraint in the DB
import java.time.LocalDate;                // Date only (no time) — for dateOfJoining
import java.time.LocalDateTime;            // Date + time — for createdAt/updatedAt

@Entity // Hibernate will manage this class and map it to a DB table
@Table(
    name = "employees", // Table name in PostgreSQL is "employees"
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_code") // No two employees can share the same code
    }
)
public class Employee {

    @Id // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL auto-assigns the ID
    private Long id; // Stores the DB-assigned ID (null before saving)

    @Column(name = "employee_code", nullable = false, unique = true, length = 50) // Required, unique, max 50 chars
    private String employeeCode;

    @Column(name = "employee_name_english", nullable = false, length = 200) // Required, max 200 chars
    private String employeeNameEnglish;

    @Column(name = "employee_name_tamil", nullable = false, length = 200) // Stores Unicode Tamil text
    private String employeeNameTamil;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "date_of_joining", nullable = false) // Maps to DATE type in PostgreSQL
    private LocalDate dateOfJoining;

    @Column(name = "mobile_number", length = 20) // Optional field (no nullable=false)
    private String mobileNumber;

    @Column(name = "email", length = 200) // Optional email field
    private String email;

    @Column(name = "remarks", length = 1000) // Optional free-text notes
    private String remarks;

    @Column(name = "appointment_order_path", length = 500) // Server disk path to the uploaded PDF
    private String appointmentOrderPath;

    @Column(name = "created_at") // When this record was first created
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // When this record was last changed
    private LocalDateTime updatedAt;

    @Column(name = "appointment_order_file_name", length = 255) // Original filename shown to the user
    private String appointmentOrderFileName;

    public Employee() {} // Required by JPA — Hibernate needs a no-arg constructor

    // Getters and setters — required by JPA and Jackson (JSON serialization)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeNameEnglish() { return employeeNameEnglish; }
    public void setEmployeeNameEnglish(String employeeNameEnglish) { this.employeeNameEnglish = employeeNameEnglish; }

    public String getEmployeeNameTamil() { return employeeNameTamil; }
    public void setEmployeeNameTamil(String employeeNameTamil) { this.employeeNameTamil = employeeNameTamil; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getAppointmentOrderPath() { return appointmentOrderPath; }
    public void setAppointmentOrderPath(String appointmentOrderPath) { this.appointmentOrderPath = appointmentOrderPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAppointmentOrderFileName() { return appointmentOrderFileName; }
    public void setAppointmentOrderFileName(String appointmentOrderFileName) { this.appointmentOrderFileName = appointmentOrderFileName; }
}
```

---

### FILE: repository/EmployeeRepository.java

**Purpose:** All database operations — Spring auto-generates the SQL.

```java
package com.employee.backend.repository;

import com.employee.backend.entity.Employee; // The entity this repository manages
import org.springframework.data.jpa.repository.JpaRepository; // Gives us free CRUD methods
import java.util.Optional; // Wrapper that avoids NullPointerException when record not found

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // JpaRepository<Employee, Long> → Employee is the entity, Long is the ID type
    // Free inherited methods: save(), findById(), findAll(), deleteById(), count(), etc.

    Optional<Employee> findByEmployeeCode(String employeeCode);
    // Spring generates: SELECT * FROM employees WHERE employee_code = ?

    boolean existsByEmployeeCode(String employeeCode);
    // Spring generates: SELECT COUNT(*) > 0 FROM employees WHERE employee_code = ?
    // Used to check for duplicate codes before saving
}
```

---

### FILE: dto/ApiResponse.java

**Purpose:** Standard wrapper for all API responses — always `{ "message": "...", "data": ... }`.

```java
package com.employee.backend.dto;

public class ApiResponse<T> { // <T> means "data" can be any type (Employee, List, null, etc.)

    private String message; // Human-readable status message (e.g. "Employee created successfully")
    private T data;         // The actual response payload (employee object, list, or null on error)

    public ApiResponse() {} // No-arg constructor required by Jackson

    public ApiResponse(String message, T data) { // Convenience constructor used throughout the app
        this.message = message;
        this.data = data;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; } // Jackson uses these getters to build JSON
}
```

---

### FILE: dto/EmployeeRequestDTO.java

**Purpose:** Shape of the JSON Angular sends to create/update an employee. Validates the input.

```java
package com.employee.backend.dto;

import jakarta.validation.constraints.Email;    // Validates email format
import jakarta.validation.constraints.NotBlank; // Checks: not null, not empty, not just spaces
import jakarta.validation.constraints.NotNull;  // Checks: not null (for non-String types)
import jakarta.validation.constraints.Size;     // Checks min/max string length
import java.time.LocalDate;

public class EmployeeRequestDTO {
    // Angular sends JSON matching this shape; Spring deserializes it automatically
    // @Valid in the controller triggers all the annotations below

    @NotBlank(message = "Employee code is required") // Fails if empty or blank
    private String employeeCode;

    @NotBlank(message = "Employee name (English) is required")
    private String employeeNameEnglish;

    @NotBlank(message = "Employee name (Tamil) is required")
    private String employeeNameTamil;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Date of joining is required") // @NotNull (not @NotBlank) because it's a Date, not String
    private LocalDate dateOfJoining;

    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits") // Exactly 10 characters
    private String mobileNumber;

    @Email(message = "Invalid email address") // Must look like user@domain.com
    private String email;

    private String remarks; // Optional — no validation annotation

    // Getters and setters — Jackson uses these to fill this object from incoming JSON
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeNameEnglish() { return employeeNameEnglish; }
    public void setEmployeeNameEnglish(String v) { this.employeeNameEnglish = v; }

    public String getEmployeeNameTamil() { return employeeNameTamil; }
    public void setEmployeeNameTamil(String v) { this.employeeNameTamil = v; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
```

---

### FILE: dto/EmployeeResponseDTO.java

**Purpose:** Shape of the JSON we send back to Angular after every operation.

```java
package com.employee.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponseDTO {
    // We never expose the Entity directly — this DTO controls exactly what Angular sees

    private Long id;                       // DB-assigned ID (used for Edit/View/Report routes)
    private String employeeCode;
    private String employeeNameEnglish;
    private String employeeNameTamil;
    private String designation;
    private String department;
    private LocalDate dateOfJoining;       // Serialized as "2024-01-15" in JSON
    private String mobileNumber;
    private String email;
    private String remarks;
    private String appointmentOrderPath;   // Server disk path to the uploaded PDF
    private LocalDateTime createdAt;       // Serialized as "2024-01-15T10:30:00" in JSON
    private LocalDateTime updatedAt;
    private String appointmentOrderFileName; // Original filename shown to the user

    // Getters and setters — Jackson serializes this to JSON using these getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeNameEnglish() { return employeeNameEnglish; }
    public void setEmployeeNameEnglish(String v) { this.employeeNameEnglish = v; }

    public String getEmployeeNameTamil() { return employeeNameTamil; }
    public void setEmployeeNameTamil(String v) { this.employeeNameTamil = v; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getAppointmentOrderPath() { return appointmentOrderPath; }
    public void setAppointmentOrderPath(String v) { this.appointmentOrderPath = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAppointmentOrderFileName() { return appointmentOrderFileName; }
    public void setAppointmentOrderFileName(String v) { this.appointmentOrderFileName = v; }
}
```

---

### FILE: exception/EmployeeNotFoundException.java

**Purpose:** Thrown when an employee ID doesn't exist in the DB.

```java
package com.employee.backend.exception;

public class EmployeeNotFoundException extends RuntimeException { // Unchecked — no try-catch needed by callers

    public EmployeeNotFoundException(String message) {
        super(message); // Pass message to RuntimeException; retrieved later with ex.getMessage()
        // Example: new EmployeeNotFoundException("Employee not found with id: 99") → HTTP 404
    }
}
```

---

### FILE: exception/DuplicateEmployeeCodeException.java

**Purpose:** Thrown when the same employee code is used twice.

```java
package com.employee.backend.exception;

public class DuplicateEmployeeCodeException extends RuntimeException {

    public DuplicateEmployeeCodeException(String message) {
        super(message); // Example: "Employee code already exists: EMP001" → HTTP 400
    }
}
```

---

### FILE: exception/InvalidFileException.java

**Purpose:** Thrown when an uploaded file is invalid (wrong type, too large, empty).

```java
package com.employee.backend.exception;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message); // Examples: "File is empty", "Only PDF files are allowed" → HTTP 400
    }
}
```

---

### FILE: exception/GlobalExceptionHandler.java

**Purpose:** Catches ALL exceptions from all controllers and returns clean JSON error responses.

```java
package com.employee.backend.exception;

import com.employee.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Applies to ALL controllers — acts as a global try-catch
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class) // Catches this exception anywhere in the app
    public ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null); // null data on error
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Returns HTTP 404
    }

    @ExceptionHandler(DuplicateEmployeeCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmployeeCode(DuplicateEmployeeCodeException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Returns HTTP 400
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // Triggered when @Valid fails on a @RequestBody
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder("Validation failed: "); // Start building error message
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) { // Loop through each failed field
            sb.append(fieldError.getField())          // e.g. "employeeCode"
              .append(" - ")
              .append(fieldError.getDefaultMessage())  // e.g. "Employee code is required"
              .append("; ");
        }
        ApiResponse<Void> response = new ApiResponse<>(sb.toString(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Returns HTTP 400
    }

    @ExceptionHandler(Exception.class) // Catches anything not handled above (fallback)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>("Unexpected error: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // Returns HTTP 500
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(InvalidFileException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Returns HTTP 400
    }
}
```

---

### FILE: service/EmployeeService.java (Interface)

**Purpose:** Defines the contract — what methods exist. The controller uses this, not the implementation directly.

```java
package com.employee.backend.service;

import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;
import java.util.List;

public interface EmployeeService { // Interface = a contract, no actual logic here

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO); // Save a new employee

    List<EmployeeResponseDTO> getAllEmployees(); // Get all employees

    EmployeeResponseDTO getEmployeeById(Long id); // Get one employee by ID (throws 404 if not found)

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO); // Update existing employee

    EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName); // Store uploaded file info

    void deleteEmployee(Long id); // Permanently delete an employee (no undo)
}
```

---

### FILE: service/impl/EmployeeServiceImpl.java

**Purpose:** The actual business logic — duplicate checking, mapping, timestamps, and DB operations.

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

@Service // Registers this class as a Spring bean (singleton)
@Transactional // Every method runs inside a DB transaction — all-or-nothing
public class EmployeeServiceImpl implements EmployeeService { // Fulfills the EmployeeService contract

    private final EmployeeRepository employeeRepository; // Injected — handles all DB operations

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) { // Constructor injection
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) { // Check for duplicate code
            throw new DuplicateEmployeeCodeException("Employee code already exists: " + requestDTO.getEmployeeCode()); // Stops here → HTTP 400
        }
        Employee employee = new Employee(); // Create empty entity (no ID yet)
        mapRequestToEntity(requestDTO, employee); // Copy all DTO fields to entity
        LocalDateTime now = LocalDateTime.now(); // Capture current time once
        employee.setCreatedAt(now); // Set creation timestamp
        employee.setUpdatedAt(now); // Set update timestamp (same as creation on first save)
        Employee saved = employeeRepository.save(employee); // INSERT INTO employees — DB assigns the ID
        return mapEntityToResponse(saved); // Convert entity to DTO and return
    }

    @Override
    @Transactional(readOnly = true) // Optimization: skip dirty checking since we're only reading
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll() // SELECT * FROM employees
            .stream() // Convert list to a stream for processing
            .map(this::mapEntityToResponse) // Convert each entity to a DTO
            .collect(Collectors.toList()); // Collect back to a List
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id) // SELECT * FROM employees WHERE id = ?
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id)); // Throw 404 if missing
        return mapEntityToResponse(employee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id)); // 404 if not found

        if (!employee.getEmployeeCode().equals(requestDTO.getEmployeeCode()) // Only check duplicate if code is changing
                && employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) {
            throw new DuplicateEmployeeCodeException("Employee code already exists: " + requestDTO.getEmployeeCode()); // 400 if duplicate
        }

        mapRequestToEntity(requestDTO, employee); // Overwrite entity fields with new values
        employee.setUpdatedAt(LocalDateTime.now()); // Update the "last modified" timestamp
        Employee updated = employeeRepository.save(employee); // UPDATE employees SET ... WHERE id = ?
        return mapEntityToResponse(updated);
    }

    @Override
    public EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        employee.setAppointmentOrderPath(filePath); // Save the server disk path
        employee.setAppointmentOrderFileName(fileName); // Save the original filename for the UI
        employee.setUpdatedAt(LocalDateTime.now());
        Employee updated = employeeRepository.save(employee); // UPDATE the file columns
        return mapEntityToResponse(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id)); // 404 if not found
        employeeRepository.deleteById(employee.getId()); // DELETE FROM employees WHERE id = ?
    }

    private void mapRequestToEntity(EmployeeRequestDTO requestDTO, Employee employee) {
        // Copies all DTO fields to the entity — used in both create and update
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
        // Copies all entity fields to a DTO — controls exactly what gets sent to Angular
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

---

### FILE: service/FileStorageService.java

**Purpose:** Saves uploaded PDF files to a folder on the server disk.

```java
package com.employee.backend.service;

import org.springframework.beans.factory.annotation.Value; // Reads values from application.properties
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Sanitizes file paths to prevent attacks
import org.springframework.web.multipart.MultipartFile; // Represents the uploaded file
import java.io.IOException;
import java.nio.file.Files; // File system operations (copy, create directory, etc.)
import java.nio.file.Path;  // Modern Java path representation
import java.nio.file.Paths; // Converts String → Path

@Service
public class FileStorageService {

    private final Path uploadDir; // Absolute path to the uploads folder on disk

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) { // Reads "uploads" from properties
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize(); // Convert to absolute path
        try {
            Files.createDirectories(this.uploadDir); // Create "uploads" folder if it doesn't exist
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + this.uploadDir, ex); // Fail fast on startup
        }
    }

    public String saveAppointmentOrderFile(Long employeeId, MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename()); // Sanitize the filename (prevent path traversal attacks)
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.'); // Find where the extension starts
        if (dotIndex != -1) {
            fileExtension = originalFileName.substring(dotIndex); // Extract ".pdf" from the filename
        }
        String fileName = "employee_" + employeeId + "_appointment" + fileExtension; // e.g. "employee_5_appointment.pdf"
        Path targetLocation = this.uploadDir.resolve(fileName); // Build full path: uploads/employee_5_appointment.pdf
        Files.copy(file.getInputStream(), targetLocation,
                   java.nio.file.StandardCopyOption.REPLACE_EXISTING); // Save the file, overwrite if exists
        return targetLocation.toString(); // Return the absolute path stored in DB
    }
}
```

---

### FILE: service/EmployeeReportService.java

**Purpose:** Generates a PDF service record report using Apache PDFBox. Tamil text is rendered as vector paths via the Java AWT text engine.

```java
package com.employee.backend.service;

import com.employee.backend.entity.Employee;
import com.employee.backend.exception.EmployeeNotFoundException;
import com.employee.backend.repository.EmployeeRepository;

import org.apache.pdfbox.pdmodel.PDDocument;         // Represents the whole PDF document
import org.apache.pdfbox.pdmodel.PDPage;              // Represents one page in the PDF
import org.apache.pdfbox.pdmodel.PDPageContentStream; // The drawing surface — write text and shapes here
import org.apache.pdfbox.pdmodel.common.PDRectangle;  // Page size constants (A4, LETTER, etc.)
import org.apache.pdfbox.pdmodel.font.PDType1Font;    // Standard PDF fonts (Helvetica, Times, etc.)
import org.springframework.core.io.ClassPathResource; // Loads files from inside the JAR
import org.springframework.stereotype.Service;

import java.awt.Font;             // Java AWT Font — used to shape Tamil text, NOT a PDF font
import java.awt.Shape;            // Geometric shape representing glyph outlines
import java.awt.font.FontRenderContext; // Provides rendering hints to the text engine
import java.awt.font.TextLayout;       // Applies OpenType shaping for Tamil characters
import java.awt.geom.AffineTransform;  // Identity transform passed to FontRenderContext
import java.awt.geom.PathIterator;     // Iterates over glyph outline segments (MOVETO, LINETO, etc.)
import java.io.ByteArrayOutputStream;  // Holds the PDF in memory as bytes
import java.io.IOException;
import java.io.InputStream;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;

    // Page layout constants
    private static final float MARGIN      = 50f;                          // Left/right margin in PDF points
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();   // A4 = 841.89 points tall
    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN; // Usable table width
    private static final float COL1_WIDTH  = 200f;                         // Label column width
    private static final float COL2_WIDTH  = TABLE_WIDTH - COL1_WIDTH;     // Value column width
    private static final float ROW_HEIGHT  = 22f;                          // Height of each table row
    private static final float CELL_PAD    = 5f;                           // Padding inside each cell
    private static final int   NUM_ROWS    = 9;                            // Total data rows in the table

    public EmployeeReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public byte[] generateEmployeeReport(Long id) {
        // Returns the entire PDF as a raw byte array
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id)); // 404 if not found

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); // In-memory output stream for the PDF
             PDDocument doc = new PDDocument()) {                        // Create a new empty PDF document

            PDPage page = new PDPage(PDRectangle.A4); // Create one A4 page
            doc.addPage(page); // Add the page to the document

            Font awtTamil14 = loadAwtFont("fonts/NotoSansTamil-Regular.ttf", 14f); // Tamil font for the heading (size 14)
            Font awtLatha11 = loadAwtFont("fonts/LATHA.TTF", 11f);                 // Tamil font for table values (size 11)

            String joiningDate = employee.getDateOfJoining() != null
                    ? employee.getDateOfJoining().toString() : ""; // Convert date to "2024-01-15" string

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) { // Open drawing context for this page

                float y = PAGE_HEIGHT - MARGIN; // Start near the top of the page

                // English title
                cs.beginText(); // Start a text block
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16); // Built-in PDF font, no embedding needed
                cs.newLineAtOffset(MARGIN, y); // Move cursor to starting position
                cs.showText("Employee Service Record Report"); // Draw the English title
                cs.endText();
                y -= 26f; // Move down for the next element

                // Tamil title
                drawTamil(cs, "பணியாளர் சேவை விவர அறிக்கை", awtTamil14, MARGIN, y); // Draw Tamil heading as vector paths
                y -= 30f; // Extra gap before the table

                // Draw the table grid
                drawTableGrid(cs, y); // Draws all horizontal and vertical lines in one stroke() call

                // Table data rows
                String[][] rows = {
                    {"Employee Code",           safe(employee.getEmployeeCode()),        "en"},
                    {"Employee Name (English)", safe(employee.getEmployeeNameEnglish()), "en"},
                    {"Employee Name (Tamil)",   safe(employee.getEmployeeNameTamil()),   "ta"}, // "ta" = use Tamil font
                    {"Designation",             safe(employee.getDesignation()),         "en"},
                    {"Department",              safe(employee.getDepartment()),          "en"},
                    {"Date of Joining",         joiningDate,                            "en"},
                    {"Mobile Number",           safe(employee.getMobileNumber()),        "en"},
                    {"Email",                   safe(employee.getEmail()),               "en"},
                    {"Remarks",                 safe(employee.getRemarks()),             "en"},
                };

                for (String[] r : rows) {
                    rowText(cs, r[0], r[1], "ta".equals(r[2]), awtLatha11, y); // Draw label + value for this row
                    y -= ROW_HEIGHT; // Move down to the next row
                }
            } // PDPageContentStream auto-closes here — page content is finalized

            doc.save(baos); // Write the complete PDF to memory
            return baos.toByteArray(); // Return as raw bytes

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate employee report PDF", ex); // Wrap and throw → HTTP 500
        }
    }

    private Font loadAwtFont(String classpathPath, float size) throws Exception {
        // Loads a TrueType font from the JAR resources into a Java AWT Font object
        ClassPathResource res = new ClassPathResource(classpathPath);
        try (InputStream is = res.getInputStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size); // Parse TTF and set size
        }
    }

    private void drawTableGrid(PDPageContentStream cs, float firstRowBottom) throws IOException {
        // Draws ALL grid lines (horizontal + vertical) in ONE stroke() call to avoid double-stroking shared borders
        float tableTop    = firstRowBottom + ROW_HEIGHT;                  // Top edge of the table
        float tableBottom = firstRowBottom - (NUM_ROWS - 1) * ROW_HEIGHT; // Bottom edge of the table
        float left    = MARGIN;              // Left edge x
        float right   = MARGIN + TABLE_WIDTH; // Right edge x
        float divider = MARGIN + COL1_WIDTH;  // Column divider x

        cs.setLineWidth(0.5f); // Thin lines for a clean look

        for (int i = 0; i <= NUM_ROWS; i++) { // Draw one horizontal line per row boundary
            float lineY = tableTop - i * ROW_HEIGHT;
            cs.moveTo(left,  lineY);
            cs.lineTo(right, lineY);
        }

        // Three vertical lines: left border, column divider, right border
        cs.moveTo(left,    tableTop); cs.lineTo(left,    tableBottom);
        cs.moveTo(divider, tableTop); cs.lineTo(divider, tableBottom);
        cs.moveTo(right,   tableTop); cs.lineTo(right,   tableBottom);

        cs.stroke(); // Render all the lines above in one call
    }

    private void rowText(PDPageContentStream cs, String label, String value,
                         boolean valueTamil, Font tamilFont, float y) throws IOException {
        // Draws the text inside one table row (label in left column, value in right column)
        float textY = y + CELL_PAD + 2f; // Vertical text baseline position inside the cell

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11); // Label always in English Helvetica
        cs.newLineAtOffset(MARGIN + CELL_PAD, textY);
        cs.showText(label); // Draw the label (e.g. "Employee Code")
        cs.endText();

        if (valueTamil) {
            drawTamil(cs, value, tamilFont, MARGIN + COL1_WIDTH + CELL_PAD, textY); // Draw Tamil value as vector paths
        } else {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN + COL1_WIDTH + CELL_PAD, textY);
            cs.showText(value); // Draw English value normally
            cs.endText();
        }
    }

    private void drawTamil(PDPageContentStream cs, String text,
                            Font awtFont, float pdfX, float pdfY) throws IOException {
        // Renders Tamil text as vector glyph paths — works in every PDF viewer without font embedding
        if (text == null || text.isEmpty()) return;

        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true); // Standard rendering context
        Shape outline = new TextLayout(text, awtFont, frc).getOutline(null); // AWT shapes Tamil text correctly (OpenType GSUB/GPOS)

        cs.setNonStrokingColor(0, 0, 0); // Fill color: black

        PathIterator pi = outline.getPathIterator(null); // Walk through the glyph outlines segment by segment
        float[] c = new float[6]; // Coordinate buffer (up to 6 floats per segment)
        float cx = 0, cy = 0;    // Current point (needed for quad→cubic conversion)
        boolean hasPath = false;

        while (!pi.isDone()) {
            int type = pi.currentSegment(c); // Get segment type and fill coordinates into c[]

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    cs.moveTo(pdfX + c[0], pdfY - c[1]); // AWT Y is flipped vs PDF Y, so negate
                    cx = c[0]; cy = c[1];
                    hasPath = true;
                    break;

                case PathIterator.SEG_LINETO:
                    cs.lineTo(pdfX + c[0], pdfY - c[1]); // Straight line with Y-flip
                    cx = c[0]; cy = c[1];
                    break;

                case PathIterator.SEG_QUADTO: {
                    // TrueType uses quadratic curves; PDF only supports cubic — convert using standard formula
                    float qx = c[0], qy = c[1]; // Quadratic control point
                    float ex = c[2], ey = c[3]; // End point
                    float bx1 = cx + 2f/3f*(qx-cx), by1 = cy + 2f/3f*(qy-cy); // Cubic CP1
                    float bx2 = ex + 2f/3f*(qx-ex), by2 = ey + 2f/3f*(qy-ey); // Cubic CP2
                    cs.curveTo(pdfX+bx1, pdfY-by1, pdfX+bx2, pdfY-by2, pdfX+ex, pdfY-ey); // Draw cubic bezier
                    cx = ex; cy = ey;
                    break;
                }

                case PathIterator.SEG_CUBICTO:
                    cs.curveTo(pdfX+c[0], pdfY-c[1], pdfX+c[2], pdfY-c[3], pdfX+c[4], pdfY-c[5]); // Already cubic — pass directly with Y-flip
                    cx = c[4]; cy = c[5];
                    break;

                case PathIterator.SEG_CLOSE:
                    cs.closePath(); // Close the current glyph outline
                    break;
            }
            pi.next(); // Move to next segment
        }

        if (hasPath) cs.fill(); // Fill all glyph outlines with black — Tamil text appears in the PDF
    }

    private String safe(String s) {
        return s != null ? s : ""; // Return empty string instead of null to prevent NullPointerException
    }
}
```

---

### FILE: controller/EmployeeController.java

**Purpose:** REST API gateway — receives HTTP requests from Angular and calls the service layer.

```java
package com.employee.backend.controller;

import com.employee.backend.dto.ApiResponse;
import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;
import com.employee.backend.exception.InvalidFileException;
import com.employee.backend.service.EmployeeReportService;
import com.employee.backend.service.EmployeeService;
import com.employee.backend.service.FileStorageService;
import jakarta.validation.Valid; // Triggers Bean Validation on the @RequestBody

import org.springframework.http.HttpHeaders;   // For setting response headers
import org.springframework.http.HttpStatus;    // HTTP status code constants
import org.springframework.http.MediaType;     // MIME type constants (e.g. APPLICATION_PDF)
import org.springframework.http.ResponseEntity; // Full HTTP response: status + headers + body
import org.springframework.web.bind.annotation.*; // All REST annotations
import org.springframework.web.multipart.MultipartFile; // Represents an uploaded file
import java.io.IOException;
import java.util.List;

@RestController // Handles HTTP requests and auto-converts return values to JSON
@RequestMapping("/api/employees") // All endpoints start with /api/employees
@CrossOrigin(origins = "*") // Allows Angular (localhost:4200) to call this API (different port = CORS)
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;
    private final EmployeeReportService employeeReportService;

    public EmployeeController(EmployeeService employeeService,
                              FileStorageService fileStorageService,
                              EmployeeReportService employeeReportService) { // Spring injects all three services
        this.employeeService = employeeService;
        this.fileStorageService = fileStorageService;
        this.employeeReportService = employeeReportService;
    }

    @PostMapping("/create") // Handles POST /api/employees/create
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO requestDTO) { // @Valid runs validators; @RequestBody reads JSON from request
        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO); // Delegate to service
        ApiResponse<EmployeeResponseDTO> response = new ApiResponse<>("Employee created successfully", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // HTTP 201 Created
    }

    @GetMapping("/list") // Handles GET /api/employees/list
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        ApiResponse<List<EmployeeResponseDTO>> response = new ApiResponse<>("Employee list fetched successfully", employees);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }

    @GetMapping("/{id}") // Handles GET /api/employees/5 (id from URL)
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable Long id) { // @PathVariable reads {id} from URL
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id); // Throws 404 if not found
        ApiResponse<EmployeeResponseDTO> response = new ApiResponse<>("Employee fetched successfully", employee);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}") // Handles PUT /api/employees/update/5
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) { // ID from URL, updated data from request body
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, requestDTO);
        ApiResponse<EmployeeResponseDTO> response = new ApiResponse<>("Employee updated successfully", updated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/{id}") // Handles POST /api/employees/upload/5 (multipart file upload)
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> uploadAppointmentOrder(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException { // "file" must match Angular's formData.append('file', ...)
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty"); // Reject empty uploads → HTTP 400
        }
        long maxSizeBytes = 2L * 1024L * 1024L; // 2MB in bytes
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException("File size must be <= 2 MB"); // Reject large files → HTTP 400
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("Only PDF files are allowed"); // Reject non-PDF → HTTP 400
        }
        String savedPath = fileStorageService.saveAppointmentOrderFile(id, file); // Save to uploads/ folder
        EmployeeResponseDTO updated = employeeService.updateAppointmentOrder(id, savedPath, originalFileName); // Store path in DB
        ApiResponse<EmployeeResponseDTO> response = new ApiResponse<>("Appointment order uploaded successfully", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}") // Handles DELETE /api/employees/delete/5
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id); // Throws 404 if not found; otherwise deletes
        ApiResponse<String> response = new ApiResponse<>("Employee deleted successfully", null);
        return ResponseEntity.ok(response); // HTTP 200
    }

    @GetMapping("/report/{id}") // Handles GET /api/employees/report/5 (returns binary PDF)
    public ResponseEntity<byte[]> generateEmployeeReport(@PathVariable Long id) {
        byte[] pdfBytes = employeeReportService.generateEmployeeReport(id); // Generate PDF in memory
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF); // Tell browser this is a PDF
        headers.setContentDispositionFormData("attachment", "employee_report_" + id + ".pdf"); // Trigger file download
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK); // HTTP 200 with PDF bytes
    }
}
```

---

## 6. FRONTEND — ANGULAR

---

### FILE: src/main.ts

**Purpose:** Entry point — starts the Angular app.

```typescript
import { bootstrapApplication } from "@angular/platform-browser"; // Starts Angular without NgModules
import { appConfig } from "./app/app.config"; // Global providers (Router, HttpClient, etc.)
import { App } from "./app/app"; // Root component that holds <router-outlet>

bootstrapApplication(App, appConfig) // Boot the app: set up DI, render root component, activate router
  .catch((err) => console.error(err)); // Log any startup errors to the browser console
```

---

### FILE: src/app/app.config.ts

**Purpose:** Registers global services (Router, HttpClient, change detection).

```typescript
import {
  ApplicationConfig,
  provideZonelessChangeDetection, // Modern change detection — no Zone.js needed
} from "@angular/core";
import { provideRouter } from "@angular/router"; // Registers the Angular Router
import { provideHttpClient, withFetch } from "@angular/common/http"; // Registers HttpClient using browser Fetch API
import { routes } from "./app.routes"; // Our route definitions

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(), // Use new zoneless mode — components manually trigger re-renders
    provideRouter(routes),            // Enable routing with our route table
    provideHttpClient(withFetch()),   // Make HttpClient available app-wide, using native fetch()
  ],
};
```

---

### FILE: src/app/app.routes.ts

**Purpose:** Maps URL paths to components.

```typescript
import { Routes } from "@angular/router"; // Type for the route array
import { EmployeeListComponent }   from "./employee/employee-list/employee-list";
import { EmployeeCreateComponent } from "./employee/employee-create/employee-create";
import { EmployeeEditComponent }   from "./employee/employee-edit/employee-edit";
import { EmployeeViewComponent }   from "./employee/employee-view/employee-view";
import { EmployeeReportComponent } from "./employee/employee-report/employee-report";

export const routes: Routes = [
  { path: "", redirectTo: "employee/list", pathMatch: "full" }, // "/" redirects to list page
  { path: "employee/list",      component: EmployeeListComponent },    // /employee/list → List page
  { path: "employee/create",    component: EmployeeCreateComponent },  // /employee/create → Create form
  { path: "employee/edit/:id",  component: EmployeeEditComponent },    // /employee/edit/5 → Edit form
  { path: "employee/view/:id",  component: EmployeeViewComponent },    // /employee/view/5 → View details
  { path: "employee/report/:id", component: EmployeeReportComponent }, // /employee/report/5 → PDF report
];
```

---

### FILE: src/app/app.ts

**Purpose:** Root component — contains the nav bar and `<router-outlet>` where pages are shown.

```typescript
import { Component } from "@angular/core";
import { RouterOutlet, RouterLink } from "@angular/router"; // RouterOutlet = page slot; RouterLink = nav links

@Component({
  selector: "app-root",    // Angular mounts this inside <app-root> in index.html
  standalone: true,        // No NgModule — imports declared directly below
  imports: [RouterOutlet, RouterLink], // Allow <router-outlet> and routerLink in this template
  templateUrl: "./app.html",
  styleUrl: "./app.scss",
})
export class App {
  title = "employee-frontend"; // App title (used in template or browser tab)
}
```

---

### FILE: src/app/services/employee.ts

**Purpose:** All HTTP calls to the Spring backend — components use this, never call HTTP directly.

```typescript
import { Injectable } from "@angular/core";  // Marks this class as injectable
import { HttpClient } from "@angular/common/http"; // Angular's HTTP client
import { Observable } from "rxjs"; // HTTP calls return Observables — they fire when you .subscribe()

export interface Employee { // TypeScript type for an employee object (used across the app)
  id?: number;              // Optional — new employees don't have an ID yet
  employeeCode: string;
  employeeNameEnglish: string;
  employeeNameTamil: string;
  designation: string;
  department: string;
  dateOfJoining: string;    // String because form inputs work with strings (Spring parses to LocalDate)
  mobileNumber: string;
  email: string;
  remarks: string;
  appointmentOrderFileName?: string; // Optional — only present if PDF was uploaded
}

@Injectable({
  providedIn: "root", // One singleton instance shared by all components
})
export class EmployeeService {
  private baseUrl = "http://localhost:8080/api/employees"; // Backend base URL — change here if port changes

  constructor(private http: HttpClient) {} // Angular injects HttpClient automatically

  getAllEmployees(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/list`); // GET /api/employees/list — fires when subscribed
  }

  getEmployeeById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`); // GET /api/employees/5
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${this.baseUrl}/create`, employee); // POST with employee JSON as body
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/update/${id}`, employee); // PUT with updated data
  }

  uploadAppointmentOrder(id: number, file: File): Observable<any> {
    const formData = new FormData(); // Required for file uploads — can't send File as JSON
    formData.append("file", file);  // Field name "file" must match @RequestParam("file") in Spring
    return this.http.post<any>(`${this.baseUrl}/upload/${id}`, formData); // POST multipart request
  }

  deleteEmployee(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/delete/${id}`); // DELETE /api/employees/delete/5
  }

  downloadEmployeeReport(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/report/${id}`, {
      responseType: "blob", // Tell Angular: don't parse response as JSON, keep as binary Blob
    });
  }
}
```

---

### FILE: employee-list/employee-list.ts

**Purpose:** Shows all employees in a searchable, sortable table. Main landing page.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common"; // Provides *ngIf, *ngFor in the template
import { FormsModule } from "@angular/forms";   // Provides [(ngModel)] for the search input
import { Router } from "@angular/router";       // For programmatic navigation
import { EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-list",
  standalone: true,
  imports: [CommonModule, FormsModule], // FormsModule for search box; CommonModule for *ngIf / *ngFor
  templateUrl: "./employee-list.html",
  styleUrl: "./employee-list.scss",
})
export class EmployeeListComponent implements OnInit {
  employees: any[] = [];       // All employees fetched from API
  searchTerm: string = "";     // Bound to the search input — updates on every keystroke
  successMessage: string = ""; // Green banner (e.g. "Employee created successfully.")
  errorMessage: string = "";   // Red banner (e.g. "Failed to load employees")
  // FLAG: set to true to show the Delete button (hidden for interview/demo)
  showDeleteButton = false;

  constructor(
    private employeeService: EmployeeService, // For API calls
    private router: Router,                   // For navigation
    private cdr: ChangeDetectorRef,           // For manual re-render (required in Zoneless mode)
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation(); // Get navigation object (only available on arrival)
    this.successMessage =
      navigation?.extras?.state?.["successMessage"] || // Read success message from router state
      (typeof window !== "undefined" ? window.history.state?.successMessage : "") || // Fallback: browser history
      "";

    this.loadEmployees(); // Fetch employees from backend

    if (this.successMessage) {
      setTimeout(() => {
        this.successMessage = ""; // Hide success message after 3 seconds
        this.cdr.markForCheck(); // Tell Angular to re-render
      }, 3000);
    }
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe(
      (response: any) => {
        console.log("Employee list API response:", response);
        if (Array.isArray(response)) {
          this.employees = response;                        // Plain array response
        } else if (response && Array.isArray(response.content)) {
          this.employees = response.content;               // Spring Page object
        } else if (response && Array.isArray(response.data)) {
          this.employees = response.data;                  // Our ApiResponse format: { data: [...] }
        } else if (response && Array.isArray(response.employees)) {
          this.employees = response.employees;             // Alternative response shape
        } else {
          this.employees = [];                             // Unknown format — show empty table
        }
        this.errorMessage = "";
        this.cdr.markForCheck(); // Re-render the table
      },
      (error: any) => {
        console.error("Load employee error:", error);
        this.errorMessage = "Failed to load employees";
        this.employees = [];
        this.cdr.markForCheck();
      },
    );
  }

  get filteredEmployees(): any[] {
    // Computed property — recalculated whenever searchTerm or employees changes
    const value = this.searchTerm.trim().toLowerCase(); // Normalize search input

    const sorted = [...this.employees].sort((a, b) =>
      (a.employeeCode || "").localeCompare(b.employeeCode || "", undefined, { numeric: true }), // Sort by code (numeric-aware)
    );

    if (!value) return sorted; // No search term → return all sorted employees

    return sorted.filter(
      (employee: any) =>
        (employee.employeeCode || "").toLowerCase().includes(value) ||        // Match on code
        (employee.employeeNameEnglish || "").toLowerCase().includes(value) || // Match on English name
        (employee.employeeNameTamil || "").toLowerCase().includes(value),     // Match on Tamil name
    );
  }

  goToAddEmployee(): void { this.router.navigate(["/employee/create"]); }      // Navigate to create page
  goToViewEmployee(id: number): void { this.router.navigate(["/employee/view", id]); } // Navigate to view page
  goToEditEmployee(id: number): void { this.router.navigate(["/employee/edit", id]); } // Navigate to edit page
  goToReport(id: number | undefined): void {
    if (id == null) return; // Guard: do nothing if id is null or undefined
    this.router.navigate(["/employee/report", id]); // Navigate to report page
  }

  deleteEmployee(id: number | undefined): void {
    if (id == null) return; // Guard: do nothing if id is null or undefined
    if (!window.confirm("Are you sure you want to permanently delete this employee?")) return; // Ask user to confirm
    this.employeeService.deleteEmployee(id).subscribe(
      () => {
        this.successMessage = "Employee deleted successfully";
        this.loadEmployees(); // Reload the table after deletion
        setTimeout(() => { this.successMessage = ""; this.cdr.markForCheck(); }, 3000);
      },
      (error: any) => {
        console.error("Delete employee error:", error);
        this.errorMessage = "Failed to delete employee"; // Show red error banner
        this.cdr.markForCheck();
      },
    );
  }
}
```

---

### FILE: employee-list/employee-list.html

```html
<div class="employee-list-container">

  <div class="page-header">
    <h2>Employee List</h2>
    <button class="add-btn" type="button" (click)="goToAddEmployee()">Add Employee</button>
    <!-- (click)="goToAddEmployee()" calls the method when button is clicked -->
  </div>

  <div class="search-box">
    <input
      type="text"
      [(ngModel)]="searchTerm"
      <!-- [(ngModel)] two-way binds the input to searchTerm — typing updates the variable instantly -->
      placeholder=" Search by Employee Name or Employee Code"
      class="search-input"
    />
  </div>

  <div *ngIf="successMessage" class="success-message">
    {{ successMessage }}
    <!-- *ngIf shows this div only when successMessage is non-empty; {{ }} displays the text -->
  </div>

  <div *ngIf="errorMessage" class="error-message">
    {{ errorMessage }}
    <!-- Shows red error banner if API call fails -->
  </div>

  <div *ngIf="filteredEmployees.length > 0; else noEmployees" class="table-wrapper">
    <!-- *ngIf with else: shows table if there are results, or #noEmployees template if empty -->

    <table class="employee-table">
      <thead>
        <tr>
          <th>ID</th><th>Employee Code</th><th>Name English</th>
          <th>Name Tamil</th><th>Designation</th><th>Department</th>
          <th>Date of Joining</th><th>Mobile</th><th>Email</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let employee of filteredEmployees">
          <!-- *ngFor repeats this <tr> for each employee in the filtered list -->
          <td>{{ employee.id }}</td>
          <td>{{ employee.employeeCode }}</td>
          <td>{{ employee.employeeNameEnglish }}</td>
          <td>{{ employee.employeeNameTamil }}</td>
          <!-- Browser renders Tamil Unicode natively — no special font needed -->
          <td>{{ employee.designation }}</td>
          <td>{{ employee.department }}</td>
          <td>{{ employee.dateOfJoining }}</td>
          <td>{{ employee.mobileNumber }}</td>
          <td>{{ employee.email }}</td>
          <td class="actions">
            <button class="view-btn"   type="button" (click)="goToViewEmployee(employee.id)">View</button>
            <button class="edit-btn"   type="button" (click)="goToEditEmployee(employee.id)">Edit</button>
            <button class="report-btn" type="button" (click)="goToReport(employee.id)">Report</button>
            @if (showDeleteButton) {
              <button class="delete-btn" type="button" (click)="deleteEmployee(employee.id)">Delete</button>
              <!-- showDeleteButton flag: set to true to test; keep false for interview/demo -->
            }
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <ng-template #noEmployees>
    <div class="no-data">No employees found.</div>
    <!-- Shown when *ngIf above is false (no employees or no search matches) -->
  </ng-template>

</div>
```

---

### FILE: employee-create/employee-create.ts

**Purpose:** Create New Employee form — builds a reactive form, validates it, and calls the API.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,          // Creates FormGroup objects easily
  FormGroup,            // Holds all form controls and tracks validity
  ReactiveFormsModule,  // Enables [formGroup] and formControlName in templates
  Validators,           // Built-in validators: required, email, pattern, etc.
} from "@angular/forms";
import { Router } from "@angular/router";
import { Employee, EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-create",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // ReactiveFormsModule for [formGroup] in template
  templateUrl: "./employee-create.html",
  styleUrl: "./employee-create.scss",
})
export class EmployeeCreateComponent implements OnInit {
  employeeForm!: FormGroup; // "!" tells TypeScript: this will be assigned in ngOnInit, not the constructor
  selectedFile: File | null = null; // The PDF file the user picks (null = no file selected)
  successMessage: string = "";
  errorMessage: string = "";

  designations: string[] = [ // Dropdown options for the designation field
    "Junior Assistant", "Senior Assistant", "Developer",
    "Senior Developer", "Manager", "Administrator",
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({ // Build the form with controls and validators
      employeeCode:         ["", Validators.required],  // Empty initial value, required
      employeeNameEnglish:  ["", Validators.required],
      employeeNameTamil:    ["", Validators.required],
      designation:          ["", Validators.required],
      department:           ["", Validators.required],
      dateOfJoining:        ["", Validators.required],
      mobileNumber:         ["", [Validators.required, Validators.pattern("^[0-9]{10}$")]], // Must be exactly 10 digits
      email:                ["", [Validators.required, Validators.email]], // Valid email format
      remarks:              [""], // Optional — no validators
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0]; // Get the first selected file
    if (!file) return; // User cancelled the file dialog
    if (file.type !== "application/pdf") { // Check MIME type
      this.errorMessage = "Only PDF files are allowed.";
      this.selectedFile = null;
      return;
    }
    if (file.size > 2 * 1024 * 1024) { // 2MB limit
      this.errorMessage = "File size must be less than or equal to 2 MB.";
      this.selectedFile = null;
      return;
    }
    this.errorMessage = "";
    this.selectedFile = file; // Store valid file for upload in onSubmit()
  }

  onSubmit(): void {
    this.successMessage = "";
    this.errorMessage = "";
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched(); // Force all validation error messages to show
      return; // Stop — don't send invalid data to the server
    }
    const employeeData: Employee = this.employeeForm.value; // Get plain object of all form values
    this.employeeService.createEmployee(employeeData).subscribe({
      next: (response: any) => {
        const created = response?.data ? response.data : response; // Unwrap from ApiResponse if needed
        if (this.selectedFile && created?.id) {
          this.uploadFile(created.id); // Upload PDF after employee is created (need the ID first)
        } else {
          this.router.navigate(["/employee/list"], {
            state: { successMessage: "Employee created successfully." }, // Pass message to list page
          });
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = "Failed to create employee. Employee code may already exist.";
        this.cdr.markForCheck();
      },
    });
  }

  private uploadFile(employeeId: number): void {
    this.employeeService.uploadAppointmentOrder(employeeId, this.selectedFile!).subscribe({ // "!" = we know it's not null
      next: () => {
        this.router.navigate(["/employee/list"], {
          state: { successMessage: "Employee created successfully." },
        });
      },
      error: () => {
        this.errorMessage = "Employee saved, but file upload failed."; // Employee exists but file failed
        this.cdr.markForCheck();
      },
    });
  }

  goToList(): void { this.router.navigate(["/employee/list"]); } // Cancel button → back to list

  get f() { return this.employeeForm.controls; } // Shorthand: f['employeeCode'] instead of employeeForm.controls['employeeCode']
}
```

---

### FILE: employee-create/employee-create.html

```html
<div class="form-container">
  <h2>Create Employee</h2>

  @if (successMessage) {
    <p class="success-message">{{ successMessage }}</p>
    <!-- @if is Angular 17+ control flow — shows paragraph only when successMessage is truthy -->
  }
  @if (errorMessage) {
    <p class="error-message">{{ errorMessage }}</p>
  }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <!-- [formGroup] links this <form> to the FormGroup; (ngSubmit) calls onSubmit() on submit -->

    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      <!-- formControlName links this input to the 'employeeCode' FormControl -->
      @if (f['employeeCode'].touched && f['employeeCode'].invalid) {
        <small class="error">Employee Code is required.</small>
        <!-- Shows only after user interacted with the field AND it's invalid -->
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
      <!-- User types Tamil using their OS Tamil keyboard input method -->
      @if (f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid) {
        <small class="error">Employee Name Tamil is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <option value="">-- Select Designation --</option>
        <!-- Empty value keeps the control invalid until user picks a real option -->
        @for (designation of designations; track designation) {
          <option [value]="designation">{{ designation }}</option>
          <!-- @for loops over the designations array; track = optimization key -->
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
      <!-- type="date" shows a date picker; value stored as "YYYY-MM-DD" string -->
      @if (f['dateOfJoining'].touched && f['dateOfJoining'].invalid) {
        <small class="error">Date of Joining is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      <!-- maxlength="10" is a UI hint; actual validation is the pattern validator -->
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['required']) {
        <small class="error">Mobile Number is required.</small>
      }
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern']) {
        <small class="error">Mobile Number must be exactly 10 digits.</small>
        <!-- Separate messages for "empty" vs "wrong format" -->
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
      <!-- Optional field — no validation error needed -->
    </div>

    <div class="form-group">
      <label>Appointment Order (PDF only)</label>
      <input type="file" accept="application/pdf" (change)="onFileChange($event)" />
      <!-- (change) fires when user picks a file; $event carries the file data -->
    </div>

    <div class="button-group">
      <button type="submit">Save Employee</button>
      <!-- type="submit" triggers (ngSubmit) → onSubmit() -->
      <button type="button" class="cancel-btn" (click)="goToList()">Cancel</button>
      <!-- type="button" prevents form submission; navigates back to list -->
    </div>
  </form>
</div>
```

---

### FILE: employee-edit/employee-edit.ts

**Purpose:** Loads an existing employee, pre-fills the form, and handles update + optional file replacement.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router"; // ActivatedRoute reads :id from the URL
import { Employee, EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-edit",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./employee-edit.html",
  styleUrl: "./employee-edit.scss",
})
export class EmployeeEditComponent implements OnInit {
  employeeForm!: FormGroup;
  employeeId!: number; // The ID read from the URL (e.g. /employee/edit/5 → employeeId = 5)
  selectedFile: File | null = null;
  currentAppointmentOrderFileName: string = ""; // Existing file name shown in the form
  successMessage: string = "";
  errorMessage: string = "";
  designations: string[] = [
    "Junior Assistant", "Senior Assistant", "Developer",
    "Senior Developer", "Manager", "Administrator",
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute, // Gives access to URL parameters
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({ // Same form structure as Create
      employeeCode:        ["", Validators.required],
      employeeNameEnglish: ["", Validators.required],
      employeeNameTamil:   ["", Validators.required],
      designation:         ["", Validators.required],
      department:          ["", Validators.required],
      dateOfJoining:       ["", Validators.required],
      mobileNumber:        ["", [Validators.required, Validators.pattern("^[0-9]{10}$")]],
      email:               ["", [Validators.required, Validators.email]],
      remarks:             [""],
    });

    const id = this.route.snapshot.paramMap.get("id"); // Read :id from the URL string
    if (id) {
      this.employeeId = Number(id); // Convert "5" → 5
      this.loadEmployee(this.employeeId); // Fetch data and pre-fill the form
    } else {
      this.errorMessage = "Employee ID not found in URL.";
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        const employee = response?.data ? response.data : response; // Unwrap ApiResponse
        this.employeeForm.patchValue({ // Fill form controls with existing data
          employeeCode:        employee.employeeCode || "",
          employeeNameEnglish: employee.employeeNameEnglish || "",
          employeeNameTamil:   employee.employeeNameTamil || "",
          designation:         employee.designation || "",
          department:          employee.department || "",
          dateOfJoining:       employee.dateOfJoining || "", // "YYYY-MM-DD" matches <input type="date">
          mobileNumber:        employee.mobileNumber || "",
          email:               employee.email || "",
          remarks:             employee.remarks || "",
        });
        this.currentAppointmentOrderFileName = employee.appointmentOrderFileName || ""; // Show existing file name
        this.employeeForm.updateValueAndValidity(); // Re-run validators after filling in values
        this.errorMessage = "";
        this.cdr.detectChanges(); // Immediately update the UI with loaded data
      },
      error: () => {
        this.errorMessage = "Failed to load employee details.";
        this.cdr.detectChanges();
      },
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (!file) return;
    if (file.type !== "application/pdf") {
      this.errorMessage = "Only PDF files are allowed."; this.selectedFile = null; return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = "File size must be less than or equal to 2 MB."; this.selectedFile = null; return;
    }
    this.errorMessage = ""; this.selectedFile = file;
  }

  onSubmit(): void {
    this.successMessage = ""; this.errorMessage = "";
    if (this.employeeForm.invalid) { this.employeeForm.markAllAsTouched(); return; }
    const employeeData: Employee = this.employeeForm.value;
    this.employeeService.updateEmployee(this.employeeId, employeeData).subscribe({ // PUT request
      next: (response: any) => {
        const updated = response?.data ? response.data : response;
        if (this.selectedFile && updated?.id) {
          this.uploadFile(updated.id); // Upload new PDF if user selected one
        } else {
          this.router.navigate(["/employee/list"], { state: { successMessage: "Employee updated successfully." } });
        }
        this.cdr.markForCheck();
      },
      error: () => { this.errorMessage = "Failed to update employee."; this.cdr.markForCheck(); },
    });
  }

  private uploadFile(employeeId: number): void {
    this.employeeService.uploadAppointmentOrder(employeeId, this.selectedFile!).subscribe({
      next: () => { this.router.navigate(["/employee/list"], { state: { successMessage: "Employee updated successfully." } }); },
      error: () => { this.errorMessage = "Employee saved, but file upload failed."; this.cdr.markForCheck(); },
    });
  }

  goToList(): void { this.router.navigate(["/employee/list"]); }
  get f() { return this.employeeForm.controls; }
}
```

---

### FILE: employee-edit/employee-edit.html

```html
<div class="form-container">
  <h2>Edit Employee</h2>

  @if (successMessage) { <p class="success-message">{{ successMessage }}</p> }
  @if (errorMessage)   { <p class="error-message">{{ errorMessage }}</p> }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <!-- Same fields as Create — pre-filled via patchValue() in loadEmployee() -->

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
      <!-- Shows the existing PDF filename so the user knows what's already uploaded -->
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

---

### FILE: employee-view/employee-view.ts

**Purpose:** Read-only display of a single employee's complete details.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, RouterLink } from "@angular/router"; // RouterLink for "Back to List" link
import { EmployeeService, Employee } from "../../services/employee";

@Component({
  selector: "app-employee-view",
  standalone: true,
  imports: [CommonModule, RouterLink], // RouterLink enables <a routerLink="..."> in template
  templateUrl: "./employee-view.html",
  styleUrl: "./employee-view.scss",
})
export class EmployeeViewComponent implements OnInit {
  employee: Employee | null = null; // null = not loaded yet; Employee = loaded successfully
  errorMessage: string = "";

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id"); // Read :id from /employee/view/:id
    if (id) {
      this.loadEmployee(Number(id)); // Fetch employee data
    } else {
      this.errorMessage = "Employee ID not found in URL.";
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        console.log("Employee details response:", response);
        if (response && response.data) {
          this.employee = response.data; // Unwrap from ApiResponse
        } else {
          this.employee = response; // Use directly if already the employee object
        }
        this.errorMessage = "";
        this.cdr.detectChanges(); // Re-render with loaded data
      },
      error: (error) => {
        console.error("Failed to load employee details:", error);
        this.employee = null;
        this.errorMessage = "Failed to load employee details.";
        this.cdr.detectChanges();
      },
    });
  }
}
```

---

### FILE: employee-view/employee-view.html

```html
<div class="view-container">
  <h2>Employee Details</h2>

  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>
  <!-- Shows error if employee failed to load -->

  <div *ngIf="employee" class="details-card">
    <!-- *ngIf="employee": only renders when employee is not null (after data loads) -->

    <p><strong>Employee Code:</strong> {{ employee?.employeeCode }}</p>
    <!-- employee? = safe navigation — avoids error if employee is null -->
    <p><strong>Employee Name English:</strong> {{ employee?.employeeNameEnglish }}</p>
    <p><strong>Employee Name Tamil:</strong> {{ employee?.employeeNameTamil }}</p>
    <!-- Browser renders Tamil Unicode natively -->
    <p><strong>Designation:</strong> {{ employee?.designation }}</p>
    <p><strong>Department:</strong> {{ employee?.department }}</p>
    <p><strong>Date of Joining:</strong> {{ employee?.dateOfJoining }}</p>
    <p><strong>Mobile Number:</strong> {{ employee?.mobileNumber }}</p>
    <p><strong>Email:</strong> {{ employee?.email }}</p>
    <p><strong>Remarks:</strong> {{ employee?.remarks || 'No remarks' }}</p>
    <!-- || 'No remarks' shows fallback text when remarks is null/empty -->
    <p><strong>Uploaded File Name:</strong> {{ employee?.appointmentOrderFileName || 'No file uploaded' }}</p>

    <div class="button-group">
      <a routerLink="/employee/list">Back to List</a>
      <!-- routerLink navigates without a full page reload (SPA navigation) -->
    </div>
  </div>
</div>
```

---

### FILE: employee-report/employee-report.ts

**Purpose:** Shows employee details on screen and lets the user download a PDF report.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { EmployeeService, Employee } from "../../services/employee";

@Component({
  selector: "app-employee-report",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./employee-report.html",
  styleUrl: "./employee-report.scss",
})
export class EmployeeReportComponent implements OnInit {
  employee: Employee | null = null;
  errorMessage: string = "";
  downloading: boolean = false; // true = button shows "Downloading..." and is disabled

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id"); // Read :id from /employee/report/:id
    if (id) { this.loadEmployee(Number(id)); }
    else { this.errorMessage = "Employee ID not found in URL."; this.cdr.detectChanges(); }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        this.employee = response?.data ? response.data : response; // Unwrap ApiResponse
        this.errorMessage = "";
        this.cdr.detectChanges();
      },
      error: () => {
        this.employee = null;
        this.errorMessage = "Failed to load employee details.";
        this.cdr.detectChanges();
      },
    });
  }

  downloadReport(): void {
    if (!this.employee?.id) return; // Guard: can't download if employee isn't loaded
    this.downloading = true; // Disable button to prevent double-clicks
    this.employeeService.downloadEmployeeReport(this.employee.id).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob); // Create a temporary browser URL for the binary PDF
        const a = document.createElement("a"); // Create an invisible <a> element
        a.href = url;
        a.download = `employee-report-${this.employee!.id}.pdf`; // Suggested filename for the download
        a.click(); // Programmatically click to trigger the file download
        URL.revokeObjectURL(url); // Free memory after download starts
        this.downloading = false; // Re-enable the button
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = "Failed to download PDF report.";
        this.downloading = false; // Re-enable so user can try again
        this.cdr.detectChanges();
      },
    });
  }
}
```

---

### FILE: employee-report/employee-report.html

```html
<div class="report-container">
  <h2>Employee Service Record Report</h2>
  <p class="report-subtitle">பணியாளர் சேவை விவர அறிக்கை</p>
  <!-- Tamil subtitle displays natively in the browser — no special font needed -->

  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>

  <div *ngIf="employee" class="report-card">
    <!-- Only renders when employee data has loaded -->

    <div class="report-row">
      <span class="label">Employee Code</span>
      <span class="value">{{ employee.employeeCode }}</span>
      <!-- Each report-row shows one label+value pair side by side -->
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
      <button
        class="download-btn"
        type="button"
        (click)="downloadReport()"
        [disabled]="downloading"
      >
        <!-- [disabled]="downloading" disables the button while download is in progress -->
        {{ downloading ? 'Downloading...' : 'Download PDF Report' }}
        <!-- Ternary in interpolation: changes button text based on state -->
      </button>

      <a routerLink="/employee/list">Back to List</a>
    </div>
  </div>
</div>
```

---

### FILE: employee-form/employee-form.ts

**Purpose:** A shared form component that handles BOTH Create and Edit in one — detects mode from the URL.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { Employee, EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-form",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./employee-form.html",
  styleUrl: "./employee-form.scss",
})
export class EmployeeFormComponent implements OnInit {
  employeeForm!: FormGroup;
  isEditMode: boolean = false;       // true if :id in URL → edit mode; false → create mode
  employeeId!: number;
  selectedFile: File | null = null;
  currentAppointmentOrderFileName: string = "";
  successMessage: string = "";
  errorMessage: string = "";
  designations: string[] = [
    "Junior Assistant", "Senior Assistant", "Developer",
    "Senior Developer", "Manager", "Administrator",
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.initializeForm(); // Build the form
    const id = this.route.snapshot.paramMap.get("id"); // Check if :id is in the URL
    if (id) {
      this.isEditMode = true;    // ID present → we're editing
      this.employeeId = Number(id);
      this.loadEmployee(this.employeeId); // Pre-fill form with existing data
    }
    // No ID → isEditMode stays false → create mode
  }

  initializeForm(): void {
    this.employeeForm = this.fb.group({ // Same fields for both create and edit
      employeeCode:        ["", Validators.required],
      employeeNameEnglish: ["", Validators.required],
      employeeNameTamil:   ["", Validators.required],
      designation:         ["", Validators.required],
      department:          ["", Validators.required],
      dateOfJoining:       ["", Validators.required],
      mobileNumber:        ["", [Validators.required, Validators.pattern("^[0-9]{10}$")]],
      email:               ["", [Validators.required, Validators.email]],
      remarks:             [""],
    });
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        const employee = response?.data ? response.data : response;
        this.employeeForm.patchValue({ // Fill form with existing values
          employeeCode: employee.employeeCode || "",
          employeeNameEnglish: employee.employeeNameEnglish || "",
          employeeNameTamil: employee.employeeNameTamil || "",
          designation: employee.designation || "",
          department: employee.department || "",
          dateOfJoining: employee.dateOfJoining || "",
          mobileNumber: employee.mobileNumber || "",
          email: employee.email || "",
          remarks: employee.remarks || "",
        });
        this.currentAppointmentOrderFileName = employee.appointmentOrderFileName || "";
        this.employeeForm.updateValueAndValidity();
        this.errorMessage = "";
        this.cdr.detectChanges();
      },
      error: (error) => { this.errorMessage = "Failed to load employee details."; this.cdr.detectChanges(); },
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type !== "application/pdf") { this.errorMessage = "Only PDF files are allowed."; this.selectedFile = null; return; }
      if (file.size > 2 * 1024 * 1024) { this.errorMessage = "File size must be less than or equal to 2 MB."; this.selectedFile = null; return; }
      this.errorMessage = ""; this.selectedFile = file;
    }
  }

  onSubmit(): void {
    this.successMessage = ""; this.errorMessage = "";
    if (this.employeeForm.invalid) { this.employeeForm.markAllAsTouched(); return; }
    const employeeData: Employee = this.employeeForm.value;

    if (this.isEditMode) { // ── UPDATE ──
      this.employeeService.updateEmployee(this.employeeId, employeeData).subscribe({
        next: (response: any) => {
          const updatedEmployee = response?.data ? response.data : response;
          this.successMessage = "Employee updated successfully.";
          if (this.selectedFile && updatedEmployee?.id) { this.uploadFile(updatedEmployee.id); }
          else { this.router.navigate(["/employee/list"], { state: { successMessage: "Employee updated successfully." } }); }
          this.cdr.markForCheck();
        },
        error: () => { this.errorMessage = "Failed to update employee."; this.cdr.markForCheck(); },
      });
    } else { // ── CREATE ──
      this.employeeService.createEmployee(employeeData).subscribe({
        next: (response: any) => {
          const createdEmployee = response?.data ? response.data : response;
          this.successMessage = "Employee created successfully.";
          if (this.selectedFile && createdEmployee?.id) { this.uploadFile(createdEmployee.id); }
          else { this.router.navigate(["/employee/list"], { state: { successMessage: "Employee created successfully." } }); }
          this.cdr.markForCheck();
        },
        error: () => { this.errorMessage = "Failed to create employee. Employee code may already exist."; this.cdr.markForCheck(); },
      });
    }
  }

  uploadFile(employeeId: number): void {
    if (!this.selectedFile) {
      this.router.navigate(["/employee/list"], {
        state: { successMessage: this.isEditMode ? "Employee updated successfully." : "Employee created successfully." },
      });
      return;
    }
    this.employeeService.uploadAppointmentOrder(employeeId, this.selectedFile).subscribe({
      next: () => {
        this.router.navigate(["/employee/list"], {
          state: { successMessage: this.isEditMode ? "Employee updated successfully." : "Employee created successfully." },
        });
      },
      error: () => { this.errorMessage = "Employee saved, but file upload failed."; this.cdr.markForCheck(); },
    });
  }

  get f() { return this.employeeForm.controls; }
}
```

---

### FILE: employee-form/employee-form.html

```html
<div class="form-container">
  <h2>{{ isEditMode ? 'Edit Employee' : 'Create Employee' }}</h2>
  <!-- Title changes based on isEditMode — ternary inside {{ }} -->

  <p class="success-message" *ngIf="successMessage">{{ successMessage }}</p>
  <p class="error-message"   *ngIf="errorMessage">{{ errorMessage }}</p>
  <!-- *ngIf (old syntax) — same as @if but requires CommonModule -->

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">

    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      <small class="error" *ngIf="f['employeeCode'].touched && f['employeeCode'].invalid">
        Employee Code is required.
      </small>
    </div>

    <div class="form-group">
      <label>Employee Name English</label>
      <input type="text" formControlName="employeeNameEnglish" />
      <small class="error" *ngIf="f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid">
        Employee Name English is required.
      </small>
    </div>

    <div class="form-group">
      <label>Employee Name Tamil</label>
      <input type="text" formControlName="employeeNameTamil" />
      <small class="error" *ngIf="f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid">
        Employee Name Tamil is required.
      </small>
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <option value="">-- Select Designation --</option>
        <option *ngFor="let designation of designations" [value]="designation">
          {{ designation }}
          <!-- *ngFor (old syntax) loops over the designations array -->
        </option>
      </select>
      <small class="error" *ngIf="f['designation'].touched && f['designation'].invalid">
        Designation is required.
      </small>
    </div>

    <div class="form-group">
      <label>Department</label>
      <input type="text" formControlName="department" />
      <small class="error" *ngIf="f['department'].touched && f['department'].invalid">
        Department is required.
      </small>
    </div>

    <div class="form-group">
      <label>Date of Joining</label>
      <input type="date" formControlName="dateOfJoining" />
      <small class="error" *ngIf="f['dateOfJoining'].touched && f['dateOfJoining'].invalid">
        Date of Joining is required.
      </small>
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      <small class="error" *ngIf="f['mobileNumber'].touched && f['mobileNumber'].errors?.['required']">Mobile Number is required.</small>
      <small class="error" *ngIf="f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern']">Mobile Number must be exactly 10 digits.</small>
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      <small class="error" *ngIf="f['email'].touched && f['email'].errors?.['required']">Email is required.</small>
      <small class="error" *ngIf="f['email'].touched && f['email'].errors?.['email']">Enter a valid email address.</small>
    </div>

    <div class="form-group">
      <label>Remarks</label>
      <textarea formControlName="remarks" rows="4"></textarea>
    </div>

    <div class="form-group" *ngIf="isEditMode">
      <!-- Only shows in edit mode — current file info not relevant when creating -->
      <label>Current Appointment Order</label>
      <p>{{ currentAppointmentOrderFileName || 'No file uploaded' }}</p>
    </div>

    <div class="form-group">
      <label>Appointment Order (PDF only)</label>
      <input type="file" accept="application/pdf" (change)="onFileChange($event)" />
      <small *ngIf="isEditMode">Select a new PDF only if you want to replace the existing file.</small>
      <!-- Replacement hint only shown in edit mode -->
    </div>

    <div class="button-group">
      <button type="submit">
        {{ isEditMode ? 'Update Employee' : 'Save Employee' }}
        <!-- Button label changes based on mode -->
      </button>
    </div>
  </form>
</div>
```

---

## SUMMARY TABLES

### Angular Concepts Used

| Concept                                     | Where Used                  | What It Does                                       |
| ------------------------------------------- | --------------------------- | -------------------------------------------------- |
| `standalone: true`                          | All components              | No NgModule needed; imports declared per-component |
| `FormGroup` + `FormBuilder`                 | Create/Edit/Form components | Reactive form with programmatic validation         |
| `Validators.required`, `.email`, `.pattern` | Form components             | Built-in form field validators                     |
| `markAllAsTouched()`                        | Form submit                 | Shows all validation errors at once                |
| `patchValue()`                              | Edit/Form components        | Pre-fills form with loaded data                    |
| `*ngIf` / `@if`                             | All templates               | Conditional rendering                              |
| `*ngFor` / `@for`                           | List template               | Loop to render table rows / dropdown options       |
| `[(ngModel)]`                               | List component              | Two-way binding for search input                   |
| `[formGroup]` + `formControlName`           | Form templates              | Connects HTML inputs to FormGroup controls         |
| `(click)` / `(ngSubmit)` / `(change)`       | Templates                   | DOM event binding                                  |
| `[disabled]`                                | Report button               | Property binding to disable button dynamically     |
| `ChangeDetectorRef`                         | All components              | Manual change detection in Zoneless mode           |
| `ActivatedRoute`                            | Edit/View/Report            | Reads URL route parameters (:id)                   |
| `Router.navigate()`                         | All components              | Programmatic URL navigation                        |
| `routerLink`                                | View/Report templates       | Template-based navigation links                    |
| `HttpClient`                                | EmployeeService             | HTTP calls to Spring backend                       |
| `Observable` + `.subscribe()`               | Service + components        | Async HTTP response handling                       |
| `responseType: 'blob'`                      | downloadEmployeeReport      | Receive binary PDF instead of JSON                 |
| `URL.createObjectURL()`                     | Report component            | Convert Blob to downloadable browser URL           |
| `provideZonelessChangeDetection`            | app.config.ts               | Modern Zone-free change detection                  |

### Spring Boot Concepts Used

| Concept                                              | Where Used             | What It Does                                            |
| ---------------------------------------------------- | ---------------------- | ------------------------------------------------------- |
| `@SpringBootApplication`                             | Main class             | Enables component scan + auto-configuration             |
| `@Entity` + `@Table`                                 | Employee               | Maps Java class to PostgreSQL table                     |
| `@Id` + `@GeneratedValue`                            | Employee               | Auto-increment primary key                              |
| `@Column`                                            | Employee               | Maps fields to columns with constraints                 |
| `JpaRepository`                                      | EmployeeRepository     | Free CRUD methods via Spring Data                       |
| Method Name Queries                                  | EmployeeRepository     | SQL generated from method name                          |
| `@Service` + `@Transactional`                        | ServiceImpl            | Business logic + DB transaction management              |
| `@RestController`                                    | EmployeeController     | REST API handler with JSON responses                    |
| `@RequestMapping` + `@GetMapping` etc.               | Controller             | Maps URLs to methods                                    |
| `@CrossOrigin`                                       | Controller             | Enables Angular-to-Spring CORS                          |
| `@Valid` + `@RequestBody`                            | Controller             | Validates incoming JSON data                            |
| `@PathVariable`                                      | Controller             | Reads URL path parameters                               |
| `@RequestParam`                                      | Controller             | Reads query/form parameters (file upload)               |
| `ResponseEntity`                                     | Controller             | Full HTTP response control                              |
| `@ExceptionHandler`                                  | GlobalExceptionHandler | Catches specific exceptions                             |
| `@RestControllerAdvice`                              | GlobalExceptionHandler | Global error handling for all controllers               |
| `@NotBlank` / `@Email` / `@Size`                     | RequestDTO             | Bean Validation annotations                             |
| `@Value`                                             | FileStorageService     | Injects application.properties values                   |
| `ClassPathResource`                                  | ReportService          | Loads TTF font files from JAR classpath                 |
| `ByteArrayOutputStream`                              | ReportService          | In-memory PDF generation (no temp file on disk)         |
| `MultipartFile`                                      | FileStorageService     | Represents uploaded file in HTTP request                |
| Apache PDFBox (`PDDocument`, `PDPageContentStream`)  | ReportService          | PDF generation; English text via PDType1Font            |
| AWT `TextLayout` + `PathIterator`                    | ReportService          | Tamil text shaped by JVM → exported as vector paths     |

---

_End of README_CODE_SIMPLE.md_
_All 17 backend files and 12 frontend files — every code line has one short comment._
