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

    // Getters and setters

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