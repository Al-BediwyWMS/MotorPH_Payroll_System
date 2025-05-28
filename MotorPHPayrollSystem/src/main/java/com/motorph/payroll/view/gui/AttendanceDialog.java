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
        setSize(400, 250);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        JTextField employeeIdField = new JTextField(String.valueOf(employeeId));
        employeeIdField.setEditable(false);
        
        JLabel dateLabel = new JLabel("Date:");
        dateSpinner = createDateSpinner(existingAttendance != null ? existingAttendance.getDate() : LocalDate.now());
        
        JLabel timeInLabel = new JLabel("Time In (HH:MM):");
        timeInField = new JTextField(10);
        if (existingAttendance != null && existingAttendance.getTimeIn() != null) {
            timeInField.setText(existingAttendance.getTimeIn().format(TIME_FORMATTER));
        }
        
        JLabel timeOutLabel = new JLabel("Time Out (HH:MM):");
        timeOutField = new JTextField(10);
        if (existingAttendance != null && existingAttendance.getTimeOut() != null) {
            timeOutField.setText(existingAttendance.getTimeOut().format(TIME_FORMATTER));
        }
        
        formPanel.add(employeeIdLabel);
        formPanel.add(employeeIdField);
        formPanel.add(dateLabel);
        formPanel.add(dateSpinner);
        formPanel.add(timeInLabel);
        formPanel.add(timeInField);
        formPanel.add(timeOutLabel);
        formPanel.add(timeOutField);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
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
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to dialog
        add(mainPanel);
    }
    
    private JSpinner createDateSpinner(LocalDate initialDate) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        
        // Configure date editor
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy");
        spinner.setEditor(editor);
        
        // Set initial value
        spinner.setValue(java.sql.Date.valueOf(initialDate));
        
        return spinner;
    }
    
    private boolean validateInputs() {
        // Validate time in
        String timeInStr = timeInField.getText().trim();
        if (!timeInStr.isEmpty()) {
            try {
                LocalTime.parse(timeInStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid Time In format. Please use HH:MM format (e.g. 08:30).", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        // Validate time out
        String timeOutStr = timeOutField.getText().trim();
        if (!timeOutStr.isEmpty()) {
            try {
                LocalTime.parse(timeOutStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid Time Out format. Please use HH:MM format (e.g. 17:30).", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        // Check if time in is before time out
        if (!timeInStr.isEmpty() && !timeOutStr.isEmpty()) {
            LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime timeOut = LocalTime.parse(timeOutStr, TIME_FORMATTER);
            
            if (timeIn.isAfter(timeOut)) {
                JOptionPane.showMessageDialog(this, 
                    "Time In cannot be after Time Out.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return true;
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