package com.motorph.payroll.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PayrollSummary {
    private Employee employee;
    private double totalHours;
    private double overtimeHours;
    private double grossPay;
    private double sssDeduction;
    private double philhealthDeduction;
    private double pagibigDeduction;
    private double totalDeductions;
    private double netPay;
    private List<Attendance> attendanceRecords;
    private LocalDate startDate;
    private LocalDate endDate;
    private double lateMinutes;
    private int daysPresent;

    public PayrollSummary(Employee employee, List<Attendance> attendanceRecords,
                         double totalHours, double overtimeHours, double grossPay, 
                         double sssDeduction, double philhealthDeduction,
                         double pagibigDeduction, double totalDeductions, double netPay,
                         LocalDate startDate, LocalDate endDate, double lateMinutes,
                         int daysPresent) {
        this.employee = employee;
        this.attendanceRecords = attendanceRecords;
        this.totalHours = totalHours;
        this.overtimeHours = overtimeHours;
        this.grossPay = grossPay;
        this.sssDeduction = sssDeduction;
        this.philhealthDeduction = philhealthDeduction;
        this.pagibigDeduction = pagibigDeduction;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lateMinutes = lateMinutes;
        this.daysPresent = daysPresent;
    }

    public void printPayslip() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        
        System.out.println("\n================ MOTORPH PAYROLL SYSTEM ================");
        System.out.println("                    PAYSLIP DETAIL                     ");
        System.out.println("====================================================");
        
        // Pay Period
        System.out.println("Pay Period: " + startDate.format(dateFormatter) + " - " + 
                          endDate.format(dateFormatter));
        
        // Employee Details
        System.out.println("\nEmployee Details:");
        System.out.println("ID: " + employee.getEmployeeId());
        System.out.println("Name: " + employee.getLastName() + ", " + employee.getFirstName());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Status: " + employee.getStatus());
        
        System.out.println("\nGovernment Numbers:");
        System.out.println("SSS: " + employee.getSssNumber());
        System.out.println("PhilHealth: " + employee.getPhilhealthNumber());
        System.out.println("TIN: " + employee.getTinNumber());
        System.out.println("Pag-IBIG: " + employee.getPagibigNumber());
        
        System.out.println("\nAttendance Summary:");
        System.out.printf("Days Present:       %d days\n", daysPresent);
        System.out.printf("Total Hours Worked: %.2f hours\n", totalHours);
        System.out.printf("Overtime Hours:     %.2f hours\n", overtimeHours);
        System.out.printf("Late Minutes:       %.2f minutes\n", lateMinutes);
        
        // Detailed Attendance Records
        System.out.println("\nDetailed Attendance Records:");
        System.out.println("Date          Time In   Time Out  Total Hours  OT Hours");
        System.out.println("----------------------------------------------------");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Attendance record : attendanceRecords) {
            System.out.printf("%-12s %-9s %-9s %-12.2f %-8.2f\n",
                record.getDate().format(dateFmt),
                record.getTimeIn() != null ? record.getTimeIn().format(timeFmt) : "N/A",
                record.getTimeOut() != null ? record.getTimeOut().format(timeFmt) : "N/A",
                record.getTotalHours(),
                record.getOvertimeHours());
        }
        
        System.out.println("\nEarnings:");
        System.out.printf("Basic Salary:       PHP %-,12.2f\n", employee.getBasicSalary());
        System.out.printf("Rice Subsidy:       PHP %-,12.2f\n", employee.getRiceSubsidy());
        System.out.printf("Phone Allowance:    PHP %-,12.2f\n", employee.getPhoneAllowance());
        System.out.printf("Clothing Allowance: PHP %-,12.2f\n", employee.getClothingAllowance());
        System.out.printf("Gross Pay:          PHP %-,12.2f\n", grossPay);
        
        System.out.println("\nDeductions:");
        System.out.printf("SSS:               PHP %-,12.2f\n", sssDeduction);
        System.out.printf("PhilHealth:        PHP %-,12.2f\n", philhealthDeduction);
        System.out.printf("Pag-IBIG:          PHP %-,12.2f\n", pagibigDeduction);
        System.out.printf("Total Deductions:  PHP %-,12.2f\n", totalDeductions);
        
        System.out.println("----------------------------------------------------");
        System.out.printf("NET PAY:           PHP %-,12.2f\n", netPay);
        System.out.println("====================================================");
        System.out.println("          This is a system-generated payslip.        \n");
    }

    public void displayAttendanceStatus() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        System.out.println("\n================ ATTENDANCE STATUS ================");
        System.out.printf("Employee ID: %d\n", employee.getEmployeeId());
        System.out.printf("Name: %s, %s\n", employee.getLastName(), employee.getFirstName());
        System.out.printf("Position: %s\n", employee.getPosition());
        System.out.printf("Status: %s\n", employee.getStatus());
        System.out.println("================================================");
        
        if (attendanceRecords.isEmpty()) {
            System.out.println("No attendance records found for this period.");
        } else {
            System.out.println("\nAttendance Summary:");
            System.out.printf("Days Present: %d days\n", daysPresent);
            System.out.printf("Total Hours: %.2f hours\n", totalHours);
            System.out.printf("Overtime Hours: %.2f hours\n", overtimeHours);
            System.out.printf("Late Minutes: %.2f minutes\n", lateMinutes);
            
            System.out.println("\nDetailed Records:");
            System.out.printf("%-12s | %-8s | %-8s | %-6s | %-8s | %-8s\n", 
                "Date", "Time In", "Time Out", "Hours", "OT Hours", "Status");
            System.out.println("------------------------------------------------------------");
            
            for (Attendance record : attendanceRecords) {
                String timeIn = record.getTimeIn() != null ? 
                    record.getTimeIn().format(timeFormatter) : "N/A";
                String timeOut = record.getTimeOut() != null ? 
                    record.getTimeOut().format(timeFormatter) : "N/A";
                String status = determineStatus(record);
                
                System.out.printf("%-12s | %-8s | %-8s | %6.2f | %8.2f | %-8s\n",
                    record.getDate().format(dateFormatter),
                    timeIn,
                    timeOut,
                    record.getTotalHours(),
                    record.getOvertimeHours(),
                    status);
            }
        }
        System.out.println("================================================\n");
    }

    public void displayAllAttendanceRecords() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        System.out.println("\n================ ALL ATTENDANCE RECORDS ================");
        System.out.printf("Employee ID: %d\n", employee.getEmployeeId());
        System.out.printf("Name: %s, %s\n", employee.getLastName(), employee.getFirstName());
        System.out.printf("Position: %s\n", employee.getPosition());
        System.out.println("======================================================");
        
        if (attendanceRecords.isEmpty()) {
            System.out.println("No attendance records found for this employee.");
        } else {
            System.out.printf("%-12s | %-8s | %-8s | %-6s | %-8s | %-10s | %-8s\n", 
                "Date", "Time In", "Time Out", "Hours", "OT Hours", "Late (min)", "Status");
            System.out.println("----------------------------------------------------------------------");
            
            attendanceRecords.sort((a1, a2) -> a1.getDate().compareTo(a2.getDate()));
            
            for (Attendance record : attendanceRecords) {
                String timeIn = record.getTimeIn() != null ? 
                    record.getTimeIn().format(timeFormatter) : "N/A";
                String timeOut = record.getTimeOut() != null ? 
                    record.getTimeOut().format(timeFormatter) : "N/A";
                String status = determineStatus(record);
                
                System.out.printf("%-12s | %-8s | %-8s | %6.2f | %8.2f | %10.0f | %-8s\n",
                    record.getDate().format(dateFormatter),
                    timeIn,
                    timeOut,
                    record.getTotalHours(),
                    record.getOvertimeHours(),
                    record.getLateMinutes(),
                    status);
            }
            
            // Print summary
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("TOTAL: %d records | %.2f hours | %.2f OT hours | %.0f late minutes\n", 
                attendanceRecords.size(), totalHours, overtimeHours, lateMinutes);
        }
        System.out.println("======================================================\n");
    }

    private String determineStatus(Attendance record) {
        if (record.getTimeIn() == null || record.getTimeOut() == null) {
            return "ABSENT";
        }
        if (record.getLateMinutes() > 0) {
            return "LATE";
        }
        return "PRESENT";
    }

    // Getters
    public Employee getEmployee() { return employee; }
    public List<Attendance> getAttendanceRecords() { return attendanceRecords; }
    public double getTotalHours() { return totalHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getGrossPay() { return grossPay; }
    public double getSssDeduction() { return sssDeduction; }
    public double getPhilhealthDeduction() { return philhealthDeduction; }
    public double getPagibigDeduction() { return pagibigDeduction; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetPay() { return netPay; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getLateMinutes() { return lateMinutes; }
    public int getDaysPresent() { return daysPresent; }
}