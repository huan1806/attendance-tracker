package com.example.attendance.model.request;

import lombok.Data;

@Data
public class DailyAttendanceRequest {
    private double workHours;
    private double overtimeHours;
} 