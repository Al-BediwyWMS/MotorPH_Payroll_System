package com.motorph.payroll.view.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DateRangeDialog extends JDialog {

    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;
    private boolean confirmed = false;
    
    public DateRangeDialog(Window owner) {
        super(owner, "Select Date Range", ModalityType.APPLICATION_MODAL);
        
        // Create date spinners
        startDateSpinner = createDateSpinner(LocalDate.now().withDayOfMonth(1));
        endDateSpinner = createDateSpinner(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 200);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel startDateLabel = new JLabel("Start Date:");
        JLabel endDateLabel = new JLabel("End Date:");
        
        formPanel.add(startDateLabel);
        formPanel.add(startDateSpinner);
        formPanel.add(endDateLabel);
        formPanel.add(endDateSpinner);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            if (validateDates()) {
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
    
    private boolean validateDates() {
        LocalDate startDate = getStartDate();
        LocalDate endDate = getEndDate();
        
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, 
                "Start date cannot be after end date.", 
                "Invalid Date Range", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public LocalDate getStartDate() {
        return ((java.sql.Date) startDateSpinner.getValue()).toLocalDate();
    }
    
    public LocalDate getEndDate() {
        return ((java.sql.Date) endDateSpinner.getValue()).toLocalDate();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}