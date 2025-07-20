package com.motorph.payroll.model.test;

import com.motorph.payroll.model.Attendance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceTest {
    
    @Test
    public void testAttendanceConstructor() {
        // Create attendance record
        int employeeId = 1;
        LocalDate date = LocalDate.of(2024, 3, 1);
        LocalTime timeIn = LocalTime.of(8, 0);
        LocalTime timeOut = LocalTime.of(17, 0);
        
        Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut);
        
        // Verify fields
        assertEquals(employeeId, attendance.getEmployeeId());
        assertEquals(date, attendance.getDate());
        assertEquals(timeIn, attendance.getTimeIn());
        assertEquals(timeOut, attendance.getTimeOut());
    }
    
    @Test
    public void testCalculateWorkingHours() {
        // Create attendance with 8 hour shift (includes 1 hour lunch)
        Attendance attendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        // Should be 8 hours (9 hours with 1 hour lunch break subtracted)
        assertEquals(8.0, attendance.getTotalHours());
        assertEquals(0.0, attendance.getOvertimeHours());
        assertEquals(0.0, attendance.getLateMinutes());
    }
    
    @Test
    public void testCalculateOvertimeHours() {
        // Create attendance with overtime
        Attendance attendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            LocalTime.of(19, 0)  // 2 hours overtime
        );
        
        assertEquals(10.0, attendance.getTotalHours());
        assertEquals(2.0, attendance.getOvertimeHours());
    }
    
    @Test
    public void testCalculateLateMinutes() {
        // Create attendance with late arrival
        Attendance attendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 30),  // 30 minutes late
            LocalTime.of(17, 0)
        );
        
        assertEquals(7.5, attendance.getTotalHours());
        assertEquals(0.0, attendance.getOvertimeHours());
        assertEquals(30.0, attendance.getLateMinutes());
    }
    
    @Test
    public void testGetFormattedTimes() {
        Attendance attendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        assertEquals("08:00", attendance.getFormattedTimeIn());
        assertEquals("17:00", attendance.getFormattedTimeOut());
    }
    
    @Test
    public void testIsComplete() {
        // Complete attendance
        Attendance complete = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        // Incomplete attendance (no time out)
        Attendance incomplete = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            null
        );
        
        assertTrue(complete.isComplete());
        assertFalse(incomplete.isComplete());
    }
    
    @Test
    public void testStatus() {
        // Present
        Attendance present = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        // Late
        Attendance late = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            LocalTime.of(8, 15),
            LocalTime.of(17, 0)
        );
        
        // Absent
        Attendance absent = new Attendance(
            1,
            LocalDate.of(2024, 3, 1),
            null,
            null
        );
        
        assertEquals("PRESENT", present.getStatus());
        assertEquals("LATE", late.getStatus());
        assertEquals("ABSENT", absent.getStatus());
    }
}