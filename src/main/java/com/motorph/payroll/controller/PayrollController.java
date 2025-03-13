package com.motorph.payroll.controller;

import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.service.EmployeeService;
import com.motorph.payroll.service.PayrollService;
import java.time.LocalDate;

public class PayrollController {
    private PayrollService payrollService;
    private EmployeeService employeeService;
    
    public PayrollController(PayrollService payrollService, EmployeeService employeeService) {
        this.payrollService = payrollService;
        this.employeeService = employeeService;
    }
    
    public PayrollSummary calculatePayroll(int employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        return payrollService.calculatePayroll(employee, startDate, endDate);
    }
    
    public PayrollSummary calculatePayroll(Employee employee, LocalDate startDate, LocalDate endDate) {
        return payrollService.calculatePayroll(employee, startDate, endDate);
    }
    
    public boolean savePayslipToFile(PayrollSummary payslip, String fileName) {
        return payrollService.savePayslipToFile(payslip, fileName);
    }
}