package com.example.attendance.repository;

import com.example.attendance.model.Attendance;
import com.example.attendance.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.employee = ?1 AND a.attendanceDate = ?2")
    void deleteByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.employee IN ?1 AND a.attendanceDate IN ?2")
    void deleteByEmployeesAndAttendanceDates(List<Employee> employees, List<LocalDate> attendanceDates);

    List<Attendance> findByEmployeeAndAttendanceDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
} 