package com.motorph.payroll.service;

import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import java.time.LocalDate;

public interface PayrollService {
    PayrollSummary calculatePayroll(Employee employee, LocalDate startDate, LocalDate endDate);
    double calculateSSSDeduction(double basicSalary);
    double calculatePhilhealthDeduction(double basicSalary);
    double calculatePagibigDeduction(double basicSalary);
    double calculateWithholdingTax(double taxableIncome);
    boolean savePayslipToFile(PayrollSummary payslip, String fileName);
}