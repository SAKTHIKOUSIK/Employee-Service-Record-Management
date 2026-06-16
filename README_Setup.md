# Employee Service Record Management

Full-stack employee record system with Angular frontend and Spring Boot backend.

## Tech Stack

| Layer    | Technology                   | Port |
| -------- | ---------------------------- | ---- |
| Frontend | Angular 21                   | 4200 |
| Backend  | Java 17 + Spring Boot 3.5.15 | 8080 |
| Database | PostgreSQL                   | 5432 |

---

## Prerequisites — Install These on the New System

### 1. Node.js & npm

- Download: https://nodejs.org (LTS version)
- Verify after install:
  ```bash
  node -v
  npm -v
  ```

### 2. Angular CLI

```bash
npm install -g @angular/cli
```

Verify: `ng version`

### 3. Java 17 (JDK)

- Download: https://adoptium.net (Eclipse Temurin 17)
- Verify: `java -version`

### 4. Eclipse IDE for Enterprise Java

- Download: https://www.eclipse.org/downloads/
- During install, pick: **Eclipse IDE for Enterprise Java and Web Developers**
- Recommended plugin: **Spring Tools 4**
  - Help → Eclipse Marketplace → search "Spring Tools 4" → Install

### 5. Maven

- Maven is usually bundled with Eclipse
- Verify in terminal: `mvn -version`
- If missing: https://maven.apache.org/download.cgi

### 6. PostgreSQL

- Download: https://www.postgresql.org/download/
- During install, set a password for the `postgres` user (remember it — you'll need it)
- Default port: 5432
- pgAdmin is installed alongside PostgreSQL and can be used to manage the database visually

---

## Step 1 — Clone from GitHub

```bash
git clone https://github.com/<your-username>/employee-service-record-management.git
cd employee-service-record-management
```

---

## Step 2 — Database Setup (PostgreSQL)

Open **pgAdmin** (comes with PostgreSQL) or use **psql** in terminal:

```sql
CREATE DATABASE employee_service_db;
```

> **Note:** You only need to create the database manually. The backend automatically creates all tables on first startup because `spring.jpa.hibernate.ddl-auto=update` is set.

Then open this file and update the password:

```
employee-backend/employee-backend/src/main/resources/application.properties
```

Find this line and replace `YOUR_PASSWORD_HERE` with your actual PostgreSQL password:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_service_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD_HERE
```

---

## Step 3 — Backend Setup in Eclipse

1. Open **Eclipse**
2. Go to **File → Import → Maven → Existing Maven Projects** → Click **Next**
3. Click **Browse** and navigate to:
   ```
   employee-service-record-management/employee-backend/employee-backend
   ```
   _(Note: it's the inner `employee-backend` folder that contains `pom.xml`)_
4. Eclipse detects `pom.xml` → Click **Finish**
5. Wait for Maven to download all dependencies (watch the progress bar in the bottom-right corner)
6. In the **Package Explorer**, right-click on the project → **Run As → Spring Boot App**
7. Watch the **Console** tab — you should see:
   ```
   Started EmployeeBackendApplication in X.XXX seconds
   ```
8. Backend is now running at: http://localhost:8080

> This is exactly the same flow as before — **Run As → Spring Boot App** — nothing changes.

---

## Step 4 — Frontend Setup in VS Code

### Option A: Using the Terminal directly

```bash
# Go into the frontend folder
cd employee-service-record-management/employee-frontend

# Install all packages (run once, or again after git pull)
npm install

# Start the development server
ng serve
```

### Option B: Using VS Code

1. Open VS Code → **File → Open Folder** → select the `employee-frontend` folder
2. Open the integrated terminal with **Ctrl + `** (backtick)
3. Run:
   ```bash
   npm install
   ng serve
   ```
4. Open your browser at: **http://localhost:4200**

---

## Step 5 — Verify Everything is Running

| Check          | URL                                  | Expected Result                              |
| -------------- | ------------------------------------ | -------------------------------------------- |
| Frontend       | http://localhost:4200                | Angular app loads in the browser             |
| Backend API    | http://localhost:8080/api/employees  | JSON response (empty array `[]` if no data)  |
| Database       | pgAdmin → employee_service_db → Tables | Tables appear after first backend startup  |

---

## Folder Structure (after clone)

```
employee-service-record-management/
├── employee-frontend/            ← Open this folder in VS Code
│   ├── src/
│   ├── package.json
│   └── angular.json
├── employee-backend/
│   └── employee-backend/         ← Import this folder into Eclipse as Maven project
│       ├── src/
│       │   └── main/resources/
│       │       └── application.properties   ← Update DB password here
│       └── pom.xml
└── README.md
```

---

## Packages Installed Automatically

### Frontend — `npm install` downloads all of these

| Package | Purpose |
|---------|---------|
| `@angular/core`, `@angular/forms`, `@angular/router` | Angular framework |
| `@angular/ssr`, `express` | Server-side rendering |
| `rxjs` | Reactive data streams |
| `typescript` | TypeScript compiler |
| `@angular/cli`, `@angular/build` | Build & serve tooling |
| `vitest`, `jsdom` | Unit testing |

### Backend — Maven downloads all of these on first build

| Package | Purpose |
|---------|---------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` | Database ORM (Hibernate) |
| `spring-boot-starter-validation` | Input validation |
| `postgresql` | PostgreSQL database driver |
| `lombok` | Reduces boilerplate code |
| `pdfbox 2.0.27` | PDF report generation (Tamil font support) |
| `spring-boot-starter-test` | Testing framework |

---

## Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| `ng` command not found | Run `npm install -g @angular/cli` in terminal |
| Port 4200 already in use | Run `ng serve --port 4201` instead |
| Port 8080 already in use | Add `server.port=8081` in `application.properties` |
| DB connection refused | Make sure PostgreSQL service is started; check password in `application.properties` |
| Maven build fails in Eclipse | Right-click project → **Maven → Update Project** (or press Alt+F5) |
| `uploads/` folder missing | Create it manually inside `employee-backend/employee-backend/` |
| Lombok errors in Eclipse | Install the Lombok plugin: run `lombok.jar` and point it to your Eclipse installation |

---

## Quick Start Summary

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/employee-service-record-management.git

# 2. Create the database (run once in psql or pgAdmin)
CREATE DATABASE employee_service_db;

# 3. Update DB password
#    Edit: employee-backend/employee-backend/src/main/resources/application.properties

# 4. Start the backend (in Eclipse)
#    Right-click project → Run As → Spring Boot App

# 5. Start the frontend
cd employee-frontend
npm install
ng serve
```

Open **http://localhost:4200** — done!
