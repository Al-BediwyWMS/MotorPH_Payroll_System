package com.motorph.payroll.view.gui;

import com.motorph.payroll.model.Employee;

import javax.swing.*;
import java.awt.*;

public class EmployeeDetailsDialog extends JDialog {

    private final Employee employee;
    
    public EmployeeDetailsDialog(Window owner, Employee employee) {
        super(owner, "Employee Details", ModalityType.APPLICATION_MODAL);
        this.employee = employee;
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(600, 700);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Employee Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create details panel
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Personal Information"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        // Add employee details
        detailsPanel.add(new JLabel("Employee ID:"));
        detailsPanel.add(new JLabel(String.valueOf(employee.getEmployeeId())));
        
        detailsPanel.add(new JLabel("Last Name:"));
        detailsPanel.add(new JLabel(employee.getLastName()));
        
        detailsPanel.add(new JLabel("First Name:"));
        detailsPanel.add(new JLabel(employee.getFirstName()));
        
        detailsPanel.add(new JLabel("Birthday:"));
        detailsPanel.add(new JLabel(employee.getBirthday()));
        
        detailsPanel.add(new JLabel("Address:"));
        detailsPanel.add(new JLabel(employee.getAddress()));
        
        detailsPanel.add(new JLabel("Phone Number:"));
        detailsPanel.add(new JLabel(employee.getPhoneNumber()));
        
        // Create employment information panel
        JPanel employmentPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        employmentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Employment Information"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        employmentPanel.add(new JLabel("Position:"));
        employmentPanel.add(new JLabel(employee.getPosition()));
        
        employmentPanel.add(new JLabel("Status:"));
        employmentPanel.add(new JLabel(employee.getStatus()));
        
        employmentPanel.add(new JLabel("Supervisor:"));
        employmentPanel.add(new JLabel(employee.getSupervisor()));
        
        // Create government IDs panel
        JPanel governmentPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        governmentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Government IDs"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        governmentPanel.add(new JLabel("SSS Number:"));
        governmentPanel.add(new JLabel(employee.getSssNumber()));
        
        governmentPanel.add(new JLabel("PhilHealth Number:"));
        governmentPanel.add(new JLabel(employee.getPhilhealthNumber()));
        
        governmentPanel.add(new JLabel("TIN Number:"));
        governmentPanel.add(new JLabel(employee.getTinNumber()));
        
        governmentPanel.add(new JLabel("Pag-IBIG Number:"));
        governmentPanel.add(new JLabel(employee.getPagibigNumber()));
        
        // Create compensation panel
        JPanel compensationPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        compensationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Compensation"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        compensationPanel.add(new JLabel("Basic Salary:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getBasicSalary())));
        
        compensationPanel.add(new JLabel("Rice Subsidy:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getRiceSubsidy())));
        
        compensationPanel.add(new JLabel("Phone Allowance:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getPhoneAllowance())));
        
        compensationPanel.add(new JLabel("Clothing Allowance:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getClothingAllowance())));
        
        compensationPanel.add(new JLabel("Gross Semi-Monthly Rate:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getGrossSemiMonthlyRate())));
        
        compensationPanel.add(new JLabel("Hourly Rate:"));
        compensationPanel.add(new JLabel(String.format("PHP %,.2f", employee.getHourlyRate())));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        // Add panels to main panel
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(detailsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(employmentPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(governmentPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(compensationPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);
        
        // Add main panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Add scroll pane to dialog
        add(scrollPane);
    }
}