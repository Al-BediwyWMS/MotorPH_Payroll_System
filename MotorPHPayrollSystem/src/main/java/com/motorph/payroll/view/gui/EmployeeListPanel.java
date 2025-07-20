package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.model.Employee;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeListPanel extends JPanel {

    private final EmployeeController employeeController;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public EmployeeListPanel(EmployeeController employeeController) {
        this.employeeController = employeeController;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Employee List");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(e -> filterEmployees());
        searchField.addActionListener(e -> filterEmployees()); // Allow Enter key to search
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Create table
        createEmployeeTable();
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewEmployeeDetails());
        
        buttonsPanel.add(viewDetailsButton);
        
        // Add components to panel
        add(titlePanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Load data
        refreshData();
    }
    
    private void createEmployeeTable() {
        // Define table columns
        String[] columnNames = {"ID", "Last Name", "First Name", "Position", "Status"};
        
        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create table with model
        employeeTable = new JTable(tableModel);
        
        // Configure table
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        employeeTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        employeeTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Last Name
        employeeTable.getColumnModel().getColumn(2).setPreferredWidth(150); // First Name
        employeeTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Position
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        
        // Add double-click listener for viewing details
        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewEmployeeDetails();
                }
            }
        });
    }
    
    public void refreshData() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get all employees
        List<Employee> employees = employeeController.getAllEmployees();
        
        // Add employees to table
        for (Employee employee : employees) {
            Object[] rowData = {
                employee.getEmployeeId(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getPosition(),
                employee.getStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // Update row count label if needed
        updateRowCountStatus(employees.size());
    }
    
    private void filterEmployees() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            refreshData();
            return;
        }
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Get all employees
        List<Employee> employees = employeeController.getAllEmployees();
        
        // Filter and add matching employees to table
        int matchCount = 0;
        for (Employee employee : employees) {
            if (String.valueOf(employee.getEmployeeId()).contains(searchText) ||
                employee.getFirstName().toLowerCase().contains(searchText) ||
                employee.getLastName().toLowerCase().contains(searchText) ||
                employee.getPosition().toLowerCase().contains(searchText) ||
                employee.getStatus().toLowerCase().contains(searchText)) {
                
                Object[] rowData = {
                    employee.getEmployeeId(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getPosition(),
                    employee.getStatus()
                };
                tableModel.addRow(rowData);
                matchCount++;
            }
        }
        
        // Update row count label
        updateRowCountStatus(matchCount);
    }
    
    private void updateRowCountStatus(int count) {
        // Implement if needed - could update a status label with count
    }
    
    private void viewEmployeeDetails() {
        int selectedRow = employeeTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an employee to view details", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
        
        try {
            Employee employee = employeeController.getEmployeeById(employeeId);
            EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(SwingUtilities.getWindowAncestor(this), employee);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading employee details: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public int getSelectedEmployeeId() {
        int selectedRow = employeeTable.getSelectedRow();
        
        if (selectedRow == -1) {
            return -1;
        }
        
        return (int) employeeTable.getValueAt(selectedRow, 0);
    }
}