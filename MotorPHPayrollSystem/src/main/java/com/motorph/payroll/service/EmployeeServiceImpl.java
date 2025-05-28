package com.motorph.payroll.service;

import com.motorph.payroll.dao.EmployeeDao;
import com.motorph.payroll.exception.EmployeeNotFoundException;
import com.motorph.payroll.model.Employee;
import java.util.List;

public class EmployeeServiceImpl implements EmployeeService {
    private EmployeeDao employeeDao;
    private static final String EMPLOYEE_USERNAME_PREFIX = "Employee";
    
    public EmployeeServiceImpl(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }
    
    @Override
    public List<Employee> getAllEmployees() {
        return employeeDao.getAllEmployees();
    }
    
    @Override
    public Employee getEmployeeById(int id) {
        Employee employee = employeeDao.getEmployeeById(id);
        if (employee == null) {
            throw new EmployeeNotFoundException(id);
        }
        return employee;
    }
    
    @Override
    public void addEmployee(Employee employee) {
        employeeDao.addEmployee(employee);
    }
    
    @Override
    public void updateEmployee(Employee employee) {
        employeeDao.updateEmployee(employee);
    }
    
    @Override
    public void deleteEmployee(int id) {
        employeeDao.deleteEmployee(id);
    }
    
    @Override
    public Employee login(String username, String password) {
        // Admin login is handled separately in the application layer
        
        // Check for employee login
        if (username.startsWith(EMPLOYEE_USERNAME_PREFIX)) {
            try {
                String idStr = username.substring(EMPLOYEE_USERNAME_PREFIX.length());
                int empId = Integer.parseInt(idStr);
                
                // Employee password should match their ID
                if (password.equals(idStr)) {
                    // Find the employee
                    Employee employee = employeeDao.getEmployeeById(empId);
                    return employee; // Will be null if not found
                }
            } catch (NumberFormatException e) {
                // Invalid employee username format
                return null;
            }
        }
        
        return null; // Login failed
    }
    
    @Override
    public boolean saveEmployees() {
        return employeeDao.saveEmployees();
    }
    
    @Override
    public int generateNewEmployeeId() {
        // Find the highest current ID and add 1
        return employeeDao.getAllEmployees().stream()
            .mapToInt(Employee::getEmployeeId)
            .max()
            .orElse(0) + 1;
    }
}