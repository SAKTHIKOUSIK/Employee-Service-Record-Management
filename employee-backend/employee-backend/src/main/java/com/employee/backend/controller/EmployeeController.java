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
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable Long id) {

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
        ApiResponse<String> response = new ApiResponse<>("Employee deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<byte[]> generateEmployeeReport(@PathVariable Long id) {

        byte[] pdfBytes = employeeReportService.generateEmployeeReport(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "employee_report_" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}