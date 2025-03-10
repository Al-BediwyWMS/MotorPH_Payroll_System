package com.motorph.payroll;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmployeeTest {
    
    @Test
    public void testEmployeeCreation() {
        Employee employee = new Employee(1, "Doe", "John", "01/01/1990", 
            "123 Main St", "1234567890", "SSS123", "PH123", "TIN123", "PI123",
            "Regular", "Developer", "Manager1", 50000.0, 1500.0, 1000.0, 1000.0, 25000.0, 284.09);
            
        assertEquals(1, employee.getEmployeeId());
        assertEquals("Doe", employee.getLastName());
        assertEquals("John", employee.getFirstName());
        assertEquals("Developer", employee.getPosition());
        assertEquals(50000.0, employee.getBasicSalary());
    }
    
    @Test
    public void testSetters() {
        Employee employee = new Employee(1, "Doe", "John", "01/01/1990", 
            "123 Main St", "1234567890", "SSS123", "PH123", "TIN123", "PI123",
            "Regular", "Developer", "Manager1", 50000.0, 1500.0, 1000.0, 1000.0, 25000.0, 284.09);
            
        employee.setPosition("Senior Developer");
        employee.setBasicSalary(60000.0);
        
        assertEquals("Senior Developer", employee.getPosition());
        assertEquals(60000.0, employee.getBasicSalary());
    }
    
    @Test
    public void testGetFullName() {
        Employee employee = new Employee(1, "Doe", "John", "01/01/1990", 
            "123 Main St", "1234567890", "SSS123", "PH123", "TIN123", "PI123",
            "Regular", "Developer", "Manager1", 50000.0, 1500.0, 1000.0, 1000.0, 25000.0, 284.09);
            
        assertEquals("John Doe", employee.getFullName());
    }
}