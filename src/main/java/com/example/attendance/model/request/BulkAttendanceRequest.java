package com.example.attendance.model.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class BulkAttendanceRequest {
    private LocalDate firstDate;
    private Map<String, EmployeeAttendanceRequest> employees;
} 