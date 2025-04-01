package com.example.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "leave_summary")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LeaveSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    @Column(name = "month")
    private LocalDate month;

    @Column(name = "half_month")
    private Integer halfMonth; // 1: 1-20, 2: 21-end

    @Column(name = "personal_leave_hours")
    private Double personalLeaveHours;

    @Column(name = "unauthorized_leave_hours")
    private Double unauthorizedLeaveHours;

    @Column(name = "annual_leave_hours")
    private Double annualLeaveHours;

    @Column(name = "total_work_hours")
    private Double totalWorkHours;
}