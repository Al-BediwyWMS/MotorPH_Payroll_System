package com.motorph.payroll.exception;

import java.time.LocalDate;

/**
 * Exception thrown when an attendance record is not found
 */
public class AttendanceNotFoundException extends RuntimeException {
    
    public AttendanceNotFoundException(String message) {
        super(message);
    }
    
    public AttendanceNotFoundException(int employeeId, LocalDate date) {
        super("Attendance record not found for employee ID " + employeeId + " on " + date);
    }
}