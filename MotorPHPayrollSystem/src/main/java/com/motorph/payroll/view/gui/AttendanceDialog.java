package com.motorph.payroll.view.gui;

import com.motorph.payroll.model.Attendance;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AttendanceDialog extends JDialog {

    private final int employeeId;
    private final Attendance existingAttendance;
    
    private JSpinner dateSpinner;
    private JTextField timeInField;
    private JTextField timeOutField;
    private boolean confirmed = false;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public AttendanceDialog(Window owner, String title, int employeeId, Attendance existingAttendance) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.employeeId = employeeId;
        this.existingAttendance = existingAttendance;
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(450, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Employee ID (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        formPanel.add(employeeIdLabel, gbc);
        
        gbc.gridx = 1;
        JTextField employeeIdField = new JTextField(String.valueOf(employeeId), 15);
        employeeIdField.setEditable(false);
        employeeIdField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(employeeIdField, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel dateLabel = new JLabel("Date:");
        formPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        dateSpinner = createDateSpinner(existingAttendance != null ? existingAttendance.getDate() : LocalDate.now());
        formPanel.add(dateSpinner, gbc);
        
        // Time In
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel timeInLabel = new JLabel("Time In (HH:MM):");
        formPanel.add(timeInLabel, gbc);
        
        gbc.gridx = 1;
        timeInField = new JTextField(15);
        timeInField.setToolTipText("Format: HH:MM (e.g., 08:30)");
        if (existingAttendance != null && existingAttendance.getTimeIn() != null) {
            timeInField.setText(existingAttendance.getTimeIn().format(TIME_FORMATTER));
        } else {
            timeInField.setText("08:00");
        }
        formPanel.add(timeInField, gbc);
        
        // Time Out
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel timeOutLabel = new JLabel("Time Out (HH:MM):");
        formPanel.add(timeOutLabel, gbc);
        
        gbc.gridx = 1;
        timeOutField = new JTextField(15);
        timeOutField.setToolTipText("Format: HH:MM (e.g., 17:00)");
        if (existingAttendance != null && existingAttendance.getTimeOut() != null) {
            timeOutField.setText(existingAttendance.getTimeOut().format(TIME_FORMATTER));
        } else {
            timeOutField.setText("17:00");
        }
        formPanel.add(timeOutField, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton okButton = new JButton("Save");
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        okButton.addActionListener(e -> {
            if (validateInputs()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to dialog
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(okButton);
    }
    
    private JSpinner createDateSpinner(LocalDate initialDate) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        
        // Configure date editor
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(150, 25));
        
        // Set initial value
        spinner.setValue(java.sql.Date.valueOf(initialDate));
        
        return spinner;
    }
    
    private boolean validateInputs() {
        // Validate time in
        String timeInStr = timeInField.getText().trim();
        if (timeInStr.isEmpty()) {
            showError("Time In is required.");
            return false;
        }
        
        try {
            LocalTime.parse(timeInStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            showError("Invalid Time In format. Please use HH:MM format (e.g. 08:30).");
            timeInField.requestFocus();
            return false;
        }
        
        // Validate time out
        String timeOutStr = timeOutField.getText().trim();
        if (!timeOutStr.isEmpty()) {
            try {
                LocalTime timeOut = LocalTime.parse(timeOutStr, TIME_FORMATTER);
                LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
                
                // Check if time in is before time out
                if (timeIn.isAfter(timeOut) || timeIn.equals(timeOut)) {
                    showError("Time In must be before Time Out.");
                    timeOutField.requestFocus();
                    return false;
                }
            } catch (DateTimeParseException e) {
                showError("Invalid Time Out format. Please use HH:MM format (e.g. 17:30).");
                timeOutField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public LocalDate getDate() {
        return ((java.sql.Date) dateSpinner.getValue()).toLocalDate();
    }
    
    public LocalTime getTimeIn() {
        String timeInStr = timeInField.getText().trim();
        if (timeInStr.isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeInStr, TIME_FORMATTER);
    }
    
    public LocalTime getTimeOut() {
        String timeOutStr = timeOutField.getText().trim();
        if (timeOutStr.isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeOutStr, TIME_FORMATTER);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}