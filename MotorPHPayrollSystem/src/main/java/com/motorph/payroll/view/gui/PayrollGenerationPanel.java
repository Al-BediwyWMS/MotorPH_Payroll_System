package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.controller.PayrollController;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.util.AppConstants;
import com.motorph.payroll.util.DateTimeUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayrollGenerationPanel extends JPanel {

    private final PayrollController payrollController;
    private final EmployeeController employeeController;
    
    private JTextField employeeIdField;
    private JComboBox<String> periodComboBox;
    private JPanel resultPanel;
    private PayrollSummary currentPayrollSummary;
    
    public PayrollGenerationPanel(PayrollController payrollController, EmployeeController employeeController) {
        this.payrollController = payrollController;
        this.employeeController = employeeController;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel titleLabel = new JLabel("Generate Payslip");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        titlePanel.add(titleLabel);
        
        // Create form panel
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        employeeIdField = new JTextField(10);
        
        JLabel periodLabel = new JLabel("Pay Period:");
        String[] periodOptions = {
            "First Half (1-15) of Current Month", 
            "Second Half (16-30/31) of Current Month", 
            "Custom Date Range"
        };
        periodComboBox = new JComboBox<>(periodOptions);
        
        JButton generateButton = new JButton("Generate Payslip");
        generateButton.addActionListener(e -> generatePayslip());
        
        formPanel.add(employeeIdLabel);
        formPanel.add(employeeIdField);
        formPanel.add(periodLabel);
        formPanel.add(periodComboBox);
        formPanel.add(generateButton);
        
        // Create result panel (initially empty)
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
    }
    
    private void generatePayslip() {
        // Clear result panel
        resultPanel.removeAll();
        
        // Validate employee ID
        String employeeIdText = employeeIdField.getText().trim();
        if (employeeIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter an Employee ID", 
                "Missing Input", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int employeeId;
        try {
            employeeId = Integer.parseInt(employeeIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid Employee ID format. Please enter a valid number.", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get employee
        Employee employee;
        try {
            employee = employeeController.getEmployeeById(employeeId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Employee not found with ID: " + employeeId, 
                "Employee Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine date range
        String selectedPeriod = (String) periodComboBox.getSelectedItem();
        LocalDate startDate;
        LocalDate endDate;
        
        if (selectedPeriod.equals("First Half (1-15) of Current Month")) {
            startDate = DateTimeUtil.getFirstHalfStart();
            endDate = DateTimeUtil.getFirstHalfEnd();
        } else if (selectedPeriod.equals("Second Half (16-30/31) of Current Month")) {
            startDate = DateTimeUtil.getSecondHalfStart();
            endDate = DateTimeUtil.getSecondHalfEnd();
        } else {
            // Show date picker dialog for custom range
            DateRangeDialog dialog = new DateRangeDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                startDate = dialog.getStartDate();
                endDate = dialog.getEndDate();
            } else {
                return; // User cancelled
            }
        }
        
        // Generate payroll summary
        try {
            currentPayrollSummary = payrollController.calculatePayroll(employee, startDate, endDate);
            
            if (currentPayrollSummary.getAttendanceRecords().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No attendance records found for the specified period.", 
                    "No Data", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Display payroll summary
            displayPayrollSummary(currentPayrollSummary);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating payslip: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayPayrollSummary(PayrollSummary payrollSummary) {
        // Create payslip view panel
        JPanel payslipPanel = new JPanel();
        payslipPanel.setLayout(new BoxLayout(payslipPanel, BoxLayout.Y_AXIS));
        payslipPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 0, 0, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20))));
        
        Employee employee = payrollSummary.getEmployee();
        
        // Title
        JLabel payslipTitleLabel = new JLabel("MOTORPH PAYROLL SYSTEM - PAYSLIP");
        payslipTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        payslipTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Date range
        JLabel dateRangeLabel = new JLabel("Pay Period: " + 
            payrollSummary.getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " - " + 
            payrollSummary.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateRangeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateRangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Employee details
        JPanel employeeDetailsPanel = new JPanel(new GridLayout(8, 2, 10, 5));
        employeeDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Employee Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        employeeDetailsPanel.add(new JLabel("Employee ID:"));
        employeeDetailsPanel.add(new JLabel(String.valueOf(employee.getEmployeeId())));
        
        employeeDetailsPanel.add(new JLabel("Name:"));
        employeeDetailsPanel.add(new JLabel(employee.getLastName() + ", " + employee.getFirstName()));
        
        employeeDetailsPanel.add(new JLabel("Position:"));
        employeeDetailsPanel.add(new JLabel(employee.getPosition()));
        
        employeeDetailsPanel.add(new JLabel("Status:"));
        employeeDetailsPanel.add(new JLabel(employee.getStatus()));
        
        employeeDetailsPanel.add(new JLabel("SSS No:"));
        employeeDetailsPanel.add(new JLabel(employee.getSssNumber()));
        
        employeeDetailsPanel.add(new JLabel("PhilHealth No:"));
        employeeDetailsPanel.add(new JLabel(employee.getPhilhealthNumber()));
        
        employeeDetailsPanel.add(new JLabel("TIN:"));
        employeeDetailsPanel.add(new JLabel(employee.getTinNumber()));
        
        employeeDetailsPanel.add(new JLabel("Pag-IBIG No:"));
        employeeDetailsPanel.add(new JLabel(employee.getPagibigNumber()));
        
        // Attendance summary
        JPanel attendancePanel = new JPanel(new GridLayout(4, 2, 10, 5));
        attendancePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Attendance Summary"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        attendancePanel.add(new JLabel("Days Present:"));
        attendancePanel.add(new JLabel(String.valueOf(payrollSummary.getDaysPresent()) + " days"));
        
        attendancePanel.add(new JLabel("Total Hours Worked:"));
        attendancePanel.add(new JLabel(String.format("%.2f hours", payrollSummary.getTotalHours())));
        
        attendancePanel.add(new JLabel("Overtime Hours:"));
        attendancePanel.add(new JLabel(String.format("%.2f hours", payrollSummary.getOvertimeHours())));
        
        attendancePanel.add(new JLabel("Late Minutes:"));
        attendancePanel.add(new JLabel(String.format("%.0f minutes", payrollSummary.getLateMinutes())));
        
        // Earnings
        JPanel earningsPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        earningsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Earnings"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        earningsPanel.add(new JLabel("Basic Salary:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", employee.getBasicSalary())));
        
        earningsPanel.add(new JLabel("Rice Subsidy:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", employee.getRiceSubsidy())));
        
        earningsPanel.add(new JLabel("Phone Allowance:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", employee.getPhoneAllowance())));
        
        earningsPanel.add(new JLabel("Clothing Allowance:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", employee.getClothingAllowance())));
        
        earningsPanel.add(new JLabel("Gross Pay:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getGrossPay())));
        
        // Deductions
        JPanel deductionsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        deductionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Deductions"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        deductionsPanel.add(new JLabel("SSS:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getSssDeduction())));
        
        deductionsPanel.add(new JLabel("PhilHealth:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getPhilhealthDeduction())));
        
        deductionsPanel.add(new JLabel("Pag-IBIG:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getPagibigDeduction())));
        
        deductionsPanel.add(new JLabel("Total Deductions:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getTotalDeductions())));
        
        // Net Pay
        JPanel netPayPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        netPayPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        
        JLabel netPayLabel = new JLabel("NET PAY:");
        netPayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel netPayValueLabel = new JLabel(String.format("PHP %,.2f", payrollSummary.getNetPay()));
        netPayValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        netPayPanel.add(netPayLabel);
        netPayPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        netPayPanel.add(netPayValueLabel);
        
        // Add components to payslip panel
        payslipPanel.add(payslipTitleLabel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        payslipPanel.add(dateRangeLabel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        payslipPanel.add(employeeDetailsPanel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        payslipPanel.add(attendancePanel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        payslipPanel.add(earningsPanel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        payslipPanel.add(deductionsPanel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        payslipPanel.add(netPayPanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save Payslip");
        saveButton.addActionListener(e -> savePayslip());
        
        JButton printButton = new JButton("Print Payslip");
        printButton.addActionListener(e -> printPayslip());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(printButton);
        
        // Add payslip and button panels to a scroll pane
        JScrollPane scrollPane = new JScrollPane(payslipPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Add scroll pane and button panel to result panel
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Update UI
        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    private void savePayslip() {
        if (currentPayrollSummary == null) {
            JOptionPane.showMessageDialog(this, 
                "No payslip to save. Please generate a payslip first.", 
                "No Data", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String fileName = String.format(AppConstants.PAYSLIP_FILENAME_PATTERN, 
            currentPayrollSummary.getEmployee().getEmployeeId(),
            currentPayrollSummary.getStartDate().format(DateTimeFormatter.ofPattern("MMddyyyy")),
            currentPayrollSummary.getEndDate().format(DateTimeFormatter.ofPattern("MMddyyyy")));
            
        if (payrollController.savePayslipToFile(currentPayrollSummary, fileName)) {
            JOptionPane.showMessageDialog(this, 
                "Payslip saved to file: " + fileName, 
                "Save Successful", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error saving payslip. Please try again.", 
                "Save Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printPayslip() {
        if (currentPayrollSummary == null) {
            JOptionPane.showMessageDialog(this, 
                "No payslip to print. Please generate a payslip first.", 
                "No Data", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Print functionality not implemented yet.", 
            "Print", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    void refreshData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}