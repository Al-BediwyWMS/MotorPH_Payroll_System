package com.motorph.payroll.controller;

import com.motorph.payroll.model.Employee;
import com.motorph.payroll.service.EmployeeService;
import java.util.List;

public class EmployeeController {
    private EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }
    
    public Employee getEmployeeById(int id) {
        return employeeService.getEmployeeById(id);
    }
    
    public void addEmployee(Employee employee) {
        employeeService.addEmployee(employee);
    }
    
    public void updateEmployee(Employee employee) {
        employeeService.updateEmployee(employee);
    }
    
    public void deleteEmployee(int id) {
        employeeService.deleteEmployee(id);
    }
    
    public Employee login(String username, String password) {
        return employeeService.login(username, password);
    }
    
    public boolean saveEmployees() {
        return employeeService.saveEmployees();
    }
    
    public int generateNewEmployeeId() {
        return employeeService.generateNewEmployeeId();
    }
}