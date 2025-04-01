package com.example.attendance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "work_hours")
    private Double workHours;

    @Column(name = "overtime_hours")
    private Double overtimeHours;
}