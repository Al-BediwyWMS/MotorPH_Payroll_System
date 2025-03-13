package com.motorph.payroll.service;

import com.motorph.payroll.dao.AttendanceDao;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.util.AppConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PayrollServiceImpl implements PayrollService {
    private AttendanceDao attendanceDao;
    
    public PayrollServiceImpl(AttendanceDao attendanceDao) {
        this.attendanceDao = attendanceDao;
    }
    
    @Override
    public PayrollSummary calculatePayroll(Employee employee, LocalDate startDate, LocalDate endDate) {
        // Get attendance records for the specified period
        List<Attendance> employeeAttendance = attendanceDao.getAttendanceByDateRange(
            employee.getEmployeeId(), startDate, endDate);
        
        // Calculate total hours worked and overtime
        double totalHours = 0;
        double overtimeHours = 0;
        double lateMinutes = 0;
        int daysPresent = employeeAttendance.size();

        for (Attendance attendance : employeeAttendance) {
            totalHours += attendance.getTotalHours();
            overtimeHours += attendance.getOvertimeHours();
            lateMinutes += attendance.getLateMinutes();
        }

        // Calculate gross pay
        double dailyRate = employee.getBasicSalary() / AppConstants.WORKING_DAYS_PER_MONTH;
        double regularPay = dailyRate * daysPresent;
        double overtimePay = overtimeHours * employee.getHourlyRate() * AppConstants.OVERTIME_RATE;
        
        // Calculate allowances (prorated based on days present)
        double riceSubsidy = calculateProportionalAllowance(employee.getRiceSubsidy(), daysPresent);
        double phoneAllowance = calculateProportionalAllowance(employee.getPhoneAllowance(), daysPresent);
        double clothingAllowance = calculateProportionalAllowance(employee.getClothingAllowance(), daysPresent);
        
        double grossPay = regularPay + overtimePay + riceSubsidy + phoneAllowance + clothingAllowance;

        // Calculate deductions
        double sssDeduction = calculateSSSDeduction(employee.getBasicSalary());
        double philhealthDeduction = calculatePhilhealthDeduction(employee.getBasicSalary());
        double pagibigDeduction = calculatePagibigDeduction(employee.getBasicSalary());
        double withholdingTax = calculateWithholdingTax(grossPay - (sssDeduction + philhealthDeduction + pagibigDeduction));
        double totalDeductions = sssDeduction + philhealthDeduction + pagibigDeduction + withholdingTax;

        // Calculate net pay
        double netPay = grossPay - totalDeductions;
        
        return new PayrollSummary(
            employee,
            employeeAttendance,
            totalHours,
            overtimeHours,
            grossPay,
            sssDeduction,
            philhealthDeduction,
            pagibigDeduction,
            totalDeductions,
            netPay,
            startDate,
            endDate,
            lateMinutes,
            daysPresent
        );
    }
    
    private double calculateProportionalAllowance(double monthlyAllowance, int daysPresent) {
        return (monthlyAllowance / AppConstants.WORKING_DAYS_PER_MONTH) * daysPresent;
    }
    
    @Override
    public double calculateSSSDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * AppConstants.SSS_RATE;
        return Math.min(monthlyContribution, AppConstants.MAX_SSS_CONTRIBUTION);
    }
    
    @Override
    public double calculatePhilhealthDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * AppConstants.PHILHEALTH_RATE;
        return Math.min(monthlyContribution, AppConstants.MAX_PHILHEALTH_CONTRIBUTION);
    }
    
    @Override
    public double calculatePagibigDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * AppConstants.PAGIBIG_RATE;
        return Math.min(monthlyContribution, AppConstants.MAX_PAGIBIG_CONTRIBUTION);
    }
    
    @Override
    public double calculateWithholdingTax(double taxableIncome) {
        // Annualize the income
        double annualizedIncome = taxableIncome * AppConstants.PAY_PERIODS_PER_YEAR;
        double annualTax;
        
        if (annualizedIncome <= AppConstants.TAX_BRACKET_1) {
            return 0;
        } else if (annualizedIncome <= AppConstants.TAX_BRACKET_2) {
            annualTax = (annualizedIncome - AppConstants.TAX_BRACKET_1) * 0.20;
        } else if (annualizedIncome <= AppConstants.TAX_BRACKET_3) {
            annualTax = 30000 + (annualizedIncome - AppConstants.TAX_BRACKET_2) * 0.25;
        } else if (annualizedIncome <= AppConstants.TAX_BRACKET_4) {
            annualTax = 130000 + (annualizedIncome - AppConstants.TAX_BRACKET_3) * 0.30;
        } else if (annualizedIncome <= AppConstants.TAX_BRACKET_5) {
            annualTax = 490000 + (annualizedIncome - AppConstants.TAX_BRACKET_4) * 0.32;
        } else {
            annualTax = 2410000 + (annualizedIncome - AppConstants.TAX_BRACKET_5) * 0.35;
        }
        
        // Return the tax for this pay period
        return annualTax / AppConstants.PAY_PERIODS_PER_YEAR;
    }
    
    @Override
    public boolean savePayslipToFile(PayrollSummary payslip, String fileName) {
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + File.separator + fileName;
        
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            
            writer.println("\n================ MOTORPH PAYROLL SYSTEM ================");
            writer.println("                    PAYSLIP DETAIL                     ");
            writer.println("====================================================");
            
            // Pay Period
            writer.println("Pay Period: " + payslip.getStartDate().format(dateFormatter) + " - " + 
                          payslip.getEndDate().format(dateFormatter));
            
            // Employee Details
            writer.println("\nEmployee Details:");
            writer.println("ID: " + payslip.getEmployee().getEmployeeId());
            writer.println("Name: " + payslip.getEmployee().getLastName() + ", " + payslip.getEmployee().getFirstName());
            writer.println("Position: " + payslip.getEmployee().getPosition());
            writer.println("Status: " + payslip.getEmployee().getStatus());
            
            writer.println("\nGovernment Numbers:");
            writer.println("SSS: " + payslip.getEmployee().getSssNumber());
            writer.println("PhilHealth: " + payslip.getEmployee().getPhilhealthNumber());
            writer.println("TIN: " + payslip.getEmployee().getTinNumber());
            writer.println("Pag-IBIG: " + payslip.getEmployee().getPagibigNumber());
            
            writer.println("\nAttendance Summary:");
            writer.printf("Days Present:       %d days\n", payslip.getDaysPresent());
            writer.printf("Total Hours Worked: %.2f hours\n", payslip.getTotalHours());
            writer.printf("Overtime Hours:     %.2f hours\n", payslip.getOvertimeHours());
            writer.printf("Late Minutes:       %.2f minutes\n", payslip.getLateMinutes());
            
            writer.println("\nEarnings:");
            writer.printf("Basic Salary:       PHP %-,12.2f\n", payslip.getEmployee().getBasicSalary());
            writer.printf("Rice Subsidy:       PHP %-,12.2f\n", payslip.getEmployee().getRiceSubsidy());
            writer.printf("Phone Allowance:    PHP %-,12.2f\n", payslip.getEmployee().getPhoneAllowance());
            writer.printf("Clothing Allowance: PHP %-,12.2f\n", payslip.getEmployee().getClothingAllowance());
            writer.printf("Gross Pay:          PHP %-,12.2f\n", payslip.getGrossPay());
            
            writer.println("\nDeductions:");
            writer.printf("SSS:               PHP %-,12.2f\n", payslip.getSssDeduction());
            writer.printf("PhilHealth:        PHP %-,12.2f\n", payslip.getPhilhealthDeduction());
            writer.printf("Pag-IBIG:          PHP %-,12.2f\n", payslip.getPagibigDeduction());
            writer.printf("Total Deductions:  PHP %-,12.2f\n", payslip.getTotalDeductions());
            
            writer.println("----------------------------------------------------");
            writer.printf("NET PAY:           PHP %-,12.2f\n", payslip.getNetPay());
            writer.println("====================================================");
            writer.println("          This is a system-generated payslip.        \n");
            
            System.out.println("Payslip saved to: " + filePath);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Error saving payslip: " + e.getMessage());
            return false;
        }
    }
}