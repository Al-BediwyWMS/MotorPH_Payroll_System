package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.dao.DaoFactory;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.EmployeeStatus;
import com.motorph.payroll.model.User;
import com.motorph.payroll.service.UserService;
import com.motorph.payroll.service.UserServiceImpl;
import com.motorph.payroll.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class EmployeeFormDialog extends JDialog {

    private final EmployeeController employeeController;
    private final Employee existingEmployee;
    private final UserService userService;
    private boolean isEditMode;
    
    // Form fields
    private JTextField idField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField birthdayField;
    private JTextArea addressField;
    private JTextField phoneNumberField;
    private JTextField sssNumberField;
    private JTextField philhealthNumberField;
    private JTextField tinNumberField;
    private JTextField pagibigNumberField;
    private JComboBox<String> statusComboBox;
    private JTextField positionField;
    private JTextField supervisorField;
    private JTextField basicSalaryField;
    private JTextField riceSubsidyField;
    private JTextField phoneAllowanceField;
    private JTextField clothingAllowanceField;
    
    public EmployeeFormDialog(Window owner, EmployeeController employeeController, Employee existingEmployee) {
        super(owner, existingEmployee == null ? "Add New Employee" : "Edit Employee", ModalityType.APPLICATION_MODAL);
        this.employeeController = employeeController;
        this.existingEmployee = existingEmployee;
        this.isEditMode = existingEmployee != null;
        this.userService = new UserServiceImpl(DaoFactory.createUserDao());
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(900, 700);
        setLocationRelativeTo(getOwner());
        
        // Create main panel with scrolling
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Employee" : "Add New Employee");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create personal information panel
        JPanel personalPanel = createPersonalInfoPanel();
        
        // Create employment information panel
        JPanel employmentPanel = createEmploymentInfoPanel();
        
        // Create government IDs panel
        JPanel governmentPanel = createGovernmentIDsPanel();
        
        // Create compensation panel
        JPanel compensationPanel = createCompensationPanel();
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(100, 35));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        saveButton.addActionListener(e -> {
            if (validateInputs()) {
                saveEmployee();
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to form panel
        formPanel.add(titlePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(personalPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(employmentPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(governmentPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(compensationPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(buttonPanel);
        
        // Add form panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Add scroll pane to main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add main panel to dialog
        add(mainPanel);
        
        // Initialize form with existing employee data if in edit mode
        if (isEditMode) {
            populateFormWithEmployeeData();
        } else {
            // Set default values for new employee
            setDefaultValues();
        }
    }
    
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Personal Information"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        idField = new JTextField(15);
        idField.setEditable(!isEditMode); // Only editable for new employees
        panel.add(idField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(15);
        panel.add(lastNameField, gbc);
        
        // First Name
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 3;
        firstNameField = new JTextField(15);
        panel.add(firstNameField, gbc);
        
        // Birthday
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Birthday (MM/DD/YYYY):"), gbc);
        gbc.gridx = 1;
        birthdayField = new JTextField(15);
        panel.add(birthdayField, gbc);
        
        // Phone Number
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 3;
        phoneNumberField = new JTextField(15);
        panel.add(phoneNumberField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        addressField = new JTextArea(3, 30);
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        addressField.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane addressScroll = new JScrollPane(addressField);
        addressScroll.setPreferredSize(new Dimension(300, 60));
        panel.add(addressScroll, gbc);
        
        return panel;
    }
    
    private JPanel createEmploymentInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Employment Information"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Status
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        String[] statusOptions = {
            EmployeeStatus.REGULAR.getDisplayName(),
            EmployeeStatus.PROBATIONARY.getDisplayName(),
            EmployeeStatus.CONTRACTUAL.getDisplayName(),
            EmployeeStatus.PART_TIME.getDisplayName()
        };
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(150, 25));
        panel.add(statusComboBox, gbc);
        
        // Position
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 3;
        positionField = new JTextField(20);
        panel.add(positionField, gbc);
        
        // Supervisor
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Supervisor:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        supervisorField = new JTextField(30);
        panel.add(supervisorField, gbc);
        
        return panel;
    }
    
    private JPanel createGovernmentIDsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Government IDs"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // SSS Number
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("SSS Number:"), gbc);
        gbc.gridx = 1;
        sssNumberField = new JTextField(15);
        panel.add(sssNumberField, gbc);
        
        // PhilHealth Number
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("PhilHealth Number:"), gbc);
        gbc.gridx = 3;
        philhealthNumberField = new JTextField(15);
        panel.add(philhealthNumberField, gbc);
        
        // TIN Number
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("TIN Number:"), gbc);
        gbc.gridx = 1;
        tinNumberField = new JTextField(15);
        panel.add(tinNumberField, gbc);
        
        // Pag-IBIG Number
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Pag-IBIG Number:"), gbc);
        gbc.gridx = 3;
        pagibigNumberField = new JTextField(15);
        panel.add(pagibigNumberField, gbc);
        
        return panel;
    }
    
    private JPanel createCompensationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Compensation"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Basic Salary
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        basicSalaryField = new JTextField(15);
        panel.add(basicSalaryField, gbc);
        
        // Rice Subsidy
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Rice Subsidy:"), gbc);
        gbc.gridx = 3;
        riceSubsidyField = new JTextField(15);
        panel.add(riceSubsidyField, gbc);
        
        // Phone Allowance
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Phone Allowance:"), gbc);
        gbc.gridx = 1;
        phoneAllowanceField = new JTextField(15);
        panel.add(phoneAllowanceField, gbc);
        
        // Clothing Allowance
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Clothing Allowance:"), gbc);
        gbc.gridx = 3;
        clothingAllowanceField = new JTextField(15);
        panel.add(clothingAllowanceField, gbc);
        
        return panel;
    }
    
    private void populateFormWithEmployeeData() {
        idField.setText(String.valueOf(existingEmployee.getEmployeeId()));
        lastNameField.setText(existingEmployee.getLastName());
        firstNameField.setText(existingEmployee.getFirstName());
        birthdayField.setText(existingEmployee.getBirthday());
        addressField.setText(existingEmployee.getAddress());
        phoneNumberField.setText(existingEmployee.getPhoneNumber());
        
        // Set status combo box
        for (int i = 0; i < statusComboBox.getItemCount(); i++) {
            if (statusComboBox.getItemAt(i).equals(existingEmployee.getStatus())) {
                statusComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        positionField.setText(existingEmployee.getPosition());
        supervisorField.setText(existingEmployee.getSupervisor());
        
        sssNumberField.setText(existingEmployee.getSssNumber());
        philhealthNumberField.setText(existingEmployee.getPhilhealthNumber());
        tinNumberField.setText(existingEmployee.getTinNumber());
        pagibigNumberField.setText(existingEmployee.getPagibigNumber());
        
        basicSalaryField.setText(String.format("%.2f", existingEmployee.getBasicSalary()));
        riceSubsidyField.setText(String.format("%.2f", existingEmployee.getRiceSubsidy()));
        phoneAllowanceField.setText(String.format("%.2f", existingEmployee.getPhoneAllowance()));
        clothingAllowanceField.setText(String.format("%.2f", existingEmployee.getClothingAllowance()));
    }
    
    private void setDefaultValues() {
        // Set default ID
        int newId = employeeController.generateNewEmployeeId();
        idField.setText(String.valueOf(newId));
        
        // Set default status to Regular
        statusComboBox.setSelectedIndex(0);
        
        // Set default values for numeric fields
        basicSalaryField.setText("25000.00");
        riceSubsidyField.setText("1500.00");
        phoneAllowanceField.setText("1000.00");
        clothingAllowanceField.setText("1000.00");
    }
    
    private boolean validateInputs() {
        // Validate required fields
        if (ValidationUtil.isEmpty(lastNameField.getText())) {
            showError("Last Name is required.");
            return false;
        }
        
        if (ValidationUtil.isEmpty(firstNameField.getText())) {
            showError("First Name is required.");
            return false;
        }
        
        if (ValidationUtil.isEmpty(birthdayField.getText())) {
            showError("Birthday is required.");
            return false;
        }
        
        if (ValidationUtil.isEmpty(positionField.getText())) {
            showError("Position is required.");
            return false;
        }
        
        // Validate phone number
        if (!ValidationUtil.isEmpty(phoneNumberField.getText()) && 
            !ValidationUtil.isValidPhoneNumber(phoneNumberField.getText())) {
            showError("Invalid phone number format.");
            return false;
        }
        
        // Validate SSS number
        if (!ValidationUtil.isEmpty(sssNumberField.getText()) && 
            !ValidationUtil.isValidSssNumber(sssNumberField.getText())) {
            showError("Invalid SSS number format.");
            return false;
        }
        
        // Validate numeric fields
        if (!ValidationUtil.isPositiveNumber(basicSalaryField.getText())) {
            showError("Basic Salary must be a positive number.");
            return false;
        }
        
        if (!ValidationUtil.isPositiveNumber(riceSubsidyField.getText())) {
            showError("Rice Subsidy must be a positive number.");
            return false;
        }
        
        if (!ValidationUtil.isPositiveNumber(phoneAllowanceField.getText())) {
            showError("Phone Allowance must be a positive number.");
            return false;
        }
        
        if (!ValidationUtil.isPositiveNumber(clothingAllowanceField.getText())) {
            showError("Clothing Allowance must be a positive number.");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void saveEmployee() {
        try {
            // Parse values
            int id = Integer.parseInt(idField.getText().trim());
            String lastName = lastNameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String birthday = birthdayField.getText().trim();
            String address = addressField.getText().trim();
            String phoneNumber = phoneNumberField.getText().trim();
            String sssNumber = sssNumberField.getText().trim();
            String philhealthNumber = philhealthNumberField.getText().trim();
            String tinNumber = tinNumberField.getText().trim();
            String pagibigNumber = pagibigNumberField.getText().trim();
            String status = Objects.requireNonNull(statusComboBox.getSelectedItem()).toString();
            String position = positionField.getText().trim();
            String supervisor = supervisorField.getText().trim();
            
            double basicSalary = Double.parseDouble(basicSalaryField.getText().trim());
            double riceSubsidy = Double.parseDouble(riceSubsidyField.getText().trim());
            double phoneAllowance = Double.parseDouble(phoneAllowanceField.getText().trim());
            double clothingAllowance = Double.parseDouble(clothingAllowanceField.getText().trim());
            
            // Calculate derived values
            double grossSemiMonthlyRate = basicSalary / 2;
            double hourlyRate = (basicSalary / 22) / 8;
            
            // Create employee object
            Employee employee = new Employee(
                id, lastName, firstName, birthday, address, phoneNumber, 
                sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, 
                position, supervisor, basicSalary, riceSubsidy, phoneAllowance, 
                clothingAllowance, grossSemiMonthlyRate, hourlyRate
            );
            
            // Save employee
            if (isEditMode) {
                employeeController.updateEmployee(employee);
                JOptionPane.showMessageDialog(this, 
                    "Employee updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                employeeController.addEmployee(employee);
                
                // Create user account for new employee
                User newUser = new User(String.valueOf(id), String.valueOf(id), "user");
                userService.addUser(newUser);
                userService.saveUsers();
                
                JOptionPane.showMessageDialog(this, 
                    "Employee added successfully!\n\n" +
                    "Login Credentials:\n" +
                    "Username: " + id + "\n" +
                    "Password: " + id, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Save changes to file
            employeeController.saveEmployees();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Error parsing numeric values. Please check your inputs.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving employee: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}