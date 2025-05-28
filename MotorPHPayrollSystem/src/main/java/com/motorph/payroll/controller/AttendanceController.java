package com.motorph.payroll.controller;

import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AttendanceController {
    private AttendanceService attendanceService;
    
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }
    
    public List<Attendance> getAttendanceByEmployeeId(int employeeId) {
        return attendanceService.getAttendanceByEmployeeId(employeeId);
    }
    
    public List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceService.getAttendanceByDateRange(employeeId, startDate, endDate);
    }
    
    public void addAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        attendanceService.addAttendance(employeeId, date, timeIn, timeOut);
    }
    
    public void updateAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        attendanceService.updateAttendance(employeeId, date, timeIn, timeOut);
    }
    
    public void deleteAttendance(int employeeId, LocalDate date) {
        attendanceService.deleteAttendance(employeeId, date);
    }
    
    public void clockIn(int employeeId) {
        attendanceService.clockIn(employeeId);
    }
    
    public void clockOut(int employeeId) {
        attendanceService.clockOut(employeeId);
    }
    
    public boolean hasCompletedAttendanceForToday(int employeeId) {
        return attendanceService.hasCompletedAttendanceForToday(employeeId);
    }
    
    public Attendance getTodayAttendance(int employeeId) {
        return attendanceService.getTodayAttendance(employeeId);
    }
    
    public boolean saveAttendance() {
        return attendanceService.saveAttendance();
    }
}