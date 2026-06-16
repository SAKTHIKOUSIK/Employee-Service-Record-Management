# Employee Service Record Management — Requirements Cross-Check Analysis

## Project Overview

Full Stack web application for Employee Service Record Management built with:

- **Frontend:** Angular (Standalone Components, Reactive Forms, SCSS)
- **Backend:** Spring Boot 3.x (REST API, JPA, PostgreSQL)
- **Database:** PostgreSQL (via pgAdmin 4)
- **PDF Generation:** Apache PDFBox 2.0.27 with Tamil text rendered as vector paths via Java `TextLayout` (NotoSansTamil-Regular.ttf for title, LATHA.TTF for body)

---

## Employee Details Form Fields

| Field                 | Type        | Validation  | Status                    |
| --------------------- | ----------- | ----------- | ------------------------- |
| Employee Name English | Text        | Required    | ✅ Done                   |
| Employee Name Tamil   | Text        | Required    | ✅ Done                   |
| Employee Code         | Text        | Required    | ✅ Done                   |
| Designation           | Dropdown    | Required    | ✅ Done (select dropdown) |
| Department            | Text        | Required    | ✅ Done                   |
| Date of Joining       | Date        | Required    | ✅ Done                   |
| Mobile Number         | Text        | 10 digits   | ✅ Done                   |
| Email                 | Text        | Valid email | ✅ Done                   |
| Remarks               | Textarea    | Optional    | ✅ Done                   |
| Appointment Order     | File Upload | PDF only    | ✅ Done                   |

---

## 1. Angular GUI Routes

| Route                  | Status         |
| ---------------------- | -------------- |
| `/employee/list`       | ✅ Done        |
| `/employee/create`     | ✅ Done        |
| `/employee/edit/:id`   | ✅ Done        |
| `/employee/view/:id`   | ✅ Done        |
| `/employee/report/:id` | ❌ **MISSING** |

> **Note:** The PDF report currently works via a "Report" button on the list page that directly downloads the PDF from the backend API. However, the requirement explicitly specifies a dedicated Angular route `/employee/report/:id` with its own component. There is no `EmployeeReportComponent` and the route is not registered in `app.routes.ts`.

### Angular Requirements

| Requirement                             | Status                                        |
| --------------------------------------- | --------------------------------------------- |
| Reactive Forms                          | ✅ Done                                       |
| Validation messages                     | ✅ Done                                       |
| Employee list table                     | ✅ Done                                       |
| Search by Employee Name / Employee Code | ✅ Done (also searches Tamil name as a bonus) |
| Edit button                             | ✅ Done                                       |
| View button                             | ✅ Done                                       |
| PDF Report button                       | ✅ Done                                       |
| File upload field                       | ✅ Done                                       |

---

## 2. Spring Boot REST API Endpoints

| Method | Endpoint                     | Status  |
| ------ | ---------------------------- | ------- |
| POST   | `/api/employees/create`      | ✅ Done |
| GET    | `/api/employees/list`        | ✅ Done |
| GET    | `/api/employees/{id}`        | ✅ Done |
| PUT    | `/api/employees/update/{id}` | ✅ Done |
| POST   | `/api/employees/upload/{id}` | ✅ Done |
| GET    | `/api/employees/report/{id}` | ✅ Done |

---

## 3. Save and Update

| Requirement                            | Status                              |
| -------------------------------------- | ----------------------------------- |
| Save employee record                   | ✅ Done                             |
| Update employee record                 | ✅ Done                             |
| Prevent duplicate Employee Code        | ✅ Done                             |
| Store created and updated date         | ✅ Done                             |
| Show success/error messages in Angular | ✅ Done (in form + list components) |

---

## 4. File Upload

| Requirement                          | Status  |
| ------------------------------------ | ------- |
| Only PDF allowed                     | ✅ Done |
| Maximum size: 2 MB                   | ✅ Done |
| Store file path or document ID       | ✅ Done |
| Show uploaded file name in view page | ✅ Done |

---

## 5. PDF Report with Tamil Font

| Requirement                             | Status                                          |
| --------------------------------------- | ----------------------------------------------- |
| Tamil font support                      | ✅ Done (NotoSansTamil-Regular.ttf title + LATHA.TTF body via PDFBox) |
| Tamil title: பணியாளர் சேவை விவர அறிக்கை | ✅ Done                                         |
| Employee Code in report                 | ✅ Done                                         |
| Employee Name English in report         | ✅ Done                                         |
| Employee Name Tamil in report           | ✅ Done                                         |
| Designation in report                   | ✅ Done                                         |
| Department in report                    | ✅ Done                                         |
| Date of Joining in report               | ✅ Done                                         |
| Mobile Number in report                 | ✅ Done                                         |
| Email in report                         | ✅ Done                                         |
| Remarks in report                       | ✅ Done                                         |

---

## Final Summary

### What is Complete

- All 10 form fields with correct types and validations
- 4 out of 5 Angular routes
- All 6 Spring Boot REST endpoints
- Save, update, duplicate prevention, timestamps
- Success/error messages in Angular
- File upload with PDF-only and 2 MB restrictions
- Uploaded file name shown in view page
- PDF report generation with Tamil font and all 9 fields

### What is Missing

| #   | Item                                 | Details                                                                                                                                                                                   |
| --- | ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `/employee/report/:id` Angular route | A dedicated Angular component and route for the report page is required. Currently the report is downloaded directly via API call from the list page without a dedicated route/component. |

---

## Project File Structure Reference

### Frontend (`employee-frontend/`)

```
src/app/
├── app.routes.ts                        — Route definitions
├── app.ts / app.html                    — Root component
├── services/
│   └── employee.ts                      — HTTP service for all API calls
└── employee/
    ├── employee-list/                   — /employee/list route
    ├── employee-form/                   — /employee/create and /employee/edit/:id
    └── employee-view/                   — /employee/view/:id
```

### Backend (`employee-backend/`)

```
src/main/java/com/employee/backend/
├── controller/EmployeeController.java   — REST endpoints
├── entity/Employee.java                 — JPA entity
├── dto/                                 — Request/Response DTOs
├── service/
│   ├── EmployeeService.java             — CRUD service
│   ├── EmployeeReportService.java       — PDF generation with Tamil font
│   └── FileStorageService.java          — PDF upload handling
├── repository/EmployeeRepository.java   — JPA repository
└── exception/                           — Custom exceptions + GlobalExceptionHandler
src/main/resources/fonts/
├── NotoSansTamil-Regular.ttf            — Tamil font used in PDF
├── LATHA.TTF
└── LATHAB.TTF
```

---

## Changes Made After Initial Analysis

### Change 1 — Added `/employee/report/:id` Angular Route

**Problem:** `/employee/report/:id` route was missing from `app.routes.ts`. No `EmployeeReportComponent` existed. The PDF report was triggered via a direct download button on the list page without navigating to a dedicated route.

**Solution:** Created `EmployeeReportComponent` with its own route.

**New files created:**

- `employee/employee-report/employee-report.ts` — loads employee data by ID, triggers PDF download
- `employee/employee-report/employee-report.html` — displays all 9 report fields + Tamil subtitle (பணியாளர் சேவை விவர அறிக்கை) + Download PDF button
- `employee/employee-report/employee-report.scss` — styled table layout matching project design

**Files updated:**

- `app.routes.ts` — added `{ path: 'employee/report/:id', component: EmployeeReportComponent }`
- `employee-list/employee-list.ts` — replaced `downloadReport()` with `goToReport()` that navigates to `/employee/report/:id`
- `employee-list/employee-list.html` — Report button now calls `goToReport(employee.id)`

**Route status after fix:**

| Route                  | Status                               |
| ---------------------- | ------------------------------------ |
| `/employee/list`       | ✅ Done                              |
| `/employee/create`     | ✅ Done                              |
| `/employee/edit/:id`   | ✅ Done                              |
| `/employee/view/:id`   | ✅ Done                              |
| `/employee/report/:id` | ✅ Done (was ❌ Missing — now fixed) |

---

### Change 2 — Separated Create and Edit into Dedicated Components

**Problem:** Both `/employee/create` and `/employee/edit/:id` shared a single `EmployeeFormComponent` using an `isEditMode` boolean flag, causing mixed logic and conditional `*ngIf` clutter in the template.

**Reason:** Single Responsibility Principle — each component should have one clear job. Separate components = cleaner code, independent layouts, easier to maintain. The PDF question paper only specifies routes — not component architecture — so this is fully valid.

**Solution:** Split into two focused, independent components.

**New files created:**

- `employee/employee-create/employee-create.ts` — create-only logic, no `isEditMode` flag
- `employee/employee-create/employee-create.html` — uses modern Angular `@if`/`@for` control flow syntax
- `employee/employee-create/employee-create.scss`
- `employee/employee-edit/employee-edit.ts` — edit-only logic, loads & patches existing employee data from API
- `employee/employee-edit/employee-edit.html` — shows current uploaded file name, replace file hint, modern `@if`/`@for` syntax
- `employee/employee-edit/employee-edit.scss`

**Files updated:**

- `app.routes.ts` — `/employee/create` now uses `EmployeeCreateComponent`, `/employee/edit/:id` now uses `EmployeeEditComponent`, old `EmployeeFormComponent` import removed

> **Note:** The old `employee-form/` folder still exists in the project but is no longer wired to any route. It can be deleted safely.

**Updated frontend structure:**

```
src/app/employee/
├── employee-list/       — /employee/list
├── employee-create/     — /employee/create         ← NEW
├── employee-edit/       — /employee/edit/:id       ← NEW
├── employee-view/       — /employee/view/:id
├── employee-report/     — /employee/report/:id     ← NEW (from Change 1)
└── employee-form/       — (old — no longer used in routing)
```

---

### Change 4 — Angular Version Downgrade: v22 → v21

**Date:** 2026-06-15

**Reason:** User requested downgrade from Angular 22 to Angular 21 for compatibility reasons.

**Files updated:**

- `employee-frontend/package.json` — all `@angular/*` packages, TypeScript, vitest, jsdom versions changed
- `employee-frontend/angular.json` — removed `security.allowedHosts` block (Angular 22-only feature)

**package.json version changes:**

| Package | Before | After |
|---|---|---|
| `@angular/common` | `^22.0.0` | `^21.0.0` |
| `@angular/compiler` | `^22.0.0` | `^21.0.0` |
| `@angular/core` | `^22.0.0` | `^21.0.0` |
| `@angular/forms` | `^22.0.0` | `^21.0.0` |
| `@angular/platform-browser` | `^22.0.0` | `^21.0.0` |
| `@angular/platform-server` | `^22.0.0` | `^21.0.0` |
| `@angular/router` | `^22.0.0` | `^21.0.0` |
| `@angular/ssr` | `^22.0.1` | `^21.0.0` |
| `@angular/build` | `^22.0.1` | `^21.0.0` |
| `@angular/cli` | `^22.0.1` | `^21.0.0` |
| `@angular/compiler-cli` | `^22.0.0` | `^21.0.0` |
| `typescript` | `~6.0.2` | `~5.9.0` |
| `vitest` | `^4.0.8` | `^4.0.8` (unchanged — Angular 21.2.x requires same) |
| `jsdom` | `^28.0.0` | `^25.0.0` |

**angular.json change:**

Removed the `security` block that is only available in Angular 22:
```json
// REMOVED (Angular 22 only):
"security": {
  "allowedHosts": []
}
```

**Key notes discovered during downgrade:**

- Angular 21.2.x (latest patch) requires TypeScript `>=5.9 <6.0` — not 5.7 as initially expected
- `@angular/build@21.2.15` still requires `vitest@^4.0.8` — same as Angular 22, no downgrade needed there
- `jsdom@25.0.1` installed (matching `^25.0.0` specified)
- Resolved installed Angular version: `21.2.15`

---

### Change 3 — Employee List Sorted by Employee Code

**Problem:** The employee list at `/employee/list` was displayed in a shuffled order (e.g., E004, E001, E002, E005, E003, E006) — not sorted by Employee Code.

**Solution:** Added a sort by `employeeCode` inside the `filteredEmployees` getter in `employee-list.ts`. Used `localeCompare` with `{ numeric: true }` so codes like E10 sort correctly after E9, not before E2.

**File updated:**

- `employee-list/employee-list.ts` — `filteredEmployees` getter now sorts by `employeeCode` before returning, applies to both full list and search results

**Before:** E004, E001, E002, E005, E003, E006 (shuffled)

**After:** E001, E002, E003, E004, E005, E006 (sorted by Employee Code)

The README_CODE_EXPLANATION.md is complete — 4,087 lines covering everything in the project.

Here's what's included:

Structure:

Project Overview with a full ASCII architecture diagram
5 complete end-to-end flows traced step-by-step (startup, create, edit, PDF download, etc.)
API Connection Map table showing exactly which Angular method → which Spring endpoint
Backend (17 files explained line-by-line):

pom.xml — every dependency explained (why JPA, why OpenPDF, why Lombok)
application.properties — every property with "what happens if you change it"
EmployeeBackendApplication.java — what SpringApplication.run() actually does
Employee.java — every @Column, @Table, @GeneratedValue annotation explained
EmployeeRepository.java — how Spring Data generates SQL from method names
All 3 custom exceptions + GlobalExceptionHandler — why unchecked exceptions, what HTTP codes go where
EmployeeServiceImpl.java — duplicate check logic, @Transactional(readOnly = true), entity mapping
FileStorageService.java — path traversal protection, REPLACE_EXISTING behavior
EmployeeReportService.java — OpenPDF, IDENTITY_H encoding for Tamil Unicode, ClassPathResource, ByteArrayOutputStream
EmployeeController.java — @CrossOrigin, @Valid, @PathVariable, ResponseEntity<byte[]> for PDF
Frontend (12 files explained line-by-line):

main.ts → app.config.ts → app.routes.ts → root component
EmployeeService — why responseType: 'blob', FormData.append(), Observable laziness
Every component's .ts and .html — patchValue(), markAllAsTouched(), ChangeDetectorRef, URL.createObjectURL(), @if/@for vs *ngIf/*ngFor
Final summary table of all Angular and Spring Boot concepts used — perfect for interview quick-reference.

---

## Changes Made in Session 2 — Tamil PDF Fix + Angular Warnings

**Date:** 2026-06-15

---

### Change 5 — Tamil PDF Spelling Fix (Root Cause + Three-Stage Fix)

**Problem:** Downloaded PDF showed scrambled Tamil text. Example: "சேவை" (service) appeared as "சவேை" in the PDF — the vowel sign ே (U+0BC7) was rendered after its consonant instead of visually before it.

**Root Cause:** Tamil is a complex Indic script. The vowel sign ே must visually appear to the LEFT of its consonant (ச), but in Unicode it is stored AFTER the consonant. A proper text rendering engine applies OpenType GSUB/GPOS shaping rules to handle this reordering. OpenPDF (`com.lowagie.text`) does not apply these shaping rules — it writes raw Unicode code points to the PDF in logical order, producing scrambled output.

---

#### Stage 1 — Attempted Fix: Flying Saucer (Failed)

**Approach:** Replace OpenPDF direct rendering with Flying Saucer (`flying-saucer-pdf-openpdf:9.1.22`), which renders HTML+CSS to PDF using Java's layout engine.

**pom.xml change:**
```xml
<!-- Added alongside existing openpdf -->
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf-openpdf</artifactId>
    <version>9.1.22</version>
</dependency>
```

**EmployeeReportService.java:** Rewritten to build XHTML document string and render via `ITextRenderer`. Tamil font registered with `ITextFontResolver` and Java's `GraphicsEnvironment`.

**Result:** Tamil text still scrambled. Flying Saucer's `ITextOutputDevice` passes raw Unicode strings to OpenPDF for text rendering rather than using Java's shaped `GlyphVector` output — the same underlying encoding problem persists.

---

#### Stage 2 — Final Fix: Apache PDFBox + TextLayout Vector Paths (Working)

**Root Fix Principle:** Use Java's `java.awt.font.TextLayout` to shape Tamil text via the JVM's built-in OpenType engine (backed by DirectWrite on Windows). Call `getOutline()` to extract the correctly-shaped glyph paths as vector curves. Write those curves directly into the PDF as filled path commands — bypassing the PDF font encoding layer entirely.

**Why this works:**
- `TextLayout` applies OpenType GSUB/GPOS rules — ே is correctly positioned to the left of its consonant
- `getOutline()` returns a `Shape` containing the actual glyph outlines after shaping
- The paths are drawn as PDF vector graphics (`moveTo`, `lineTo`, `curveTo`, `fill`) — no font encoding needed
- Every PDF viewer renders the paths identically regardless of Tamil font support

**pom.xml changes:**
```xml
<!-- Removed: openpdf and flying-saucer-pdf-openpdf -->
<!-- Added: -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.27</version>
</dependency>
```

**EmployeeReportService.java — Key method:**
```java
private void drawTamil(PDPageContentStream cs, String text, Font awtFont, float pdfX, float pdfY) {
    FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
    Shape outline = new TextLayout(text, awtFont, frc).getOutline(null);
    // Iterate PathIterator → write moveTo/lineTo/curveTo/closePath → cs.fill()
    // TrueType quadratic beziers converted to PDF cubic beziers inline
    // AWT Y-down coordinate flipped to PDF Y-up: pdfY - awtY
}
```

**Files changed:**
- `employee-backend/pom.xml` — removed `openpdf` and `flying-saucer-pdf-openpdf`, added `pdfbox:2.0.27`
- `EmployeeReportService.java` — complete rewrite using PDFBox + TextLayout path rendering

---

### Change 6 — Tamil Table Field Appeared Bold

**Problem:** The "Employee Name (Tamil)" field value in the PDF appeared heavier/bolder than the surrounding English text (Helvetica).

**Root Cause:** NotoSansTamil-Regular has thicker stroke weights by design — it is optimised for screen readability at small sizes. At 11pt in a PDF, its strokes appear visually heavier than Helvetica Regular at the same size.

**Solution:** Use `LATHA.TTF` (Microsoft's standard Tamil font) for table body values. Latha has lighter, traditional Tamil strokes that match Helvetica body-text weight visually. Keep NotoSansTamil for the heading title where heavier strokes are appropriate.

**Font usage after fix:**

| Location | Font | Size |
|---|---|---|
| Tamil title heading | NotoSansTamil-Regular.ttf | 14pt |
| Tamil name field value | LATHA.TTF | 11pt |

**File changed:** `EmployeeReportService.java` — `loadAwtFont("fonts/LATHA.TTF", 11f)` used for `awtLatha11`, passed to Tamil table value rows only.

---

### Change 7 — Table Borders Appeared Darker Between Rows

**Problem:** The horizontal lines between table rows and the vertical column divider appeared darker/thicker than the outer table border.

**Root Cause:** Each row drew a complete rectangle (`cs.addRect(x, y, width, height)`). Adjacent rows share edges — the bottom of row N is the same line as the top of row N+1. Both rows stroke that line independently, making it effectively twice as thick. Same issue with the column divider (stroked once by each of the two columns).

**Solution:** Replaced per-cell rectangle drawing with a single grid pass that draws every line exactly once:

```java
private void drawTableGrid(PDPageContentStream cs, float firstRowBottom) throws IOException {
    // 10 horizontal lines for 9 rows (one per row top + final bottom)
    for (int i = 0; i <= NUM_ROWS; i++) {
        float lineY = tableTop - i * ROW_HEIGHT;
        cs.moveTo(left, lineY); cs.lineTo(right, lineY);
    }
    // 3 vertical lines: left edge, column divider, right edge
    cs.moveTo(left, tableTop);     cs.lineTo(left, tableBottom);
    cs.moveTo(divider, tableTop);  cs.lineTo(divider, tableBottom);
    cs.moveTo(right, tableTop);    cs.lineTo(right, tableBottom);
    cs.stroke(); // single stroke call — every line drawn exactly once
}
```

Text content (`rowText()`) is written separately after the grid is drawn — no borders in the text rendering method.

**File changed:** `EmployeeReportService.java` — new `drawTableGrid()` method; `rowText()` method handles text only.

---

### Change 8 — Angular HttpClient Fetch Warning Fixed

**Problem:** Angular console showed two warnings on every request:
- `NG02801: Angular detected that HttpClient is not configured to use fetch APIs`
- `XHR support in @angular/platform-server is deprecated`

**Root Cause:** `provideHttpClient()` in `app.config.ts` had no options — Angular SSR defaulted to the deprecated XHR backend instead of the modern fetch-based backend.

**Solution:** Added `withFetch()` feature to `provideHttpClient()`.

**File changed:** `src/app/app.config.ts`

```typescript
// Before:
import { provideHttpClient } from '@angular/common/http';
provideHttpClient()

// After:
import { provideHttpClient, withFetch } from '@angular/common/http';
provideHttpClient(withFetch())
```

> **Note:** The `DEP0169 url.parse()` Node.js deprecation warning still appears — it comes from Angular's internal dev server machinery and cannot be fixed from application code.

---

### Change 9 — tsconfig.app.json rootDir Error Fixed

**Problem:** VS Code Problems panel showed a TypeScript error in `tsconfig.app.json`:
> "The common source directory of 'tsconfig.app.json' is './src'. The 'rootDir' setting must be explicitly set to this or another path to adjust your output's file layout."

**Root Cause:** `tsconfig.app.json` had `"outDir": "./out-tsc/app"` but no explicit `"rootDir"`. TypeScript inferred the root as `./src` from the `include` glob but requires it to be declared explicitly when `outDir` is set.

**Solution:** Added `"rootDir": "./src"` to `compilerOptions`.

**File changed:** `tsconfig.app.json`

```json
"compilerOptions": {
  "outDir": "./out-tsc/app",
  "rootDir": "./src",
  "types": ["node"]
}
```

---

### Summary of Session 2 Changes

| # | File | Change |
|---|---|---|
| 5a | `pom.xml` | Removed `openpdf` + `flying-saucer-pdf-openpdf`, added `pdfbox:2.0.27` |
| 5b | `EmployeeReportService.java` | Complete rewrite — PDFBox + TextLayout vector path rendering for Tamil |
| 6 | `EmployeeReportService.java` | Tamil table values switched from NotoSansTamil to LATHA.TTF (lighter weight) |
| 7 | `EmployeeReportService.java` | Table grid drawn once via `drawTableGrid()` — eliminates double-stroked borders |
| 8 | `src/app/app.config.ts` | Added `withFetch()` to `provideHttpClient()` — eliminates SSR XHR warnings |
| 9 | `tsconfig.app.json` | Added `"rootDir": "./src"` to resolve TypeScript compiler error |
