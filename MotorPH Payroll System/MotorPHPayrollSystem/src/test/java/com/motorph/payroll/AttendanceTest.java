package com.motorph.payroll;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceTest {
    
    @Test
    public void testNormalWorkDay() {
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime timeIn = LocalTime.of(8, 0);  // 8:00 AM
        LocalTime timeOut = LocalTime.of(17, 0); // 5:00 PM
        
        Attendance attendance = new Attendance(1, date, timeIn, timeOut);
        
        assertEquals(8.0, attendance.getTotalHours(), 0.01); // 9 hours - 1 hour lunch
        assertEquals(0.0, attendance.getOvertimeHours(), 0.01);
        assertEquals(0.0, attendance.getLateMinutes(), 0.01);
    }
    
    @Test
    public void testLateArrival() {
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime timeIn = LocalTime.of(8, 30);  // 8:30 AM (30 minutes late)
        LocalTime timeOut = LocalTime.of(17, 0); // 5:00 PM
        
        Attendance attendance = new Attendance(1, date, timeIn, timeOut);
        
        assertEquals(7.5, attendance.getTotalHours(), 0.01); // 8.5 hours - 1 hour lunch
        assertEquals(0.0, attendance.getOvertimeHours(), 0.01);
        assertEquals(30.0, attendance.getLateMinutes(), 0.01);
    }
    
    @Test
    public void testOvertime() {
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime timeIn = LocalTime.of(8, 0);   // 8:00 AM
        LocalTime timeOut = LocalTime.of(19, 0); // 7:00 PM (2 hours overtime)
        
        Attendance attendance = new Attendance(1, date, timeIn, timeOut);
        
        assertEquals(10.0, attendance.getTotalHours(), 0.01); // 11 hours - 1 hour lunch
        assertEquals(2.0, attendance.getOvertimeHours(), 0.01);
        assertEquals(0.0, attendance.getLateMinutes(), 0.01);
    }
    
    @Test
    public void testNullTimeOut() {
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime timeIn = LocalTime.of(8, 0);   // 8:00 AM
        
        Attendance attendance = new Attendance(1, date, timeIn, null);
        
        assertEquals(0.0, attendance.getTotalHours(), 0.01);
        assertEquals(0.0, attendance.getOvertimeHours(), 0.01);
        assertEquals(0.0, attendance.getLateMinutes(), 0.01);
    }
}