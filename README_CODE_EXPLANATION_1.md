# README_CODE_EXPLANATION.md

# Employee Service Record Management System — Complete Code Explanation

# (Interview-Ready: Line-by-Line with Flow Tracing)

---

## TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Complete Application Flow (All User Journeys)](#3-complete-application-flow)
4. [API Connection Map](#4-api-connection-map)
5. [BACKEND — Spring Boot](#5-backend--spring-boot)
   - pom.xml
   - application.properties
   - EmployeeBackendApplication.java
   - Employee.java (Entity)
   - EmployeeRepository.java
   - ApiResponse.java
   - EmployeeRequestDTO.java
   - EmployeeResponseDTO.java
   - Custom Exceptions
   - GlobalExceptionHandler.java
   - EmployeeService.java (Interface)
   - EmployeeServiceImpl.java
   - FileStorageService.java
   - EmployeeReportService.java
   - EmployeeController.java
6. [FRONTEND — Angular](#6-frontend--angular)
   - main.ts
   - app.config.ts
   - app.routes.ts
   - app.ts
   - services/employee.ts
   - employee-list.ts + .html
   - employee-create.ts + .html
   - employee-edit.ts + .html
   - employee-view.ts + .html
   - employee-form.ts + .html
   - employee-report.ts + .html

---

## 1. PROJECT OVERVIEW

This is a **full-stack Employee Service Record Management System**.

- The **backend** is built with **Spring Boot 3.5.15 (Java 17)** and exposes a REST API.
- The **frontend** is built with **Angular 21** (standalone components, no NgModules).
- The **database** is **PostgreSQL** (managed via Hibernate/JPA).
- The system lets you **Create, View, Edit** employee records, **upload PDF appointment orders**, and **download PDF service record reports** (with Tamil language support).

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

## 3. COMPLETE APPLICATION FLOW

### Flow 1: Backend Startup

```
JVM starts main()
  → SpringApplication.run(EmployeeBackendApplication.class, args)
  → Spring Boot scans all classes with @SpringBootApplication (same package + sub-packages)
  → Finds @RestController EmployeeController  → registers as HTTP handler bean
  → Finds @Service EmployeeServiceImpl        → registers as service bean
  → Finds @Service FileStorageService         → registers, creates "uploads/" folder on disk
  → Finds @Service EmployeeReportService      → registers as PDF generator bean
  → Finds EmployeeRepository (JpaRepository)  → Spring Data auto-generates SQL implementation
  → Reads application.properties              → connects to PostgreSQL on localhost:5432
  → Hibernate sees @Entity Employee           → runs "ddl-auto=update" → creates/updates "employees" table
  → Embedded Tomcat starts on port 8080
  → Application is READY to accept HTTP requests
```

### Flow 2: Frontend Startup

```
Browser loads index.html
  → <script> loads main.ts bundle
  → bootstrapApplication(App, appConfig)
  → Registers providers: Router, HttpClient, ZonelessChangeDetection
  → Angular renders <app-root> selector → mounts App component
  → App component template has <router-outlet>
  → Router reads current URL "/"
  → Matches route { path: '', redirectTo: 'employee/list' }
  → Redirects to /employee/list
  → Router loads EmployeeListComponent into <router-outlet>
  → EmployeeListComponent.ngOnInit() fires
  → Calls EmployeeService.getAllEmployees()
  → HttpClient sends GET http://localhost:8080/api/employees/list
  → Response arrives → employees[] array is populated → table renders
```

### Flow 3: Create Employee (Complete End-to-End)

```
User clicks "Add Employee" button
  → EmployeeListComponent.goToAddEmployee()
  → router.navigate(['/employee/create'])
  → Router loads EmployeeCreateComponent
  → ngOnInit() → builds FormGroup with validators
  → User fills in all fields + optionally selects a PDF file
  → onFileChange() → validates file type (PDF only) and size (≤ 2MB)
  → User clicks "Save Employee" → onSubmit() fires
  → Form validity check → if invalid: markAllAsTouched() shows errors, stops here
  → If valid: reads form value as Employee object
  → EmployeeService.createEmployee(employeeData)
  → HttpClient.post('http://localhost:8080/api/employees/create', employeeData)
  → HTTP POST reaches EmployeeController.createEmployee()
  → @Valid annotation triggers Bean Validation on EmployeeRequestDTO
    → If validation fails: GlobalExceptionHandler catches MethodArgumentNotValidException
    → Returns HTTP 400 with error message
  → If valid: EmployeeServiceImpl.createEmployee(requestDTO)
    → employeeRepository.existsByEmployeeCode(code) → checks for duplicates
    → If duplicate: throws DuplicateEmployeeCodeException
      → GlobalExceptionHandler returns HTTP 400
    → If unique: new Employee() created
    → mapRequestToEntity() copies all DTO fields to entity
    → Sets createdAt and updatedAt to LocalDateTime.now()
    → employeeRepository.save(employee) → Hibernate INSERT INTO employees (...)
    → PostgreSQL assigns auto-increment ID
    → mapEntityToResponse() → builds EmployeeResponseDTO
    → Returns ApiResponse("Employee created successfully", dto)
    → HTTP 201 Created sent back to Angular
  → Angular subscribe next() fires
    → Extracts created employee from response.data
    → If PDF file was selected:
      → EmployeeService.uploadAppointmentOrder(created.id, file)
      → FormData with file appended
      → HttpClient.post('/api/employees/upload/{id}', formData)
      → EmployeeController.uploadAppointmentOrder()
        → Validates file not empty, size ≤ 2MB, extension is .pdf
        → FileStorageService.saveAppointmentOrderFile(id, file)
          → Constructs filename: "employee_{id}_appointment.pdf"
          → Files.copy() saves to uploads/ directory on server disk
          → Returns absolute file path string
        → EmployeeServiceImpl.updateAppointmentOrder(id, path, originalName)
          → Updates appointmentOrderPath and appointmentOrderFileName in DB
          → Returns updated EmployeeResponseDTO
      → HTTP 200 back to Angular
    → router.navigate(['/employee/list'], { state: { successMessage: '...' } })
    → EmployeeListComponent.ngOnInit() reads window.history.state.successMessage
    → Shows green success banner for 3 seconds
    → Table reloads with new employee visible
```

### Flow 4: Edit Employee (Complete End-to-End)

```
User clicks "Edit" button on a table row
  → EmployeeListComponent.goToEditEmployee(employee.id)
  → router.navigate(['/employee/edit', id])
  → Router loads EmployeeEditComponent
  → ngOnInit() → builds FormGroup → reads :id from URL via ActivatedRoute.snapshot.paramMap
  → EmployeeService.getEmployeeById(id)
  → HttpClient GET /api/employees/{id}
  → EmployeeController.getEmployeeById(@PathVariable id)
  → EmployeeServiceImpl.getEmployeeById(id)
    → employeeRepository.findById(id)
    → If not found: throws EmployeeNotFoundException → GlobalExceptionHandler → HTTP 404
    → If found: mapEntityToResponse() → ApiResponse → HTTP 200
  → Angular: form.patchValue() pre-fills all fields with existing data
  → Shows "Current Appointment Order" filename if it exists
  → User edits fields and clicks "Update Employee"
  → onSubmit() → validates form
  → EmployeeService.updateEmployee(id, formData)
  → HttpClient PUT /api/employees/update/{id}
  → EmployeeController.updateEmployee()
  → EmployeeServiceImpl.updateEmployee()
    → findById() → if not found: 404
    → Checks if employeeCode changed AND new code already exists → duplicate error
    → mapRequestToEntity() → updates all fields on existing entity
    → Sets updatedAt to now
    → employeeRepository.save() → Hibernate UPDATE employees SET ... WHERE id=?
  → If new PDF selected: uploadAppointmentOrder() same as create flow
  → navigate to /employee/list with success message
```

### Flow 5: Download PDF Report

```
User clicks "Report" on table row
  → EmployeeListComponent.goToReport(id)
  → router.navigate(['/employee/report', id])
  → EmployeeReportComponent.ngOnInit()
  → Loads employee details (same GET by id flow)
  → Displays employee data on screen in a card layout
  → User clicks "Download PDF Report"
  → EmployeeReportComponent.downloadReport()
  → EmployeeService.downloadEmployeeReport(id)
  → HttpClient.get('/api/employees/report/{id}', { responseType: 'blob' })
    (responseType:'blob' tells Angular: don't parse as JSON, keep as binary)
  → EmployeeController.generateEmployeeReport(@PathVariable id)
  → EmployeeReportService.generateEmployeeReport(id)
    → Finds employee from DB
    → new PDDocument() + new PDPage(PDRectangle.A4) → creates PDF in memory
    → doc.addPage(page) → adds the A4 page to the document
    → loadAwtFont("fonts/NotoSansTamil-Regular.ttf", 14f)
        → ClassPathResource loads TTF → Font.createFont(TRUETYPE_FONT) → AWT Font (size 14)
        → Used for the Tamil heading (heavier NotoSansTamil strokes suit bold headings)
    → loadAwtFont("fonts/LATHA.TTF", 11f)
        → Loads Latha TTF → AWT Font (size 11)
        → Used for Tamil value cells in the table (lighter strokes match body text weight)
    → new PDPageContentStream(doc, page) → opens the content stream for drawing
    → English title: cs.setFont(PDType1Font.HELVETICA_BOLD, 16) + cs.showText(...)
    → Tamil title: drawTamil(cs, "பணியாளர் சேவை விவர அறிக்கை", awtTamil14, x, y)
        → TextLayout(text, awtFont, FontRenderContext) applies OpenType GSUB/GPOS shaping
          (vowel reordering, conjunct formation, matras — done by the JVM text engine)
        → TextLayout.getOutline(null) extracts the shaped glyphs as a Java 2D Shape
        → PathIterator walks the Shape segment by segment:
            SEG_MOVETO  → cs.moveTo(pdfX+x, pdfY-y)   [Y-flip: AWT↓ vs PDF↑]
            SEG_LINETO  → cs.lineTo(pdfX+x, pdfY-y)
            SEG_QUADTO  → converted to cubic bezier (TrueType uses quadratic curves;
                          PDF only supports cubic) using the standard Q→C formula:
                          CP1 = current + 2/3*(quad-current)
                          CP2 = end     + 2/3*(quad-end)
                          → cs.curveTo(...)
            SEG_CUBICTO → cs.curveTo(...)
            SEG_CLOSE   → cs.closePath()
        → cs.fill() → fills all glyph outlines with black
        (Vector paths embed in every PDF viewer without needing the font installed)
    → drawTableGrid(cs, y)
        → Calculates tableTop and tableBottom from firstRowBottom + ROW_HEIGHT * NUM_ROWS
        → Draws all horizontal lines (one per row boundary, NUM_ROWS+1 lines total) +
          three vertical lines (left edge, column divider, right edge) in ONE cs.stroke() call
        → Single-pass drawing prevents shared row/column borders being stroked twice
          (double-stroked lines appear darker than the outer border — visual bug avoided)
    → for each of 9 data rows: rowText(cs, label, value, isTamil, latha11, y)
        → Label cell: cs.setFont(PDType1Font.HELVETICA, 11) + cs.showText(label)
        → Value cell: if Tamil → drawTamil(...) else cs.showText(value)
        → y decrements by ROW_HEIGHT each iteration
    → content stream closed → doc.save(baos) → baos.toByteArray() → returns byte[]
  → Controller wraps in ResponseEntity<byte[]>:
    → Content-Type: application/pdf
    → Content-Disposition: attachment; filename="employee_report_{id}.pdf"
    → HTTP 200 with byte[] body
  → Angular receives Blob
  → URL.createObjectURL(blob) → creates temporary browser URL for the binary data
  → Creates <a> element, sets href=url, sets download="employee-report-{id}.pdf"
  → Programmatically calls a.click() → browser triggers file download
  → URL.revokeObjectURL(url) → frees memory
  → downloading=false → button re-enables
```

### Flow 6: Delete Employee (hidden behind interview-safe flag)

```
Developer sets showDeleteButton = true in employee-list.ts to reveal the Delete button
  → Delete button appears next to Report button (hidden from DOM when flag is false)
  → User clicks "Delete" on a table row
  → EmployeeListComponent.deleteEmployee(employee.id)
  → window.confirm("Are you sure you want to permanently delete this employee?")
    → If user cancels: method returns immediately, nothing happens
    → If user confirms:
  → EmployeeService.deleteEmployee(id)
  → HttpClient.delete('http://localhost:8080/api/employees/delete/{id}')
  → EmployeeController.deleteEmployee(@PathVariable Long id)
  → EmployeeServiceImpl.deleteEmployee(id)
    → employeeRepository.findById(id)
      → If not found: throws EmployeeNotFoundException → GlobalExceptionHandler → HTTP 404
    → employeeRepository.deleteById(id)
      → Hibernate: DELETE FROM employees WHERE id = ?
      → Row permanently removed from PostgreSQL
  → HTTP 200 { "message": "Employee deleted successfully", "data": null }
  → Angular subscribe success callback:
    → successMessage = "Employee deleted successfully"
    → loadEmployees() called → table reloads without deleted row
    → After 3 seconds: successMessage cleared, cdr.markForCheck() triggers re-render
  → Set showDeleteButton = false before interview/demo to hide the button from the DOM
```

---

## 4. API CONNECTION MAP

| Angular Method (services/employee.ts) | HTTP Method | Spring Endpoint              | Controller Method          | Service Method             |
| ------------------------------------- | ----------- | ---------------------------- | -------------------------- | -------------------------- |
| `getAllEmployees()`                   | GET         | `/api/employees/list`        | `getAllEmployees()`        | `getAllEmployees()`        |
| `getEmployeeById(id)`                 | GET         | `/api/employees/{id}`        | `getEmployeeById()`        | `getEmployeeById()`        |
| `createEmployee(data)`                | POST        | `/api/employees/create`      | `createEmployee()`         | `createEmployee()`         |
| `updateEmployee(id, data)`            | PUT         | `/api/employees/update/{id}` | `updateEmployee()`         | `updateEmployee()`         |
| `uploadAppointmentOrder(id, file)`    | POST        | `/api/employees/upload/{id}`  | `uploadAppointmentOrder()` | `updateAppointmentOrder()` |
| `downloadEmployeeReport(id)`          | GET         | `/api/employees/report/{id}`  | `generateEmployeeReport()` | `generateEmployeeReport()` |
| `deleteEmployee(id)`                  | DELETE      | `/api/employees/delete/{id}`  | `deleteEmployee()`         | `deleteEmployee()`         |

---

## 5. BACKEND — SPRING BOOT

---

### FILE: pom.xml

**Purpose:** Maven project configuration — defines dependencies, Java version, and build plugins.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Standard XML declaration — tells XML parser this is UTF-8 encoded XML version 1.0 -->

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="...">
  <!-- <project> is the root element of every Maven POM file -->

  <modelVersion>4.0.0</modelVersion>
  <!-- Always 4.0.0 for Maven 2/3/4 — this is the POM schema version, NOT your app version -->

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.15</version>
    <!-- We inherit from Spring Boot's parent POM.
         This gives us: default plugin versions, dependency management (so we don't
         need to specify versions for most Spring libraries), and sensible defaults.
         Think of it as: our pom.xml "extends" Spring Boot's base pom. -->
    <relativePath/>
    <!-- Empty relativePath means: fetch this parent from Maven Central, not from local disk -->
  </parent>

  <groupId>com.employee</groupId>
  <!-- Our company/organization identifier — reverse domain convention -->

  <artifactId>employee-backend</artifactId>
  <!-- The name of this project/module — used as the JAR filename: employee-backend-0.0.1-SNAPSHOT.jar -->

  <version>0.0.1-SNAPSHOT</version>
  <!-- SNAPSHOT means "work in progress / development version", not yet released -->

  <properties>
    <java.version>17</java.version>
    <!-- Tells Spring Boot parent to compile with Java 17.
         Spring Boot 3.x requires minimum Java 17. -->
  </properties>

  <dependencies>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
      <!-- Pulls in: Spring Data JPA + Hibernate (ORM) + HikariCP (connection pool).
           This is what lets us use @Entity, @Repository, JpaRepository<>, etc.
           JPA = Java Persistence API (the standard); Hibernate = the implementation. -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
      <!-- Pulls in: Hibernate Validator (Bean Validation 3.0).
           This is what makes @NotBlank, @Email, @Size work on our DTOs.
           When @Valid is used in controller, these annotations are checked. -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <!-- Pulls in: Spring MVC + embedded Tomcat server.
           This is what makes @RestController, @RequestMapping, ResponseEntity work.
           Embedded Tomcat means no external server needed — the JAR IS the server. -->
    </dependency>

    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
      <!-- PostgreSQL JDBC driver. scope=runtime means it's needed to RUN the app
           but not to COMPILE it (we never directly import PostgreSQL classes in code —
           we use JPA abstractions). -->
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
      <!-- Lombok generates boilerplate code at compile time (@Getter, @Setter, etc.)
           optional=true means: projects that depend on THIS project don't inherit Lombok.
           Note: In this project, Lombok annotations are NOT actually used in the entity —
           getters/setters are written manually. -->
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
      <!-- Testing libraries: JUnit 5, Mockito, AssertJ, Spring Test.
           scope=test means this is ONLY available in test code, not in the final JAR. -->
    </dependency>

    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
      <version>2.0.27</version>
      <!-- Apache PDFBox is the open-source PDF library used for generating the employee
           service record PDF report. Unlike OpenPDF/iText, PDFBox uses the native Java AWT
           text engine for Tamil rendering — Tamil glyphs are shaped by TextLayout (which
           applies OpenType GSUB/GPOS rules) and then exported as vector paths via PathIterator.
           This approach embeds perfectly into every PDF viewer with no font encoding issues.
           Provides: PDDocument, PDPage, PDPageContentStream, PDType1Font, PDRectangle. -->
    </dependency>

  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <!-- This plugin creates an executable "fat JAR" — a single JAR that contains
             your code + ALL dependencies + embedded Tomcat.
             Run with: java -jar employee-backend-0.0.1-SNAPSHOT.jar -->
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </exclude>
            <!-- Exclude Lombok from the fat JAR — it's only needed at compile time,
                 not at runtime. Reduces JAR size. -->
          </excludes>
        </configuration>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <!-- Configures the Java compiler. Two executions: one for main sources, one for tests. -->
        <executions>
          <execution>
            <id>default-compile</id>
            <phase>compile</phase>
            <goals>
              <goal>compile</goal>
            </goals>
            <configuration>
              <annotationProcessorPaths>
                <path>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                </path>
                <!-- Tells the compiler to run Lombok's annotation processor BEFORE compiling main sources.
                     Lombok reads @Getter/@Setter etc. and generates .java source code
                     into the build directory, then the compiler compiles those generated files. -->
              </annotationProcessorPaths>
            </configuration>
          </execution>
          <execution>
            <id>default-testCompile</id>
            <phase>test-compile</phase>
            <goals>
              <goal>testCompile</goal>
            </goals>
            <configuration>
              <annotationProcessorPaths>
                <path>
                  <groupId>org.projectlombok</groupId>
                  <artifactId>lombok</artifactId>
                </path>
                <!-- Same Lombok annotation processor wired for test source compilation.
                     Ensures Lombok-generated code is available in test classes too. -->
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

**Purpose:** Spring Boot's configuration file — all runtime settings in one place.

```properties
# ─── SERVER ───────────────────────────────────────────────
server.port=8080
# The embedded Tomcat will listen on port 8080.
# Angular calls http://localhost:8080/api/employees/...
# If you change this, also update baseUrl in Angular's employee.ts service.

# ─── POSTGRESQL DATASOURCE ────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_service_db
# JDBC URL format: jdbc:postgresql://{host}:{port}/{database_name}
# localhost = database runs on same machine as the Spring Boot app
# 5432 = PostgreSQL default port
# employee_service_db = the database we created in PostgreSQL

spring.datasource.username=postgres
# PostgreSQL username — "postgres" is the default superuser created during installation

spring.datasource.password=Sakthi@123
# PostgreSQL password for the above user
# WARNING: In production, this should be in environment variables or a secrets manager,
# never hardcoded in a file that goes to version control.

spring.datasource.driver-class-name=org.postgresql.Driver
# Tells Spring which JDBC driver class to use to connect.
# This class comes from the postgresql dependency in pom.xml.

# ─── JPA / HIBERNATE ──────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
# Hibernate's schema management strategy:
# "update" = on startup, compare @Entity classes with existing DB tables.
#   - If table doesn't exist: CREATE TABLE
#   - If column is missing: ALTER TABLE ADD COLUMN
#   - Never drops existing data or columns
# Other options: "create" (drop+recreate every start), "validate" (error if mismatch),
#                "none" (do nothing — for production after schema is stable)

spring.jpa.show-sql=true
# Print every SQL query Hibernate executes to the console.
# Useful for development/debugging. Turn off in production for performance.

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
# Tells Hibernate which SQL dialect to use when generating SQL.
# PostgreSQL has some syntax differences from MySQL, Oracle, etc.
# This ensures Hibernate generates PostgreSQL-compatible SQL.

# ─── FILE UPLOAD ──────────────────────────────────────────
spring.servlet.multipart.max-file-size=2MB
# Maximum size for a single uploaded file. If Angular sends a file > 2MB,
# Spring rejects it BEFORE it even reaches our controller code.
# This is the server-side guard; we also check on the Angular side (defense in depth).

spring.servlet.multipart.max-request-size=2MB
# Maximum size for the entire HTTP multipart request (file + other fields combined).

file.upload-dir=uploads
# This is a CUSTOM property — not a Spring built-in.
# We read it in FileStorageService with @Value("${file.upload-dir}")
# "uploads" is a relative path → resolves to a folder named "uploads"
# in the directory where you launch the JAR.
```

---

### FILE: EmployeeBackendApplication.java

**Purpose:** The entry point of the entire Spring Boot application.

```java
package com.employee.backend;
// Declares which Java package this class belongs to.
// All Spring component scanning starts from this package and scans sub-packages:
// com.employee.backend.controller, .service, .repository, .entity, .dto, .exception

import org.springframework.boot.SpringApplication;
// SpringApplication is the class that bootstraps the Spring container.

import org.springframework.boot.autoconfigure.SpringBootApplication;
// @SpringBootApplication is a convenience annotation that combines THREE annotations:
//   @Configuration       → this class can define @Bean methods
//   @EnableAutoConfiguration → Spring Boot auto-configures based on what's on the classpath
//                              (e.g., sees Hibernate on classpath → auto-configures JPA)
//   @ComponentScan       → scans this package and all sub-packages for Spring components

@SpringBootApplication
// Applies all three annotations described above to this class.
public class EmployeeBackendApplication {

    public static void main(String[] args) {
        // Standard Java entry point — JVM calls this method when you run the JAR.

        SpringApplication.run(EmployeeBackendApplication.class, args);
        // This one line does everything:
        // 1. Creates the Spring ApplicationContext (the "container" that manages all beans)
        // 2. Triggers component scanning → finds @RestController, @Service, @Repository
        // 3. Creates all beans and injects dependencies
        // 4. Triggers Hibernate schema validation/update
        // 5. Starts embedded Tomcat on port 8080
        // 6. Application is now running and accepting HTTP requests
        //
        // EmployeeBackendApplication.class = the starting point for component scan
        // args = command-line arguments (e.g., --server.port=9090 to override config)
    }
}
```

**How this connects:** This is the START of everything. Nothing runs without this. Spring Boot reads this class, scans all sub-packages, and wires up the entire application.

---

### FILE: entity/Employee.java

**Purpose:** JPA Entity class — maps to the "employees" table in PostgreSQL. Each instance of this class = one row in the database.

```java
package com.employee.backend.entity;
// Entities live in the "entity" package by convention.

import jakarta.persistence.Column;
// @Column lets us customize how a Java field maps to a database column.

import jakarta.persistence.Entity;
// @Entity marks this class as a JPA entity — Hibernate will manage it and map it to a table.

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
// These control how the primary key (ID) is generated automatically.

import jakarta.persistence.Id;
// @Id marks which field is the primary key.

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
// @Table lets us customize the table name and add database-level constraints.

import java.time.LocalDate;
// Java 8+ date-only type (no time). Used for dateOfJoining. Maps to DATE in PostgreSQL.

import java.time.LocalDateTime;
// Java 8+ date+time type. Used for createdAt/updatedAt. Maps to TIMESTAMP in PostgreSQL.

@Entity
// Tells JPA/Hibernate: "This class represents a database table."
// Hibernate will manage SELECT/INSERT/UPDATE/DELETE for this class.

@Table(
    name = "employees",
    // The actual PostgreSQL table name will be "employees" (lowercase).
    // Without @Table, Hibernate would use the class name "Employee" (or "employee").

    uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_code")
        // Creates a UNIQUE INDEX on the employee_code column in PostgreSQL.
        // This means the database ITSELF will reject duplicate employee codes,
        // even if our application-level check somehow fails.
        // Defense in depth: we check in code (EmployeeServiceImpl) AND enforce in DB.
    }
)
public class Employee {

    @Id
    // Marks this field as the PRIMARY KEY of the table.

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY strategy = use PostgreSQL's SERIAL/BIGSERIAL auto-increment.
    // When we INSERT a new row, we don't provide an ID — PostgreSQL assigns it automatically.
    // The assigned ID is then read back and set on this Java object.

    private Long id;
    // Long (64-bit integer) maps to BIGINT in PostgreSQL. Can hold very large numbers.
    // Using Long (object) not long (primitive) so it can be null before saving.

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    // name="employee_code" → column in DB is called "employee_code" (snake_case convention)
    // nullable=false → adds NOT NULL constraint in PostgreSQL
    // unique=true → adds another UNIQUE constraint (redundant with @UniqueConstraint above,
    //               but makes the intent explicit at the column level too)
    // length=50 → VARCHAR(50) in PostgreSQL — limits storage to 50 characters
    private String employeeCode;

    @Column(name = "employee_name_english", nullable = false, length = 200)
    // Maps to VARCHAR(200) NOT NULL column named "employee_name_english"
    private String employeeNameEnglish;

    @Column(name = "employee_name_tamil", nullable = false, length = 200)
    // Tamil name in Unicode. VARCHAR(200) supports Unicode characters including Tamil script.
    // PostgreSQL uses UTF-8 by default, so Tamil characters store correctly.
    private String employeeNameTamil;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "date_of_joining", nullable = false)
    // LocalDate maps to DATE type in PostgreSQL (stores year-month-day only, no time)
    private LocalDate dateOfJoining;

    @Column(name = "mobile_number", length = 20)
    // No nullable=false here → this column ALLOWS NULL (mobile is optional)
    // length=20 to allow numbers with country codes like "+91-9876543210"
    private String mobileNumber;

    @Column(name = "email", length = 200)
    // Optional email field. length=200 for long email addresses.
    private String email;

    @Column(name = "remarks", length = 1000)
    // Optional free-text remarks. length=1000 allows up to 1000 characters.
    private String remarks;

    @Column(name = "appointment_order_path", length = 500)
    // Stores the file system PATH where the uploaded PDF is saved on the server disk.
    // e.g., "C:\projects\employee-backend\uploads\employee_1_appointment.pdf"
    // We store the PATH, not the actual file content. Files stay on disk.
    private String appointmentOrderPath;

    @Column(name = "created_at")
    // Timestamp of when this employee record was first created.
    // Set in EmployeeServiceImpl.createEmployee() to LocalDateTime.now()
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    // Timestamp updated every time the record is modified (create OR update).
    private LocalDateTime updatedAt;

    @Column(name = "appointment_order_file_name", length = 255)
    // Stores the ORIGINAL filename of the uploaded PDF (e.g., "appointment_john.pdf")
    // This is what we show to the user in the UI ("Current Appointment Order: appointment_john.pdf")
    // Separate from appointmentOrderPath which is the server-side storage path.
    private String appointmentOrderFileName;

    public Employee() {
        // No-argument constructor required by JPA specification.
        // JPA/Hibernate needs to create instances using reflection (new Employee())
        // before populating fields with values from the database.
        // Without this, Hibernate will throw an error.
    }

    // ─── GETTERS AND SETTERS ───────────────────────────────
    // JPA requires getters/setters for all persistent fields.
    // These are standard Java Bean conventions.
    // (In newer code, Lombok @Data or @Getter/@Setter would generate these automatically)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // getId() used in: EmployeeServiceImpl.mapEntityToResponse() → dto.setId(employee.getId())

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeNameEnglish() { return employeeNameEnglish; }
    public void setEmployeeNameEnglish(String employeeNameEnglish) {
        this.employeeNameEnglish = employeeNameEnglish;
    }

    public String getEmployeeNameTamil() { return employeeNameTamil; }
    public void setEmployeeNameTamil(String employeeNameTamil) {
        this.employeeNameTamil = employeeNameTamil;
    }

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
    public void setAppointmentOrderPath(String appointmentOrderPath) {
        this.appointmentOrderPath = appointmentOrderPath;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAppointmentOrderFileName() { return appointmentOrderFileName; }
    public void setAppointmentOrderFileName(String appointmentOrderFileName) {
        this.appointmentOrderFileName = appointmentOrderFileName;
    }
}
```

**How this connects:**

- Hibernate reads this class → creates `employees` table in PostgreSQL
- `EmployeeRepository` performs CRUD operations on this class
- `EmployeeServiceImpl.mapRequestToEntity()` WRITES fields into this class
- `EmployeeServiceImpl.mapEntityToResponse()` READS fields from this class

---

### FILE: repository/EmployeeRepository.java

**Purpose:** The data access layer — all database operations go through here. Spring Data JPA auto-generates the SQL implementation at runtime — we write ZERO SQL.

```java
package com.employee.backend.repository;

import com.employee.backend.entity.Employee;
// We need to know the entity type to work with.

import org.springframework.data.jpa.repository.JpaRepository;
// JpaRepository is a Spring Data interface that provides ready-made methods:
// save(), findById(), findAll(), deleteById(), existsById(), count(), etc.
// Spring Data generates the actual SQL implementation automatically at startup.

import java.util.Optional;
// Optional<T> is a Java 8+ wrapper that forces callers to handle the "not found" case.
// Instead of returning null (which causes NullPointerException), we return Optional.empty().

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // We EXTEND JpaRepository<EntityType, PrimaryKeyType>
    // EntityType = Employee (the @Entity class we want to query)
    // PrimaryKeyType = Long (the type of the @Id field in Employee)
    //
    // By extending JpaRepository, we INHERIT all these methods for FREE:
    //   save(Employee)          → INSERT or UPDATE (if ID exists, UPDATE; if null, INSERT)
    //   findById(Long)          → SELECT * FROM employees WHERE id = ?
    //   findAll()               → SELECT * FROM employees
    //   deleteById(Long)        → DELETE FROM employees WHERE id = ?
    //   existsById(Long)        → SELECT COUNT(*) FROM employees WHERE id = ?
    //   count()                 → SELECT COUNT(*) FROM employees
    //
    // Spring Data reads the METHOD NAME and generates SQL from it (Method Name Queries):

    Optional<Employee> findByEmployeeCode(String employeeCode);
    // Spring Data parses "findBy" + "EmployeeCode" and generates:
    //   SELECT * FROM employees WHERE employee_code = ?
    // Returns Optional<Employee> — caller must check if value is present.
    // Used in: (could be used to look up by code, but currently not called directly)

    boolean existsByEmployeeCode(String employeeCode);
    // Spring Data generates: SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
    //                        FROM employees WHERE employee_code = ?
    // Returns true if a record with that code exists, false otherwise.
    // Used in: EmployeeServiceImpl.createEmployee() and updateEmployee()
    //          to check for duplicate codes before saving.
}
```

**How this connects:**

- Injected into `EmployeeServiceImpl` via constructor
- Injected into `EmployeeReportService` via constructor
- All database reads/writes in the app go through this interface

---

### FILE: dto/ApiResponse.java

**Purpose:** A generic response wrapper — ALL our API endpoints return this type so Angular always receives a consistent structure: `{ "message": "...", "data": {...} }`.

```java
package com.employee.backend.dto;
// DTO = Data Transfer Object. Lives in the "dto" package.
// DTOs carry data between layers. They are NOT persisted to the database.

public class ApiResponse<T> {
    // Generic class using <T> (type parameter).
    // T can be: EmployeeResponseDTO, List<EmployeeResponseDTO>, Void (null), etc.
    // This one class handles ALL our API responses regardless of what data they contain.
    //
    // Example JSON output when T = EmployeeResponseDTO:
    // { "message": "Employee created successfully", "data": { "id": 1, "employeeCode": "EMP001", ... } }
    //
    // Example JSON output when T = Void (error case):
    // { "message": "Employee not found with id: 99", "data": null }

    private String message;
    // Human-readable status message. Examples:
    // "Employee created successfully", "Employee not found with id: 5", "Validation failed: ..."

    private T data;
    // The actual payload. Generic T means this field can hold any type.
    // Jackson (Spring's JSON library) will serialize whatever T is into JSON automatically.

    public ApiResponse() {
        // No-arg constructor required for Jackson deserialization.
        // (Though we mostly use ApiResponse on the server side, not deserialize it)
    }

    public ApiResponse(String message, T data) {
        // Convenience constructor used throughout the codebase:
        // new ApiResponse<>("Employee created successfully", createdDto)
        this.message = message;
        this.data = data;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    // Jackson uses these getters to serialize the object to JSON.
}
```

**How this connects:**

- Every `EmployeeController` method returns `ResponseEntity<ApiResponse<...>>`
- On the Angular side, `response.message` and `response.data` are accessed in `.subscribe()`

---

### FILE: dto/EmployeeRequestDTO.java

**Purpose:** The INPUT data shape for create/update operations. Angular sends JSON matching this structure. Bean Validation annotations enforce rules BEFORE business logic runs.

```java
package com.employee.backend.dto;

import jakarta.validation.constraints.Email;
// @Email validates that the string looks like a valid email address (has @, has domain, etc.)

import jakarta.validation.constraints.NotBlank;
// @NotBlank checks: not null AND not empty string AND not only whitespace.
// Stricter than @NotNull (which only checks for null) and @NotEmpty (which allows "   ")

import jakarta.validation.constraints.NotNull;
// @NotNull only checks that the value is not null. Used for non-String types.

import jakarta.validation.constraints.Size;
// @Size checks minimum and maximum length of a String (or collection).

import java.time.LocalDate;

public class EmployeeRequestDTO {
    // This class represents the JSON body Angular sends in POST /create and PUT /update.
    // Jackson automatically deserializes the incoming JSON into this object.
    // Then @Valid in the controller triggers all the annotations below.

    @NotBlank(message = "Employee code is required")
    // message= is what goes into the error response if validation fails.
    // @NotBlank = null check + empty check + blank check. "EMP001" passes. "" fails. "  " fails.
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
    // @NotNull (not @NotBlank) because LocalDate is not a String — it can't be blank.
    // Angular sends "2024-01-15" → Jackson converts it to LocalDate automatically.
    private LocalDate dateOfJoining;

    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    // min=10 AND max=10 means EXACTLY 10 characters.
    // Note: this only checks length, not that they are digits.
    // The Angular side validates the pattern (^[0-9]{10}$) for digits.
    private String mobileNumber;

    @Email(message = "Invalid email address")
    // Validates format like "user@domain.com". Does NOT verify the email actually exists.
    private String email;

    private String remarks;
    // No validation annotation — remarks is optional. Can be null or any string.

    // ─── GETTERS AND SETTERS ───────────────────────────────
    // Required by Jackson to deserialize JSON into this object (sets fields)
    // and by EmployeeServiceImpl to read values from it (gets fields).

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

**How this connects:**

- Angular sends JSON → Spring deserializes into this DTO
- `@Valid` in `EmployeeController` triggers validation → failures caught by `GlobalExceptionHandler`
- `EmployeeServiceImpl.mapRequestToEntity()` reads from this DTO and copies to `Employee` entity

---

### FILE: dto/EmployeeResponseDTO.java

**Purpose:** The OUTPUT data shape — what we send back to Angular. Includes all fields Angular might need (including ID, timestamps, file name).

```java
package com.employee.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponseDTO {
    // This is what we send back to Angular in every response.
    // It's separate from the Entity for two reasons:
    // 1. SECURITY: We control exactly what fields are exposed (never expose internal paths etc.)
    // 2. FLEXIBILITY: Response shape can differ from DB shape without breaking the entity.
    //
    // Jackson serializes this to JSON automatically. Angular receives:
    // { "id": 1, "employeeCode": "EMP001", "employeeNameEnglish": "John", ... }

    private Long id;
    // The database-assigned ID. Angular uses this for Edit/View/Report routes.
    // e.g., router.navigate(['/employee/edit', employee.id])

    private String employeeCode;
    private String employeeNameEnglish;
    private String employeeNameTamil;
    private String designation;
    private String department;
    private LocalDate dateOfJoining;
    // Jackson serializes LocalDate as "2024-01-15" (ISO-8601 format) in JSON.

    private String mobileNumber;
    private String email;
    private String remarks;

    private String appointmentOrderPath;
    // The server disk path. Included in response but Angular currently only shows the fileName.
    // Could be used in future to serve the file for download.

    private LocalDateTime createdAt;
    // Jackson serializes as "2024-01-15T10:30:00" (ISO-8601 datetime)

    private LocalDateTime updatedAt;

    private String appointmentOrderFileName;
    // The original filename of the uploaded PDF (e.g., "appointment_john.pdf").
    // Angular displays this in the View page and Edit form as "Current Appointment Order".

    // ─── GETTERS AND SETTERS ───────────────────────────────
    // Jackson uses getters to serialize this object to JSON for the HTTP response.
    // EmployeeServiceImpl.mapEntityToResponse() uses setters to populate this object.

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

**Purpose:** Custom exception thrown when an employee ID doesn't exist in the database.

```java
package com.employee.backend.exception;

public class EmployeeNotFoundException extends RuntimeException {
    // Extends RuntimeException = this is an UNCHECKED exception.
    // Unchecked means: callers do NOT have to declare "throws EmployeeNotFoundException"
    // or wrap it in try-catch. It propagates up automatically.
    // This makes service code cleaner — just throw and let GlobalExceptionHandler catch it.

    public EmployeeNotFoundException(String message) {
        super(message);
        // super(message) calls RuntimeException's constructor which stores the message.
        // message can be retrieved later with ex.getMessage().
        // Example: new EmployeeNotFoundException("Employee not found with id: 99")
        // → GlobalExceptionHandler catches it → returns HTTP 404 with that message.
    }
}
```

---

### FILE: exception/DuplicateEmployeeCodeException.java

**Purpose:** Custom exception for when the same employee code is used twice.

```java
package com.employee.backend.exception;

public class DuplicateEmployeeCodeException extends RuntimeException {
    // Same pattern as EmployeeNotFoundException — unchecked, stores a message.

    public DuplicateEmployeeCodeException(String message) {
        super(message);
        // Example: new DuplicateEmployeeCodeException("Employee code already exists: EMP001")
        // → GlobalExceptionHandler catches it → returns HTTP 400 Bad Request.
    }
}
```

---

### FILE: exception/InvalidFileException.java

**Purpose:** Custom exception for invalid file uploads (wrong type, too large, empty).

```java
package com.employee.backend.exception;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
        // Examples:
        //   "File is empty" → thrown when file.isEmpty() is true
        //   "File size must be <= 2 MB" → thrown when file > 2MB
        //   "Only PDF files are allowed" → thrown when extension is not .pdf
        // → GlobalExceptionHandler catches it → returns HTTP 400 Bad Request.
    }
}
```

---

### FILE: exception/GlobalExceptionHandler.java

**Purpose:** Central error handler — catches ALL exceptions from ALL controllers and returns consistent JSON error responses. Without this, Spring would return ugly HTML error pages or raw stack traces.

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
// This annotation does two things:
// 1. @ControllerAdvice: This class applies "advice" (cross-cutting behavior) to ALL controllers.
//    Any exception thrown in ANY @RestController will be caught here.
// 2. @ResponseBody: The return value is automatically serialized to JSON (like @RestController).
// Think of it as: a global try-catch wrapper around all your controllers.

public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    // @ExceptionHandler tells Spring: "when an EmployeeNotFoundException is thrown anywhere,
    // call THIS method instead of letting Spring use its default error handling."

    public ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        // EmployeeNotFoundException ex — Spring automatically passes the thrown exception here.
        // ApiResponse<Void> — Void means the "data" field will be null (no payload, just message).

        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        // ex.getMessage() → "Employee not found with id: 99"
        // null → no data payload on error responses

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        // HTTP 404 Not Found + JSON body: { "message": "Employee not found with id: 99", "data": null }
        // Angular's subscribe error() callback receives this.
    }

    @ExceptionHandler(DuplicateEmployeeCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmployeeCode(DuplicateEmployeeCodeException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        // HTTP 400 Bad Request — "EMP001 already exists" error
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // This exception is thrown by Spring when @Valid validation fails on a @RequestBody.
    // For example: Angular sends { "employeeCode": "" } → @NotBlank fails → this fires.

    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {

        StringBuilder sb = new StringBuilder("Validation failed: ");
        // Start building a human-readable error message.

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // getBindingResult() = the result of running all @Constraint annotations.
            // getFieldErrors() = list of individual field failures.
            // FieldError contains: field name ("employeeCode") + message ("Employee code is required")

            sb.append(fieldError.getField())          // e.g., "employeeCode"
              .append(" - ")
              .append(fieldError.getDefaultMessage())  // e.g., "Employee code is required"
              .append("; ");
            // Result: "Validation failed: employeeCode - Employee code is required; email - Invalid email address; "
        }

        ApiResponse<Void> response = new ApiResponse<>(sb.toString(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        // HTTP 400 with all validation errors concatenated into one message.
    }

    @ExceptionHandler(Exception.class)
    // Catches EVERYTHING ELSE — this is the catch-all fallback.
    // Any unexpected exception (NullPointerException, IOException, etc.) ends up here.

    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>("Unexpected error: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        // HTTP 500 Internal Server Error — something unexpected broke on the server.
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(InvalidFileException ex) {
        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        // HTTP 400 for invalid file uploads (empty, wrong type, too large).
    }
}
```

**How this connects:**

- Registered globally — all exceptions from `EmployeeController` pass through here
- Angular's `subscribe({ error: (err) => ... })` callback receives the HTTP error responses this produces

---

### FILE: service/EmployeeService.java (Interface)

**Purpose:** Defines the CONTRACT for employee business operations. The controller depends on this interface, not the implementation — following the "program to interfaces" principle.

```java
package com.employee.backend.service;

import com.employee.backend.dto.EmployeeRequestDTO;
import com.employee.backend.dto.EmployeeResponseDTO;
import java.util.List;

public interface EmployeeService {
    // Interface = a contract. Declares WHAT methods exist but not HOW they work.
    // EmployeeController depends on THIS interface (not on EmployeeServiceImpl directly).
    // This makes the code testable: we can swap the real implementation with a mock in tests.
    // The actual logic lives in EmployeeServiceImpl which "implements" this interface.

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);
    // Input: EmployeeRequestDTO (data from Angular)
    // Output: EmployeeResponseDTO (saved data sent back to Angular)
    // Implementation: EmployeeServiceImpl.createEmployee()

    List<EmployeeResponseDTO> getAllEmployees();
    // Input: nothing
    // Output: List of all employees as DTOs
    // Implementation: EmployeeServiceImpl.getAllEmployees()

    EmployeeResponseDTO getEmployeeById(Long id);
    // Input: employee database ID
    // Output: single employee DTO
    // Throws EmployeeNotFoundException if not found

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO);
    // Input: existing employee's ID + new data from Angular
    // Output: updated employee DTO

    EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName);
    // Input: employee ID + server file path + original file name
    // Output: updated employee DTO with file info
    // Called after FileStorageService saves the file to disk

    void deleteEmployee(Long id);
    // Input: employee database ID
    // Output: void (nothing returned — the row is gone)
    // Throws EmployeeNotFoundException if id doesn't exist → GlobalExceptionHandler → HTTP 404
    // INTERVIEW NOTE: This method exists but the Delete button is hidden behind showDeleteButton flag in Angular
}
```

---

### FILE: service/impl/EmployeeServiceImpl.java

**Purpose:** The actual business logic implementation. Handles duplicate checking, entity mapping, timestamp management, and database operations.

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
// @Service is a @Component specialization — it tells Spring:
// "Create one instance (singleton) of this class and register it in the bean container."
// When EmployeeController declares "private final EmployeeService employeeService",
// Spring injects this EmployeeServiceImpl instance because it implements EmployeeService.

@Transactional
// Applied at the CLASS level — every method is wrapped in a database transaction by default.
// A transaction means: all DB operations in a method succeed together OR roll back together.
// Example: if createEmployee() saves the employee but then throws an exception,
// the INSERT is rolled back — no partial/corrupt data in the database.
public class EmployeeServiceImpl implements EmployeeService {
    // "implements EmployeeService" = this class fulfills the EmployeeService contract.
    // Spring will inject this class wherever EmployeeService is declared as a dependency.

    private final EmployeeRepository employeeRepository;
    // "final" = assigned once in constructor, never changed. Good practice.
    // EmployeeRepository is injected by Spring (constructor injection).

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        // Constructor injection — Spring calls this constructor and passes the repository bean.
        // This is the PREFERRED injection style (over @Autowired on field) because:
        // - The dependency is explicit and required
        // - Makes testing easier (can pass mock in test constructor)
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        // This method is called by EmployeeController.createEmployee()
        // It handles: duplicate check → entity creation → DB save → DTO mapping

        if (employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) {
            // Ask the DB: "Does any employee with this code already exist?"
            // If YES → throw an exception (GlobalExceptionHandler will convert to HTTP 400)
            throw new DuplicateEmployeeCodeException(
                "Employee code already exists: " + requestDTO.getEmployeeCode()
            );
        }

        Employee employee = new Employee();
        // Create a new empty Employee entity (no ID yet — DB hasn't assigned one).

        mapRequestToEntity(requestDTO, employee);
        // Copy all fields from the DTO to the entity.
        // Private helper method defined at the bottom of this class.

        LocalDateTime now = LocalDateTime.now();
        // Capture the current date and time once, use it for both fields.
        // Using the same "now" ensures createdAt and updatedAt are identical on creation.

        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        // Set both timestamps. updatedAt will be refreshed on every update.

        Employee saved = employeeRepository.save(employee);
        // save() with null ID → Hibernate generates INSERT INTO employees (...)
        // PostgreSQL assigns the ID via auto-increment and returns it.
        // Hibernate reads back the assigned ID and sets employee.id automatically.
        // "saved" is the same object but now has a non-null id.

        return mapEntityToResponse(saved);
        // Convert the saved entity to a DTO for the HTTP response.
        // We return the DTO, not the entity — keeps internal DB structure hidden from API.
    }

    @Override
    @Transactional(readOnly = true)
    // readOnly=true is an optimization hint:
    // - Hibernate skips "dirty checking" (no need to track changes on read-only query)
    // - Some databases use read replicas for readOnly transactions (better performance)
    // - Makes intent clear: this method should NEVER modify data
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
            // findAll() → SELECT * FROM employees (inherited from JpaRepository)
            // Returns List<Employee> (list of entity objects)

            .stream()
            // Convert List to a Java Stream for functional-style processing.

            .map(this::mapEntityToResponse)
            // For EACH Employee entity, call mapEntityToResponse() to convert it to a DTO.
            // this::mapEntityToResponse is a method reference (shorthand for e -> mapEntityToResponse(e))

            .collect(Collectors.toList());
            // Collect all the mapped DTOs back into a List<EmployeeResponseDTO>.
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            // findById() → SELECT * FROM employees WHERE id = ?
            // Returns Optional<Employee> — may or may not contain a value.

            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
            // If Optional is empty (no employee with that ID):
            //   → throw EmployeeNotFoundException
            //   → GlobalExceptionHandler catches it → HTTP 404
            // If Optional has a value:
            //   → employee variable gets the Employee entity

        return mapEntityToResponse(employee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        // First: find existing employee. If not found: 404 error.

        if (!employee.getEmployeeCode().equals(requestDTO.getEmployeeCode())
                && employeeRepository.existsByEmployeeCode(requestDTO.getEmployeeCode())) {
            // Check for duplicate code ONLY IF the code is being CHANGED.
            // Explanation:
            //   If employee's current code is "EMP001" and requestDTO.code is also "EMP001"
            //   → Code hasn't changed → no duplicate check needed (it's the same record)
            //   If employee's current code is "EMP001" but requestDTO.code is "EMP002"
            //   → Code IS changing → check if "EMP002" already belongs to another employee
            //   → If yes: throw DuplicateEmployeeCodeException → HTTP 400
            throw new DuplicateEmployeeCodeException(
                "Employee code already exists: " + requestDTO.getEmployeeCode()
            );
        }

        mapRequestToEntity(requestDTO, employee);
        // Overwrite all fields on the EXISTING entity object (not a new one).
        // Hibernate is tracking this entity — any changes to it will be detected.

        employee.setUpdatedAt(LocalDateTime.now());
        // Update the "last modified" timestamp.

        Employee updated = employeeRepository.save(employee);
        // save() with existing ID → Hibernate generates UPDATE employees SET ... WHERE id = ?
        // (Hibernate knows this is an update because the entity has a non-null ID)

        return mapEntityToResponse(updated);
    }

    @Override
    public EmployeeResponseDTO updateAppointmentOrder(Long id, String filePath, String fileName) {
        // Called after FileStorageService saves the PDF to disk.
        // filePath = absolute path on server disk (e.g., "C:\...\uploads\employee_1_appointment.pdf")
        // fileName = original filename from user's computer (e.g., "my_appointment_letter.pdf")

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        employee.setAppointmentOrderPath(filePath);
        // Store server disk path — used internally to locate the file.

        employee.setAppointmentOrderFileName(fileName);
        // Store original filename — shown to user in UI.

        employee.setUpdatedAt(LocalDateTime.now());

        Employee updated = employeeRepository.save(employee);
        // UPDATE employees SET appointment_order_path=?, appointment_order_file_name=?, updated_at=? WHERE id=?

        return mapEntityToResponse(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        // This method permanently removes an employee from the database.
        // It is called by EmployeeController.deleteEmployee() via the DELETE /api/employees/delete/{id} endpoint.
        // The Delete button in Angular is hidden behind showDeleteButton = false flag for interview/demo purposes.

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        // findById() → SELECT * FROM employees WHERE id = ?
        // If not found: throws EmployeeNotFoundException → GlobalExceptionHandler → HTTP 404
        // We find first (instead of calling deleteById directly) so we get a proper 404 if missing.

        employeeRepository.deleteById(employee.getId());
        // deleteById() → DELETE FROM employees WHERE id = ?
        // This is a permanent, irreversible operation — no soft delete, no recycle bin.
        // Inherited from JpaRepository for free — no SQL needed.
    }

    // ─── PRIVATE HELPER METHODS ────────────────────────────

    private void mapRequestToEntity(EmployeeRequestDTO requestDTO, Employee employee) {
        // Copies all fields from DTO → Entity.
        // Used in BOTH createEmployee() and updateEmployee().
        // DRY principle: Don't Repeat Yourself — write this mapping once, call it twice.

        employee.setEmployeeCode(requestDTO.getEmployeeCode());
        employee.setEmployeeNameEnglish(requestDTO.getEmployeeNameEnglish());
        employee.setEmployeeNameTamil(requestDTO.getEmployeeNameTamil());
        employee.setDesignation(requestDTO.getDesignation());
        employee.setDepartment(requestDTO.getDepartment());
        employee.setDateOfJoining(requestDTO.getDateOfJoining());
        employee.setMobileNumber(requestDTO.getMobileNumber());
        employee.setEmail(requestDTO.getEmail());
        employee.setRemarks(requestDTO.getRemarks());
        // Note: id, createdAt, appointmentOrderPath, appointmentOrderFileName are NOT set here.
        // id is managed by JPA. createdAt is set separately. File fields updated separately.
    }

    private EmployeeResponseDTO mapEntityToResponse(Employee employee) {
        // Copies all fields from Entity → ResponseDTO.
        // This is called after every DB operation to build the API response.
        // We NEVER expose the Entity class directly in API responses.

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

**Purpose:** Handles saving uploaded PDF files to the server's file system.

```java
package com.employee.backend.service;

import org.springframework.beans.factory.annotation.Value;
// @Value injects values from application.properties into fields.

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
// StringUtils.cleanPath() sanitizes file paths to prevent path traversal attacks.

import org.springframework.web.multipart.MultipartFile;
// MultipartFile represents an uploaded file in an HTTP multipart/form-data request.

import java.io.IOException;
import java.nio.file.Files;
// java.nio.file.Files provides static methods for file system operations (copy, create, etc.)

import java.nio.file.Path;
// Path is the modern Java way to represent a file system path (replaces java.io.File).

import java.nio.file.Paths;
// Paths.get() converts a String to a Path object.

@Service
public class FileStorageService {

    private final Path uploadDir;
    // This is the resolved, absolute directory path where files will be stored.
    // Computed once in the constructor from the application.properties value.

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        // @Value("${file.upload-dir}") reads from application.properties:
        //   file.upload-dir=uploads
        // So uploadDir parameter here = "uploads" (a relative path string).

        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        // Paths.get("uploads") → creates a relative Path object
        // .toAbsolutePath() → converts to absolute path based on current working directory
        //   e.g., "C:\projects\employee-backend\uploads"
        // .normalize() → resolves ".." and "." in paths to prevent path traversal

        try {
            Files.createDirectories(this.uploadDir);
            // Create the "uploads" directory if it doesn't exist.
            // createDirectories() also creates any missing parent directories.
            // If directory already exists: does nothing (no error).
            // This runs ONCE when Spring starts the application.
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + this.uploadDir, ex);
            // If directory creation fails (permission error, disk full, etc.),
            // wrap in RuntimeException and fail fast — we can't run without an upload directory.
        }
    }

    public String saveAppointmentOrderFile(Long employeeId, MultipartFile file) throws IOException {
        // Called from EmployeeController.uploadAppointmentOrder()
        // Returns: the absolute file path string where the file was saved.

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        // file.getOriginalFilename() → the filename from the user's machine (e.g., "my doc.pdf")
        // StringUtils.cleanPath() → sanitizes it: removes "../", normalizes separators.
        //   Without this, a malicious user could send "../../../etc/passwd" as filename
        //   and potentially overwrite system files (Path Traversal attack).

        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        // Find the position of the last "." in the filename.
        // "appointment.order.pdf" → dotIndex = 17 (position of last ".")

        if (dotIndex != -1) {
            // dotIndex == -1 means no "." found → file has no extension
            fileExtension = originalFileName.substring(dotIndex);
            // Extract the extension including the dot: ".pdf", ".PDF", etc.
        }

        String fileName = "employee_" + employeeId + "_appointment" + fileExtension;
        // Build a consistent, predictable filename on the server:
        // employee_1_appointment.pdf, employee_42_appointment.pdf, etc.
        // This OVERWRITES any previous upload for the same employee (REPLACE_EXISTING below).
        // User's original filename is NOT used as the server filename (security best practice).

        Path targetLocation = this.uploadDir.resolve(fileName);
        // resolve() combines paths: uploadDir + fileName
        // e.g., "C:\projects\employee-backend\uploads\employee_1_appointment.pdf"

        Files.copy(file.getInputStream(), targetLocation,
                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        // file.getInputStream() → reads the uploaded file bytes from the HTTP request.
        // targetLocation → where to write those bytes on the server disk.
        // REPLACE_EXISTING → if "employee_1_appointment.pdf" already exists, overwrite it.
        //   This handles re-uploading a new appointment order for an existing employee.

        return targetLocation.toString();
        // Return the absolute path string. This gets stored in Employee.appointmentOrderPath
        // so we know where the file is on disk.
    }
}
```

---

### FILE: service/EmployeeReportService.java

**Purpose:** Generates a PDF "Service Record Report" for an employee using Apache PDFBox. Tamil text is rendered as vector glyph paths via the Java AWT text engine — no PDF font encoding needed.

```java
package com.employee.backend.service;

import com.employee.backend.entity.Employee;
import com.employee.backend.exception.EmployeeNotFoundException;
import com.employee.backend.repository.EmployeeRepository;

// Apache PDFBox imports:
import org.apache.pdfbox.pdmodel.PDDocument;
// PDDocument = the PDF document object. Pages are added to it, then saved to an OutputStream.

import org.apache.pdfbox.pdmodel.PDPage;
// Represents a single page in the PDF. We create one A4 page.

import org.apache.pdfbox.pdmodel.PDPageContentStream;
// The drawing surface for a page. All text and graphics operations go through this.
// Analogous to a Graphics2D context — you must open it and close it.

import org.apache.pdfbox.pdmodel.common.PDRectangle;
// Defines page dimensions. PDRectangle.A4 = 595.28 x 841.89 points (1 point = 1/72 inch).

import org.apache.pdfbox.pdmodel.font.PDType1Font;
// Built-in PDF Type 1 fonts (Helvetica, Times, Courier, etc.).
// These 14 fonts are guaranteed to be available in every PDF viewer — no embedding needed.
// Used here for all English text: HELVETICA_BOLD for labels, HELVETICA for values.

import org.springframework.core.io.ClassPathResource;
// Spring utility to load a resource file from the classpath (inside the JAR's resources folder).

import org.springframework.stereotype.Service;

import java.awt.Font;
// Java AWT Font — NOT the PDF font. Used to load TTF files for Tamil shaping.
// AWT's text engine applies OpenType GSUB/GPOS rules, which handles Tamil's
// complex character ordering (vowel signs, conjuncts, matras).

import java.awt.Shape;
// Represents a geometric shape. TextLayout.getOutline() returns a Shape
// containing the pixel-precise outline of all shaped glyphs.

import java.awt.font.FontRenderContext;
// Provides rendering hints (antialiasing, fractional metrics) to the font rendering engine.
// Required as a parameter to TextLayout.

import java.awt.font.TextLayout;
// The key AWT class that does OpenType shaping: it takes a String + Font and produces
// correctly shaped glyphs. getOutline() extracts those glyphs as a Java 2D Shape.

import java.awt.geom.AffineTransform;
// Identity transform passed to FontRenderContext (no rotation/scaling of the context).

import java.awt.geom.PathIterator;
// Iterates over the segments (MOVETO, LINETO, QUADTO, CUBICTO, CLOSE) of a Shape.
// We walk the Tamil glyph outlines segment by segment and replay them as PDF path commands.

import java.io.ByteArrayOutputStream;
// In-memory output stream. The PDF is written here and returned as byte[].

import java.io.IOException;
import java.io.InputStream;

@Service
public class EmployeeReportService {

    private final EmployeeRepository employeeRepository;

    // ─── PAGE LAYOUT CONSTANTS ─────────────────────────────────────────────────
    private static final float MARGIN      = 50f;
    // Left/right margin in PDF points. Content starts at x=50.

    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    // A4 height = 841.89 points. PDF coordinate origin is BOTTOM-LEFT (y=0 at bottom).
    // Content is placed starting near the top: y = PAGE_HEIGHT - MARGIN.

    private static final float TABLE_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
    // Available table width = page width - left margin - right margin.

    private static final float COL1_WIDTH  = 200f;
    // Width of the label column (left). e.g., "Employee Name (Tamil)".

    private static final float COL2_WIDTH  = TABLE_WIDTH - COL1_WIDTH;
    // Width of the value column (right) — whatever remains after the label column.

    private static final float ROW_HEIGHT  = 22f;
    // Height of each table row in points. Enough space for 11pt text + padding.

    private static final float CELL_PAD    = 5f;
    // Padding inside each cell — text starts CELL_PAD points from the cell border.

    private static final int   NUM_ROWS    = 9;
    // Total number of data rows: Code, Name(En), Name(Ta), Designation, Department,
    // Date of Joining, Mobile, Email, Remarks.

    public EmployeeReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        // Constructor injection — same pattern as EmployeeServiceImpl.
    }

    public byte[] generateEmployeeReport(Long id) {
        // Returns the entire PDF as a byte array.
        // Called by EmployeeController.generateEmployeeReport()
        // Angular receives this as a Blob and triggers a file download.

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        // Load the employee from the database.

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PDDocument doc = new PDDocument()) {
            // try-with-resources: both baos and PDDocument close automatically.
            // PDDocument holds the in-memory PDF structure.

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            // Create one A4 page and add it to the document.

            // ─── LOAD AWT FONTS FOR TAMIL ──────────────────────────
            Font awtTamil14 = loadAwtFont("fonts/NotoSansTamil-Regular.ttf", 14f);
            // NotoSansTamil at size 14 — used for the Tamil title heading.
            // NotoSansTamil has heavier strokes that suit heading weight.

            Font awtLatha11 = loadAwtFont("fonts/LATHA.TTF", 11f);
            // Latha at size 11 — used for the Tamil name value cell in the table.
            // Latha has lighter, more traditional strokes that match Helvetica body-text weight.

            String joiningDate = employee.getDateOfJoining() != null
                    ? employee.getDateOfJoining().toString() : "";
            // Pre-compute the date string. toString() gives "2024-01-15" (ISO-8601).

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // PDPageContentStream is the drawing context for this page.
                // All text and path operations must happen inside this try block.
                // When closed: finalizes the page's content stream in the PDF structure.

                float y = PAGE_HEIGHT - MARGIN;
                // Starting y position near the top of the page.
                // In PDFBox (like all PDF), y=0 is the BOTTOM — we subtract from top.

                // ─── ENGLISH TITLE ────────────────────────────────
                cs.beginText();
                // beginText() / endText() bracket all text operations.
                // You CANNOT mix path operations (moveTo, lineTo) inside a text block.

                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                // PDType1Font.HELVETICA_BOLD is one of the 14 standard PDF fonts.
                // No file loading needed — every PDF viewer includes these fonts.

                cs.newLineAtOffset(MARGIN, y);
                // Move the text cursor to (MARGIN, y) — the starting position for this text.

                cs.showText("Employee Service Record Report");
                // Writes the English title string at the current cursor position.

                cs.endText();
                y -= 26f;
                // Move y down 26 points to leave space below the English title.

                // ─── TAMIL TITLE ──────────────────────────────────
                drawTamil(cs, "பணியாளர் சேவை விவர அறிக்கை", awtTamil14, MARGIN, y);
                // Renders the Tamil title as vector glyph paths (explained in drawTamil below).
                y -= 30f;
                // Extra vertical gap before the table.

                // ─── TABLE GRID ───────────────────────────────────
                drawTableGrid(cs, y);
                // Draws ALL grid lines (horizontals + verticals) in ONE stroke() call.
                // This avoids double-stroking shared borders (which would look darker).
                // After this call, the visual grid is drawn but no text is inside yet.

                // ─── TABLE DATA ───────────────────────────────────
                String[][] rows = {
                    {"Employee Code",           safe(employee.getEmployeeCode()),        "en"},
                    {"Employee Name (English)", safe(employee.getEmployeeNameEnglish()), "en"},
                    {"Employee Name (Tamil)",   safe(employee.getEmployeeNameTamil()),   "ta"},
                    // "ta" flag → value column uses drawTamil() instead of cs.showText()
                    {"Designation",             safe(employee.getDesignation()),         "en"},
                    {"Department",              safe(employee.getDepartment()),          "en"},
                    {"Date of Joining",         joiningDate,                            "en"},
                    {"Mobile Number",           safe(employee.getMobileNumber()),        "en"},
                    {"Email",                   safe(employee.getEmail()),               "en"},
                    {"Remarks",                 safe(employee.getRemarks()),             "en"},
                };

                for (String[] r : rows) {
                    rowText(cs, r[0], r[1], "ta".equals(r[2]), awtLatha11, y);
                    // r[0] = label, r[1] = value, r[2] = language flag.
                    // rowText() positions and draws the text inside the current row.
                    y -= ROW_HEIGHT;
                    // Move y down one row height for the next row.
                }
            }
            // PDPageContentStream is auto-closed here → page content is finalized.

            doc.save(baos);
            // Write the complete PDF to the in-memory ByteArrayOutputStream.

            return baos.toByteArray();
            // Return the PDF as a raw byte array.
            // EmployeeController wraps this in ResponseEntity<byte[]> with PDF content-type.

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate employee report PDF", ex);
            // If PDFBox fails or font loading fails, wrap and rethrow as unchecked.
            // GlobalExceptionHandler will catch this and return HTTP 500.
        }
    }

    private Font loadAwtFont(String classpathPath, float size) throws Exception {
        // Loads a TrueType font from the JAR's classpath resources into a Java AWT Font.
        // AWT Font is NOT a PDF font — it's Java's own font engine used for shaping Tamil text.
        //
        // classpathPath = "fonts/NotoSansTamil-Regular.ttf" or "fonts/LATHA.TTF"
        // These files live in: src/main/resources/fonts/ and are packaged into the JAR.

        ClassPathResource res = new ClassPathResource(classpathPath);
        try (InputStream is = res.getInputStream()) {
            // Font.createFont(TRUETYPE_FONT, inputStream) parses the TTF binary format
            // and creates an AWT Font object. The font is in "plain" style at 1pt by default.
            // .deriveFont(Font.PLAIN, size) creates a new variant at the requested point size.
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
        }
    }

    private void drawTableGrid(PDPageContentStream cs, float firstRowBottom) throws IOException {
        // Draws the complete table grid (all horizontal and vertical lines) as a SINGLE
        // stroked path. This is important: if each cell drew its own rectangle, shared
        // borders (e.g., the line between row 1 and row 2) would be stroked TWICE —
        // once by row 1's bottom and once by row 2's top. Double-stroked lines appear
        // visually darker than the single outer border — a subtle but noticeable bug.
        // Drawing everything in one pass guarantees consistent line thickness.

        float tableTop    = firstRowBottom + ROW_HEIGHT;
        // The Y coordinate of the very first horizontal line (top of table).

        float tableBottom = firstRowBottom - (NUM_ROWS - 1) * ROW_HEIGHT;
        // The Y coordinate of the last horizontal line (bottom of table).
        // NUM_ROWS-1 because firstRowBottom is already the bottom of row 1.

        float left    = MARGIN;
        float right   = MARGIN + TABLE_WIDTH;
        float divider = MARGIN + COL1_WIDTH;
        // X coordinates: left edge, column divider, right edge.

        cs.setLineWidth(0.5f);
        // Thin lines for a clean look. Default line width is 1.0.

        // Horizontal lines: one for each row top, plus the final bottom line.
        // i=0 → tableTop, i=1 → top of row 2, ..., i=NUM_ROWS → tableBottom.
        for (int i = 0; i <= NUM_ROWS; i++) {
            float lineY = tableTop - i * ROW_HEIGHT;
            cs.moveTo(left,  lineY);
            cs.lineTo(right, lineY);
        }

        // Three vertical lines: left edge, column divider, right edge.
        // Each spans the full table height from tableTop down to tableBottom.
        cs.moveTo(left,    tableTop); cs.lineTo(left,    tableBottom);
        cs.moveTo(divider, tableTop); cs.lineTo(divider, tableBottom);
        cs.moveTo(right,   tableTop); cs.lineTo(right,   tableBottom);

        cs.stroke();
        // stroke() renders all the moveTo/lineTo segments above as actual lines.
        // This ONE call draws the complete grid — efficient and visually consistent.
    }

    private void rowText(PDPageContentStream cs, String label, String value,
                         boolean valueTamil, Font tamilFont, float y) throws IOException {
        // Draws the text content of one table row (label in col1, value in col2).
        // Does NOT draw any borders — drawTableGrid() handles all borders.
        //
        // valueTamil = true → the value is Tamil text → use drawTamil() for the value cell.
        // valueTamil = false → value is English/numeric → use standard PDFBox cs.showText().

        float textY = y + CELL_PAD + 2f;
        // Vertical position of the text baseline inside the cell.
        // y is the bottom of the row; we add CELL_PAD + a small offset for visual centering.

        // Label cell (always English):
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(MARGIN + CELL_PAD, textY);
        cs.showText(label);
        cs.endText();

        // Value cell:
        if (valueTamil) {
            drawTamil(cs, value, tamilFont, MARGIN + COL1_WIDTH + CELL_PAD, textY);
            // Tamil value → render as vector glyph paths.
        } else {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(MARGIN + COL1_WIDTH + CELL_PAD, textY);
            cs.showText(value);
            cs.endText();
            // English value → standard PDFBox text rendering (fast, simple).
        }
    }

    private void drawTamil(PDPageContentStream cs, String text,
                            Font awtFont, float pdfX, float pdfY) throws IOException {
        // Renders Tamil (or any complex-script) text as VECTOR GLYPH PATHS in the PDF.
        //
        // WHY vector paths instead of just embedding the font?
        // PDF font embedding requires correct cmap/encoding tables. Tamil's OpenType
        // shaping (GSUB substitutions for vowel signs, GPOS positioning for matras,
        // conjunct forms) is handled by the operating system's text engine — not by
        // PDF viewers. Embedding the raw font doesn't guarantee correct shaping.
        // Solution: let the JVM's AWT text engine DO the shaping, then capture the
        // resulting glyph outlines as geometric paths. Those paths embed perfectly
        // in every PDF viewer — no font, no encoding, just shapes.

        if (text == null || text.isEmpty()) return;

        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        // FontRenderContext provides antialiasing and fractional-metrics hints.
        // new AffineTransform() = identity (no transform applied to the font itself).

        Shape outline = new TextLayout(text, awtFont, frc).getOutline(null);
        // TextLayout applies full OpenType shaping:
        //   GSUB: substitutes base + vowel sign sequences with correct combined glyphs
        //   GPOS: positions combining marks (matras) at the correct attachment points
        // getOutline(null) extracts the shaped glyphs as a Java 2D Shape.
        // null = no additional AffineTransform on the outline itself.

        cs.setNonStrokingColor(0, 0, 0);
        // Set fill color to black (RGB 0,0,0). Tamil paths are filled, not stroked.

        PathIterator pi = outline.getPathIterator(null);
        // PathIterator walks the Shape's path segment by segment.
        // Segment types: SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, SEG_CLOSE.

        float[] c = new float[6];
        // Coordinate array: up to 6 floats per segment (for cubic: x1,y1, x2,y2, x3,y3).

        float cx = 0, cy = 0;
        // Track the current point (needed for quadratic-to-cubic conversion below).

        boolean hasPath = false;

        while (!pi.isDone()) {
            int type = pi.currentSegment(c);
            // currentSegment(c) fills 'c' with coordinates and returns the segment type.

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    cs.moveTo(pdfX + c[0], pdfY - c[1]);
                    // KEY: AWT Y-axis increases DOWNWARD from the text baseline.
                    //      PDF Y-axis increases UPWARD from the page bottom.
                    // So to convert AWT (x, y) → PDF: pdfX + c[0], pdfY - c[1]
                    // pdfY is the baseline in PDF coordinates; subtracting AWT y flips the axis.
                    cx = c[0]; cy = c[1];
                    hasPath = true;
                    break;

                case PathIterator.SEG_LINETO:
                    cs.lineTo(pdfX + c[0], pdfY - c[1]);
                    // Straight line to (x, y). Same Y-flip as MOVETO.
                    cx = c[0]; cy = c[1];
                    break;

                case PathIterator.SEG_QUADTO: {
                    // TrueType fonts (TTF) use quadratic Bézier curves.
                    // PDF only supports CUBIC Bézier curves.
                    // We must convert quadratic → cubic using the standard formula:
                    //
                    // Given: current point (cx,cy), control point (qx,qy), end point (ex,ey)
                    // Cubic CP1 = current + 2/3 * (quad - current)
                    // Cubic CP2 = end     + 2/3 * (quad - end)
                    //
                    // This is mathematically exact — the cubic exactly represents the quadratic.
                    float qx = c[0], qy = c[1]; // quadratic control point
                    float ex = c[2], ey = c[3]; // end point
                    float bx1 = cx + 2f/3f*(qx-cx), by1 = cy + 2f/3f*(qy-cy); // cubic CP1
                    float bx2 = ex + 2f/3f*(qx-ex), by2 = ey + 2f/3f*(qy-ey); // cubic CP2
                    cs.curveTo(pdfX+bx1, pdfY-by1, pdfX+bx2, pdfY-by2, pdfX+ex, pdfY-ey);
                    // curveTo(cp1x, cp1y, cp2x, cp2y, endX, endY) — cubic Bézier in PDF.
                    cx = ex; cy = ey;
                    break;
                }

                case PathIterator.SEG_CUBICTO:
                    // Already a cubic — pass directly to PDF, with Y-flip applied.
                    cs.curveTo(pdfX+c[0], pdfY-c[1], pdfX+c[2], pdfY-c[3], pdfX+c[4], pdfY-c[5]);
                    cx = c[4]; cy = c[5];
                    break;

                case PathIterator.SEG_CLOSE:
                    cs.closePath();
                    // Closes the current sub-path (draws a line back to the last MOVETO point).
                    // Needed to close glyph outlines correctly (e.g., the inner hole of "ர").
                    break;
            }
            pi.next();
            // Advance to the next segment.
        }

        if (hasPath) cs.fill();
        // fill() renders all the path segments as filled shapes using the non-stroking color.
        // Result: solid black Tamil glyphs, perfectly shaped, embedded as vector art.
    }

    private String safe(String s) {
        return s != null ? s : "";
        // Null-to-empty-string helper. Prevents NullPointerException in cs.showText()
        // and drawTamil() when optional fields (remarks, mobile, email) are null.
    }
}
```

---

### FILE: controller/EmployeeController.java

**Purpose:** The REST API gateway — receives HTTP requests from Angular, calls the service layer, and returns HTTP responses. This is the ONLY class Angular directly communicates with.

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
// @Valid triggers Bean Validation on the @RequestBody argument.

import org.springframework.http.HttpHeaders;
// HttpHeaders lets us set custom response headers like Content-Type and Content-Disposition.

import org.springframework.http.HttpStatus;
// HTTP status code constants: OK (200), CREATED (201), BAD_REQUEST (400), NOT_FOUND (404), etc.

import org.springframework.http.MediaType;
// MIME type constants: APPLICATION_PDF, APPLICATION_JSON, etc.

import org.springframework.http.ResponseEntity;
// A wrapper that lets us control the full HTTP response: status code + headers + body.

import org.springframework.web.bind.annotation.*;
// Brings in @RestController, @RequestMapping, @GetMapping, @PostMapping, @PutMapping,
// @PathVariable, @RequestBody, @RequestParam, @CrossOrigin

import org.springframework.web.multipart.MultipartFile;
// Represents an uploaded file from a multipart/form-data request.

import java.io.IOException;
import java.util.List;

@RestController
// Combines @Controller + @ResponseBody.
// @Controller: This class handles HTTP requests.
// @ResponseBody: Return values from methods are automatically serialized to JSON
//               (using Jackson) and written to the HTTP response body.

@RequestMapping("/api/employees")
// ALL methods in this class will have URLs starting with /api/employees
// e.g., a method mapped to /list → full URL is /api/employees/list

@CrossOrigin(origins = "*")
// Enables CORS (Cross-Origin Resource Sharing) for ALL origins.
// Without this, browsers would BLOCK Angular (on localhost:4200) from calling
// the Spring API (on localhost:8080) because they're on different ports.
// origins="*" means: accept requests from any domain.
// In production, restrict this to your specific frontend domain.

public class EmployeeController {

    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;
    private final EmployeeReportService employeeReportService;

    public EmployeeController(EmployeeService employeeService,
                              FileStorageService fileStorageService,
                              EmployeeReportService employeeReportService) {
        // Constructor injection of all three service dependencies.
        // Spring injects EmployeeServiceImpl for EmployeeService (it's the only implementation).
        this.employeeService = employeeService;
        this.fileStorageService = fileStorageService;
        this.employeeReportService = employeeReportService;
    }

    // ─── ENDPOINT 1: CREATE EMPLOYEE ──────────────────────
    @PostMapping("/create")
    // Maps HTTP POST /api/employees/create to this method.
    // Angular calls: this.http.post('http://localhost:8080/api/employees/create', employeeData)

    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        // @RequestBody: reads the JSON from the HTTP request body and deserializes to EmployeeRequestDTO.
        // @Valid: triggers Bean Validation on requestDTO (checks @NotBlank, @Email, etc.)
        //   If validation fails → Spring throws MethodArgumentNotValidException
        //   → GlobalExceptionHandler catches it → HTTP 400 with error details

        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO);
        // Delegate to the service layer. All business logic is there.

        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee created successfully", created);
        // Wrap the result in our standard response envelope.

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // HTTP 201 Created (not 200 OK) — the standard status for resource creation.
        // Body: { "message": "Employee created successfully", "data": { "id": 1, ... } }
        // Angular's subscribe next() callback receives this.
    }

    // ─── ENDPOINT 2: GET ALL EMPLOYEES ────────────────────
    @GetMapping("/list")
    // Maps HTTP GET /api/employees/list
    // Angular calls: this.http.get('http://localhost:8080/api/employees/list')

    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees() {

        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        // Fetch all employees from DB via service.

        ApiResponse<List<EmployeeResponseDTO>> response =
                new ApiResponse<>("Employee list fetched successfully", employees);
        // data field = array of employee objects in JSON.

        return ResponseEntity.ok(response);
        // ResponseEntity.ok() = HTTP 200 OK
        // { "message": "Employee list fetched successfully", "data": [...] }
    }

    // ─── ENDPOINT 3: GET EMPLOYEE BY ID ───────────────────
    @GetMapping("/{id}")
    // Maps HTTP GET /api/employees/{id} where {id} is a path variable.
    // e.g., GET /api/employees/5 → id = 5

    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable Long id) {
        // @PathVariable Long id: extracts {id} from the URL and converts to Long.

        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        // Service throws EmployeeNotFoundException if id not found → GlobalExceptionHandler → 404

        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee fetched successfully", employee);

        return ResponseEntity.ok(response);
    }

    // ─── ENDPOINT 4: UPDATE EMPLOYEE ──────────────────────
    @PutMapping("/update/{id}")
    // Maps HTTP PUT /api/employees/update/{id}
    // Angular calls: this.http.put('...update/5', updatedData)

    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        // @PathVariable id: employee ID from URL
        // @RequestBody: updated employee data from Angular
        // @Valid: validates the request DTO before proceeding

        EmployeeResponseDTO updated = employeeService.updateEmployee(id, requestDTO);

        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Employee updated successfully", updated);

        return ResponseEntity.ok(response);
    }

    // ─── ENDPOINT 5: UPLOAD APPOINTMENT ORDER ─────────────
    @PostMapping("/upload/{id}")
    // Maps HTTP POST /api/employees/upload/{id} — multipart file upload.
    // Angular calls: this.http.post('...upload/5', formData)
    // formData contains the PDF file with field name "file".

    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> uploadAppointmentOrder(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        // @PathVariable id: which employee this file belongs to.
        // @RequestParam("file"): reads the file part named "file" from the multipart request.
        //   Angular sends: formData.append('file', selectedFile) → "file" is the key.
        // throws IOException: file operations can throw IOExceptions.

        // ─── SERVER-SIDE VALIDATIONS ──────────────────────
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
            // GlobalExceptionHandler catches this → HTTP 400
        }

        long maxSizeBytes = 2L * 1024L * 1024L;
        // 2L * 1024L * 1024L = 2,097,152 bytes = 2 MB.
        // Using "L" suffix for long integer literals (not int) to avoid integer overflow.

        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException("File size must be <= 2 MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            // .toLowerCase() handles "appointment.PDF" and "appointment.Pdf" etc.
            throw new InvalidFileException("Only PDF files are allowed");
        }

        // ─── SAVE FILE TO DISK ────────────────────────────
        String savedPath = fileStorageService.saveAppointmentOrderFile(id, file);
        // Saves the file to uploads/employee_{id}_appointment.pdf on server disk.
        // Returns the absolute path string.

        // ─── UPDATE DB WITH FILE INFO ─────────────────────
        EmployeeResponseDTO updated =
                employeeService.updateAppointmentOrder(id, savedPath, originalFileName);
        // Stores the path and original filename in the database.
        // originalFileName is what the user sees in the UI ("my_appointment.pdf").

        ApiResponse<EmployeeResponseDTO> response =
                new ApiResponse<>("Appointment order uploaded successfully", updated);

        return ResponseEntity.ok(response);
    }

    // ─── ENDPOINT 6: DELETE EMPLOYEE ─────────────────────
    @DeleteMapping("/delete/{id}")
    // Maps HTTP DELETE /api/employees/delete/{id}
    // Angular calls: this.http.delete('http://localhost:8080/api/employees/delete/5')
    // NOTE: The Delete button in Angular is hidden when showDeleteButton = false (interview-safe flag).

    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long id) {
        // @PathVariable Long id: extracts {id} from the URL and converts to Long.
        // Returns ApiResponse<String> because there's no employee object to return after deletion.

        employeeService.deleteEmployee(id);
        // Delegate to service. If employee not found: throws EmployeeNotFoundException → 404.
        // If found: permanently deletes the row from the database.

        ApiResponse<String> response = new ApiResponse<>("Employee deleted successfully", null);
        // null data: nothing to return after a delete operation.

        return ResponseEntity.ok(response);
        // HTTP 200 OK — deletion was successful.
        // Angular's subscribe success callback fires with this response.
    }

    // ─── ENDPOINT 7: GENERATE PDF REPORT ─────────────────
    @GetMapping("/report/{id}")
    // Maps HTTP GET /api/employees/report/{id}
    // Angular calls with responseType:'blob' to receive binary data.

    public ResponseEntity<byte[]> generateEmployeeReport(@PathVariable Long id) {
        // Returns ResponseEntity<byte[]> (not ApiResponse) because this is binary PDF data,
        // not a JSON payload. Angular expects raw bytes, not JSON.

        byte[] pdfBytes = employeeReportService.generateEmployeeReport(id);
        // Generates the PDF in memory and returns the raw bytes.

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);
        // Content-Type: application/pdf
        // Tells the browser/client that the response body is a PDF file.

        headers.setContentDispositionFormData("attachment", "employee_report_" + id + ".pdf");
        // Content-Disposition: attachment; filename="employee_report_5.pdf"
        // "attachment" = browser should DOWNLOAD the file (not display in tab).
        // The filename hint appears in the browser's "Save As" dialog.
        // Angular overrides this filename in code anyway (a.download = "employee-report-5.pdf").

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        // HTTP 200 with PDF bytes as body + custom headers.
        // Angular receives this as a Blob object (binary data).
    }
}
```

**How this connects:**

- Angular's `EmployeeService` (HttpClient) → HTTP requests → this controller
- Controller → `EmployeeServiceImpl` for business logic
- Controller → `FileStorageService` for file saving
- Controller → `EmployeeReportService` for PDF generation
- All exceptions bubble up to `GlobalExceptionHandler`

---

## 6. FRONTEND — ANGULAR

---

### FILE: src/main.ts

**Purpose:** The JavaScript entry point of the Angular application. The browser runs this first.

```typescript
import { bootstrapApplication } from "@angular/platform-browser";
// bootstrapApplication() is the Angular 14+ way to start an app WITHOUT NgModules.
// It replaces the older platformBrowserDynamic().bootstrapModule(AppModule) pattern.

import { appConfig } from "./app/app.config";
// appConfig contains the global providers: Router, HttpClient, change detection strategy.
// Read in app.config.ts below.

import { App } from "./app/app";
// App is the ROOT COMPONENT — the top-level component that contains <router-outlet>.
// Every other component renders INSIDE this one.

bootstrapApplication(App, appConfig)
  // This one call starts the entire Angular application:
  // 1. Sets up the dependency injection container with providers from appConfig
  // 2. Renders the App component into <app-root> element in index.html
  // 3. Initializes the Router and evaluates the current URL
  // 4. Router matches URL to a route and renders the appropriate component
  // 5. App is now live in the browser

  .catch((err) => console.error(err));
// If bootstrapping fails (e.g., config error, missing provider),
// log the error to the browser console. The app won't load but at least we see why.
```

**Flow:** `browser loads index.html` → `<script>` tag loads bundled main.ts → `bootstrapApplication()` → Angular starts

---

### FILE: src/app/app.config.ts

**Purpose:** Configures the global providers (services/features) available throughout the app.

```typescript
import {
  ApplicationConfig,
  provideZonelessChangeDetection,
} from "@angular/core";
// ApplicationConfig = the type for the config object.
// provideZonelessChangeDetection = opt into Angular's new Zone-free change detection (Angular 18+).

import { provideRouter } from "@angular/router";
// Registers the Angular Router and its services into the DI container.

import { provideHttpClient, withFetch } from "@angular/common/http";
// provideHttpClient: Registers HttpClient so it can be injected into services like EmployeeService.
//   Without this, injecting HttpClient would throw a NullInjectorError.
// withFetch: A feature flag that switches HttpClient from XMLHttpRequest to the native browser
//   Fetch API. Required when Angular SSR (Server-Side Rendering) is enabled — the server-side
//   Node.js environment does not have XMLHttpRequest, but does have a fetch-compatible polyfill.

import { routes } from "./app.routes";
// Import our route definitions from app.routes.ts.

export const appConfig: ApplicationConfig = {
  providers: [
    // providers = the list of things to register in Angular's dependency injection system.

    provideZonelessChangeDetection(),
    // Angular traditionally used Zone.js to detect changes (monkey-patches async operations).
    // Zoneless change detection is the new, faster approach: components explicitly call
    // markForCheck() or detectChanges() to trigger UI updates.
    // That's why you see cdr.markForCheck() and cdr.detectChanges() throughout components.

    provideRouter(routes),
    // Registers the Angular Router with our route table.
    // This enables <router-outlet>, routerLink, ActivatedRoute, Router injection, etc.
    // routes is the array from app.routes.ts that maps URL paths to components.

    provideHttpClient(withFetch()),
    // Makes HttpClient available for injection everywhere, using the native Fetch API.
    // withFetch() is passed as a "feature" argument — it configures the HttpClient
    // to use window.fetch() instead of XMLHttpRequest under the hood.
    // Benefits: works in both browser AND SSR server environments, and aligns
    // with modern browser standards.
    // HttpClient is what EmployeeService uses to make HTTP calls to the Spring backend.
  ],
};
```

---

### FILE: src/app/app.routes.ts

**Purpose:** Defines all URL routes and which component to show for each URL.

```typescript
import { Routes } from "@angular/router";
// Routes is a type alias for Array<Route>. Each Route maps a URL path to a component.

import { EmployeeListComponent } from "./employee/employee-list/employee-list";
import { EmployeeCreateComponent } from "./employee/employee-create/employee-create";
import { EmployeeEditComponent } from "./employee/employee-edit/employee-edit";
import { EmployeeViewComponent } from "./employee/employee-view/employee-view";
import { EmployeeReportComponent } from "./employee/employee-report/employee-report";
// Import all the components that can be navigated to.

export const routes: Routes = [
  { path: "", redirectTo: "employee/list", pathMatch: "full" },
  // When URL is exactly "" (root path "/"):
  //   pathMatch: 'full' = only match if the ENTIRE URL is empty (not just starts with "")
  //   redirectTo: 'employee/list' = immediately redirect to /employee/list
  // This means the app always lands on the employee list on first load.

  { path: "employee/list", component: EmployeeListComponent },
  // URL /employee/list → render EmployeeListComponent in <router-outlet>

  { path: "employee/create", component: EmployeeCreateComponent },
  // URL /employee/create → render EmployeeCreateComponent

  { path: "employee/edit/:id", component: EmployeeEditComponent },
  // :id is a ROUTE PARAMETER — it's dynamic. Can be any value.
  // /employee/edit/5 → id = "5" (string, must convert to Number in component)
  // /employee/edit/42 → id = "42"
  // Component reads it with: this.route.snapshot.paramMap.get('id')

  { path: "employee/view/:id", component: EmployeeViewComponent },
  // Same pattern: /employee/view/3 → loads EmployeeViewComponent with id=3

  { path: "employee/report/:id", component: EmployeeReportComponent },
  // /employee/report/7 → loads EmployeeReportComponent with id=7
];
```

---

### FILE: src/app/app.ts

**Purpose:** The root component — the shell of the application. Contains the navigation and the `<router-outlet>` where all other components are rendered.

```typescript
import { Component } from "@angular/core";
// @Component decorator — marks this class as an Angular component.

import { RouterOutlet, RouterLink } from "@angular/router";
// RouterOutlet: the placeholder where the matched route's component is rendered.
// RouterLink: directive for navigation links (like <a routerLink="/employee/list">).

@Component({
  selector: "app-root",
  // The CSS selector for this component. In index.html there's <app-root></app-root>.
  // Angular finds that element and renders this component inside it.

  standalone: true,
  // Angular 14+ feature: this component does NOT belong to any NgModule.
  // All dependencies must be declared in the imports array below.
  // This is the modern Angular pattern — simpler and tree-shakable.

  imports: [RouterOutlet, RouterLink],
  // Must import RouterOutlet to use <router-outlet> in the template.
  // Must import RouterLink to use routerLink directive on <a> tags.
  // Without these imports, Angular would throw a template parse error.

  templateUrl: "./app.html",
  // Points to the HTML template file for this component.

  styleUrl: "./app.scss",
  // Points to the SCSS stylesheet. Styles here are SCOPED to this component only
  // (Angular adds a unique attribute like _ngcontent-abc to prevent leaking).
})
export class App {
  title = "employee-frontend";
  // A property on this component class. Could be used in the template as {{ title }}.
  // Not currently displayed in the UI but could be used in the browser tab title.
}
```

---

### FILE: src/app/services/employee.ts

**Purpose:** The Angular service layer — handles ALL HTTP communication with the Spring backend. All components inject this service; none make HTTP calls directly.

```typescript
import { Injectable } from "@angular/core";
// @Injectable marks this class as injectable into other classes via Angular's DI system.

import { HttpClient } from "@angular/common/http";
// HttpClient is Angular's HTTP client for making AJAX requests to the backend.
// Provides: .get(), .post(), .put(), .delete() etc. that return Observables.

import { Observable } from "rxjs";
// Observable is RxJS's async data stream type.
// HTTP calls return Observable — they haven't fired yet. Fire when you .subscribe().

export interface Employee {
  // TypeScript interface — defines the shape of an employee object on the frontend.
  // This is NOT a class, just a type contract for type safety.

  id?: number;
  // "?" = optional. id may not exist for new employees (before they're saved).

  employeeCode: string;
  employeeNameEnglish: string;
  employeeNameTamil: string;
  designation: string;
  department: string;
  dateOfJoining: string;
  // String (not Date) on the frontend because Angular form controls work with strings.
  // The Spring backend parses "2024-01-15" → LocalDate automatically.

  mobileNumber: string;
  email: string;
  remarks: string;
  appointmentOrderFileName?: string;
  // Optional — only present if an appointment order PDF has been uploaded.
}

@Injectable({
  providedIn: "root",
})
// providedIn: 'root' = register this service as a SINGLETON in the root injector.
// Singleton means: ONE instance of EmployeeService exists for the entire application.
// All components that inject EmployeeService get the SAME instance.
// This is the modern alternative to listing services in NgModule providers array.
export class EmployeeService {
  private baseUrl = "http://localhost:8080/api/employees";
  // The base URL for all backend API calls.
  // "private" = only accessible within this class, not from outside.
  // If the Spring server runs on a different port or host, change this ONE place.

  constructor(private http: HttpClient) {}
  // Constructor injection: Angular provides an HttpClient instance automatically.
  // "private" = shorthand that both declares the field AND assigns the constructor param.
  // Equivalent to: private http: HttpClient; constructor(http: HttpClient) { this.http = http; }

  getAllEmployees(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/list`);
    // Template literal builds URL: 'http://localhost:8080/api/employees/list'
    // http.get() creates an Observable — does NOT fire the request yet.
    // The request fires when a component calls .subscribe() on the returned Observable.
    // Returns Observable<any> because the actual type depends on server response shape.
    // → EmployeeListComponent.loadEmployees() subscribes to this.
  }

  getEmployeeById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
    // GET http://localhost:8080/api/employees/5
    // → Used by EmployeeEditComponent, EmployeeViewComponent, EmployeeReportComponent
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${this.baseUrl}/create`, employee);
    // POST http://localhost:8080/api/employees/create
    // employee object is serialized to JSON and sent as the request body.
    // Angular automatically sets Content-Type: application/json header.
    // → EmployeeCreateComponent.onSubmit() subscribes to this.
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/update/${id}`, employee);
    // PUT http://localhost:8080/api/employees/update/5
    // PUT = replace the entire resource (as opposed to PATCH = partial update).
    // employee object sent as JSON body.
    // → EmployeeEditComponent.onSubmit() subscribes to this.
  }

  uploadAppointmentOrder(id: number, file: File): Observable<any> {
    const formData = new FormData();
    // FormData is the browser's API for building multipart/form-data requests.
    // Used for file uploads — you can't send File objects as JSON.

    formData.append("file", file);
    // Adds the file to the form data with field name "file".
    // Spring Controller reads it with @RequestParam("file") MultipartFile file
    // The field names MUST match: Angular's 'file' ↔ Spring's @RequestParam("file")

    return this.http.post<any>(`${this.baseUrl}/upload/${id}`, formData);
    // POST http://localhost:8080/api/employees/upload/5
    // Angular automatically sets Content-Type: multipart/form-data with boundary.
    // Do NOT manually set Content-Type — Angular needs to set the boundary parameter.
    // → EmployeeCreateComponent.uploadFile() and EmployeeEditComponent.uploadFile() use this.
  }

  deleteEmployee(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/delete/${id}`);
    // HTTP DELETE to /api/employees/delete/{id}
    // Returns Observable<any> — fires when subscribed in deleteEmployee() method of EmployeeListComponent.
    // The Delete button that triggers this is hidden behind showDeleteButton = false flag.
    // → EmployeeListComponent.deleteEmployee() subscribes to this.
  }

  downloadEmployeeReport(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/report/${id}`, {
      responseType: "blob",
      // CRITICAL: tells Angular NOT to parse the response as JSON.
      // Instead, treat the response body as binary data (Blob).
      // Without this, Angular would try to JSON.parse() the PDF bytes and fail.
      // Returns Observable<Blob> — the Blob contains the raw PDF bytes.
    });
    // → EmployeeReportComponent.downloadReport() subscribes to this.
  }
}
```

---

### FILE: employee-list/employee-list.ts

**Purpose:** Displays all employees in a searchable, sortable table. The main landing page of the app.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
// Component: decorator for defining an Angular component.
// OnInit: lifecycle hook interface — ngOnInit() fires after component initialization.
// ChangeDetectorRef: gives manual control over Angular's change detection.
//   Needed because we're using provideZonelessChangeDetection() — no Zone.js.

import { CommonModule } from "@angular/common";
// Provides: *ngIf, *ngFor, async pipe, date pipe, etc. in the template.
// Must import explicitly in standalone components.

import { FormsModule } from "@angular/forms";
// Provides: [(ngModel)] two-way binding. Used for the search input box.

import { Router } from "@angular/router";
// Router service for programmatic navigation (router.navigate([...])).

import { EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-list",
  standalone: true,
  imports: [CommonModule, FormsModule],
  // FormsModule needed for [(ngModel)] on the search input.
  // CommonModule needed for *ngIf and *ngFor in the template.
  templateUrl: "./employee-list.html",
  styleUrl: "./employee-list.scss",
})
export class EmployeeListComponent implements OnInit {
  employees: any[] = [];
  // Array that holds all employees fetched from the backend.
  // Initialized to empty array — the table shows "No employees found." until data loads.

  searchTerm: string = "";
  // Bound to the search input via [(ngModel)]. Updated on every keystroke.
  // Used by the filteredEmployees getter to filter the displayed rows.

  successMessage: string = "";
  errorMessage: string = "";
  // Messages shown as banners above the table.
  // successMessage: "Employee created successfully." (passed via router state)
  // errorMessage: "Failed to load employees" (API error)

  // ─── INTERVIEW-SAFE DELETE FLAG ───────────────────────
  // FLAG: set to true to show the Delete button (hidden for interview/demo)
  showDeleteButton = false;
  // When false (default): the Delete button is completely absent from the DOM (@if block not rendered).
  // When true: Delete button appears next to the Report button in each row.
  // This lets the developer test deletion locally without exposing it in an interview/demo.
  // USAGE: flip to true → test deletes → flip back to false before the interview.

  constructor(
    private employeeService: EmployeeService,
    // Inject the service to call the backend API.

    private router: Router,
    // Inject the Router for navigation to edit/view/create pages.

    private cdr: ChangeDetectorRef,
    // Inject ChangeDetectorRef to manually trigger UI updates.
    // Required with Zoneless change detection.
  ) {}

  ngOnInit(): void {
    // ngOnInit() is called by Angular ONCE after the component is created and
    // all @Input() properties are set. It's the right place to:
    // - Read route params/state
    // - Fetch initial data from APIs
    // Do NOT put these in the constructor (constructor runs too early).

    const navigation = this.router.getCurrentNavigation();
    // getCurrentNavigation() gives us the current navigation object.
    // This is available ONLY during the navigation (inside ngOnInit at route arrival).
    // After navigation completes, this returns null.

    this.successMessage =
      navigation?.extras?.state?.["successMessage"] ||
      // Primary method: read success message from the navigation state object.
      // Set by previous component: router.navigate(['/employee/list'], { state: { successMessage: '...' } })
      // Optional chaining (?.) prevents errors if navigation/extras/state is null.

      (typeof window !== "undefined"
        ? window.history.state?.successMessage
        : "") ||
      // Fallback method: read from browser's history.state.
      // window.history.state persists even after navigation completes.
      // typeof window !== 'undefined' check is for SSR compatibility (server doesn't have window).

      "";
    // Default: empty string if no success message was passed.

    this.loadEmployees();
    // Fetch employees from backend API.

    if (this.successMessage) {
      setTimeout(() => {
        // setTimeout with 3000ms = hide success message after 3 seconds.
        // Uses setTimeout because we want a time-based side effect.
        this.successMessage = "";
        this.cdr.markForCheck();
        // markForCheck() tells Angular: "this component needs to be re-rendered."
        // Required in Zoneless mode — Angular won't know the data changed otherwise.
      }, 3000);
    }
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployees().subscribe(
      // .subscribe() fires the HTTP request and handles the response.
      // Two callback form (older style): (success, error) => ...

      (response: any) => {
        // SUCCESS callback: called when HTTP 200 response arrives.
        console.log("Employee list API response:", response);
        // Log helps during development to see what the API actually returns.

        if (Array.isArray(response)) {
          this.employees = response;
          // If the backend returns a plain array (shouldn't happen with our ApiResponse wrapper,
          // but defensive check in case the API changes).
        } else if (response && Array.isArray(response.content)) {
          this.employees = response.content;
          // Spring Data Page objects have a "content" field for the data array.
          // If pagination is added later, this handles it.
        } else if (response && Array.isArray(response.data)) {
          this.employees = response.data;
          // Our ApiResponse wrapper format: { "message": "...", "data": [...] }
          // This is the ACTUAL format our backend sends.
        } else if (response && Array.isArray(response.employees)) {
          this.employees = response.employees;
          // Fallback for alternative response shapes.
        } else {
          this.employees = [];
          // If none of the above match, show empty table.
        }

        this.errorMessage = "";
        // Clear any previous error message on success.
        this.cdr.markForCheck();
        // Trigger re-render with the new employees array.
      },

      (error: any) => {
        // ERROR callback: called when HTTP request fails (4xx, 5xx, network error).
        console.error("Load employee error:", error);
        this.errorMessage = "Failed to load employees";
        this.employees = [];
        this.cdr.markForCheck();
      },
    );
  }

  get filteredEmployees(): any[] {
    // A computed property (getter) — recalculated whenever searchTerm or employees changes.
    // Returns the list of employees to display (filtered + sorted).
    // Template uses: *ngFor="let employee of filteredEmployees"

    const value = this.searchTerm.trim().toLowerCase();
    // trim() removes leading/trailing spaces. toLowerCase() makes search case-insensitive.
    // "  EMP  ".trim().toLowerCase() → "emp"

    const sorted = [...this.employees].sort((a, b) =>
      (a.employeeCode || "").localeCompare(b.employeeCode || "", undefined, {
        numeric: true,
      }),
    );
    // [...this.employees] = spread to create a NEW array (don't modify the original).
    // .sort() sorts by employeeCode.
    // localeCompare with numeric: true means: "EMP2" < "EMP10" (numeric sort, not alphabetic).
    // Without numeric: true, "EMP10" would sort before "EMP2" alphabetically.
    // (a.employeeCode || '') handles null/undefined employeeCode safely.

    if (!value) {
      return sorted;
      // No search term → return all employees (just sorted).
    }

    return sorted.filter(
      (employee: any) =>
        (employee.employeeCode || "").toLowerCase().includes(value) ||
        (employee.employeeNameEnglish || "").toLowerCase().includes(value) ||
        (employee.employeeNameTamil || "").toLowerCase().includes(value),
      // .filter() keeps employees where ANY of these fields match the search term.
      // includes(value) = case-insensitive substring match.
      // Search "john" finds employees with "John Smith" or "johnso@email.com" in name.
    );
  }

  goToAddEmployee(): void {
    this.router.navigate(["/employee/create"]);
    // Programmatic navigation to the Create Employee page.
    // Called when "Add Employee" button is clicked.
  }

  goToViewEmployee(id: number): void {
    this.router.navigate(["/employee/view", id]);
    // Navigate to view page: /employee/view/5
    // id is passed as a separate array element → Router builds the URL.
  }

  goToEditEmployee(id: number): void {
    this.router.navigate(["/employee/edit", id]);
    // Navigate to edit page: /employee/edit/5
  }

  goToReport(id: number | undefined): void {
    if (id == null) return;
    // Guard: if id is null or undefined (shouldn't happen), do nothing.
    // "== null" catches both null and undefined.

    this.router.navigate(["/employee/report", id]);
    // Navigate to report page: /employee/report/5
  }

  deleteEmployee(id: number | undefined): void {
    // Called when the Delete button is clicked (only visible when showDeleteButton = true).
    // Permanently removes the employee from the database after user confirmation.

    if (id == null) return;
    // Guard: do nothing if id is somehow null or undefined.

    if (!window.confirm("Are you sure you want to permanently delete this employee?")) return;
    // Show a browser confirm dialog.
    // If the user clicks "Cancel" → confirm() returns false → method returns immediately.
    // If the user clicks "OK" → confirm() returns true → proceed with deletion.
    // This prevents accidental deletes.

    this.employeeService.deleteEmployee(id).subscribe(
      () => {
        // Success callback: HTTP 200 received
        this.successMessage = "Employee deleted successfully";
        // Show green success banner above the table.

        this.loadEmployees();
        // Reload the employee list from the backend.
        // The deleted employee will no longer be in the response → table updates.

        setTimeout(() => {
          this.successMessage = "";
          this.cdr.markForCheck();
          // Clear the success banner after 3 seconds and trigger re-render.
        }, 3000);
      },
      (error: any) => {
        // Error callback: HTTP 404 (not found) or HTTP 500 (server error)
        console.error("Delete employee error:", error);
        this.errorMessage = "Failed to delete employee";
        // Show red error banner.
        this.cdr.markForCheck();
      },
    );
  }
}
```

---

### FILE: employee-list/employee-list.html

**Purpose:** The HTML template for the employee list page.

```html
<div class="employee-list-container">
  <!-- Root div with CSS class for styling the entire list page -->

  <div class="page-header">
    <h2>Employee List</h2>
    <!-- Page title -->

    <button class="add-btn" type="button" (click)="goToAddEmployee()">Add Employee</button>
    <!-- (click)="goToAddEmployee()" = Angular event binding.
         When button is clicked, calls goToAddEmployee() in the TypeScript class.
         goToAddEmployee() → router.navigate(['/employee/create'])
         type="button" prevents accidental form submission (important inside <form> elements). -->
  </div>

  <div class="search-box">
    <input
      type="text"
      [(ngModel)]="searchTerm"
      <!-- [(ngModel)] = two-way data binding.
           [ ] = property binding (searchTerm → input value): input shows what searchTerm is.
           ( ) = event binding (input value → searchTerm): as user types, searchTerm updates.
           Together, they keep the TypeScript variable and the input field in sync.
           When searchTerm changes, filteredEmployees getter recalculates → table updates. -->
      placeholder=" Search by Employee Name or Employee Code"
      class="search-input"
    />
  </div>

  <div *ngIf="successMessage" class="success-message">
    {{ successMessage }}
    <!-- *ngIf="successMessage": only shows this div if successMessage is a non-empty string.
         {{ successMessage }}: Angular interpolation — inserts the variable value as text.
         This shows the green "Employee created successfully." banner. -->
  </div>

  <div *ngIf="errorMessage" class="error-message">
    {{ errorMessage }}
    <!-- Shows in red if API call fails. -->
  </div>

  <div *ngIf="filteredEmployees.length > 0; else noEmployees" class="table-wrapper">
    <!-- *ngIf with else syntax:
         If filteredEmployees has items → show this div (the table).
         If empty → show the <ng-template #noEmployees> block instead.
         filteredEmployees is the computed getter that filters+sorts employees. -->

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
          <!-- Column headers. "Actions" column contains the View/Edit/Report buttons. -->
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let employee of filteredEmployees">
        <!-- *ngFor = structural directive that repeats this <tr> for EACH employee in filteredEmployees.
             "let employee" = loop variable for each iteration.
             Angular renders one <tr> per employee, creating a table row for each. -->

          <td>{{ employee.id }}</td>
          <!-- {{ employee.id }} = interpolation: inserts employee.id as text in the cell.
               Angular automatically converts numbers/dates to strings for display. -->
          <td>{{ employee.employeeCode }}</td>
          <td>{{ employee.employeeNameEnglish }}</td>
          <td>{{ employee.employeeNameTamil }}</td>
          <!-- Tamil Unicode characters display correctly in the browser.
               The browser handles Tamil rendering — no special font needed in HTML. -->
          <td>{{ employee.designation }}</td>
          <td>{{ employee.department }}</td>
          <td>{{ employee.dateOfJoining }}</td>
          <td>{{ employee.mobileNumber }}</td>
          <td>{{ employee.email }}</td>
          <td class="actions">
            <button class="view-btn" type="button" (click)="goToViewEmployee(employee.id)">
              View
              <!-- Calls goToViewEmployee(employee.id) → router.navigate(['/employee/view', employee.id]) -->
            </button>

            <button class="edit-btn" type="button" (click)="goToEditEmployee(employee.id)">
              Edit
              <!-- Calls goToEditEmployee(employee.id) → router.navigate(['/employee/edit', employee.id]) -->
            </button>

            <button class="report-btn" type="button" (click)="goToReport(employee.id)">
              Report
              <!-- Calls goToReport(employee.id) → router.navigate(['/employee/report', employee.id]) -->
            </button>

            @if (showDeleteButton) {
              <button class="delete-btn" type="button" (click)="deleteEmployee(employee.id)">
                Delete
                <!-- @if (showDeleteButton): Angular 17+ control flow — renders this block ONLY when
                     showDeleteButton is true. When false, the button is completely absent from the DOM
                     (not just hidden with CSS). This is the interview-safe flag.
                     (click)="deleteEmployee(employee.id)" → calls EmployeeListComponent.deleteEmployee()
                     which shows a confirm dialog then calls DELETE /api/employees/delete/{id} -->
              </button>
            }
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <ng-template #noEmployees>
    <div class="no-data">No employees found.</div>
    <!-- ng-template is NOT rendered directly — it's a blueprint.
         Angular renders it when the *ngIf condition above is false (filteredEmployees is empty).
         #noEmployees = template reference variable — the *ngIf "else noEmployees" points to this. -->
  </ng-template>

</div>
```

---

### FILE: employee-create/employee-create.ts

**Purpose:** Handles the "Create New Employee" form. Builds a reactive form, validates it, and calls the API to save the new employee.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
// FormBuilder: factory for creating FormGroup objects — simplifies form creation.
// FormGroup: represents a collection of form controls as a group. Validates all together.
// ReactiveFormsModule: must be imported to use [formGroup], formControlName in templates.
// Validators: built-in validation functions: required, email, pattern, minLength, etc.

import { Router } from "@angular/router";
import { Employee, EmployeeService } from "../../services/employee";

@Component({
  selector: "app-employee-create",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  // ReactiveFormsModule: enables formGroup directive in the template.
  templateUrl: "./employee-create.html",
  styleUrl: "./employee-create.scss",
})
export class EmployeeCreateComponent implements OnInit {
  employeeForm!: FormGroup;
  // "!" = definite assignment assertion (TypeScript).
  // Tells TypeScript: "I know this will be assigned before use (in ngOnInit), trust me."
  // Without "!", TypeScript would error: "Property has no initializer and is not definitely assigned."
  // FormGroup holds all form controls + tracks validity and dirty/touched state.

  selectedFile: File | null = null;
  // The PDF file selected by the user for the appointment order.
  // null = no file selected yet. File = a File object from the browser's FileList API.

  successMessage: string = "";
  errorMessage: string = "";

  designations: string[] = [
    "Junior Assistant",
    "Senior Assistant",
    "Developer",
    "Senior Developer",
    "Manager",
    "Administrator",
  ];
  // Array of designation options for the dropdown select.
  // Rendered with @for in the template to build <option> elements.
  // Hardcoded here — in a real app this might come from an API.

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({
      // fb.group() creates a FormGroup with the specified controls.
      // Each key = a form field name. Value = [initial value, validators]

      employeeCode: ["", Validators.required],
      // Control named 'employeeCode', initial value = '', required.
      // Connected to <input formControlName="employeeCode"> in the template.
      // Angular keeps the control and input in sync automatically.

      employeeNameEnglish: ["", Validators.required],
      employeeNameTamil: ["", Validators.required],
      designation: ["", Validators.required],
      department: ["", Validators.required],
      dateOfJoining: ["", Validators.required],

      mobileNumber: [
        "",
        [Validators.required, Validators.pattern("^[0-9]{10}$")],
      ],
      // Two validators in an array: required AND pattern.
      // Validators.pattern('^[0-9]{10}$'):
      //   ^ = start of string
      //   [0-9] = any digit 0-9
      //   {10} = exactly 10 times
      //   $ = end of string
      //   Together: exactly 10 digits, nothing else.

      email: ["", [Validators.required, Validators.email]],
      // Validators.email: checks for valid email format (has @, has domain, etc.)

      remarks: [""],
      // Remarks is optional — no validators. Empty string initial value is fine.
    });
  }

  onFileChange(event: any): void {
    // Called by (change)="onFileChange($event)" on the file input in the template.
    // $event is the browser's DOM Event object from the file input change event.

    const file = event.target.files[0];
    // event.target = the <input type="file"> element.
    // .files = FileList (array-like object) of selected files.
    // [0] = the first (and only) selected file (we don't allow multiple).

    if (!file) return;
    // Guard: if user clicked "Cancel" in the file dialog, files is empty.

    if (file.type !== "application/pdf") {
      // file.type = MIME type reported by the browser. "application/pdf" for PDF files.
      // MIME type can be spoofed — that's why the server also checks the extension.
      this.errorMessage = "Only PDF files are allowed.";
      this.selectedFile = null;
      // Clear any previously selected file.
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      // file.size = file size in bytes.
      // 2 * 1024 * 1024 = 2,097,152 bytes = 2MB.
      // Client-side check for faster feedback. Server also checks.
      this.errorMessage = "File size must be less than or equal to 2 MB.";
      this.selectedFile = null;
      return;
    }

    this.errorMessage = "";
    // Clear any previous validation error.
    this.selectedFile = file;
    // Store the valid file reference for use in onSubmit().
  }

  onSubmit(): void {
    this.successMessage = "";
    this.errorMessage = "";

    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      // markAllAsTouched() marks every control as "touched".
      // Error messages only show when a control is "touched" (user interacted with it).
      // By calling this on submit, we force ALL error messages to appear at once
      // so the user can see every problem, not just the first one.
      return;
      // Stop here — don't submit invalid data to the server.
    }

    const employeeData: Employee = this.employeeForm.value;
    // .value returns a plain JavaScript object with all form field values.
    // { employeeCode: "EMP001", employeeNameEnglish: "John", ... }
    // TypeScript cast to Employee interface for type checking.

    this.employeeService.createEmployee(employeeData).subscribe({
      // Observable subscription with object notation (modern style).
      // next: fired on success (HTTP 2xx response)
      // error: fired on failure (HTTP 4xx/5xx or network error)

      next: (response: any) => {
        const created = response?.data ? response.data : response;
        // Our ApiResponse format: { "message": "...", "data": { "id": 1, ... } }
        // If response.data exists → extract the employee object from it.
        // If not (raw employee object) → use response directly.
        // Optional chaining (?.) prevents null reference error.

        if (this.selectedFile && created?.id) {
          // If user selected a PDF file AND we got back the new employee's ID:
          this.uploadFile(created.id);
          // Upload the file separately after the employee is created.
          // We need the employee's ID first (to name the file "employee_5_appointment.pdf").
        } else {
          this.router.navigate(["/employee/list"], {
            state: { successMessage: "Employee created successfully." },
          });
          // No file to upload → go straight to the list page.
          // state: { successMessage: '...' } passes data through router navigation.
          // EmployeeListComponent.ngOnInit() reads this and shows the green banner.
        }

        this.cdr.markForCheck();
        // Tell Angular to re-render this component (update success/error messages).
      },

      error: () => {
        this.errorMessage =
          "Failed to create employee. Employee code may already exist.";
        // Common reason: duplicate employee code → backend returns HTTP 400.
        this.cdr.markForCheck();
      },
    });
  }

  private uploadFile(employeeId: number): void {
    // "private" = only called from within this class (from onSubmit).

    this.employeeService
      .uploadAppointmentOrder(employeeId, this.selectedFile!)
      .subscribe({
        // this.selectedFile! = non-null assertion operator.
        // "!" tells TypeScript: "I know selectedFile is not null here." (we checked above)

        next: () => {
          // File uploaded successfully → navigate to list.
          this.router.navigate(["/employee/list"], {
            state: { successMessage: "Employee created successfully." },
          });
        },

        error: () => {
          // Employee was saved but file upload failed.
          // Don't delete the employee — just show error and let user try re-uploading.
          this.errorMessage = "Employee saved, but file upload failed.";
          this.cdr.markForCheck();
        },
      });
  }

  goToList(): void {
    this.router.navigate(["/employee/list"]);
    // Called by the "Cancel" button. Navigate back without saving.
  }

  get f() {
    return this.employeeForm.controls;
    // Shorthand getter for accessing form controls in the template.
    // Instead of writing: employeeForm.controls['employeeCode'].touched
    // Template writes: f['employeeCode'].touched
    // "get" makes it a computed property — auto-updates when form changes.
  }
}
```

---

### FILE: employee-create/employee-create.html

**Purpose:** The Create Employee form template using Angular's modern @if / @for control flow syntax.

```html
<div class="form-container">
  <h2>Create Employee</h2>

  @if (successMessage) {
  <p class="success-message">{{ successMessage }}</p>
  <!-- @if is Angular 17+'s new control flow syntax (replaces *ngIf).
         Shows this paragraph only when successMessage is a non-empty string (truthy).
         {{ successMessage }} interpolates the string value. -->
  } @if (errorMessage) {
  <p class="error-message">{{ errorMessage }}</p>
  <!-- Same pattern: only shows if errorMessage has content. -->
  }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <!-- [formGroup]="employeeForm" : binds this <form> element to the FormGroup in the component.
       Angular tracks validity, dirty state, and values for all child controls.
       (ngSubmit)="onSubmit()" : when form is submitted (button click or Enter key),
       calls onSubmit() in the TypeScript class. Angular prevents the default browser
       form submission (which would reload the page). -->

    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      <!-- formControlName="employeeCode" : connects this input to the 'employeeCode'
           FormControl inside employeeForm. Angular binds their values bidirectionally.
           When user types → FormControl value updates.
           When control is patched programmatically → input value updates. -->

      @if (f['employeeCode'].touched && f['employeeCode'].invalid) {
      <small class="error">Employee Code is required.</small>
      <!-- Shows validation error ONLY when:
             1. touched = user has focused+blurred this input (or markAllAsTouched() was called)
             2. invalid = the control currently fails validation (empty = required fails)
             This prevents showing errors before the user has interacted with the field. -->
      }
    </div>

    <div class="form-group">
      <label>Employee Name English</label>
      <input type="text" formControlName="employeeNameEnglish" />
      @if (f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid)
      {
      <small class="error">Employee Name English is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Employee Name Tamil</label>
      <input type="text" formControlName="employeeNameTamil" />
      <!-- User can type Tamil characters directly using an OS Tamil keyboard or input method. -->
      @if (f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid) {
      <small class="error">Employee Name Tamil is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <!-- <select> connected to the 'designation' FormControl.
           When user picks an option, the FormControl value = the [value] of that option. -->

        <option value="">-- Select Designation --</option>
        <!-- Empty option with value="" — makes the control invalid (required validator)
             until user picks a real designation. -->

        @for (designation of designations; track designation) {
        <option [value]="designation">{{ designation }}</option>
        <!-- @for is Angular 17+'s new loop syntax (replaces *ngFor).
               Loops over the designations array in the TypeScript class.
               "track designation" = optimization hint: use designation string as unique key.
               [value]="designation" : binds the option's value attribute to the loop variable.
               {{ designation }} : displays the text. Both are the same string here. -->
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
      <!-- type="date" shows a date picker widget in modern browsers.
           Value is stored as "YYYY-MM-DD" string which Spring parses to LocalDate. -->
      @if (f['dateOfJoining'].touched && f['dateOfJoining'].invalid) {
      <small class="error">Date of Joining is required.</small>
      }
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      <!-- maxlength="10" : HTML attribute that prevents typing more than 10 characters.
           This is a UI hint — the real validation is Validators.pattern('^[0-9]{10}$'). -->

      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['required'])
      {
      <small class="error">Mobile Number is required.</small>
      <!-- Shows ONLY when the 'required' validator fails (field is empty).
             errors?.['required'] = optional chaining: errors may be null when no errors. -->
      } @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern'])
      {
      <small class="error">Mobile Number must be exactly 10 digits.</small>
      <!-- Shows ONLY when the 'pattern' validator fails (not exactly 10 digits).
             Separate messages for required vs pattern give the user precise guidance. -->
      }
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      <!-- type="email" enables browser's built-in email validation UI hints,
           but Angular's Validators.email does the actual validation. -->
      @if (f['email'].touched && f['email'].errors?.['required']) {
      <small class="error">Email is required.</small>
      } @if (f['email'].touched && f['email'].errors?.['email']) {
      <small class="error">Enter a valid email address.</small>
      <!-- Separate message for invalid format vs missing. -->
      }
    </div>

    <div class="form-group">
      <label>Remarks</label>
      <textarea formControlName="remarks" rows="4"></textarea>
      <!-- Optional field — no validation error needed. rows="4" = 4 visible rows tall. -->
    </div>

    <div class="form-group">
      <label>Appointment Order (PDF only)</label>
      <input
        type="file"
        accept="application/pdf"
        (change)="onFileChange($event)"
      />
      <!-- type="file" : shows a file picker button in the browser.
           accept="application/pdf" : hints to OS file dialog to filter for PDFs.
           (The user can still bypass this filter — that's why we validate in onFileChange)
           (change)="onFileChange($event)" : fires when user selects a file.
           $event = the DOM Event object containing the selected file(s). -->
    </div>

    <div class="button-group">
      <button type="submit">Save Employee</button>
      <!-- type="submit" triggers the form's (ngSubmit) event → calls onSubmit(). -->

      <button type="button" class="cancel-btn" (click)="goToList()">
        Cancel
      </button>
      <!-- type="button" prevents this button from submitting the form.
           (click)="goToList()" navigates back to the employee list. -->
    </div>
  </form>
</div>
```

---

### FILE: employee-edit/employee-edit.ts

**Purpose:** Loads an existing employee by ID, pre-fills the form, and handles the update + optional file replacement. Very similar to employee-create but adds a load-first step.

```typescript
import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
// ActivatedRoute: gives access to the current route's URL parameters.
// We use it to read :id from /employee/edit/:id

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
  employeeId!: number;
  // The employee's database ID, read from the URL parameter.

  selectedFile: File | null = null;
  currentAppointmentOrderFileName: string = "";
  // Shows the existing appointment order filename in the form:
  // "Current Appointment Order: my_old_file.pdf"
  // Set when the employee data is loaded from the backend.

  successMessage: string = "";
  errorMessage: string = "";

  designations: string[] = [
    "Junior Assistant",
    "Senior Assistant",
    "Developer",
    "Senior Developer",
    "Manager",
    "Administrator",
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute,
    // ActivatedRoute is injected — gives us access to URL params for the current route.
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.employeeForm = this.fb.group({
      employeeCode: ["", Validators.required],
      employeeNameEnglish: ["", Validators.required],
      employeeNameTamil: ["", Validators.required],
      designation: ["", Validators.required],
      department: ["", Validators.required],
      dateOfJoining: ["", Validators.required],
      mobileNumber: [
        "",
        [Validators.required, Validators.pattern("^[0-9]{10}$")],
      ],
      email: ["", [Validators.required, Validators.email]],
      remarks: [""],
    });
    // Same form structure as Create. Starts with empty values, then patchValue() fills them.

    const id = this.route.snapshot.paramMap.get("id");
    // this.route.snapshot: a snapshot of the current route at this moment.
    // .paramMap: a map of all URL parameters.
    // .get('id'): gets the value of the :id parameter from the URL.
    // e.g., URL /employee/edit/5 → id = "5" (always a string from the URL).

    if (id) {
      this.employeeId = Number(id);
      // Convert string "5" to number 5. Required because our service methods expect number.
      this.loadEmployee(this.employeeId);
    } else {
      this.errorMessage = "Employee ID not found in URL.";
      // Should never happen if routing is set up correctly, but defensive check.
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        const employee = response?.data ? response.data : response;
        // Unwrap from ApiResponse wrapper if present.

        this.employeeForm.patchValue({
          // patchValue() fills in SOME or ALL form controls.
          // Unlike setValue() which requires ALL controls to be provided,
          // patchValue() ignores missing keys — safer for partial updates.
          employeeCode: employee.employeeCode || "",
          employeeNameEnglish: employee.employeeNameEnglish || "",
          employeeNameTamil: employee.employeeNameTamil || "",
          designation: employee.designation || "",
          department: employee.department || "",
          dateOfJoining: employee.dateOfJoining || "",
          // dateOfJoining comes as "2024-01-15" string from JSON,
          // which matches the "YYYY-MM-DD" format that <input type="date"> expects.
          mobileNumber: employee.mobileNumber || "",
          email: employee.email || "",
          remarks: employee.remarks || "",
          // || '' = use empty string if the field is null/undefined from the API.
        });

        this.currentAppointmentOrderFileName =
          employee.appointmentOrderFileName || "";
        // Show the existing file name in the UI if one exists.

        this.employeeForm.updateValueAndValidity();
        // Forces Angular to re-run all validators after patchValue.
        // Ensures the form's valid/invalid state reflects the loaded data correctly.

        this.errorMessage = "";
        this.cdr.detectChanges();
        // detectChanges() immediately runs change detection for this component and its children.
        // Used here (instead of markForCheck) to ensure the form visually updates right away.
      },
      error: () => {
        this.errorMessage = "Failed to load employee details.";
        this.cdr.detectChanges();
      },
    });
  }

  onFileChange(event: any): void {
    // Same logic as EmployeeCreateComponent.onFileChange().
    // Validates file type and size before storing in selectedFile.
    const file = event.target.files[0];
    if (!file) return;
    if (file.type !== "application/pdf") {
      this.errorMessage = "Only PDF files are allowed.";
      this.selectedFile = null;
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = "File size must be less than or equal to 2 MB.";
      this.selectedFile = null;
      return;
    }
    this.errorMessage = "";
    this.selectedFile = file;
  }

  onSubmit(): void {
    this.successMessage = "";
    this.errorMessage = "";

    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    const employeeData: Employee = this.employeeForm.value;

    this.employeeService
      .updateEmployee(this.employeeId, employeeData)
      .subscribe({
        // PUT /api/employees/update/{id} with the updated form data.
        next: (response: any) => {
          const updated = response?.data ? response.data : response;

          if (this.selectedFile && updated?.id) {
            this.uploadFile(updated.id);
            // If new file was selected → upload it (replaces the old one on server disk).
          } else {
            this.router.navigate(["/employee/list"], {
              state: { successMessage: "Employee updated successfully." },
            });
            // No new file → go directly to list.
          }
          this.cdr.markForCheck();
        },
        error: () => {
          this.errorMessage = "Failed to update employee.";
          this.cdr.markForCheck();
        },
      });
  }

  private uploadFile(employeeId: number): void {
    this.employeeService
      .uploadAppointmentOrder(employeeId, this.selectedFile!)
      .subscribe({
        next: () => {
          this.router.navigate(["/employee/list"], {
            state: { successMessage: "Employee updated successfully." },
          });
        },
        error: () => {
          this.errorMessage = "Employee saved, but file upload failed.";
          this.cdr.markForCheck();
        },
      });
  }

  goToList(): void {
    this.router.navigate(["/employee/list"]);
  }

  get f() {
    return this.employeeForm.controls;
  }
}
```

---

### FILE: employee-edit/employee-edit.html

```html
<div class="form-container">
  <h2>Edit Employee</h2>
  <!-- Title clearly says "Edit" so user knows they're updating existing data. -->

  @if (successMessage) {
  <p class="success-message">{{ successMessage }}</p>
  } @if (errorMessage) {
  <p class="error-message">{{ errorMessage }}</p>
  }

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <!-- All fields same as Create form — pre-filled via patchValue() in loadEmployee() -->
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
      @if (f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid)
      {
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
      @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['required'])
      {
      <small class="error">Mobile Number is required.</small>
      } @if (f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern'])
      {
      <small class="error">Mobile Number must be exactly 10 digits.</small>
      }
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      @if (f['email'].touched && f['email'].errors?.['required']) {
      <small class="error">Email is required.</small>
      } @if (f['email'].touched && f['email'].errors?.['email']) {
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
      <!-- Shows the filename of the existing uploaded PDF.
           || 'No file uploaded' = fallback text if no file was previously uploaded.
           This is informational — user can see what file exists before deciding to replace it. -->
    </div>

    <div class="form-group">
      <label>Replace Appointment Order (PDF only)</label>
      <input
        type="file"
        accept="application/pdf"
        (change)="onFileChange($event)"
      />
      <!-- Selecting a file here REPLACES the existing one on submit.
           If user leaves this empty → existing file is kept unchanged. -->
      <small
        >Select a new PDF only if you want to replace the existing file.</small
      >
      <!-- Helper text so user understands this is OPTIONAL. -->
    </div>

    <div class="button-group">
      <button type="submit">Update Employee</button>
      <!-- type="submit" triggers (ngSubmit) → onSubmit() -->
      <button type="button" class="cancel-btn" (click)="goToList()">
        Cancel
      </button>
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
import { ActivatedRoute, RouterLink } from "@angular/router";
// RouterLink: for the "Back to List" link in the template (<a routerLink="/employee/list">).
// No Router import needed — we use RouterLink directive in template instead of programmatic nav.

import { EmployeeService, Employee } from "../../services/employee";

@Component({
  selector: "app-employee-view",
  standalone: true,
  imports: [CommonModule, RouterLink],
  // RouterLink imported here so <a routerLink="..."> works in the template.
  templateUrl: "./employee-view.html",
  styleUrl: "./employee-view.scss",
})
export class EmployeeViewComponent implements OnInit {
  employee: Employee | null = null;
  // null = employee not loaded yet. Employee = loaded successfully.
  // Template checks *ngIf="employee" before rendering the details card.

  errorMessage: string = "";

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id");
    // Read :id from URL /employee/view/:id

    if (id) {
      this.loadEmployee(Number(id));
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
          this.employee = response.data;
          // Unwrap from ApiResponse: { message: '...', data: { id: 1, ... } }
        } else {
          this.employee = response;
          // Fallback if response is the employee object directly.
        }

        this.errorMessage = "";
        this.cdr.detectChanges();
        // detectChanges() triggers immediate re-render to show the loaded data.
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
  <!-- Shows error if employee failed to load (API error or invalid ID in URL). -->

  <div *ngIf="employee" class="details-card">
    <!-- *ngIf="employee": only renders this div when employee is not null.
       Until the API call completes, employee is null → nothing shows.
       After successful load, employee is set → this div appears. -->

    <p><strong>Employee Code:</strong> {{ employee?.employeeCode }}</p>
    <!-- employee? = optional chaining: safe even if employee becomes null.
         <strong> makes the label bold. The value is interpolated. -->
    <p>
      <strong>Employee Name English:</strong> {{ employee?.employeeNameEnglish
      }}
    </p>
    <p>
      <strong>Employee Name Tamil:</strong> {{ employee?.employeeNameTamil }}
    </p>
    <!-- Tamil Unicode text displays correctly in browsers without any special fonts.
         Modern browsers include built-in Unicode support. -->
    <p><strong>Designation:</strong> {{ employee?.designation }}</p>
    <p><strong>Department:</strong> {{ employee?.department }}</p>
    <p><strong>Date of Joining:</strong> {{ employee?.dateOfJoining }}</p>
    <!-- Displays as "2024-01-15" (ISO format from JSON). Could use Angular's DatePipe
         to format it: {{ employee?.dateOfJoining | date:'dd/MM/yyyy' }} -->
    <p><strong>Mobile Number:</strong> {{ employee?.mobileNumber }}</p>
    <p><strong>Email:</strong> {{ employee?.email }}</p>
    <p><strong>Remarks:</strong> {{ employee?.remarks || 'No remarks' }}</p>
    <!-- || 'No remarks' = if remarks is null/empty, show fallback text. -->

    <p>
      <strong>Uploaded File Name:</strong>
      {{ employee?.appointmentOrderFileName || 'No file uploaded' }}
      <!-- Shows the original filename of the uploaded appointment order PDF.
           Or "No file uploaded" if no PDF has been attached yet. -->
    </p>

    <div class="button-group">
      <a routerLink="/employee/list">Back to List</a>
      <!-- routerLink is the Angular directive equivalent of href for SPA navigation.
           Clicking this navigates to /employee/list WITHOUT a full page reload.
           RouterLink imported in the component's imports array. -->
    </div>
  </div>
</div>
```

---

### FILE: employee-report/employee-report.ts

**Purpose:** Shows employee details in a "report card" layout and lets the user download a PDF report.

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
  downloading: boolean = false;
  // Controls the "Download PDF" button state:
  // false → button shows "Download PDF Report" and is enabled
  // true → button shows "Downloading..." and is disabled (prevents double-clicks)

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get("id");
    // Read :id from URL /employee/report/:id

    if (id) {
      this.loadEmployee(Number(id));
    } else {
      this.errorMessage = "Employee ID not found in URL.";
      this.cdr.detectChanges();
    }
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      // First: load employee details to display on screen.
      next: (response: any) => {
        if (response && response.data) {
          this.employee = response.data;
        } else {
          this.employee = response;
        }
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
    if (!this.employee?.id) return;
    // Guard: can't download if employee isn't loaded or has no ID.

    this.downloading = true;
    // Disable the button immediately to prevent double-clicks.
    // Angular re-renders: button text → "Downloading..." and disabled=true.

    this.employeeService.downloadEmployeeReport(this.employee.id).subscribe({
      // Observable<Blob>: fires GET /api/employees/report/{id} with responseType:'blob'

      next: (blob: Blob) => {
        // blob = the PDF binary data received from the server.

        const url = URL.createObjectURL(blob);
        // Creates a temporary browser-local URL pointing to the blob in memory.
        // Format: "blob:http://localhost:4200/some-uuid"
        // This URL only works in the current browser tab.

        const a = document.createElement("a");
        // Dynamically create an <a> anchor element (not added to DOM, just in memory).

        a.href = url;
        // Set the href to the blob URL.

        a.download = `employee-report-${this.employee!.id}.pdf`;
        // The "download" attribute tells the browser to DOWNLOAD the linked file
        // instead of navigating to it. The value becomes the suggested filename.
        // Result: "employee-report-5.pdf" file in the user's Downloads folder.

        a.click();
        // Programmatically click the anchor → browser initiates the file download.
        // No user interaction needed — the download starts automatically.

        URL.revokeObjectURL(url);
        // IMPORTANT: revoke the object URL to free memory.
        // Without this, the blob stays in memory until the tab closes.
        // After revocation, the blob: URL no longer works (but we don't need it anymore).

        this.downloading = false;
        // Re-enable the download button.
        this.cdr.detectChanges();
      },

      error: () => {
        this.errorMessage = "Failed to download PDF report.";
        this.downloading = false;
        // Re-enable button so user can try again.
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
  <!-- Tamil subtitle displayed directly in HTML — browsers render Tamil Unicode natively. -->

  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>

  <div *ngIf="employee" class="report-card">
    <!-- Only renders when employee data has loaded from the API. -->

    <div class="report-row">
      <span class="label">Employee Code</span>
      <span class="value">{{ employee.employeeCode }}</span>
      <!-- report-row layout: label on left, value on right.
           CSS flex layout makes this display as a clean key-value table. -->
    </div>
    <div class="report-row">
      <span class="label">Employee Name (English)</span>
      <span class="value">{{ employee.employeeNameEnglish }}</span>
    </div>
    <div class="report-row">
      <span class="label">Employee Name (Tamil)</span>
      <span class="value">{{ employee.employeeNameTamil }}</span>
      <!-- Tamil Unicode renders correctly in all modern browsers without special fonts. -->
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
        <!-- (click)="downloadReport()" : calls downloadReport() on click.
           [disabled]="downloading" : Property binding — disables button when downloading=true.
           [ ] around disabled means Angular evaluates the expression (not a plain string).
           When downloading=true → button gets disabled HTML attribute.
           When downloading=false → disabled attribute is removed. -->

        {{ downloading ? 'Downloading...' : 'Download PDF Report' }}
        <!-- Ternary expression in interpolation:
             If downloading is true → show "Downloading..."
             If downloading is false → show "Download PDF Report"
             Angular re-evaluates this on every change detection cycle. -->
      </button>

      <a routerLink="/employee/list">Back to List</a>
      <!-- Navigation link back to the employee list. -->
    </div>
  </div>
</div>
```

---

### FILE: employee-form/employee-form.ts + employee-form.html

**Purpose:** A shared reusable form component that works for both CREATE and EDIT modes. Detects mode by checking if :id is in the URL.

```typescript
// This component is a generic form that:
// - If :id is NOT in URL → operates in CREATE mode (calls createEmployee API)
// - If :id IS in URL → operates in EDIT mode (loads data first, then calls updateEmployee API)
// It's an alternative approach to having separate EmployeeCreateComponent and EmployeeEditComponent.
// Both approaches are valid; this project implements BOTH (the separate components are actually used in routing).

import { Component, OnInit, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
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
  isEditMode: boolean = false;
  // Determines which API to call (create vs update) and what title/button text to show.
  // Set to true if :id parameter is present in the URL.

  employeeId!: number;
  selectedFile: File | null = null;
  currentAppointmentOrderFileName: string = "";
  successMessage: string = "";
  errorMessage: string = "";

  designations: string[] = [
    "Junior Assistant",
    "Senior Assistant",
    "Developer",
    "Senior Developer",
    "Manager",
    "Administrator",
  ];

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    // Separated into its own method for clarity.

    const id = this.route.snapshot.paramMap.get("id");
    if (id) {
      this.isEditMode = true;
      // :id present in URL → we're editing an existing employee.
      this.employeeId = Number(id);
      this.loadEmployee(this.employeeId);
      // Load existing data to pre-fill the form.
    }
    // If no :id → isEditMode stays false → create mode.
  }

  initializeForm(): void {
    this.employeeForm = this.fb.group({
      employeeCode: ["", Validators.required],
      employeeNameEnglish: ["", Validators.required],
      employeeNameTamil: ["", Validators.required],
      designation: ["", Validators.required],
      department: ["", Validators.required],
      dateOfJoining: ["", Validators.required],
      mobileNumber: [
        "",
        [Validators.required, Validators.pattern("^[0-9]{10}$")],
      ],
      email: ["", [Validators.required, Validators.email]],
      remarks: [""],
    });
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployeeById(id).subscribe({
      next: (response: any) => {
        const employee = response?.data ? response.data : response;
        this.employeeForm.patchValue({
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
        this.currentAppointmentOrderFileName =
          employee.appointmentOrderFileName || "";
        this.employeeForm.updateValueAndValidity();
        this.errorMessage = "";
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = "Failed to load employee details.";
        this.cdr.detectChanges();
      },
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type !== "application/pdf") {
        this.errorMessage = "Only PDF files are allowed.";
        this.selectedFile = null;
        return;
      }
      if (file.size > 2 * 1024 * 1024) {
        this.errorMessage = "File size must be less than or equal to 2 MB.";
        this.selectedFile = null;
        return;
      }
      this.errorMessage = "";
      this.selectedFile = file;
    }
  }

  onSubmit(): void {
    this.successMessage = "";
    this.errorMessage = "";
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    const employeeData: Employee = this.employeeForm.value;

    if (this.isEditMode) {
      // UPDATE flow:
      this.employeeService
        .updateEmployee(this.employeeId, employeeData)
        .subscribe({
          next: (response: any) => {
            const updatedEmployee = response?.data ? response.data : response;
            this.successMessage = "Employee updated successfully.";
            if (this.selectedFile && updatedEmployee?.id) {
              this.uploadFile(updatedEmployee.id);
            } else {
              this.router.navigate(["/employee/list"], {
                state: { successMessage: "Employee updated successfully." },
              });
            }
            this.cdr.markForCheck();
          },
          error: () => {
            this.errorMessage = "Failed to update employee.";
            this.cdr.markForCheck();
          },
        });
    } else {
      // CREATE flow:
      this.employeeService.createEmployee(employeeData).subscribe({
        next: (response: any) => {
          const createdEmployee = response?.data ? response.data : response;
          this.successMessage = "Employee created successfully.";
          if (this.selectedFile && createdEmployee?.id) {
            this.uploadFile(createdEmployee.id);
          } else {
            this.router.navigate(["/employee/list"], {
              state: { successMessage: "Employee created successfully." },
            });
          }
          this.cdr.markForCheck();
        },
        error: () => {
          this.errorMessage =
            "Failed to create employee. Employee code may already exist.";
          this.cdr.markForCheck();
        },
      });
    }
  }

  uploadFile(employeeId: number): void {
    if (!this.selectedFile) {
      this.router.navigate(["/employee/list"], {
        state: {
          successMessage: this.isEditMode
            ? "Employee updated successfully."
            : "Employee created successfully.",
        },
      });
      return;
    }
    this.employeeService
      .uploadAppointmentOrder(employeeId, this.selectedFile)
      .subscribe({
        next: () => {
          this.router.navigate(["/employee/list"], {
            state: {
              successMessage: this.isEditMode
                ? "Employee updated successfully."
                : "Employee created successfully.",
            },
          });
        },
        error: () => {
          this.errorMessage = "Employee saved, but file upload failed.";
          this.cdr.markForCheck();
        },
      });
  }

  get f() {
    return this.employeeForm.controls;
  }
}
```

---

### FILE: employee-form/employee-form.html

```html
<div class="form-container">
  <h2>{{ isEditMode ? 'Edit Employee' : 'Create Employee' }}</h2>
  <!-- Ternary in interpolation: title changes based on isEditMode flag.
       If isEditMode=true → "Edit Employee". If false → "Create Employee". -->

  <p class="success-message" *ngIf="successMessage">{{ successMessage }}</p>
  <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>
  <!-- Old *ngIf syntax (vs @if in employee-create) — both are valid in Angular 17+.
       *ngIf is the structural directive approach (requires CommonModule).
       @if is the newer block-based control flow. -->

  <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
    <!-- Fields are same as Create/Edit components — the key difference is
         this single component handles BOTH modes using isEditMode flag. -->

    <div class="form-group">
      <label>Employee Code</label>
      <input type="text" formControlName="employeeCode" />
      <small
        class="error"
        *ngIf="f['employeeCode'].touched && f['employeeCode'].invalid"
      >
        Employee Code is required.
      </small>
    </div>

    <div class="form-group">
      <label>Employee Name English</label>
      <input type="text" formControlName="employeeNameEnglish" />
      <small
        class="error"
        *ngIf="f['employeeNameEnglish'].touched && f['employeeNameEnglish'].invalid"
      >
        Employee Name English is required.
      </small>
    </div>

    <div class="form-group">
      <label>Employee Name Tamil</label>
      <input type="text" formControlName="employeeNameTamil" />
      <small
        class="error"
        *ngIf="f['employeeNameTamil'].touched && f['employeeNameTamil'].invalid"
      >
        Employee Name Tamil is required.
      </small>
    </div>

    <div class="form-group">
      <label>Designation</label>
      <select formControlName="designation">
        <option value="">-- Select Designation --</option>
        <option *ngFor="let designation of designations" [value]="designation">
          {{ designation }}
          <!-- *ngFor (old syntax) loops over designations array. Same result as @for. -->
        </option>
      </select>
      <small
        class="error"
        *ngIf="f['designation'].touched && f['designation'].invalid"
      >
        Designation is required.
      </small>
    </div>

    <div class="form-group">
      <label>Department</label>
      <input type="text" formControlName="department" />
      <small
        class="error"
        *ngIf="f['department'].touched && f['department'].invalid"
      >
        Department is required.
      </small>
    </div>

    <div class="form-group">
      <label>Date of Joining</label>
      <input type="date" formControlName="dateOfJoining" />
      <small
        class="error"
        *ngIf="f['dateOfJoining'].touched && f['dateOfJoining'].invalid"
      >
        Date of Joining is required.
      </small>
    </div>

    <div class="form-group">
      <label>Mobile Number</label>
      <input type="text" formControlName="mobileNumber" maxlength="10" />
      <small
        class="error"
        *ngIf="f['mobileNumber'].touched && f['mobileNumber'].errors?.['required']"
      >
        Mobile Number is required.
      </small>
      <small
        class="error"
        *ngIf="f['mobileNumber'].touched && f['mobileNumber'].errors?.['pattern']"
      >
        Mobile Number must be exactly 10 digits.
      </small>
    </div>

    <div class="form-group">
      <label>Email</label>
      <input type="email" formControlName="email" />
      <small
        class="error"
        *ngIf="f['email'].touched && f['email'].errors?.['required']"
      >
        Email is required.
      </small>
      <small
        class="error"
        *ngIf="f['email'].touched && f['email'].errors?.['email']"
      >
        Enter a valid email address.
      </small>
    </div>

    <div class="form-group">
      <label>Remarks</label>
      <textarea formControlName="remarks" rows="4"></textarea>
    </div>

    <div class="form-group" *ngIf="isEditMode">
      <!-- This block only shows in EDIT mode — not needed when creating a new employee. -->
      <label>Current Appointment Order</label>
      <p>{{ currentAppointmentOrderFileName || 'No file uploaded' }}</p>
    </div>

    <div class="form-group">
      <label>Appointment Order (PDF only)</label>
      <input
        type="file"
        accept="application/pdf"
        (change)="onFileChange($event)"
      />
      <small *ngIf="isEditMode">
        Select a new PDF only if you want to replace the existing file.
      </small>
      <!-- Only shows the "replacement" hint in edit mode. -->
    </div>

    <div class="button-group">
      <button type="submit">
        {{ isEditMode ? 'Update Employee' : 'Save Employee' }}
        <!-- Button label changes based on mode:
             Create mode → "Save Employee"
             Edit mode → "Update Employee" -->
      </button>
    </div>
  </form>
</div>
```

---

## SUMMARY: KEY CONCEPTS FOR INTERVIEW

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

| Concept                                | Where Used             | What It Does                                |
| -------------------------------------- | ---------------------- | ------------------------------------------- |
| `@SpringBootApplication`               | Main class             | Enables component scan + auto-configuration |
| `@Entity` + `@Table`                   | Employee               | Maps Java class to PostgreSQL table         |
| `@Id` + `@GeneratedValue`              | Employee               | Auto-increment primary key                  |
| `@Column`                              | Employee               | Maps fields to columns with constraints     |
| `JpaRepository`                        | EmployeeRepository     | Free CRUD methods via Spring Data           |
| Method Name Queries                    | EmployeeRepository     | SQL generated from method name              |
| `@Service` + `@Transactional`          | ServiceImpl            | Business logic + DB transaction management  |
| `@RestController`                      | EmployeeController     | REST API handler with JSON responses        |
| `@RequestMapping` + `@GetMapping` etc. | Controller             | Maps URLs to methods                        |
| `@CrossOrigin`                         | Controller             | Enables Angular-to-Spring CORS              |
| `@Valid` + `@RequestBody`              | Controller             | Validates incoming JSON data                |
| `@PathVariable`                        | Controller             | Reads URL path parameters                   |
| `@RequestParam`                        | Controller             | Reads query/form parameters (file upload)   |
| `ResponseEntity`                       | Controller             | Full HTTP response control                  |
| `@ExceptionHandler`                    | GlobalExceptionHandler | Catches specific exceptions                 |
| `@RestControllerAdvice`                | GlobalExceptionHandler | Global error handling for all controllers   |
| `@NotBlank` / `@Email` / `@Size`       | RequestDTO             | Bean Validation annotations                 |
| `@Value`                               | FileStorageService     | Injects application.properties values       |
| `ClassPathResource`                                  | ReportService          | Loads TTF font files from JAR classpath                         |
| `ByteArrayOutputStream`                              | ReportService          | In-memory PDF generation (no temp file on disk)                 |
| `MultipartFile`                                      | FileStorageService     | Represents uploaded file in HTTP request                        |
| Apache PDFBox (`PDDocument`, `PDPageContentStream`)  | ReportService          | PDF generation; English text via PDType1Font                    |
| AWT `TextLayout` + `PathIterator`                    | ReportService          | Tamil text shaped by JVM → exported as vector glyph paths in PDF |

---

_End of README_CODE_EXPLANATION.md_
_This document covers all 17 backend files and all 12 frontend files with complete line-by-line explanations and end-to-end flow traces._
