package com.motorph.payroll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FileHandlerTest {
    
    @TempDir
    Path tempDir;
    
    private File employeeFile;
    private File attendanceFile;
    private List<Employee> sampleEmployees;
    private List<Attendance> sampleAttendance;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create temporary test files
        employeeFile = tempDir.resolve("test_employees.csv").toFile();
        attendanceFile = tempDir.resolve("test_attendance.csv").toFile();
        
        // Sample employee data
        sampleEmployees = new ArrayList<>();
        sampleEmployees.add(new Employee(
            1, "Doe", "John", "01/01/1990", 
            "123 Main St", "1234567890", "SSS123", "PH123", "TIN123", "PI123",
            "Regular", "Developer", "Manager1", 50000.0, 1500.0, 1000.0, 1000.0, 25000.0, 284.09
        ));
        
        // Sample attendance data
        sampleAttendance = new ArrayList<>();
        sampleAttendance.add(new Attendance(
            1, LocalDate.of(2024, 3, 1), 
            LocalTime.of(8, 0), LocalTime.of(17, 0)
        ));
        
        // Write sample data to test files
        createSampleEmployeeFile();
        createSampleAttendanceFile();
    }
    
    private void createSampleEmployeeFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(employeeFile)) {
            writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate");
            writer.println("1,Doe,John,01/01/1990,\"123 Main St\",1234567890,SSS123,PH123,TIN123,PI123,Regular,Developer,Manager1,50000.0,1500.0,1000.0,1000.0,25000.0,284.09");
        }
    }
    
    private void createSampleAttendanceFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(attendanceFile)) {
            writer.println("Employee #,Last Name,First Name,Date,Time In,Time Out");
            writer.println("1,,,03/01/2024,08:00,17:00");
        }
    }
    
    @Test
    public void testReadEmployees() {
        List<Employee> employees = FileHandler.readEmployees(employeeFile.getAbsolutePath());
        
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals(1, employees.get(0).getEmployeeId());
        assertEquals("Doe", employees.get(0).getLastName());
        assertEquals("John", employees.get(0).getFirstName());
        assertEquals(50000.0, employees.get(0).getBasicSalary());
    }
    
    @Test
    public void testReadAttendance() {
        List<Attendance> attendance = FileHandler.readAttendance(attendanceFile.getAbsolutePath());
        
        assertNotNull(attendance);
        assertEquals(1, attendance.size());
        assertEquals(1, attendance.get(0).getEmployeeId());
        assertEquals(LocalDate.of(2024, 3, 1), attendance.get(0).getDate());
        assertEquals(LocalTime.of(8, 0), attendance.get(0).getTimeIn());
        assertEquals(LocalTime.of(17, 0), attendance.get(0).getTimeOut());
    }
    
    @Test
    public void testWriteAndReadEmployees() {
        // Write sample data
        FileHandler.writeEmployeesCsv(sampleEmployees, employeeFile.getAbsolutePath());
        
        // Read it back
        List<Employee> employees = FileHandler.readEmployees(employeeFile.getAbsolutePath());
        
        // Verify
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals(sampleEmployees.get(0).getEmployeeId(), employees.get(0).getEmployeeId());
        assertEquals(sampleEmployees.get(0).getLastName(), employees.get(0).getLastName());
    }
    
    @Test
    public void testWriteAndReadAttendance() {
        // Write sample data
        FileHandler.writeAttendanceCsv(sampleAttendance, attendanceFile.getAbsolutePath());
        
        // Read it back
        List<Attendance> attendance = FileHandler.readAttendance(attendanceFile.getAbsolutePath());
        
        // Verify
        assertNotNull(attendance);
        assertEquals(1, attendance.size());
        assertEquals(sampleAttendance.get(0).getEmployeeId(), attendance.get(0).getEmployeeId());
        assertEquals(sampleAttendance.get(0).getDate(), attendance.get(0).getDate());
    }
}