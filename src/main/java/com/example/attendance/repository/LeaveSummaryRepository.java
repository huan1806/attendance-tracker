package com.example.attendance.repository;

import com.example.attendance.model.Employee;
import com.example.attendance.model.LeaveSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveSummaryRepository extends JpaRepository<LeaveSummary, Long> {
    @Modifying
    @Query("DELETE FROM LeaveSummary l WHERE l.employee = ?1 AND l.month = ?2 AND l.halfMonth = ?3")
    void deleteByEmployeeAndMonthAndHalfMonth(Employee employee, LocalDate month, Integer halfMonth);

    @Modifying
    @Query("DELETE FROM LeaveSummary l WHERE l.employee IN ?1 AND l.month IN ?2 AND l.halfMonth IN ?3")
    void deleteByEmployeesAndMonthsAndHalfMonths(List<Employee> employees, List<LocalDate> months, List<Integer> halfMonths);

    List<LeaveSummary> findByEmployeeAndMonth(Employee employee, LocalDate month);
} 