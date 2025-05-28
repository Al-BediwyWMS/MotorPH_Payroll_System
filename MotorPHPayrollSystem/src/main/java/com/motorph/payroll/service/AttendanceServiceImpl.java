package com.motorph.payroll.service;

import com.motorph.payroll.dao.AttendanceDao;
import com.motorph.payroll.model.Attendance;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AttendanceServiceImpl implements AttendanceService {
    private AttendanceDao attendanceDao;
    
    public AttendanceServiceImpl(AttendanceDao attendanceDao) {
        this.attendanceDao = attendanceDao;
    }
    
    @Override
    public List<Attendance> getAllAttendance() {
        return attendanceDao.getAllAttendance();
    }
    
    @Override
    public List<Attendance> getAttendanceByEmployeeId(int employeeId) {
        return attendanceDao.getAttendanceByEmployeeId(employeeId);
    }
    
    @Override
    public List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceDao.getAttendanceByDateRange(employeeId, startDate, endDate);
    }
    
    @Override
    public void addAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut);
        attendanceDao.addAttendance(attendance);
    }
    
    @Override
    public void updateAttendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut);
        attendanceDao.updateAttendance(attendance);
    }
    
    @Override
    public void deleteAttendance(int employeeId, LocalDate date) {
        attendanceDao.deleteAttendance(employeeId, date);
    }
    
    @Override
    public void clockIn(int employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // Check if employee already has an attendance record for today
        Attendance todayRecord = getTodayAttendance(employeeId);
        
        if (todayRecord == null) {
            // No record for today, create new record with time in
            addAttendance(employeeId, today, currentTime, null);
        } else if (todayRecord.getTimeOut() == null) {
            // Record exists but no time out - do nothing, can't clock in twice
        } else {
            // Complete record already exists - do nothing
        }
    }
    
    @Override
    public void clockOut(int employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // Check if employee already has an attendance record for today
        Attendance todayRecord = getTodayAttendance(employeeId);
        
        if (todayRecord == null) {
            // No record for today - can't clock out without clocking in
        } else if (todayRecord.getTimeOut() == null) {
            // Record exists but no time out, update record with time out
            updateAttendance(employeeId, today, todayRecord.getTimeIn(), currentTime);
        } else {
            // Complete record already exists - do nothing
        }
    }
    
    @Override
    public boolean hasCompletedAttendanceForToday(int employeeId) {
        Attendance todayRecord = getTodayAttendance(employeeId);
        return todayRecord != null && todayRecord.getTimeOut() != null;
    }
    
    @Override
    public Attendance getTodayAttendance(int employeeId) {
        LocalDate today = LocalDate.now();
        return getAttendanceByDateRange(employeeId, today, today)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean saveAttendance() {
        return attendanceDao.saveAttendance();
    }
}