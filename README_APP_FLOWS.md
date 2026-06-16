# Employee Service Record Management — Complete Application Guide

> **Who is this for?** Anyone learning how this app works — beginners included. Every technical term is explained the first time it appears.

---

## Table of Contents

1. [What Is This App?](#1-what-is-this-app)
2. [How the Two Parts Connect](#2-how-the-two-parts-connect)
3. [Project File Map](#3-project-file-map)
4. [Starting Up the App](#4-starting-up-the-app)
5. [Feature Flows](#5-feature-flows)
   - [Flow A — Viewing the Employee List](#flow-a--viewing-the-employee-list)
   - [Flow B — Adding a New Employee](#flow-b--adding-a-new-employee)
   - [Flow C — Editing an Employee](#flow-c--editing-an-employee)
   - [Flow D — Viewing Employee Details](#flow-d--viewing-employee-details)
   - [Flow E — Downloading a PDF Report](#flow-e--downloading-a-pdf-report)
6. [API Quick Reference](#6-api-quick-reference)
7. [What Happens When Things Go Wrong](#7-what-happens-when-things-go-wrong)
8. [Key Terms Explained](#8-key-terms-explained)

---

## 1. What Is This App?

This is a **web application** to manage employee service records for an organisation. You can:

- **Add, edit, and view** employee information (name in English and Tamil, designation, department, date of joining, etc.)
- **Upload** an appointment order document (PDF file) for each employee
- **Download** a formatted PDF report of any employee's service record — the report includes both English and Tamil text

The app has two separate programs that work together:
- A **frontend** (what you see in the browser) — built with **Angular**
- A **backend** (the server that stores data) — built with **Spring Boot** (Java), connected to a **PostgreSQL** database

---

## 2. How the Two Parts Connect

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│   YOUR BROWSER                                                              │
│   (Angular App — http://localhost:4200)                                     │
│                                                                             │
│   What it does: Shows pages, forms, tables.                                 │
│   Sends requests to the backend and displays the results.                   │
│                                                                             │
└────────────────────────────┬────────────────────────────────────────────────┘
                             │  HTTP requests (GET / POST / PUT)
                             │  e.g. "Give me all employees"
                             │  e.g. "Save this new employee"
                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│   SPRING BOOT SERVER                                                        │
│   (Java Backend — http://localhost:8080)                                    │
│                                                                             │
│   What it does: Receives requests, applies business rules,                  │
│   reads/writes to the database, generates PDF reports.                      │
│                                                                             │
└────────────────────────────┬────────────────────────────────────────────────┘
                             │  SQL queries
                             │  e.g. INSERT INTO employees (...)
                             │  e.g. SELECT * FROM employees
                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│   POSTGRESQL DATABASE                                                       │
│   (localhost:5432, database: employee_service_db)                           │
│                                                                             │
│   What it does: Permanently stores all employee records in a table.         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**In simple words:**
- The browser never talks to the database directly — it always goes through the Spring Boot server.
- The Spring Boot server is the "middle man" that enforces the rules (e.g. no duplicate employee codes) and handles file storage.

---

## 3. Project File Map

### Frontend (Angular) — `employee-frontend/src/app/`

```
app/
├── employee/
│   ├── employee-list/          ← The main table showing all employees
│   ├── employee-create/        ← The form to add a new employee
│   ├── employee-edit/          ← The form to edit an existing employee
│   ├── employee-view/          ← Read-only page showing one employee's details
│   ├── employee-report/        ← Page showing employee data + PDF download button
│   └── employee-form/          ← (Shared form component — not directly routed)
│
├── services/
│   └── employee.ts             ← All functions that talk to the backend (HTTP calls)
│
├── app.ts                      ← Root component with the navigation bar
├── app.config.ts               ← App setup: routing, HTTP, change detection
├── app.routes.ts               ← URL-to-component mapping (the "sitemap")
└── main.ts                     ← The very first file that runs; boots the app
```

### Backend (Spring Boot) — `employee-backend/src/main/java/com/employee/backend/`

```
backend/
├── EmployeeBackendApplication.java   ← The very first file that runs; starts the server
│
├── controller/
│   └── EmployeeController.java       ← Receives HTTP requests from Angular; the "front door"
│
├── service/
│   ├── EmployeeService.java          ← Interface (a contract listing what the service must do)
│   ├── EmployeeServiceImpl.java      ← The actual business logic (duplicate checks, etc.)
│   ├── FileStorageService.java       ← Saves uploaded PDF files to disk
│   └── EmployeeReportService.java    ← Generates the PDF report for download
│
├── repository/
│   └── EmployeeRepository.java       ← Talks to the database (SELECT / INSERT / UPDATE)
│
├── entity/
│   └── Employee.java                 ← The Java class that represents one row in the DB table
│
├── dto/
│   ├── EmployeeRequestDTO.java       ← Data shape for incoming requests (create/update)
│   ├── EmployeeResponseDTO.java      ← Data shape for outgoing responses
│   └── ApiResponse.java             ← Standard wrapper: { "message": "...", "data": ... }
│
└── exception/
    ├── EmployeeNotFoundException.java       ← Thrown when an ID doesn't exist in DB
    ├── DuplicateEmployeeCodeException.java  ← Thrown when employee code already exists
    ├── InvalidFileException.java            ← Thrown when the uploaded file is bad
    └── GlobalExceptionHandler.java         ← Catches all exceptions; sends proper error responses
```

### Backend Resources — `employee-backend/src/main/resources/`

```
resources/
├── application.properties     ← Configuration: database URL, port, file upload limits
└── fonts/
    ├── NotoSansTamil-Regular.ttf   ← Tamil font used for the report title
    ├── LATHA.TTF                   ← Tamil font used for table values in the report
    └── LATHAB.TTF                  ← Bold Tamil font (available but not used in report)
```

---

## 4. Starting Up the App

### Backend Startup (Running the Spring Boot server)

When you run the Java backend, here is what happens step by step:

**Step 1 — Java starts**
The program enters `EmployeeBackendApplication.java` and calls `SpringApplication.run(...)`.

**Step 2 — Spring scans all files**
Spring Boot automatically looks at every Java class in the project. It finds:
- `EmployeeController` → registers it to handle incoming HTTP requests
- `EmployeeServiceImpl` → registers it as the business logic handler
- `FileStorageService` → registers it; also **creates the `uploads/` folder on disk** if it doesn't exist
- `EmployeeReportService` → registers it as the PDF generator
- `EmployeeRepository` → Spring Data automatically generates all the SQL code needed to talk to the database

**Step 3 — Spring reads `application.properties`**
It finds the database connection details (host, port, username, password) and connects to PostgreSQL.

**Step 4 — Hibernate checks the database**
Hibernate (the ORM — Object-Relational Mapper — that converts Java objects to database rows) reads the `Employee` Java class and the setting `ddl-auto=update`. If the `employees` table does not exist in the database yet, it **creates the table automatically**. If the table exists but is missing a column, it adds the column.

**Step 5 — The web server starts**
Spring Boot's built-in web server (Apache Tomcat) starts listening on **port 8080**. The backend is now ready to accept requests.

---

### Frontend Startup (Opening the Angular app in the browser)

**Step 1 — Browser downloads the app**
The browser loads `index.html`, which contains a reference to the compiled Angular JavaScript. The browser downloads and runs that JavaScript.

**Step 2 — Angular boots**
`main.ts` calls `bootstrapApplication(App, appConfig)`. This sets up three things:
- **Router** — so URLs like `/employee/list` load the right component (page)
- **HttpClient with Fetch API** — so Angular can send HTTP requests to the backend
- **ZonelessChangeDetection** — a modern Angular setting that makes UI updates more efficient

**Step 3 — The root component renders**
Angular renders `<app-root>` which shows the navigation bar and a `<router-outlet>` (a placeholder where page content appears).

**Step 4 — The router reads the URL**
The URL is `/` → the router rule says "redirect `/` to `/employee/list`" → so Angular immediately goes to the list page.

**Step 5 — The list page loads**
`EmployeeListComponent` starts up, calls `loadEmployees()`, which calls `EmployeeService.getAllEmployees()`, which sends an HTTP GET request to `http://localhost:8080/api/employees/list`. When the response arrives, the table is populated with all employees.

---

## 5. Feature Flows

### Flow A — Viewing the Employee List

**What the user sees:** A table of all employees with search and action buttons.

1. The user opens the app at `http://localhost:4200`. The router redirects to `/employee/list`.
2. `EmployeeListComponent` starts and calls `loadEmployees()`.
3. `loadEmployees()` calls the Angular service, which sends `GET /api/employees/list` to the backend.
4. The backend's `EmployeeController` receives the request and calls `EmployeeServiceImpl.getAllEmployees()`.
5. The service asks the repository to fetch all rows from the `employees` table.
6. Each database row is converted to an `EmployeeResponseDTO` object (a simple data holder).
7. The response is wrapped as `{ "message": "...", "data": [ ... list of employees ... ] }` and sent back.
8. Angular receives the list and displays it in the table.

**Search:** The search box filters the displayed rows by employee code, English name, or Tamil name — this filtering is done entirely in the browser (no extra server request). Results are sorted by employee code numerically.

**If the backend is not running:** Angular shows an error message in the UI instead of the table.

---

### Flow B — Adding a New Employee

**What the user sees:** A form to fill in employee details, with an optional PDF file upload.

#### Part 1 — Filling and Submitting the Form

1. The user clicks **"Add Employee"** in the navigation. The router navigates to `/employee/create`.
2. `EmployeeCreateComponent` starts and builds a **Reactive Form** (a form controlled entirely by TypeScript code) with these fields:

   | Field | Required? | Extra Rules |
   |-------|-----------|-------------|
   | Employee Code | Yes | — |
   | Name (English) | Yes | — |
   | Name (Tamil) | Yes | — |
   | Designation | Yes | — |
   | Department | Yes | — |
   | Date of Joining | Yes | — |
   | Mobile Number | Yes | Must be exactly 10 digits |
   | Email | Yes | Must be a valid email format |
   | Remarks | No | — |

3. The user fills in the fields.
4. If the user picks a PDF file, `onFileChange()` validates it:
   - File type must be `application/pdf`
   - File size must be **2 MB or less**
   - If either check fails, an error message is shown and the file is rejected.

5. The user clicks **"Save Employee"**. The `onSubmit()` function runs.
6. If any required field is empty or invalid, all error messages are shown at once. The form stops here.
7. If the form is valid, it calls `EmployeeService.createEmployee(employeeData)`, which sends `POST /api/employees/create` with the form data as JSON.

#### Part 2 — What the Backend Does (Create Employee)

> **Behind the scenes:**
>
> 8. `EmployeeController.createEmployee()` receives the request.
> 9. The `@Valid` annotation tells Spring to check all the validation rules on `EmployeeRequestDTO`:
>    - If any field fails (e.g. blank name, invalid email): `GlobalExceptionHandler` sends back HTTP 400 with the error message. The form shows this error.
> 10. If validation passes, `EmployeeServiceImpl.createEmployee()` runs:
>     - It checks if another employee already has the same employee code (`existsByEmployeeCode()`).
>     - If the code is taken: throws `DuplicateEmployeeCodeException` → HTTP 400 error sent back.
>     - If the code is unique: creates a new `Employee` object, copies all the form fields into it, sets `createdAt` and `updatedAt` to the current date/time, and saves it to the database.
>     - PostgreSQL automatically assigns a unique numeric ID.
>     - The saved employee is converted to a response and sent back as HTTP 201 (Created).

#### Part 3 — Optional File Upload

11. Angular receives the "created successfully" response and gets the new employee's ID.
12. If the user had selected a PDF file, Angular now uploads it: `POST /api/employees/upload/{id}` with the file attached.

> **Behind the scenes (file upload):**
>
> 13. `EmployeeController` checks the file: not empty, ≤ 2 MB, ends with `.pdf`.
> 14. `FileStorageService.saveAppointmentOrderFile()` saves the file to the `uploads/` folder with the name `employee_{id}_appointment.pdf` (preserving the file extension).
> 15. `EmployeeServiceImpl.updateAppointmentOrder()` saves the file path and original filename in the database for this employee.

#### Part 4 — Navigation

16. Angular navigates back to `/employee/list` with a success message in the navigation state.
17. `EmployeeListComponent` reads the success message and displays a **green banner for 3 seconds**.
18. The table reloads and the new employee is visible.

---

### Flow C — Editing an Employee

**What the user sees:** A pre-filled form with the employee's current data; edit and save.

1. The user clicks **"Edit"** on a table row. The router navigates to `/employee/edit/{id}`.
2. `EmployeeEditComponent` starts, reads the `id` from the URL, and builds the same form as the create component.
3. It calls `EmployeeService.getEmployeeById(id)`, which sends `GET /api/employees/{id}`.

> **Behind the scenes:**
>
> 4. `EmployeeController.getEmployeeById()` is called.
> 5. `EmployeeServiceImpl.getEmployeeById()` searches the database by ID.
>    - If not found: throws `EmployeeNotFoundException` → HTTP 404 error.
>    - If found: converts the `Employee` entity to a response DTO and sends it back.

6. Angular receives the employee data and uses `form.patchValue()` to **pre-fill all form fields** with the existing values.
7. If the employee already has an appointment order file, its filename is shown above the file input.
8. The user makes changes and clicks **"Update Employee"**.
9. The same form validation rules apply as in create.
10. If valid, Angular sends `PUT /api/employees/update/{id}` with the updated data.

> **Behind the scenes:**
>
> 11. `EmployeeServiceImpl.updateEmployee()` runs:
>     - Finds the employee by ID (404 if missing).
>     - If the employee code was changed, checks the new code is not already used by a different employee.
>     - Updates all fields on the existing database record and sets `updatedAt` to now.
>     - Saves the updated record. PostgreSQL runs `UPDATE employees SET ... WHERE id = ?`.

12. If a new PDF file was selected, Angular uploads it the same way as in the create flow.
13. Angular navigates to `/employee/list` with a success message.

---

### Flow D — Viewing Employee Details

**What the user sees:** A read-only card showing all details for one employee.

1. The user clicks **"View"** on a table row. The router navigates to `/employee/view/{id}`.
2. `EmployeeViewComponent` starts and fetches the employee with `GET /api/employees/{id}`.
3. The employee's details are displayed in a card layout (no editing is possible here).
4. If the employee has an appointment order, the filename is shown.
5. A **"Back to List"** link navigates back to `/employee/list`.

This flow is purely read-only — no data is changed on the server.

---

### Flow E — Downloading a PDF Report

**What the user sees:** An employee details card with a "Download PDF Report" button.

#### Part 1 — Loading the Report Page

1. The user clicks **"Report"** on a table row. The router navigates to `/employee/report/{id}`.
2. `EmployeeReportComponent` starts, reads the `id` from the URL, and fetches employee data with `GET /api/employees/{id}`.
3. The employee's details are displayed on screen so the user can preview them.

#### Part 2 — Downloading the PDF

4. The user clicks **"Download PDF Report"**. The button shows a loading state (`downloading = true`).
5. Angular calls `EmployeeService.downloadEmployeeReport(id)`, which sends `GET /api/employees/report/{id}`.
   - The key detail: Angular asks for the response as a **Blob** (binary data), not as JSON. This is needed because PDFs are binary files, not text.

> **Behind the scenes — PDF Generation:**
>
> 6. `EmployeeController.generateEmployeeReport()` receives the request and calls `EmployeeReportService`.
>
> 7. `EmployeeReportService` builds the PDF in memory using **Apache PDFBox 2.0.27** (a Java library for creating PDFs):
>
>    a. Creates a blank A4-sized page.
>
>    b. **Loads two Tamil fonts** from the resources folder:
>       - `NotoSansTamil-Regular.ttf` at 14pt — used for the Tamil heading at the top
>       - `LATHA.TTF` at 11pt — used for Tamil values inside the table
>
>    c. **Draws the English title** "Employee Service Record Report" using standard Helvetica Bold font (PDF has Helvetica built-in, no file needed).
>
>    d. **Draws the Tamil title** "பணியாளர் சேவை விவர அறிக்கை" using a special technique:
>       - Standard PDF tools cannot draw Tamil correctly because Tamil letters change shape depending on neighbouring letters (e.g. a vowel sign moves to the left of its consonant).
>       - Instead, the Java text engine shapes the Tamil text into the correct visual form, then the result is extracted as a series of **vector paths** (lines and curves that describe each letter's outline).
>       - These paths are drawn directly into the PDF, so the Tamil text looks correct in every PDF viewer without needing the font installed.
>
>    e. **Draws the table grid** — 9 rows for 9 data fields, with a label column and a value column. All lines are drawn in one operation to avoid any line appearing darker where borders cross.
>
>    f. **Fills in each row** with the employee's data:
>
>       | Row | Label | Value type |
>       |-----|-------|-----------|
>       | 1 | Employee Code | English text |
>       | 2 | Name (English) | English text |
>       | 3 | Name (Tamil) | Tamil text (vector paths) |
>       | 4 | Designation | English text |
>       | 5 | Department | English text |
>       | 6 | Date of Joining | English text |
>       | 7 | Mobile Number | English text |
>       | 8 | Email | English text |
>       | 9 | Remarks | English text |
>
>    g. The completed PDF is saved to a memory buffer (not to disk) and returned as a byte array.
>
> 8. The controller sends the byte array back to Angular with:
>    - `Content-Type: application/pdf` (tells the browser this is a PDF)
>    - `Content-Disposition: attachment; filename="employee_report_{id}.pdf"` (tells the browser to download it, not display it)

#### Part 3 — Saving the File in the Browser

9. Angular receives the PDF as binary data (a Blob).
10. Angular creates a **temporary URL** pointing to that binary data in memory.
11. Angular creates a hidden `<a>` link element, sets its `href` to the temporary URL and its `download` attribute to `employee-report-{id}.pdf`, then programmatically clicks it.
12. The browser triggers a file download — the user sees the PDF save dialog.
13. Angular immediately frees the temporary URL from memory.
14. The download button returns to its normal state.

---

## 6. API Quick Reference

| What You Click | Angular Service Method | HTTP Method + URL | What the Backend Does | Response |
|----------------|----------------------|-------------------|-----------------------|----------|
| Open list page | `getAllEmployees()` | `GET /api/employees/list` | Fetches all rows from DB | JSON list of all employees |
| View / Edit / Report button | `getEmployeeById(id)` | `GET /api/employees/{id}` | Finds one employee by ID | JSON of one employee, or 404 |
| Save new employee form | `createEmployee(data)` | `POST /api/employees/create` | Validates + inserts new DB row | JSON of created employee (HTTP 201) |
| Save edited employee form | `updateEmployee(id, data)` | `PUT /api/employees/update/{id}` | Validates + updates existing DB row | JSON of updated employee |
| After saving, if file selected | `uploadAppointmentOrder(id, file)` | `POST /api/employees/upload/{id}` | Saves file to disk, updates DB with path | JSON of updated employee |
| Download PDF Report button | `downloadEmployeeReport(id)` | `GET /api/employees/report/{id}` | Generates PDF in memory | PDF file as binary data |

**Base URL:** `http://localhost:8080/api/employees`

All responses (except the PDF download) follow this JSON wrapper format:
```json
{
  "message": "Employee created successfully",
  "data": { ... employee object ... }
}
```

---

## 7. What Happens When Things Go Wrong

| Situation | Where It Is Caught | What the User Sees |
|-----------|-------------------|-------------------|
| Form field left empty or invalid | Angular (browser-side validation) | Red error text appears under the field immediately |
| Mobile number not exactly 10 digits | Angular form validator | "Mobile number must be exactly 10 digits" |
| Duplicate employee code entered | Backend → `DuplicateEmployeeCodeException` → `GlobalExceptionHandler` | HTTP 400 → Angular shows the error message from the server |
| Employee ID does not exist in DB | Backend → `EmployeeNotFoundException` → `GlobalExceptionHandler` | HTTP 404 → Angular shows "Employee not found" |
| Uploaded file is not PDF | Backend → `InvalidFileException` → `GlobalExceptionHandler` | HTTP 400 → Angular shows file validation error |
| Uploaded file is over 2 MB | Both Angular (before upload) and backend (after upload) | Angular shows size error before sending; backend also rejects if check is bypassed |
| Backend server is not running | No response from server | Angular catches the error and shows a generic error message in the UI |
| Any other unexpected backend error | Backend → generic `Exception` handler → `GlobalExceptionHandler` | HTTP 500 → Angular shows the error message |

---

## 8. Key Terms Explained

| Term | Plain-English Meaning |
|------|----------------------|
| **Angular** | A framework (set of tools) for building web pages that update dynamically without reloading the browser tab |
| **Spring Boot** | A Java framework that makes it easy to build web servers (APIs) quickly |
| **PostgreSQL** | A database system that stores data in tables, like Excel but for programs |
| **HTTP** | The language browsers and servers use to talk to each other. GET = "give me data", POST = "save new data", PUT = "update existing data" |
| **API** | A set of URLs the backend exposes so the frontend can request data or actions |
| **DTO (Data Transfer Object)** | A simple container object used to pass data between the frontend and backend. `RequestDTO` = data coming in; `ResponseDTO` = data going out |
| **JPA / Hibernate** | Tools that let Java code talk to the database using Java objects instead of writing raw SQL |
| **DDL-auto=update** | A setting that tells Hibernate to automatically create or update the database table when the server starts |
| **Bean Validation / `@Valid`** | A Java feature where you put rules (`@NotBlank`, `@Email`) on a class and Spring automatically checks them when a request arrives |
| **Reactive Forms** | Angular's way of building forms where the form's rules and values are controlled in TypeScript code (not in the HTML template) |
| **Observable** | An Angular/RxJS concept. Think of it like a "subscription" — your code says "when the HTTP response eventually arrives, do this" |
| **Router / RouterOutlet** | Angular's system for showing different components (pages) based on the URL, without reloading the browser tab |
| **CORS (Cross-Origin Resource Sharing)** | A browser security rule that normally blocks a page on port 4200 from calling a server on port 8080. The backend opts-in by setting `@CrossOrigin(*)` to allow all origins |
| **Blob** | "Binary Large Object" — a chunk of raw binary data. Used when downloading files (like PDFs) that are not plain text |
| **FormData** | A special format for sending files over HTTP. Angular wraps the PDF file in FormData before uploading |
| **`@Transactional`** | A Java annotation that wraps a database operation in a transaction — if anything fails mid-way, all changes are automatically rolled back |
| **OpenType Shaping** | The process of determining the correct visual form of letters in complex scripts like Tamil, where letters change shape depending on context |
| **Vector Paths** | Mathematical descriptions of shapes (lines, curves) used to draw text in the PDF. Unlike images, they stay sharp at any zoom level |
| **Zoneless Change Detection** | A modern Angular setting (used here) where Angular only updates the UI when explicitly told to, rather than checking constantly — it is more efficient |
| **Fetch API** | The modern browser API for making HTTP requests. Angular is configured to use it (`withFetch()`) instead of the older XMLHttpRequest |
