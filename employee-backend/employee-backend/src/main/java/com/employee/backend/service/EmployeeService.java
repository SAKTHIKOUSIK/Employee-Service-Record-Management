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