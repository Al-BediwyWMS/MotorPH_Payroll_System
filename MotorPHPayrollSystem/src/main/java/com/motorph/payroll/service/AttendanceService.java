package com.motorph.payroll.service;

import com.motorph.payroll.model.Attendance;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AttendanceService {
    List<Attendance> getAllAttendance();
    List<Attendance> getAttendanceByEmployeeId(int employeeId);
    List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate);
    void addAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut);
    void updateAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut);
    void deleteAttendance(int employeeId, LocalDate date);
    void clockIn(int employeeId);
    void clockOut(int employeeId);
    boolean hasCompletedAttendanceForToday(int employeeId);
    Attendance getTodayAttendance(int employeeId);
    boolean saveAttendance();
}