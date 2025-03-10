package com.motorph.payroll;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PayrollCalculator {
    private static final double SSS_RATE = 0.045;
    private static final double PHILHEALTH_RATE = 0.04;
    private static final double PAGIBIG_RATE = 0.02;
    private static final double OVERTIME_RATE = 1.25;
    private static final double REGULAR_HOURS_PER_DAY = 8.0;
    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate DEFAULT_END_DATE = LocalDate.of(2024, 1, 15);

    public static PayrollSummary calculatePayroll(Employee employee, List<Attendance> attendanceRecords) {
        // Filter attendance records for this employee
        List<Attendance> employeeAttendance = attendanceRecords.stream()
                .filter(a -> a.getEmployeeId() == employee.getEmployeeId())
                .collect(Collectors.toList());

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
        double daysInPeriod = 15.0; // Assuming semi-monthly period
        double dailyRate = employee.getBasicSalary() / 22.0; // Assuming 22 working days per month
        double regularPay = dailyRate * daysPresent;
        double overtimePay = overtimeHours * employee.getHourlyRate() * OVERTIME_RATE;
        
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

        // Get date range from attendance records or use defaults
        LocalDate startDate = employeeAttendance.stream()
                .map(Attendance::getDate)
                .min(LocalDate::compareTo)
                .orElse(DEFAULT_START_DATE);

        LocalDate endDate = employeeAttendance.stream()
                .map(Attendance::getDate)
                .max(LocalDate::compareTo)
                .orElse(DEFAULT_END_DATE);

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

    private static double calculateProportionalAllowance(double monthlyAllowance, int daysPresent) {
        return (monthlyAllowance / 22) * daysPresent; // Assuming 22 working days per month
    }

    private static double calculateSSSDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * SSS_RATE;
        return Math.min(monthlyContribution, 1125.0); // Maximum SSS contribution is 1,125
    }

    private static double calculatePhilhealthDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * PHILHEALTH_RATE;
        return Math.min(monthlyContribution, 1800.0); // Maximum Philhealth contribution is 1,800
    }

    private static double calculatePagibigDeduction(double basicSalary) {
        double monthlyContribution = basicSalary * PAGIBIG_RATE;
        return Math.min(monthlyContribution, 100.0); // Maximum Pag-IBIG contribution is 100
    }

    private static double calculateWithholdingTax(double taxableIncome) {
        // Simplified tax calculation based on Philippine tax brackets
        double annualizedIncome = taxableIncome * 24; // Assuming semi-monthly pay periods
        
        if (annualizedIncome <= 250000) {
            return 0;
        } else if (annualizedIncome <= 400000) {
            return ((annualizedIncome - 250000) * 0.20) / 24;
        } else if (annualizedIncome <= 800000) {
            return (30000 + (annualizedIncome - 400000) * 0.25) / 24;
        } else if (annualizedIncome <= 2000000) {
            return (130000 + (annualizedIncome - 800000) * 0.30) / 24;
        } else if (annualizedIncome <= 8000000) {
            return (490000 + (annualizedIncome - 2000000) * 0.32) / 24;
        } else {
            return (2410000 + (annualizedIncome - 8000000) * 0.35) / 24;
        }
    }
}