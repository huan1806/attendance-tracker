package com.example.attendance.controller;

import com.example.attendance.model.request.BulkAttendanceRequest;
import com.example.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("attendance")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/bulk")
    public ResponseEntity<?> saveBulkAttendance(@RequestBody BulkAttendanceRequest request) {
        try {
            attendanceService.saveBulkAttendance(request);
            return ResponseEntity.ok().body("Attendance data saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving data: " + e.getMessage());
        }
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Map<String, Object>> employees = attendanceService.getAllEmployees();
           return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting employees: " + e.getMessage());
        }
    }

    @GetMapping("/employee/{employeeId}/month/{year}/{month}")
    public ResponseEntity<?> getEmployeeAttendance(
            @PathVariable String employeeId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            Map<String, Object> data = attendanceService.getEmployeeAttendance(employeeId, year, month);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting data: " + e.getMessage());
        }
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<?> getAttendanceByMonth(
            @PathVariable int year,
            @PathVariable int month) {
        try {
            Map<String, Object> data = attendanceService.getAttendanceByMonth(year, month);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting data: " + e.getMessage());
        }
    }
} 