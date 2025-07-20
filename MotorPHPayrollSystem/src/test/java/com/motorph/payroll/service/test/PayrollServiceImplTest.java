package com.motorph.payroll.service.test;

import com.motorph.payroll.dao.AttendanceDao;
import com.motorph.payroll.dao.FileAttendanceDao;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.service.PayrollService;
import com.motorph.payroll.service.PayrollServiceImpl;
import com.motorph.payroll.util.test.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class PayrollServiceImplTest {
    
    private PayrollService payrollService;
    private String testFilePath;
    private final String TEMP_FILE = "temp-attendance.csv";
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary copy of the test file
        testFilePath = TestUtil.createTempTestFile(TestUtil.TEST_ATTENDANCE_CSV, TEMP_FILE);
        AttendanceDao attendanceDao = new FileAttendanceDao(testFilePath);
        payrollService = new PayrollServiceImpl(attendanceDao);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up the temporary file
        TestUtil.cleanupTempTestFile(TEMP_FILE);
    }
    
    @Test
    public void testCalculateSSSDeduction() {
        // Test with salary below maximum contribution
        double sssDeduction = payrollService.calculateSSSDeduction(20000);
        assertEquals(900.0, sssDeduction); // 20000 * 0.045 = 900
        
        // Test with salary above maximum contribution
        double maxSssDeduction = payrollService.calculateSSSDeduction(30000);
        assertEquals(1125.0, maxSssDeduction); // Maximum SSS contribution
    }
    
    @Test
    public void testCalculatePhilhealthDeduction() {
        // Test with salary below maximum contribution
        double philhealthDeduction = payrollService.calculatePhilhealthDeduction(20000);
        assertEquals(800.0, philhealthDeduction); // 20000 * 0.04 = 800
        
        // Test with salary above maximum contribution
        double maxPhilhealthDeduction = payrollService.calculatePhilhealthDeduction(50000);
        assertEquals(1800.0, maxPhilhealthDeduction); // Maximum Philhealth contribution
    }
    
    @Test
    public void testCalculatePagibigDeduction() {
        // Test with salary below maximum contribution
        double pagibigDeduction = payrollService.calculatePagibigDeduction(4000);
        assertEquals(80.0, pagibigDeduction); // 4000 * 0.02 = 80
        
        // Test with salary above maximum contribution
        double maxPagibigDeduction = payrollService.calculatePagibigDeduction(10000);
        assertEquals(100.0, maxPagibigDeduction); // Maximum Pag-IBIG contribution
    }
    
    @Test
    public void testCalculateWithholdingTax() {
        // Test taxable income in different tax brackets
        
        // First bracket (0%)
        double tax1 = payrollService.calculateWithholdingTax(10000);
        assertEquals(0.0, tax1);
        
        // Second bracket (20%)
        double tax2 = payrollService.calculateWithholdingTax(15000);
        assertTrue(tax2 > 0);
        
        // Higher bracket
        double tax3 = payrollService.calculateWithholdingTax(40000);
        assertTrue(tax3 > tax2);
    }
    
    @Test
    public void testCalculatePayroll() {
        // Create a test employee
        Employee employee = new Employee(
            1, "Test", "Employee", "01/01/1990", "Test Address", "1234567890",
            "99-9999999-9", "99-999999999-9", "999-999-999-999", "9999-9999-9999",
            "Regular", "Test Position", "Test Supervisor", 
            20000, 1500, 1000, 1000, 10000, 113.64
        );
        
        // Calculate payroll for March 2024
        LocalDate startDate = LocalDate.of(2024, 3, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        
        PayrollSummary payrollSummary = payrollService.calculatePayroll(employee, startDate, endDate);
        
        // Verify payroll calculations
        assertNotNull(payrollSummary);
        assertEquals(employee, payrollSummary.getEmployee());
        assertEquals(2, payrollSummary.getAttendanceRecords().size());
        assertEquals(startDate, payrollSummary.getStartDate());
        assertEquals(endDate, payrollSummary.getEndDate());
        
        // Verify hours calculation
        assertTrue(payrollSummary.getTotalHours() > 0);
        assertTrue(payrollSummary.getGrossPay() > 0);
        assertTrue(payrollSummary.getNetPay() > 0);
        assertTrue(payrollSummary.getNetPay() < payrollSummary.getGrossPay());
    }
}