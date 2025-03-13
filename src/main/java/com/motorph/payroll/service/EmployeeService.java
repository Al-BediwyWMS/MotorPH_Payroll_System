package com.motorph.payroll.service;

import com.motorph.payroll.model.Employee;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(int id);
    void addEmployee(Employee employee);
    void updateEmployee(Employee employee);
    void deleteEmployee(int id);
    Employee login(String username, String password);
    boolean saveEmployees();
    int generateNewEmployeeId();
}