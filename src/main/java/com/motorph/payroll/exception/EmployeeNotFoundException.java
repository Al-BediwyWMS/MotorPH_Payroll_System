package com.motorph.payroll.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
    
    public EmployeeNotFoundException(int employeeId) {
        super("Employee not found with ID: " + employeeId);
    }
}