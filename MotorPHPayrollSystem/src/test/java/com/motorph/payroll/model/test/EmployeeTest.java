package com.motorph.payroll.model.test;

import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.EmployeeStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {
    
    @Test
    public void testEmployeeConstructor() {
        // Create an employee
        Employee employee = new Employee(
            1, "Smith", "John", "01/01/1990", "123 Test St", "1234567890",
            "12-3456789-1", "12-345678901-2", "123-456-789-123", "1234-5678-9012",
            "Regular", "Software Engineer", "Jane Doe", 
            30000.0, 1500.0, 1000.0, 800.0, 15000.0, 170.45
        );
        
        // Verify all fields
        assertEquals(1, employee.getEmployeeId());
        assertEquals("Smith", employee.getLastName());
        assertEquals("John", employee.getFirstName());
        assertEquals("01/01/1990", employee.getBirthday());
        assertEquals("123 Test St", employee.getAddress());
        assertEquals("1234567890", employee.getPhoneNumber());
        assertEquals("12-3456789-1", employee.getSssNumber());
        assertEquals("12-345678901-2", employee.getPhilhealthNumber());
        assertEquals("123-456-789-123", employee.getTinNumber());
        assertEquals("1234-5678-9012", employee.getPagibigNumber());
        assertEquals("Regular", employee.getStatus());
        assertEquals(EmployeeStatus.REGULAR, employee.getEmployeeStatus());
        assertEquals("Software Engineer", employee.getPosition());
        assertEquals("Jane Doe", employee.getSupervisor());
        assertEquals(30000.0, employee.getBasicSalary());
        assertEquals(1500.0, employee.getRiceSubsidy());
        assertEquals(1000.0, employee.getPhoneAllowance());
        assertEquals(800.0, employee.getClothingAllowance());
        assertEquals(15000.0, employee.getGrossSemiMonthlyRate());
        assertEquals(170.45, employee.getHourlyRate());
    }
    
    @Test
    public void testEmployeeSetters() {
        // Create a basic employee
        Employee employee = new Employee(
            1, "Smith", "John", "01/01/1990", "123 Test St", "1234567890",
            "12-3456789-1", "12-345678901-2", "123-456-789-123", "1234-5678-9012",
            "Regular", "Software Engineer", "Jane Doe", 
            30000.0, 1500.0, 1000.0, 800.0, 15000.0, 170.45
        );
        
        // Test setters
        employee.setEmployeeId(2);
        employee.setLastName("Johnson");
        employee.setFirstName("Robert");
        employee.setBirthday("02/02/1992");
        employee.setAddress("456 Main St");
        employee.setPhoneNumber("9876543210");
        employee.setSssNumber("98-7654321-0");
        employee.setPhilhealthNumber("98-765432109-8");
        employee.setTinNumber("987-654-321-987");
        employee.setPagibigNumber("9876-5432-1098");
        employee.setStatus("Probationary");
        employee.setPosition("Senior Developer");
        employee.setSupervisor("John Manager");
        employee.setBasicSalary(35000.0);
        employee.setRiceSubsidy(1600.0);
        employee.setPhoneAllowance(1200.0);
        employee.setClothingAllowance(900.0);
        employee.setGrossSemiMonthlyRate(17500.0);
        employee.setHourlyRate(198.86);
        
        // Verify all fields were updated
        assertEquals(2, employee.getEmployeeId());
        assertEquals("Johnson", employee.getLastName());
        assertEquals("Robert", employee.getFirstName());
        assertEquals("02/02/1992", employee.getBirthday());
        assertEquals("456 Main St", employee.getAddress());
        assertEquals("9876543210", employee.getPhoneNumber());
        assertEquals("98-7654321-0", employee.getSssNumber());
        assertEquals("98-765432109-8", employee.getPhilhealthNumber());
        assertEquals("987-654-321-987", employee.getTinNumber());
        assertEquals("9876-5432-1098", employee.getPagibigNumber());
        assertEquals("Probationary", employee.getStatus());
        assertEquals(EmployeeStatus.PROBATIONARY, employee.getEmployeeStatus());
        assertEquals("Senior Developer", employee.getPosition());
        assertEquals("John Manager", employee.getSupervisor());
        assertEquals(35000.0, employee.getBasicSalary());
        assertEquals(1600.0, employee.getRiceSubsidy());
        assertEquals(1200.0, employee.getPhoneAllowance());
        assertEquals(900.0, employee.getClothingAllowance());
        assertEquals(17500.0, employee.getGrossSemiMonthlyRate());
        assertEquals(198.86, employee.getHourlyRate());
    }
    
    @Test
    public void testGetFullName() {
        Employee employee = new Employee(
            1, "Smith", "John", "01/01/1990", "123 Test St", "1234567890",
            "12-3456789-1", "12-345678901-2", "123-456-789-123", "1234-5678-9012",
            "Regular", "Software Engineer", "Jane Doe", 
            30000.0, 1500.0, 1000.0, 800.0, 15000.0, 170.45
        );
        
        assertEquals("John Smith", employee.getFullName());
    }
    
    @Test
    public void testToString() {
        Employee employee = new Employee(
            1, "Smith", "John", "01/01/1990", "123 Test St", "1234567890",
            "12-3456789-1", "12-345678901-2", "123-456-789-123", "1234-5678-9012",
            "Regular", "Software Engineer", "Jane Doe", 
            30000.0, 1500.0, 1000.0, 800.0, 15000.0, 170.45
        );
        
        String toString = employee.toString();
        
        assertTrue(toString.contains("Employee ID: 1"));
        assertTrue(toString.contains("Name: Smith, John"));
        assertTrue(toString.contains("Position: Software Engineer"));
        assertTrue(toString.contains("Status: Regular"));
    }
}