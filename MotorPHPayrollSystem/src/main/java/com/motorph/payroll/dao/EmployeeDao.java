package com.motorph.payroll.dao;

import com.motorph.payroll.model.Employee;
import java.util.List;

public interface EmployeeDao {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(int id);
    void addEmployee(Employee employee);
    void updateEmployee(Employee employee);
    void deleteEmployee(int id);
    boolean saveEmployees();
}