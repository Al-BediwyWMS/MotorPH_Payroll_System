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
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        employeeIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(employeeIdLabel, gbc);
        
        gbc.gridx = 1;
        employeeIdField = new JTextField(15);
        employeeIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(employeeIdField, gbc);
        
        // Pay Period
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(periodLabel, gbc);
        
        gbc.gridx = 1;
        String[] periodOptions = {
            "First Half (1-15) of Current Month", 
            "Second Half (16-30/31) of Current Month", 
            "Custom Date Range"
        };
        periodComboBox = new JComboBox<>(periodOptions);
        periodComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodComboBox.setPreferredSize(new Dimension(300, 25));
        formPanel.add(periodComboBox, gbc);
        
        // Generate button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton generateButton = new JButton("Generate Payslip");
        generateButton.setFont(new Font("Arial", Font.BOLD, 14));
        generateButton.setPreferredSize(new Dimension(200, 35));
        generateButton.addActionListener(e -> generatePayslip());
        formPanel.add(generateButton, gbc);
        
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
            
            // Display payroll summary in a new window
            displayPayrollSummaryWindow(currentPayrollSummary);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating payslip: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayPayrollSummaryWindow(PayrollSummary payrollSummary) {
        // Create a new window to display the payslip
        JFrame payslipWindow = new JFrame("Payslip - " + payrollSummary.getEmployee().getFullName());
        payslipWindow.setSize(800, 900);
        payslipWindow.setLocationRelativeTo(this);
        payslipWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create main panel with scrolling
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create payslip content panel
        JPanel payslipPanel = new JPanel();
        payslipPanel.setLayout(new BoxLayout(payslipPanel, BoxLayout.Y_AXIS));
        payslipPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        payslipPanel.setBackground(Color.WHITE);
        
        Employee employee = payrollSummary.getEmployee();
        
        // Company Header
        JLabel companyLabel = new JLabel("MOTORPH CORPORATION");
        companyLabel.setFont(new Font("Arial", Font.BOLD, 20));
        companyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel payslipTitleLabel = new JLabel("EMPLOYEE PAYSLIP");
        payslipTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        payslipTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Date range
        JLabel dateRangeLabel = new JLabel("Pay Period: " + 
            payrollSummary.getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " - " + 
            payrollSummary.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateRangeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateRangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Employee details section
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
        
        // Attendance summary section
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
        
        // Earnings section
        JPanel earningsPanel = new JPanel(new GridLayout(6, 2, 10, 5));
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
        
        earningsPanel.add(new JLabel("Overtime Pay:"));
        earningsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getOvertimeHours() * employee.getHourlyRate() * 1.25)));
        
        earningsPanel.add(new JLabel("Gross Pay:"));
        JLabel grossPayLabel = new JLabel(String.format("PHP %,.2f", payrollSummary.getGrossPay()));
        grossPayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        earningsPanel.add(grossPayLabel);
        
        // Deductions section
        JPanel deductionsPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        deductionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Deductions"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        deductionsPanel.add(new JLabel("SSS:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getSssDeduction())));
        
        deductionsPanel.add(new JLabel("PhilHealth:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getPhilhealthDeduction())));
        
        deductionsPanel.add(new JLabel("Pag-IBIG:"));
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", payrollSummary.getPagibigDeduction())));
        
        deductionsPanel.add(new JLabel("Withholding Tax:"));
        double withholdingTax = payrollSummary.getTotalDeductions() - 
                              (payrollSummary.getSssDeduction() + payrollSummary.getPhilhealthDeduction() + payrollSummary.getPagibigDeduction());
        deductionsPanel.add(new JLabel(String.format("PHP %,.2f", withholdingTax)));
        
        deductionsPanel.add(new JLabel("Total Deductions:"));
        JLabel totalDeductionsLabel = new JLabel(String.format("PHP %,.2f", payrollSummary.getTotalDeductions()));
        totalDeductionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        deductionsPanel.add(totalDeductionsLabel);
        
        // Net Pay section
        JPanel netPayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        netPayPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK));
        
        JLabel netPayLabel = new JLabel("NET PAY: " + String.format("PHP %,.2f", payrollSummary.getNetPay()));
        netPayLabel.setFont(new Font("Arial", Font.BOLD, 18));
        netPayLabel.setForeground(new Color(0, 100, 0));
        
        netPayPanel.add(netPayLabel);
        
        // Add all components to payslip panel
        payslipPanel.add(companyLabel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        payslipPanel.add(payslipTitleLabel);
        payslipPanel.add(Box.createRigidArea(new Dimension(0, 5)));
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton saveButton = new JButton("Save to File");
        saveButton.setFont(new Font("Arial", Font.PLAIN, 14));
        saveButton.addActionListener(e -> savePayslip());
        
        JButton printButton = new JButton("Print");
        printButton.setFont(new Font("Arial", Font.PLAIN, 14));
        printButton.addActionListener(e -> printPayslip());
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        closeButton.addActionListener(e -> payslipWindow.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        // Add payslip and button panels to a scroll pane
        JScrollPane scrollPane = new JScrollPane(payslipPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Add scroll pane and button panel to main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to window
        payslipWindow.add(mainPanel);
        
        // Display window
        payslipWindow.setVisible(true);
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
        // Clear result panel
        resultPanel.removeAll();
        resultPanel.revalidate();
        resultPanel.repaint();
        
        // Reset form fields
        employeeIdField.setText("");
        periodComboBox.setSelectedIndex(0);
        
        // Reset current payroll summary
        currentPayrollSummary = null;
    }
}