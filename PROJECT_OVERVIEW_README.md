# Employee Service Record Management — Complete Project Overview

This document is a full reconstruction guide for the Employee Service Record Management system. Every file, field, method, route, style, and logic decision is described here in enough detail that an AI assistant (Claude Code, etc.) can rebuild the entire project from scratch by reading this file alone.

---

## 1. Project Purpose

The Employee Service Record Management system is a full-stack web application built for managing employee records in an organization. It supports storing employee details in both English and Tamil (bilingual), uploading PDF appointment order documents per employee, and generating formatted PDF service record reports that render Tamil text correctly using TrueType fonts.

The system is designed for government or semi-government departments where employees have official appointment orders and service records that need to be maintained digitally.

---

## 2. Technology Stack

| Layer | Technology | Version | Port |
|---|---|---|---|
| Frontend | Angular (Standalone Components) | 21.0.0 | 4200 |
| Backend | Spring Boot | 3.5.15 | 8080 |
| Language (Backend) | Java | 17 | — |
| Database | PostgreSQL | Latest | 5432 |
| Build Tool (Backend) | Apache Maven | Latest | — |
| Package Manager (Frontend) | npm | 11.17.0 | — |
| PDF Generation | Apache PDFBox | 2.0.27 | — |
| ORM | Spring Data JPA / Hibernate | — | — |
| Change Detection | Angular Zoneless (provideZonelessChangeDetection) | — | — |
| HTTP Client | Angular HttpClient with Fetch backend | — | — |
| Styling | SCSS | — | — |
| Testing | Vitest | 4.0.8 | — |
| SSR | Angular SSR with Express 5 | — | — |
| TypeScript | TypeScript | 5.9.0 | — |

---

## 3. Features

### Core CRUD Operations
- Create a new employee with all details
- List all employees in a searchable, sortable table
- View a single employee's full details (read-only)
- Edit/update an existing employee's information
- Delete an employee record

### Bilingual Support
- Every employee has both an English name (employeeNameEnglish) and a Tamil name (employeeNameTamil)
- The PDF report displays titles and field labels in Tamil
- Tamil text is rendered using proper TrueType font files to avoid encoding issues in PDF viewers

### PDF Appointment Order Upload
- Each employee can have one PDF appointment order document uploaded
- Maximum file size: 2 MB
- Only PDF files are accepted (validated on both frontend and backend)
- File is stored on the server disk under an "uploads/" directory
- File path and original filename are stored in the database
- On edit, the user can replace the existing file

### PDF Service Record Report Generation
- Backend generates a formatted A4 PDF using Apache PDFBox
- Report has an English title and a Tamil title
- Contains a 2-column table (label | value) with 9 rows of employee data
- Tamil text is converted to vector paths for universal PDF viewer compatibility
- Frontend downloads the PDF as a binary blob and triggers browser download

### Search and Filter
- Employee list has a real-time search box
- Search filters by employee code and employee name (English)
- Results are sorted alphabetically by employee code

### Form Validation
- Frontend: Angular Reactive Forms with validators (required, pattern, email, size)
- Backend: Jakarta Bean Validation (@NotBlank, @NotNull, @Email, @Size) via @Valid on controller
- Mobile number must be exactly 10 digits (pattern validation)
- Email must be a valid format
- Employee code must be unique across the system

### Error Handling
- Global exception handler on backend covers all error types
- Duplicate employee code returns 400 Bad Request
- Employee not found returns 404 Not Found
- Validation failures return 400 with per-field error messages
- Invalid file returns 400 Bad Request
- Generic exceptions return 500 Internal Server Error
- Frontend shows success/error messages that auto-dismiss after 3 seconds

### Architecture
- Frontend and backend are completely separate projects
- Backend exposes REST API with CORS enabled for all origins
- Frontend communicates exclusively through the EmployeeService using HttpClient
- All API responses are wrapped in a generic ApiResponse<T> wrapper with message and data fields

---

## 4. Project Directory Structure

```
Employee Service Record Management/
├── employee-backend/
│   └── employee-backend/
│       ├── pom.xml
│       └── src/
│           └── main/
│               ├── java/com/employee/backend/
│               │   ├── EmployeeBackendApplication.java
│               │   ├── controller/
│               │   │   └── EmployeeController.java
│               │   ├── dto/
│               │   │   ├── ApiResponse.java
│               │   │   ├── EmployeeRequestDTO.java
│               │   │   └── EmployeeResponseDTO.java
│               │   ├── entity/
│               │   │   └── Employee.java
│               │   ├── exception/
│               │   │   ├── DuplicateEmployeeCodeException.java
│               │   │   ├── EmployeeNotFoundException.java
│               │   │   ├── GlobalExceptionHandler.java
│               │   │   └── InvalidFileException.java
│               │   ├── repository/
│               │   │   └── EmployeeRepository.java
│               │   └── service/
│               │       ├── EmployeeReportService.java
│               │       ├── EmployeeService.java
│               │       ├── FileStorageService.java
│               │       └── impl/
│               │           └── EmployeeServiceImpl.java
│               └── resources/
│                   ├── application.properties
│                   └── fonts/
│                       ├── NotoSansTamil-Regular.ttf
│                       └── LATHA.TTF
│
└── employee-frontend/
    ├── angular.json
    ├── package.json
    ├── tsconfig.json
    ├── tsconfig.app.json
    ├── tsconfig.spec.json
    └── src/
        ├── index.html
        ├── main.ts
        ├── styles.scss
        └── app/
            ├── app.ts
            ├── app.html
            ├── app.scss
            ├── app.config.ts
            ├── app.routes.ts
            ├── services/
            │   └── employee.ts
            └── employee/
                ├── employee-list/
                │   ├── employee-list.ts
                │   ├── employee-list.html
                │   └── employee-list.scss
                ├── employee-create/
                │   ├── employee-create.ts
                │   ├── employee-create.html
                │   └── employee-create.scss
                ├── employee-edit/
                │   ├── employee-edit.ts
                │   ├── employee-edit.html
                │   └── employee-edit.scss
                ├── employee-view/
                │   ├── employee-view.ts
                │   ├── employee-view.html
                │   └── employee-view.scss
                ├── employee-report/
                │   ├── employee-report.ts
                │   ├── employee-report.html
                │   └── employee-report.scss
                └── employee-form/
                    ├── employee-form.ts
                    ├── employee-form.html
                    └── employee-form.scss
```

---

## 5. Database Schema

**Database name:** employee_service_db
**Auto DDL:** spring.jpa.hibernate.ddl-auto=update (Hibernate creates/updates the schema automatically)

### Table: employees

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PRIMARY KEY, auto-increment | Internal unique identifier |
| employee_code | VARCHAR(50) | NOT NULL, UNIQUE | Business identifier (e.g. EMP001) |
| employee_name_english | VARCHAR(200) | NOT NULL | Full name in English |
| employee_name_tamil | VARCHAR(200) | NOT NULL | Full name in Tamil Unicode |
| designation | VARCHAR(100) | NOT NULL | Job title / role |
| department | VARCHAR(100) | NOT NULL | Department name |
| date_of_joining | DATE | NOT NULL | Date employee joined |
| mobile_number | VARCHAR(20) | nullable | 10-digit phone number |
| email | VARCHAR(200) | nullable | Valid email address |
| remarks | VARCHAR(1000) | nullable | Optional free-text notes |
| appointment_order_path | VARCHAR(500) | nullable | Absolute file path on server disk |
| appointment_order_file_name | VARCHAR(255) | nullable | Original uploaded filename |
| created_at | TIMESTAMP | auto-set on create | Record creation timestamp |
| updated_at | TIMESTAMP | auto-set on update | Last modification timestamp |

---

## 6. API Endpoints

**Base URL:** http://localhost:8080/api/employees
**CORS:** All origins allowed (@CrossOrigin on controller)

### 1. Create Employee
- Method: POST
- Path: /api/employees/create
- Request body: JSON — EmployeeRequestDTO (all fields required except remarks)
- Response: ApiResponse wrapping EmployeeResponseDTO
- HTTP Status: 201 Created
- Validation: @Valid applied; returns 400 with field errors if validation fails

### 2. Get All Employees
- Method: GET
- Path: /api/employees/list
- Request: none
- Response: ApiResponse wrapping List of EmployeeResponseDTO
- HTTP Status: 200 OK

### 3. Get Employee by ID
- Method: GET
- Path: /api/employees/{id}
- Path variable: id (Long)
- Response: ApiResponse wrapping EmployeeResponseDTO
- HTTP Status: 200 OK; 404 if not found

### 4. Update Employee
- Method: PUT
- Path: /api/employees/update/{id}
- Path variable: id (Long)
- Request body: JSON — EmployeeRequestDTO
- Response: ApiResponse wrapping updated EmployeeResponseDTO
- HTTP Status: 200 OK
- Validation: @Valid applied; 400 if employee code already used by another record

### 5. Upload Appointment Order PDF
- Method: POST
- Path: /api/employees/upload/{id}
- Path variable: id (Long)
- Request: multipart/form-data with field named "file"
- Validations: file must not be empty, must be PDF (content type application/pdf), max 2MB
- Response: ApiResponse wrapping updated EmployeeResponseDTO (with appointmentOrderFileName set)
- HTTP Status: 200 OK; 400 if file is invalid

### 6. Delete Employee
- Method: DELETE
- Path: /api/employees/delete/{id}
- Path variable: id (Long)
- Response: ApiResponse wrapping a String success message
- HTTP Status: 200 OK; 404 if not found

### 7. Download Employee PDF Report
- Method: GET
- Path: /api/employees/report/{id}
- Path variable: id (Long)
- Response: Binary PDF file (application/pdf)
- HTTP Headers: Content-Disposition: attachment; filename="employee_report_{id}.pdf"
- HTTP Status: 200 OK; 404 if employee not found

---

## 7. Backend Files — Detailed Description

### EmployeeBackendApplication.java
- Package: com.employee.backend
- Annotation: @SpringBootApplication
- Entry point class with main method that calls SpringApplication.run()
- No custom configuration; Spring Boot auto-configures everything

### pom.xml — Maven Dependencies

**Parent:** spring-boot-starter-parent version 3.5.15
**Java version property:** 17

**Dependencies:**
- spring-boot-starter-data-jpa — JPA/Hibernate ORM for PostgreSQL
- spring-boot-starter-validation — Jakarta Bean Validation (@NotBlank, @Email, etc.)
- spring-boot-starter-web — REST API support, embedded Tomcat
- postgresql (runtime scope) — PostgreSQL JDBC driver
- lombok — Reduces boilerplate (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor, @Getter, @Setter)
- pdfbox version 2.0.27 — PDF creation and Tamil font rendering
- spring-boot-starter-test (test scope) — JUnit, Mockito

**Build plugins:**
- spring-boot-maven-plugin
- maven-compiler-plugin with Lombok annotation processor path

### application.properties

- server.port = 8080
- spring.datasource.url = jdbc:postgresql://localhost:5432/employee_service_db
- spring.datasource.username = postgres
- spring.datasource.password = Sakthi@123
- spring.datasource.driver-class-name = org.postgresql.Driver
- spring.jpa.hibernate.ddl-auto = update
- spring.jpa.show-sql = true
- spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
- spring.servlet.multipart.max-file-size = 2MB
- spring.servlet.multipart.max-request-size = 2MB
- file.upload-dir = uploads

### Employee.java (Entity)
- Package: com.employee.backend.entity
- Annotations: @Entity, @Table(name="employees"), @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Lombok @Data generates getters, setters, toString, equals, hashCode automatically

**Fields:**
- id: Long — @Id, @GeneratedValue(strategy=IDENTITY), auto-incremented primary key
- employeeCode: String — @Column(unique=true, nullable=false, length=50)
- employeeNameEnglish: String — @Column(nullable=false, length=200)
- employeeNameTamil: String — @Column(nullable=false, length=200)
- designation: String — @Column(nullable=false, length=100)
- department: String — @Column(nullable=false, length=100)
- dateOfJoining: LocalDate — @Column(nullable=false)
- mobileNumber: String — @Column(length=20)
- email: String — @Column(length=200)
- remarks: String — @Column(length=1000)
- appointmentOrderPath: String — @Column(length=500) — absolute path to uploaded PDF on server
- appointmentOrderFileName: String — @Column(length=255) — original file name shown to users
- createdAt: LocalDateTime — automatically set when record is created
- updatedAt: LocalDateTime — automatically updated on every save

### EmployeeRequestDTO.java
- Package: com.employee.backend.dto
- Annotations: @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Used as the request body for create and update operations

**Fields with validation annotations:**
- employeeCode: String — @NotBlank
- employeeNameEnglish: String — @NotBlank
- employeeNameTamil: String — @NotBlank
- designation: String — @NotBlank
- department: String — @NotBlank
- dateOfJoining: LocalDate — @NotNull
- mobileNumber: String — @Size(min=10, max=10) — exactly 10 digits
- email: String — @Email
- remarks: String — no validation (optional)

### EmployeeResponseDTO.java
- Package: com.employee.backend.dto
- Annotations: @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Returned in all API responses; never exposes file path to client, only the file name

**Fields (mirrors Entity plus extra):**
- id: Long
- employeeCode: String
- employeeNameEnglish: String
- employeeNameTamil: String
- designation: String
- department: String
- dateOfJoining: LocalDate
- mobileNumber: String
- email: String
- remarks: String
- appointmentOrderFileName: String — only the file name, not the server path
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

### ApiResponse.java
- Package: com.employee.backend.dto
- Generic class: ApiResponse<T>
- Annotations: @Data, @NoArgsConstructor, @AllArgsConstructor

**Fields:**
- message: String — human-readable description of the result
- data: T — the actual payload (EmployeeResponseDTO, List, String, etc.)

All controller endpoints wrap their return value in ApiResponse so clients always receive a consistent envelope.

### EmployeeRepository.java
- Package: com.employee.backend.repository
- Annotation: @Repository (implicit from JpaRepository)
- Extends JpaRepository<Employee, Long>
- JpaRepository provides: save, findById, findAll, deleteById, existsById, count, etc.

**Custom query methods (Spring Data JPA derived queries):**
- findByEmployeeCode(String employeeCode): Optional<Employee> — look up by code
- existsByEmployeeCode(String employeeCode): boolean — check uniqueness before insert

### EmployeeService.java (Interface)
- Package: com.employee.backend.service
- Defines the contract for all employee business operations

**Method signatures:**
- createEmployee(EmployeeRequestDTO requestDTO): EmployeeResponseDTO
- getAllEmployees(): List<EmployeeResponseDTO>
- getEmployeeById(Long id): EmployeeResponseDTO
- updateEmployee(Long id, EmployeeRequestDTO requestDTO): EmployeeResponseDTO
- updateAppointmentOrder(Long id, String filePath, String fileName): EmployeeResponseDTO
- deleteEmployee(Long id): void

### EmployeeServiceImpl.java
- Package: com.employee.backend.service.impl
- Annotations: @Service, @Transactional, @RequiredArgsConstructor (Lombok)
- Implements EmployeeService interface
- Injected dependency: EmployeeRepository

**Business logic per method:**

createEmployee:
- Checks if employeeCode already exists via existsByEmployeeCode; throws DuplicateEmployeeCodeException if so
- Maps EmployeeRequestDTO to Employee entity
- Sets createdAt and updatedAt to LocalDateTime.now()
- Saves via repository
- Maps saved entity to EmployeeResponseDTO and returns

getAllEmployees:
- Calls repository.findAll()
- Maps each Employee entity to EmployeeResponseDTO
- Returns list

getEmployeeById:
- Calls repository.findById(id)
- Throws EmployeeNotFoundException if empty
- Maps to EmployeeResponseDTO and returns

updateEmployee:
- Finds existing employee by id (throws EmployeeNotFoundException if not found)
- If employeeCode changed, checks new code isn't used by another record; throws DuplicateEmployeeCodeException if conflict
- Updates all fields from DTO
- Sets updatedAt to LocalDateTime.now()
- Saves and returns EmployeeResponseDTO

updateAppointmentOrder:
- Finds employee by id
- Sets appointmentOrderPath and appointmentOrderFileName
- Sets updatedAt
- Saves and returns EmployeeResponseDTO

deleteEmployee:
- Finds employee by id (throws EmployeeNotFoundException if not found)
- Calls repository.deleteById(id)

### FileStorageService.java
- Package: com.employee.backend.service
- Annotation: @Service
- Reads file.upload-dir property using @Value("${file.upload-dir}")

**Method: saveAppointmentOrderFile(Long employeeId, MultipartFile file): String**
- Creates the upload directory if it does not exist (Files.createDirectories)
- Generates file name: "employee_{employeeId}_appointment.pdf"
- Resolves the full path: uploadDir / fileName
- Copies the file bytes to disk (overwrites if file already exists)
- Returns the absolute path as a String

### EmployeeReportService.java
- Package: com.employee.backend.service
- Annotation: @Service
- Dependency: EmployeeRepository

**Method: generateEmployeeReport(Long id): byte[]**

PDF generation steps using Apache PDFBox 2.0.27:
1. Finds employee by id; throws EmployeeNotFoundException if not found
2. Creates a new PDDocument with a blank PDPage (A4 size)
3. Loads Tamil fonts from classpath resources/fonts/:
   - NotoSansTamil-Regular.ttf (primary Tamil font)
   - LATHA.TTF (fallback Tamil font)
   - Fonts loaded as PDType0Font for Unicode/OpenType support
4. Begins a PDPageContentStream on the A4 page
5. Draws English title "Employee Service Record Report" at top using a standard font
6. Draws Tamil title "பணியாளர் சேவை விவர அறிக்கை" below using Tamil PDType0Font
7. Draws a 2-column table (label column | value column) with 9 rows:
   - Row 1: Employee Code
   - Row 2: Name (English)
   - Row 3: Name (Tamil)
   - Row 4: Designation
   - Row 5: Department
   - Row 6: Date of Joining
   - Row 7: Mobile Number
   - Row 8: Email
   - Row 9: Remarks
8. Tamil text is converted to glyph paths (vector outlines) so it renders correctly in any PDF viewer without needing the font installed
9. Closes the content stream
10. Saves the document to a ByteArrayOutputStream
11. Returns the byte array

Page settings: A4 with 50px margins on all sides.

### Exception Classes

**EmployeeNotFoundException**
- Package: com.employee.backend.exception
- Extends RuntimeException
- Constructor accepts a String message (typically "Employee not found with id: {id}")

**DuplicateEmployeeCodeException**
- Package: com.employee.backend.exception
- Extends RuntimeException
- Constructor accepts a String message (typically "Employee code already exists: {code}")

**InvalidFileException**
- Package: com.employee.backend.exception
- Extends RuntimeException
- Constructor accepts a String message (typically "Invalid file: only PDF allowed" or "File size exceeds 2MB")

### GlobalExceptionHandler.java
- Package: com.employee.backend.exception
- Annotation: @RestControllerAdvice
- Handles exceptions globally across all controllers; returns JSON responses

**Exception handler methods:**

handleEmployeeNotFound(EmployeeNotFoundException ex):
- Returns ResponseEntity with HttpStatus.NOT_FOUND (404)
- Body: ApiResponse with message from exception, data null

handleDuplicateEmployeeCode(DuplicateEmployeeCodeException ex):
- Returns ResponseEntity with HttpStatus.BAD_REQUEST (400)
- Body: ApiResponse with message from exception

handleValidationErrors(MethodArgumentNotValidException ex):
- Returns ResponseEntity with HttpStatus.BAD_REQUEST (400)
- Collects all field errors into a map (fieldName -> error message)
- Body: ApiResponse with message "Validation failed" and data as the error map

handleInvalidFile(InvalidFileException ex):
- Returns ResponseEntity with HttpStatus.BAD_REQUEST (400)
- Body: ApiResponse with exception message

handleGenericException(Exception ex):
- Returns ResponseEntity with HttpStatus.INTERNAL_SERVER_ERROR (500)
- Body: ApiResponse with message "Internal server error"

### EmployeeController.java
- Package: com.employee.backend.controller
- Annotations: @RestController, @RequestMapping("/api/employees"), @CrossOrigin(origins="*"), @RequiredArgsConstructor
- Injected dependencies: EmployeeService, FileStorageService, EmployeeReportService

**Endpoint method details:**

POST /create — createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO)
- Calls employeeService.createEmployee(requestDTO)
- Returns ResponseEntity.status(201).body(new ApiResponse<>("Employee created successfully", result))

GET /list — getAllEmployees()
- Calls employeeService.getAllEmployees()
- Returns ResponseEntity.ok(new ApiResponse<>("Employees retrieved", result))

GET /{id} — getEmployeeById(@PathVariable Long id)
- Calls employeeService.getEmployeeById(id)
- Returns ResponseEntity.ok(new ApiResponse<>("Employee retrieved", result))

PUT /update/{id} — updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO requestDTO)
- Calls employeeService.updateEmployee(id, requestDTO)
- Returns ResponseEntity.ok(new ApiResponse<>("Employee updated successfully", result))

POST /upload/{id} — uploadAppointmentOrder(@PathVariable Long id, @RequestParam("file") MultipartFile file)
- Validates: file not empty, content type is application/pdf, size <= 2MB
- Calls fileStorageService.saveAppointmentOrderFile(id, file)
- Calls employeeService.updateAppointmentOrder(id, savedPath, originalFilename)
- Returns ResponseEntity.ok(new ApiResponse<>("File uploaded successfully", result))

DELETE /delete/{id} — deleteEmployee(@PathVariable Long id)
- Calls employeeService.deleteEmployee(id)
- Returns ResponseEntity.ok(new ApiResponse<>("Employee deleted successfully", "Deleted"))

GET /report/{id} — generateReport(@PathVariable Long id)
- Calls employeeReportService.generateEmployeeReport(id) to get byte[]
- Sets HttpHeaders: Content-Type=application/pdf, Content-Disposition=attachment; filename="employee_report_{id}.pdf"
- Returns ResponseEntity with byte[] body and headers

---

## 8. Frontend Files — Detailed Description

### index.html
- Standard HTML5 boilerplate
- Title tag: "EmployeeFrontend"
- Viewport meta for responsive: width=device-width, initial-scale=1
- Root element: <app-root></app-root>
- Base href: "/"

### main.ts
- Imports bootstrapApplication from @angular/platform-browser
- Imports App component and appConfig
- Calls bootstrapApplication(App, appConfig).catch(console.error)

### styles.scss (global)
- Empty file — all styles are component-level SCSS

### app.config.ts
- Exports appConfig as ApplicationConfig
- Providers array contains:
  - provideZonelessChangeDetection() — enables zoneless Angular with signal-based change detection
  - provideRouter(routes) — registers the application routes
  - provideHttpClient(withFetch()) — uses the Fetch API instead of XMLHttpRequest for HTTP calls

### app.routes.ts
- Exports Routes array named "routes"

**Route definitions:**
1. path: '' — redirectTo: 'employee/list', pathMatch: 'full'
2. path: 'employee/list' — component: EmployeeListComponent
3. path: 'employee/create' — component: EmployeeCreateComponent
4. path: 'employee/edit/:id' — component: EmployeeEditComponent
5. path: 'employee/view/:id' — component: EmployeeViewComponent
6. path: 'employee/report/:id' — component: EmployeeReportComponent

### app.ts (Root App Component)
- Selector: app-root
- Standalone: true
- Imports: RouterOutlet, RouterLink
- Template file: app.html
- Style file: app.scss

**Template structure (app.html):**
- Outer div with class "app-container"
- Header element with class "header" containing:
  - h1 with text "Employee Service Record Management"
  - nav with class "nav-links" containing two RouterLink anchors:
    - Link to /employee/list — text "Employee List"
    - Link to /employee/create — text "Add Employee"
- Main element with class "main-content" containing:
  - <router-outlet> — where routed components are rendered

**Styling (app.scss):**
- .header: background-color #1976d2, color white, padding 20px, border-radius 8px
- .nav-links a: color white, font-weight bold, margin-right 20px, text-decoration none, underline on hover
- .main-content: margin-top 20px
- .app-container: width 90%, margin 0 auto

### employee.ts (EmployeeService)
- Path: src/app/services/employee.ts
- Injectable: providedIn root (singleton)
- Injected: HttpClient

**Employee interface (TypeScript):**
- id: number (optional)
- employeeCode: string
- employeeNameEnglish: string
- employeeNameTamil: string
- designation: string
- department: string
- dateOfJoining: string
- mobileNumber: string
- email: string
- remarks: string
- appointmentOrderFileName: string (optional)

**Base URL constant:** http://localhost:8080/api/employees

**Service methods:**

getAllEmployees(): Observable<any>
- HTTP GET to baseUrl + '/list'

getEmployeeById(id: number): Observable<any>
- HTTP GET to baseUrl + '/' + id

createEmployee(employee: Employee): Observable<Employee>
- HTTP POST to baseUrl + '/create'
- Body: employee object as JSON

updateEmployee(id: number, employee: Employee): Observable<Employee>
- HTTP PUT to baseUrl + '/update/' + id
- Body: employee object as JSON

uploadAppointmentOrder(id: number, file: File): Observable<any>
- Creates FormData object, appends file with key 'file'
- HTTP POST to baseUrl + '/upload/' + id
- Body: FormData (multipart)

deleteEmployee(id: number): Observable<any>
- HTTP DELETE to baseUrl + '/delete/' + id

downloadEmployeeReport(id: number): Observable<Blob>
- HTTP GET to baseUrl + '/report/' + id
- responseType: 'blob' — binary PDF data

### employee-list.ts (EmployeeListComponent)
- Selector: app-employee-list
- Standalone: true
- Imports: CommonModule, FormsModule, RouterLink, RouterModule
- Injected: EmployeeService, Router

**Properties:**
- employees: any[] — full list from backend
- searchTerm: string — bound to search input
- successMessage: string — shown after create/delete
- errorMessage: string — shown on load failure
- showDeleteButton: boolean = false — hides delete button by default (demo safety)

**ngOnInit:**
- Reads navigation state for successMessage (set by create/edit components after save)
- Calls loadEmployees()
- If successMessage exists, clears it after 3 seconds using setTimeout

**Methods:**

loadEmployees():
- Calls employeeService.getAllEmployees()
- Handles both response.data array and direct array response formats
- Sets employees array on success
- Sets errorMessage on error

filteredEmployees (getter):
- Filters employees where employeeCode or employeeNameEnglish includes the searchTerm (case-insensitive)
- Sorts filtered results alphabetically by employeeCode
- Returns sorted, filtered array

goToAddEmployee(): navigates to /employee/create
goToViewEmployee(id): navigates to /employee/view/:id
goToEditEmployee(id): navigates to /employee/edit/:id
goToReport(id): navigates to /employee/report/:id
deleteEmployee(id): shows confirm dialog, calls service, reloads list on success

**Template (employee-list.html):**
- Page container div
- Header row: title "Employee List" on left, "Add Employee" button on right
- Search input ([(ngModel)] bound to searchTerm)
- Success message div (shown when successMessage is set)
- Error message div (shown when errorMessage is set)
- Table with columns: ID, Employee Code, Name (English), Name (Tamil), Designation, Department, Date of Joining, Mobile, Email
- Each row has action buttons: View (green), Edit (orange), Report (purple), Delete (red, conditionally shown)
- No data row shown when filteredEmployees is empty
- Uses Angular @for control flow (Angular 17+ syntax, not *ngFor)

**Styles (employee-list.scss):**
- Container: white background, padding 24px, border-radius 12px, box-shadow 0 2px 8px rgba(0,0,0,0.08)
- Search input: max-width 600px, full width, padding 10px
- Table: full width, border-collapse collapse
- Table header: background #f5f5f5
- Buttons: border-radius 4px, padding 6px 12px, cursor pointer, no border
- Primary button: background #1976d2, color white
- View button: background #4caf50 (green)
- Edit button: background #ff9800 (orange)
- Report button: background #9c27b0 (purple)
- Delete button: background #f44336 (red)
- Success message: background #e8f5e9, color #2e7d32, border 1px solid #a5d6a7
- Error message: background #ffebee, color #c62828, border 1px solid #ef9a9a

### employee-create.ts (EmployeeCreateComponent)
- Selector: app-employee-create
- Standalone: true
- Imports: CommonModule, ReactiveFormsModule, RouterLink
- Injected: FormBuilder, EmployeeService, Router

**Properties:**
- employeeForm: FormGroup
- selectedFile: File | null = null
- successMessage: string = ''
- errorMessage: string = ''
- designations: string[] = ['Junior Assistant', 'Senior Assistant', 'Developer', 'Senior Developer', 'Manager', 'Administrator']

**Form initialization in constructor or ngOnInit:**
- employeeCode: ['', [Validators.required]]
- employeeNameEnglish: ['', [Validators.required]]
- employeeNameTamil: ['', [Validators.required]]
- designation: ['', [Validators.required]]
- department: ['', [Validators.required]]
- dateOfJoining: ['', [Validators.required]]
- mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]]
- email: ['', [Validators.required, Validators.email]]
- remarks: ['']

**Methods:**

get f(): returns employeeForm.controls (shorthand for template)

onFileChange(event):
- Gets file from event.target.files[0]
- Validates: file type must be application/pdf; if not, sets errorMessage and clears selectedFile
- Validates: file size must be <= 2MB (2 * 1024 * 1024 bytes); if over, sets errorMessage
- If valid, sets selectedFile

onSubmit():
- Returns immediately if form is invalid
- Extracts form values
- Calls employeeService.createEmployee(formValues)
- On success: if selectedFile, calls uploadAppointmentOrder; then navigates to /employee/list with successMessage state
- On error: sets errorMessage

goToList(): navigates to /employee/list

**Template (employee-create.html):**
- Form container with title "Create Employee"
- Success and error message divs
- Reactive form with [formGroup]="employeeForm" and (ngSubmit)="onSubmit()"
- Fields: Employee Code, Name (English), Name (Tamil), Designation (dropdown <select>), Department, Date of Joining (date input), Mobile Number, Email, Remarks (textarea)
- File input labeled "Appointment Order (PDF)" with accept=".pdf"
- Error messages under each field using @if syntax
- Buttons: "Save Employee" (type submit) and "Cancel" (calls goToList)

**Styles (employee-create.scss):** Same as employee-list.scss patterns, max-width 700px form

### employee-edit.ts (EmployeeEditComponent)
- Selector: app-employee-edit
- Standalone: true
- Imports: CommonModule, ReactiveFormsModule, RouterLink
- Injected: FormBuilder, EmployeeService, Router, ActivatedRoute

**Properties:**
- employeeForm: FormGroup (same validators as Create)
- employeeId: number
- selectedFile: File | null = null
- currentAppointmentOrderFileName: string = ''
- successMessage: string = ''
- errorMessage: string = ''
- designations: string[] (same list as Create)

**ngOnInit:**
- Initializes form with same validators as Create
- Gets id from route.snapshot.paramMap.get('id')
- Calls loadEmployee(id)

**Methods:**

loadEmployee(id):
- Calls employeeService.getEmployeeById(id)
- Patches form with employee data using patchValue()
- Sets currentAppointmentOrderFileName from response

onFileChange(event): same as Create

onSubmit():
- Validates form
- Calls employeeService.updateEmployee(employeeId, formValues)
- If selectedFile, calls uploadFile(employeeId) after update
- Navigates to /employee/list with successMessage

uploadFile(id):
- Calls employeeService.uploadAppointmentOrder(id, selectedFile)
- Sets errorMessage on failure

goToList(): navigates to /employee/list

**Template (employee-edit.html):**
- Same structure as Create but titled "Edit Employee"
- Additional section showing "Current Appointment Order: {currentAppointmentOrderFileName}" if a file exists
- File input labeled "Replace Appointment Order (PDF)" with hint text "Select a PDF to replace the current file"
- Button text: "Update Employee" instead of "Save Employee"

### employee-view.ts (EmployeeViewComponent)
- Selector: app-employee-view
- Standalone: true
- Imports: CommonModule, RouterLink
- Injected: EmployeeService, ActivatedRoute, Router

**Properties:**
- employee: Employee | null = null
- errorMessage: string = ''

**ngOnInit:**
- Gets id from route.snapshot.paramMap.get('id')
- Calls loadEmployee(id)

**Methods:**

loadEmployee(id):
- Calls employeeService.getEmployeeById(id)
- Sets employee (handles response.data or direct response)
- Sets errorMessage on failure

**Template (employee-view.html):**
- Title: "Employee Details"
- Error message div
- Details card showing all fields as label: value pairs:
  - Employee Code
  - Name (English)
  - Name (Tamil)
  - Designation
  - Department
  - Date of Joining
  - Mobile Number
  - Email
  - Remarks
  - Appointment Order (shows filename or "No file uploaded")
  - Created At
  - Updated At
- "Back to List" button/link

### employee-report.ts (EmployeeReportComponent)
- Selector: app-employee-report
- Standalone: true
- Imports: CommonModule, RouterLink
- Injected: EmployeeService, ActivatedRoute, Router

**Properties:**
- employee: Employee | null = null
- errorMessage: string = ''
- downloading: boolean = false

**ngOnInit:**
- Gets id from route.snapshot.paramMap.get('id')
- Calls loadEmployee(id)

**Methods:**

loadEmployee(id):
- Calls employeeService.getEmployeeById(id)
- Sets employee; handles response.data or direct response

downloadReport():
- Sets downloading = true
- Calls employeeService.downloadEmployeeReport(employee.id)
- On success: creates URL.createObjectURL(blob), creates invisible anchor element, sets href and download attribute to "employee_report_{id}.pdf", triggers click(), revokes object URL, sets downloading = false
- On error: sets errorMessage, sets downloading = false

**Template (employee-report.html):**
- Title: "Employee Service Record Report"
- Tamil subtitle: "பணியாளர் சேவை விவர அறிக்கை"
- Error message div
- Report card with rows displaying all employee fields in report-row layout (label on left, value on right)
- "Download PDF Report" button — disabled when downloading=true, text changes to "Downloading..."
- "Back to List" link

**Styles (employee-report.scss):**
- report-card: border 1px solid #e0e0e0, border-radius 8px, overflow hidden
- report-row: display flex, padding 12px 16px, border-bottom 1px solid #e0e0e0
- report-row alternating background: nth-child(even) has #f9f9f9
- report-label: width 220px, font-weight bold, color #555
- report-value: flex 1
- Download button: background #1976d2, color white, disabled state background #90caf9

### employee-form.ts (EmployeeFormComponent — Reusable/Utility)
- Selector: app-employee-form
- Standalone: true
- This is a utility component that centralizes create+edit form logic in one place
- Not used in routing directly — create and edit have their own dedicated components
- Supports a mode toggle: isEditMode boolean controls form title and submit button label

**Properties:**
- employeeForm: FormGroup
- isEditMode: boolean = false
- employeeId: number
- selectedFile: File | null = null
- currentAppointmentOrderFileName: string = ''
- successMessage: string = ''
- errorMessage: string = ''
- designations: string[] (same list)

**Methods:**
- initializeForm(): sets up form with same validators as Create/Edit
- loadEmployee(id): loads data into form for edit mode
- onFileChange(event): same PDF validation
- onSubmit(): routes to createEmployee or updateEmployee based on isEditMode
- uploadFile(id): uploads PDF
- get f(): returns form controls

---

## 9. Angular Configuration Details

### angular.json Key Settings
- Project type: application
- Style preprocessor: SCSS
- Output path: dist/employee-frontend
- Global styles file: src/styles.scss
- Index file: src/index.html
- Main entry: src/main.ts
- SSR server entry: src/server.ts
- Serve default port: 4200
- Development config: sourceMap true, optimization false
- Production config: budgets — initial bundle max 1MB, component styles max 4kB

### tsconfig.json Key Settings
- Target: ES2022
- Module: preserve
- Strict mode enabled
- experimentalDecorators: true
- noImplicitOverride: true
- noImplicitReturns: true
- Angular strictInjectionParameters: true
- Angular strictInputAccessModifiers: true

### package.json Scripts
- npm start — runs ng serve (development server on port 4200 with hot reload)
- npm run build — production build to dist/
- npm run watch — development build in watch mode
- npm test — runs tests with Vitest

---

## 10. Validation Rules (Complete Reference)

### Frontend Validation (Angular Reactive Forms)

| Field | Validators |
|---|---|
| employeeCode | required |
| employeeNameEnglish | required |
| employeeNameTamil | required |
| designation | required |
| department | required |
| dateOfJoining | required |
| mobileNumber | required, pattern: ^[0-9]{10}$ (exactly 10 digits, no letters) |
| email | required, Validators.email |
| remarks | none (optional) |
| File | type must be application/pdf, size must be <= 2097152 bytes (2MB) |

### Backend Validation (Jakarta Bean Validation)

| Field | Annotation |
|---|---|
| employeeCode | @NotBlank |
| employeeNameEnglish | @NotBlank |
| employeeNameTamil | @NotBlank |
| designation | @NotBlank |
| department | @NotBlank |
| dateOfJoining | @NotNull |
| mobileNumber | @Size(min=10, max=10) |
| email | @Email |
| remarks | none |

### Backend Business Validation (in Service)
- Employee code must not already exist in database on create
- Employee code must not conflict with another record's code on update

### Backend File Validation (in Controller)
- MultipartFile must not be empty
- Content type must be "application/pdf"
- File size must be <= 2MB (2 * 1024 * 1024 bytes)

---

## 11. Error Handling Flow

### Backend Error Flow
1. Controller receives request
2. If @Valid fails: Spring throws MethodArgumentNotValidException -> GlobalExceptionHandler catches it -> returns 400 with per-field errors
3. If service throws EmployeeNotFoundException -> GlobalExceptionHandler -> 404
4. If service throws DuplicateEmployeeCodeException -> GlobalExceptionHandler -> 400
5. If controller throws InvalidFileException -> GlobalExceptionHandler -> 400
6. Any unhandled exception -> GlobalExceptionHandler -> 500

### Frontend Error Flow
1. Service call fails (HTTP error) -> component's error handler sets errorMessage property
2. errorMessage is displayed in a red error div in the template
3. Success messages are passed via Angular Router's navigation state (router.navigate(['/employee/list'], { state: { successMessage: '...' } }))
4. EmployeeListComponent reads navigation state in ngOnInit and displays the message, clearing it after 3 seconds

---

## 12. Color and Style Reference

### Color Palette
- Primary blue: #1976d2 (header, primary buttons, download button)
- Success green: #4caf50 (view button), #2e7d32 (text), #e8f5e9 (background), #a5d6a7 (border)
- Warning orange: #ff9800 (edit button)
- Purple: #9c27b0 (report button)
- Danger red: #f44336 (delete button), #c62828 (error text), #ffebee (error background), #ef9a9a (error border)
- Gray: #757575 (cancel button, back links)
- Disabled blue: #90caf9 (download button disabled state)
- Table header: #f5f5f5
- Alternating row: #f9f9f9

### Layout Constants
- App container: width 90%, margin 0 auto
- Form max-width: 700px, centered with margin 20px auto
- Header padding: 20px
- Border radius (cards): 12px
- Border radius (buttons): 4px
- Border radius (inputs): 4px
- Box shadow: 0 2px 8px rgba(0, 0, 0, 0.08)
- Input padding: 10px
- Input border: 1px solid #cccccc
- Report label width: 220px

---

## 13. Designation Options (Hardcoded Dropdown)

The designation field in create and edit forms is a dropdown (<select>) with these options:
- Junior Assistant
- Senior Assistant
- Developer
- Senior Developer
- Manager
- Administrator

---

## 14. File Upload Flow (End-to-End)

1. User selects a PDF file in the file input on create or edit form
2. Frontend validates: must be PDF type, must be <= 2MB; shows error if not
3. On form submit, employee is created/updated first via the employee API
4. After getting the employee id from the response, the file is uploaded separately via POST /api/employees/upload/{id}
5. Controller validates file again: not empty, content type application/pdf, size <= 2MB
6. FileStorageService saves file to "uploads/" directory as "employee_{id}_appointment.pdf"
7. EmployeeService.updateAppointmentOrder saves the path and original filename to the database
8. EmployeeResponseDTO returns with appointmentOrderFileName set
9. Frontend shows the filename in view/edit screens

---

## 15. PDF Report Flow (End-to-End)

1. User clicks "Report" button on employee list or "Download PDF Report" on report page
2. Frontend navigates to /employee/report/{id}, which loads employee data for display
3. User clicks "Download PDF Report" button
4. Frontend calls employeeService.downloadEmployeeReport(id) with responseType blob
5. HTTP GET to /api/employees/report/{id}
6. Backend EmployeeReportService.generateEmployeeReport(id) creates PDF using PDFBox:
   - Loads Tamil fonts from classpath resources/fonts/
   - Creates A4 page
   - Draws English and Tamil titles
   - Draws 9-row, 2-column table with employee data
   - Tamil text converted to glyph vectors for compatibility
7. PDF byte array returned as response with Content-Disposition: attachment
8. Frontend receives Blob, creates object URL, triggers invisible anchor click to download
9. File saved as "employee_report_{id}.pdf" in browser's download location

---

## 16. Setup and Run Instructions

### Prerequisites
- Java 17 installed
- Maven installed
- Node.js and npm installed
- PostgreSQL running locally on port 5432

### Database Setup
1. Open PostgreSQL and create database: CREATE DATABASE employee_service_db;
2. User: postgres, Password: Sakthi@123 (or update application.properties)
3. Table is auto-created by Hibernate (ddl-auto=update)

### Backend Setup
1. Navigate to: employee-backend/employee-backend/
2. Run: mvn spring-boot:run
3. Backend starts on http://localhost:8080
4. Verify: GET http://localhost:8080/api/employees/list returns empty list

### Frontend Setup
1. Navigate to: employee-frontend/
2. Run: npm install
3. Run: npm start
4. Frontend starts on http://localhost:4200
5. Open browser to http://localhost:4200 — redirects to /employee/list

### File Uploads
- Uploaded files are stored in the "uploads/" directory relative to where the backend JAR runs
- This directory is created automatically by FileStorageService

---

## 17. Keywords for Interview Preparation

### Angular Keywords
- Standalone Components (no NgModule needed)
- Angular 21, Zoneless Change Detection (provideZonelessChangeDetection)
- Angular Signals compatible architecture
- Reactive Forms (FormGroup, FormBuilder, FormControl, Validators)
- Template-driven vs Reactive forms (this project uses Reactive)
- HttpClient with withFetch() provider
- RouterOutlet, RouterLink, ActivatedRoute, Router
- Navigation state (router.navigate with state object)
- Angular lifecycle hooks (ngOnInit)
- @for, @if (new Angular control flow syntax, Angular 17+)
- Observable, subscribe, RxJS
- Blob download technique (URL.createObjectURL, invisible anchor click)
- SCSS, component-scoped styles
- provideRouter, provideHttpClient, appConfig (standalone app config pattern)
- Lazy loading (not used here but know the concept)
- OnPush change detection (related to zoneless)

### Spring Boot Keywords
- Spring Boot 3.x, Java 17
- Spring Data JPA, Hibernate ORM
- JpaRepository, derived query methods
- @Entity, @Table, @Column, @Id, @GeneratedValue
- @RestController, @RequestMapping, @CrossOrigin
- @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
- @PathVariable, @RequestBody, @RequestParam, @Valid
- @Service, @Repository, @Transactional, @RequiredArgsConstructor
- @RestControllerAdvice (global exception handler)
- DTO pattern (Request DTO vs Response DTO vs Entity separation)
- Bean Validation: @NotBlank, @NotNull, @Email, @Size
- MultipartFile (file upload handling)
- ResponseEntity, HttpStatus
- ApiResponse wrapper (generic response envelope)
- application.properties configuration
- CORS (Cross-Origin Resource Sharing)
- Custom exceptions (RuntimeException subclasses)
- Maven build tool, pom.xml, spring-boot-starter-parent

### Database Keywords
- PostgreSQL
- JPA / Hibernate DDL auto
- Unique constraints
- VARCHAR, BIGSERIAL, DATE, TIMESTAMP
- Primary key, foreign key concepts
- ORM (Object Relational Mapping)

### General Concepts
- RESTful API design
- CRUD operations
- HTTP methods: GET, POST, PUT, DELETE
- HTTP status codes: 200, 201, 400, 404, 500
- JSON request/response
- Multipart form data
- File storage on server disk
- PDF generation (Apache PDFBox)
- Unicode / Tamil language support in PDFs
- TrueType fonts in PDF generation
- Binary file download (blob response)
- Full-stack architecture (frontend + backend + database)
- Separation of concerns (controller → service → repository layers)
- DTOs vs Entities
- Validation at multiple layers (frontend + backend)
- Error handling strategy (global exception handler)

---

## 18. What to Say About This Project in an Interview

This project is a full-stack Employee Service Record Management system built with Angular 21 on the frontend and Spring Boot 3 on the backend, connected to a PostgreSQL database.

The main purpose is to manage employee records for an organization, supporting bilingual data entry in both English and Tamil. Employees can have appointment order PDFs uploaded against their record, and a formatted PDF service record report can be generated and downloaded for any employee.

On the frontend, I used the latest Angular standalone component architecture without NgModule, with zoneless change detection. Forms use Angular Reactive Forms with validators. The employee service uses HttpClient with the fetch backend. PDF download is handled by getting a blob from the API and triggering a browser download via a temporary anchor element.

On the backend, I followed a layered architecture: Controller layer for HTTP handling, Service layer for business logic, Repository layer for database access. DTOs separate the API contract from the database entity. A global exception handler using @RestControllerAdvice provides consistent JSON error responses across all endpoints. File uploads are handled using MultipartFile and stored on disk using Java NIO APIs.

The most technically interesting part is the Tamil PDF generation using Apache PDFBox — Tamil text requires special TrueType fonts with OpenType shaping for correct rendering, and I used vector path conversion to ensure the text displays correctly in any PDF viewer regardless of font availability.
