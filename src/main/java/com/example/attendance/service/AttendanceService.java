package com.example.attendance.service;

import com.example.attendance.model.Attendance;
import com.example.attendance.model.Employee;
import com.example.attendance.model.LeaveSummary;
import com.example.attendance.model.request.BulkAttendanceRequest;
import com.example.attendance.model.request.DailyAttendanceRequest;
import com.example.attendance.model.request.EmployeeAttendanceRequest;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    private final LeaveSummaryRepository leaveSummaryRepository;

    private final EmployeeRepository employeeRepository;

    private static List<Employee> getEmployees(BulkAttendanceRequest request) {
        List<Employee> employees = new ArrayList<>();

        for (Map.Entry<String, EmployeeAttendanceRequest> entry : request.getEmployees().entrySet()) {
            String employeeId = entry.getKey();
            EmployeeAttendanceRequest employeeData = entry.getValue();

            Employee employee = new Employee();
            employee.setEmployeeId(employeeId);
            employee.setEmployeeName(employeeData.getName());
            employee.setDepartment(employeeData.getDepartment());
            employees.add(employee);
        }
        return employees;
    }

    private static Map<String, Object> getEmployeeData(Employee employee, Map<String, Object> attendanceDates,
                                                       double personalLeaveHours, double unauthorizedLeaveHours,
                                                       double annualLeaveHours) {
        Map<String, Object> employeeData = new HashMap<>();
        employeeData.put("name", employee.getEmployeeName());
        employeeData.put("department", employee.getDepartment());
        employeeData.put("attendanceDates", attendanceDates);
        employeeData.put("personalLeaveHours", personalLeaveHours);
        employeeData.put("unauthorizedLeaveHours", unauthorizedLeaveHours);
        employeeData.put("annualLeaveHours", annualLeaveHours);
        return employeeData;
    }

    private static Map<String, Object> getAttendanceDates(List<Attendance> attendances) {
        Map<String, Object> attendanceDates = new HashMap<>();
        for (Attendance attendance : attendances) {
            String dateStr = attendance.getAttendanceDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Map<String, Object> dateData = new HashMap<>();
            dateData.put("workHours", attendance.getWorkHours());
            dateData.put("overtimeHours", attendance.getOvertimeHours());
            attendanceDates.put(dateStr, dateData);
        }
        return attendanceDates;
    }

    private static void addAttendances(List<Attendance> attendances,
                                       Map<String, DailyAttendanceRequest> attendanceDates, Employee employee) {
        for (Map.Entry<String, DailyAttendanceRequest> dateEntry : attendanceDates.entrySet()) {
            String dateStr = dateEntry.getKey();
            DailyAttendanceRequest dateData = dateEntry.getValue();

            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setAttendanceDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            attendance.setWorkHours(dateData.getWorkHours());
            attendance.setOvertimeHours(dateData.getOvertimeHours());

            attendances.add(attendance);
        }
    }

    private static LeaveSummary buildLeaveSummary(Employee employee, LocalDate firstDate, Integer halfMonth,
                                                  EmployeeAttendanceRequest employeeData) {
        LeaveSummary leaveSummary = new LeaveSummary();
        leaveSummary.setEmployee(employee);
        leaveSummary.setMonth(firstDate.withDayOfMonth(1));
        leaveSummary.setHalfMonth(halfMonth);
        leaveSummary.setPersonalLeaveHours(employeeData.getPersonalLeaveHours());
        leaveSummary.setUnauthorizedLeaveHours(employeeData.getUnauthorizedLeaveHours());
        leaveSummary.setAnnualLeaveHours(employeeData.getAnnualLeaveHours());
        leaveSummary.setTotalWorkHours(employeeData.getTotalWorkHours());
        return leaveSummary;
    }

    @Transactional
    public void saveBulkAttendance(BulkAttendanceRequest request) {
        List<Attendance> attendances = new ArrayList<>();
        List<LeaveSummary> leaveSummaries = new ArrayList<>();
        List<Employee> employees = getEmployees(request);
        employeeRepository.saveAll(employees);

        try {
            getLeaveSummariesAndAttendancesForSaving(request, leaveSummaries, attendances);
            attendanceRepository.saveAll(attendances);
            leaveSummaryRepository.saveAll(leaveSummaries);
        } catch (Exception e) {
            log.error("Error on saving bulk attendance", e);
            throw e;
        }
    }

    public Map<String, Object> getAttendanceByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Employee> employees = employeeRepository.findAll();
        Map<String, Object> result = new HashMap<>();

        for (Employee employee : employees) {
            List<Attendance> attendances = attendanceRepository.findByEmployeeAndAttendanceDateBetween(
                    employee, startDate, endDate);

            List<LeaveSummary> leaveSummaries = leaveSummaryRepository.findByEmployeeAndMonth(
                    employee, startDate);

            double personalLeaveHours = 0;
            double unauthorizedLeaveHours = 0;
            double annualLeaveHours = 0;
            for (LeaveSummary summary : leaveSummaries) {
                personalLeaveHours += summary.getPersonalLeaveHours();
                unauthorizedLeaveHours += summary.getUnauthorizedLeaveHours();
                annualLeaveHours += summary.getAnnualLeaveHours();
            }

            Map<String, Object> attendanceDates = getAttendanceDates(attendances);
            Map<String, Object> employeeData = getEmployeeData(employee, attendanceDates, personalLeaveHours,
                    unauthorizedLeaveHours, annualLeaveHours);

            result.put(employee.getEmployeeId(), employeeData);
        }

        return result;
    }

    public List<Map<String, Object>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employee -> {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("id", employee.getEmployeeId());
                    employeeData.put("name", employee.getEmployeeName());
                    employeeData.put("department", employee.getDepartment());
                    return employeeData;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getEmployeeAttendance(String employeeId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Attendance> attendances = attendanceRepository.findByEmployeeAndAttendanceDateBetween(
                employee, startDate, endDate);

        List<LeaveSummary> leaveSummaries = leaveSummaryRepository.findByEmployeeAndMonth(
                employee, startDate);

        Map<String, Object> attendanceDates = getAttendanceDates(attendances);

        double personalLeaveHours = 0;
        double unauthorizedLeaveHours = 0;
        double annualLeaveHours = 0;
        double totalWorkHours = 0;

        Map<Integer, List<LeaveSummary>> summariesByHalfMonth = leaveSummaries.stream()
                .collect(Collectors.groupingBy(LeaveSummary::getHalfMonth));

        if (summariesByHalfMonth.containsKey(1)) {
            for (LeaveSummary summary : summariesByHalfMonth.get(1)) {
                personalLeaveHours += summary.getPersonalLeaveHours();
                unauthorizedLeaveHours += summary.getUnauthorizedLeaveHours();
                annualLeaveHours += summary.getAnnualLeaveHours();
                totalWorkHours += summary.getTotalWorkHours();
            }
        }

        if (summariesByHalfMonth.containsKey(2)) {
            for (LeaveSummary summary : summariesByHalfMonth.get(2)) {
                personalLeaveHours += summary.getPersonalLeaveHours();
                unauthorizedLeaveHours += summary.getUnauthorizedLeaveHours();
                annualLeaveHours += summary.getAnnualLeaveHours();
                totalWorkHours += summary.getTotalWorkHours();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("name", employee.getEmployeeName());
        result.put("department", employee.getDepartment());
        result.put("attendanceDates", attendanceDates);
        result.put("personalLeaveHours", personalLeaveHours);
        result.put("unauthorizedLeaveHours", unauthorizedLeaveHours);
        result.put("annualLeaveHours", annualLeaveHours);
        result.put("totalWorkHours", totalWorkHours);

        return result;
    }

    private void getLeaveSummariesAndAttendancesForSaving(BulkAttendanceRequest request,
                                                          List<LeaveSummary> leaveSummaries,
                                                          List<Attendance> attendances) {
        List<Employee> employeesToDelete = new ArrayList<>();
        List<LocalDate> attendanceDatesToDelete = new ArrayList<>();
        List<LocalDate> monthsToDelete = new ArrayList<>();
        List<Integer> halfMonthsToDelete = new ArrayList<>();

        LocalDate firstDate = request.getFirstDate();
        Integer halfMonth = firstDate.getDayOfMonth() <= 15 ? 1 : 2;

        for (Map.Entry<String, EmployeeAttendanceRequest> entry : request.getEmployees().entrySet()) {
            String employeeId = entry.getKey();
            EmployeeAttendanceRequest employeeData = entry.getValue();
            Map<String, DailyAttendanceRequest> attendanceDates = employeeData.getAttendanceDates();

            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) {
                continue;
            }

            employeesToDelete.add(employee);
            monthsToDelete.add(firstDate.withDayOfMonth(1));
            halfMonthsToDelete.add(halfMonth);

            attendanceDatesToDelete.addAll(
                    attendanceDates.keySet().stream()
                            .map(dateStr -> LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                            .toList()
            );

            leaveSummaries.add(buildLeaveSummary(employee, firstDate, halfMonth, employeeData));
            addAttendances(attendances, attendanceDates, employee);
        }

        deleteLeaveSummariesAndAttendances(employeesToDelete, monthsToDelete, halfMonthsToDelete,
                attendanceDatesToDelete);
    }

    private void deleteLeaveSummariesAndAttendances(List<Employee> employeesToDelete, List<LocalDate> monthsToDelete,
                                                    List<Integer> halfMonthsToDelete,
                                                    List<LocalDate> attendanceDatesToDelete) {
        if (!employeesToDelete.isEmpty()) {
            leaveSummaryRepository.deleteByEmployeesAndMonthsAndHalfMonths(
                    employeesToDelete, monthsToDelete, halfMonthsToDelete);
            attendanceRepository.deleteByEmployeesAndAttendanceDates(
                    employeesToDelete, attendanceDatesToDelete);
        }
    }
}