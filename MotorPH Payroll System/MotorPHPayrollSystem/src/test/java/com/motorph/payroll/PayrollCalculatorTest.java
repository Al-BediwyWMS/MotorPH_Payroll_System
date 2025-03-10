package com.motorph.payroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PayrollCalculatorTest {
    
    private Employee testEmployee;
    private List<Attendance> testAttendance;
    
    @BeforeEach
    public void setUp() {
        // Create a test employee
        testEmployee = new Employee(
            1, "Doe", "John", "01/01/1990", 
            "123 Main St", "1234567890", "SSS123", "PH123", "TIN123", "PI123",
            "Regular", "Developer", "Manager1", 50000.0, 1500.0, 1000.0, 1000.0, 25000.0, 284.09
        );
        
        // Create sample attendance records
        testAttendance = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        
        // Add 10 days of attendance (10 working days)
        for (int i = 0; i < 10; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalTime timeIn = LocalTime.of(8, 0);
            LocalTime timeOut = LocalTime.of(17, 0);
            
            Attendance attendance = new Attendance(1, date, timeIn, timeOut);
            testAttendance.add(attendance);
        }
    }
    
    @Test
    public void testCalculatePayrollWithRegularAttendance() {
        PayrollSummary summary = PayrollCalculator.calculatePayroll(testEmployee, testAttendance);
        
        // Verify basic calculations
        assertEquals(testEmployee, summary.getEmployee());
        assertEquals(10, summary.getDaysPresent());
        assertEquals(80.0, summary.getTotalHours(), 0.01); // 10 days * 8 hours
        assertEquals(0.0, summary.getOvertimeHours(), 0.01);
        
        // Check if gross pay is calculated correctly
        // For 10 days, employee should get roughly 10/22 of monthly salary plus allowances
        double expectedBasicPay = (50000.0 / 22) * 10;
        double expectedAllowances = (1500.0 + 1000.0 + 1000.0) / 22 * 10; // prorated allowances
        double expectedGrossPay = expectedBasicPay + expectedAllowances;
        
        assertEquals(expectedGrossPay, summary.getGrossPay(), 1.0); // Allow some rounding difference
    }
    
    @Test
    public void testCalculatePayrollWithOvertimeAndLates() {
        // Get current attendance for the first day
        Attendance oldAttendance = testAttendance.get(0);
        
        // Create a new Attendance with overtime (2 hours after regular end time)
        Attendance overtimeAttendance = new Attendance(
            oldAttendance.getEmployeeId(),
            oldAttendance.getDate(),
            oldAttendance.getTimeIn(),
            LocalTime.of(19, 0) // 7:00 PM (2 hours overtime)
        );
        
        // Replace the first attendance record
        testAttendance.set(0, overtimeAttendance);
        
        // Add a late arrival to the second day
        testAttendance.set(1, new Attendance(1, 
            testAttendance.get(1).getDate(), 
            LocalTime.of(8, 30), // 30 min late
            LocalTime.of(17, 0)));
        
        PayrollSummary summary = PayrollCalculator.calculatePayroll(testEmployee, testAttendance);
        
        // Check that overtime and lates are calculated
        assertTrue(summary.getOvertimeHours() > 0);
        assertTrue(summary.getLateMinutes() > 0);
    }
    
    @Test
    public void testDeductions() {
        PayrollSummary summary = PayrollCalculator.calculatePayroll(testEmployee, testAttendance);
        
        // Verify that deductions are calculated
        assertTrue(summary.getSssDeduction() > 0);
        assertTrue(summary.getPhilhealthDeduction() > 0);
        assertTrue(summary.getPagibigDeduction() > 0);
        assertTrue(summary.getTotalDeductions() > 0);
        
        // Net pay should be less than gross pay
        assertTrue(summary.getNetPay() < summary.getGrossPay());
    }
}