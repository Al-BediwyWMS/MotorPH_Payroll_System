package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.AttendanceController;
import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceManagementPanel extends JPanel {

    private final AttendanceController attendanceController;
    private final EmployeeController employeeController;
    
    private JTextField employeeIdField;
    private JComboBox<String> periodComboBox;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JPanel buttonPanel;
    
    private int currentEmployeeId = -1;
    private LocalDate startDate;
    private LocalDate endDate;
    
    public AttendanceManagementPanel(AttendanceController attendanceController, EmployeeController employeeController) {
        this.attendanceController = attendanceController;
        this.employeeController = employeeController;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        titlePanel.add(titleLabel);
        
        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        employeeIdField = new JTextField(10);
        
        JLabel periodLabel = new JLabel("Period:");
        String[] periodOptions = {
            "Current Month", 
            "Previous Month",
            "Custom Date Range"
        };
        periodComboBox = new JComboBox<>(periodOptions);
        
        JButton viewButton = new JButton("View Attendance");
        viewButton.addActionListener(e -> viewAttendance());
        
        controlPanel.add(employeeIdLabel);
        controlPanel.add(employeeIdField);
        controlPanel.add(periodLabel);
        controlPanel.add(periodComboBox);
        controlPanel.add(viewButton);
        
        // Create table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create table
        createAttendanceTable();
        JScrollPane tableScrollPane = new JScrollPane(attendanceTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Create button panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Attendance");
        JButton editButton = new JButton("Edit Attendance");
        JButton deleteButton = new JButton("Delete Attendance");
        JButton saveButton = new JButton("Save Changes");
        
        addButton.addActionListener(e -> addAttendance());
        editButton.addActionListener(e -> editAttendance());
        deleteButton.addActionListener(e -> deleteAttendance());
        saveButton.addActionListener(e -> saveAttendance());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createAttendanceTable() {
        // Define table columns
        String[] columnNames = {"Date", "Time In", "Time Out", "Total Hours", "OT Hours", "Late (min)", "Status"};
        
        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create table with model
        attendanceTable = new JTable(tableModel);
        
        // Configure table
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
    }
    
    private void viewAttendance() {
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
        
        // Check if employee exists
        try {
            employeeController.getEmployeeById(employeeId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Employee not found with ID: " + employeeId, 
                "Employee Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine date range
        String selectedPeriod = (String) periodComboBox.getSelectedItem();
        
        if (selectedPeriod.equals("Current Month")) {
            startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
            endDate = DateTimeUtil.getLastDayOfCurrentMonth();
        } else if (selectedPeriod.equals("Previous Month")) {
            LocalDate now = LocalDate.now();
            startDate = now.minusMonths(1).withDayOfMonth(1);
            endDate = now.withDayOfMonth(1).minusDays(1);
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
        
        // Store current employee ID
        currentEmployeeId = employeeId;
        
        // Load attendance records
        loadAttendanceRecords(employeeId, startDate, endDate);
    }
    
    private void loadAttendanceRecords(int employeeId, LocalDate startDate, LocalDate endDate) {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get attendance records
        List<Attendance> records = attendanceController.getAttendanceByDateRange(employeeId, startDate, endDate);
        
        // Add records to table
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        for (Attendance record : records) {
            Object[] rowData = {
                record.getDate().format(dateFormatter),
                record.getFormattedTimeIn(),
                record.getFormattedTimeOut(),
                String.format("%.2f", record.getTotalHours()),
                String.format("%.2f", record.getOvertimeHours()),
                String.format("%.0f", record.getLateMinutes()),
                record.getStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // Update employee info display
        try {
            Employee employee = employeeController.getEmployeeById(employeeId);
            
            // Add a label to button panel to show current employee
            for (Component comp : buttonPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    buttonPanel.remove(comp);
                    break;
                }
            }
            
            JLabel employeeLabel = new JLabel("Employee: " + employee.getFirstName() + " " + employee.getLastName() + 
                " | Date Range: " + startDate.format(dateFormatter) + " - " + endDate.format(dateFormatter));
            employeeLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            buttonPanel.add(employeeLabel);
            buttonPanel.revalidate();
            buttonPanel.repaint();
            
        } catch (Exception e) {
            // Ignore if employee not found (shouldn't happen at this point)
        }
    }
    
    private void addAttendance() {
        if (currentEmployeeId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please view an employee's attendance first.", 
                "No Employee Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create dialog for adding attendance
        AttendanceDialog dialog = new AttendanceDialog(
            SwingUtilities.getWindowAncestor(this), 
            "Add Attendance Record", 
            currentEmployeeId,
            null);
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // Get new attendance data
            LocalDate date = dialog.getDate();
            LocalTime timeIn = dialog.getTimeIn();
            LocalTime timeOut = dialog.getTimeOut();
            
            try {
                // Add attendance
                attendanceController.addAttendance(currentEmployeeId, date, timeIn, timeOut);
                
                // Refresh attendance records
                loadAttendanceRecords(currentEmployeeId, startDate, endDate);
                
                JOptionPane.showMessageDialog(this, 
                    "Attendance record added successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding attendance record: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editAttendance() {
        if (currentEmployeeId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please view an employee's attendance first.", 
                "No Employee Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an attendance record to edit.", 
                "No Record Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get date from selected row
        String dateStr = (String) tableModel.getValueAt(selectedRow, 0);
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        
        // Get existing attendance record
        List<Attendance> records = attendanceController.getAttendanceByDateRange(currentEmployeeId, date, date);
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Attendance record not found.", 
                "Record Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Attendance record = records.get(0);
        
        // Create dialog for editing attendance
        AttendanceDialog dialog = new AttendanceDialog(
            SwingUtilities.getWindowAncestor(this), 
            "Edit Attendance Record", 
            currentEmployeeId,
            record);
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // Get updated attendance data
            LocalDate newDate = dialog.getDate();
            LocalTime newTimeIn = dialog.getTimeIn();
            LocalTime newTimeOut = dialog.getTimeOut();
            
            try {
                // Update attendance
                attendanceController.updateAttendance(currentEmployeeId, newDate, newTimeIn, newTimeOut);
                
                // Refresh attendance records
                loadAttendanceRecords(currentEmployeeId, startDate, endDate);
                
                JOptionPane.showMessageDialog(this, 
                    "Attendance record updated successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error updating attendance record: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteAttendance() {
        if (currentEmployeeId == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please view an employee's attendance first.", 
                "No Employee Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an attendance record to delete.", 
                "No Record Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get date from selected row
        String dateStr = (String) tableModel.getValueAt(selectedRow, 0);
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete the attendance record for " + dateStr + "?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete attendance
                attendanceController.deleteAttendance(currentEmployeeId, date);
                
                // Refresh attendance records
                loadAttendanceRecords(currentEmployeeId, startDate, endDate);
                
                JOptionPane.showMessageDialog(this, 
                    "Attendance record deleted successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting attendance record: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveAttendance() {
        if (attendanceController.saveAttendance()) {
            JOptionPane.showMessageDialog(this, 
                "Attendance records saved successfully.", 
                "Save Successful", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error saving attendance records. Please try again.", 
                "Save Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    void refreshData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}