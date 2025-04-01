package com.example.attendance.model.request;

import lombok.Data;
import java.util.Map;

@Data
public class EmployeeAttendanceRequest {
    private String name;
    private String department;
    private double personalLeaveHours;
    private double unauthorizedLeaveHours;
    private double annualLeaveHours;
    private double totalWorkHours;
    private Map<String, DailyAttendanceRequest> attendanceDates;
} 