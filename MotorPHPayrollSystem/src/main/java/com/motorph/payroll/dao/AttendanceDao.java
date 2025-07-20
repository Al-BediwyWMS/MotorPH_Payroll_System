package com.motorph.payroll.dao;

import com.motorph.payroll.model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceDao {
    List<Attendance> getAllAttendance();
    List<Attendance> getAttendanceByEmployeeId(int employeeId);
    List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate);
    void addAttendance(Attendance attendance);
    void updateAttendance(Attendance attendance);
    void deleteAttendance(int employeeId, LocalDate date);
    boolean saveAttendance();
}