package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.AttendanceController;
import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.controller.PayrollController;
import com.motorph.payroll.dao.DaoFactory;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.service.AttendanceService;
import com.motorph.payroll.service.AttendanceServiceImpl;
import com.motorph.payroll.service.EmployeeService;
import com.motorph.payroll.service.EmployeeServiceImpl;
import com.motorph.payroll.service.PayrollService;
import com.motorph.payroll.service.PayrollServiceImpl;
import com.motorph.payroll.util.AppConstants;
import com.motorph.payroll.util.DateTimeUtil;
import com.motorph.payroll.util.ImageHelper;
import com.motorph.payroll.view.gui.components.DashboardCard;
import com.motorph.payroll.view.gui.components.GradientPanel;
import com.motorph.payroll.view.gui.components.ModernButton;
import com.motorph.payroll.view.gui.components.NotificationPopup;
import com.motorph.payroll.view.gui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final EmployeeController employeeController;
    private AttendanceController attendanceController;
    private PayrollController payrollController;
    
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JLabel clockLabel;
    private Timer clockTimer;
    private NotificationPopup notificationPopup;
    private JLabel notificationLabel;
    
    public AdminDashboard(EmployeeController employeeController) {
        this.employeeController = employeeController;
        initializeControllers();
        initializeUI();
    }
    
    private void initializeControllers() {
        // Initialize other controllers
        EmployeeService employeeService = new EmployeeServiceImpl(DaoFactory.createEmployeeDao());
        AttendanceService attendanceService = new AttendanceServiceImpl(DaoFactory.createAttendanceDao());
        PayrollService payrollService = new PayrollServiceImpl(DaoFactory.createAttendanceDao());
        
        this.attendanceController = new AttendanceController(attendanceService);
        this.payrollController = new PayrollController(payrollService, employeeService);
    }
    
    private void initializeUI() {
        // Set up the frame
        setTitle("MotorPH Payroll System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
        
        // Create notification popup
        notificationPopup = new NotificationPopup();
        
        // Create main panel with gradient background
        mainPanel = new GradientPanel(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Create sidebar panel
        JPanel sidebarPanel = createSidebarPanel();
        
        // Create content panel (initially empty)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create footer panel
        JPanel footerPanel = createFooterPanel();
        
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Initialize clock timer
        initializeClockTimer();
        
        // Default view: Dashboard
        showDashboard();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        headerPanel.setBackground(new Color(41, 128, 185, 200));
        
        // Load logo with proper aspect ratio
        ImageIcon logoIcon = ImageHelper.loadImage("/images/motorph_logo.png", 
                                                 "resources/images/motorph_logo.png", 
                                                 150, 40);
        
        // Create logo label
        JLabel logoLabel;
        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon);
        } else {
            // If logo can't be loaded, create text-based logo
            ImageIcon textLogo = ImageHelper.createTextLogo("MOTORPH", 150, 40, 
                                                         Color.WHITE, 
                                                         new Color(0, 0, 0, 0), 
                                                         "Montserrat", 24, Font.BOLD);
            logoLabel = new JLabel(textLogo);
        }
        
        // Admin info panel
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        adminPanel.setOpaque(false);
        
        // Notification icon with count
        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setOpaque(false);
        
        // Load notification icon
        ImageIcon notifIcon = ImageHelper.loadImage("/images/notification_icon.png", 
                                                  "resources/images/notification_icon.png", 
                                                  24, 24);
        
        if (notifIcon == null) {
            // Create simple bell icon as fallback
            notifIcon = ImageHelper.createSimpleIcon("🔔", 20, Color.WHITE);
        }
        
        notificationLabel = new JLabel(notifIcon);
        notificationLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toggleNotificationPopup(evt);
            }
        });
        notificationPanel.add(notificationLabel, BorderLayout.CENTER);
        
        // Notification count bubble
        JLabel notificationCountLabel = new JLabel("5");
        notificationCountLabel.setFont(new Font("Montserrat", Font.BOLD, 10));
        notificationCountLabel.setForeground(Color.WHITE);
        notificationCountLabel.setBackground(Color.RED);
        notificationCountLabel.setOpaque(true);
        notificationCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notificationCountLabel.setVerticalAlignment(SwingConstants.CENTER);
        notificationCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        notificationPanel.add(notificationCountLabel, BorderLayout.EAST);
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Administrator");
        welcomeLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.WHITE);
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        
        adminPanel.add(notificationPanel);
        adminPanel.add(Box.createHorizontalStrut(15));
        adminPanel.add(welcomeLabel);
        adminPanel.add(Box.createHorizontalStrut(15));
        adminPanel.add(logoutButton);
        
        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(adminPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void toggleNotificationPopup(java.awt.event.MouseEvent evt) {
        if (notificationPopup.isVisible()) {
            notificationPopup.setVisible(false);
        } else {
            notificationPopup.show(notificationLabel, 0, notificationLabel.getHeight());
        }
    }
    
    private JPanel createSidebarPanel() {
        RoundedPanel sidebarPanel = new RoundedPanel(new BorderLayout(), new Color(52, 73, 94, 230));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));
        
        // Admin profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setOpaque(false);
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Admin profile image with proper dimensions
        ImageIcon profileIcon = ImageHelper.loadImage("/images/admin_profile.png", 
                                                   "resources/images/admin_profile.png", 
                                                   120, 120);
        
        if (profileIcon == null) {
            // Create default admin profile image
            profileIcon = ImageHelper.createInitialsProfileImage("SA", 120, 
                                                             new Color(231, 76, 60), 
                                                             Color.WHITE);
        }
        
        JLabel profileImageLabel = new JLabel(profileIcon);
        profileImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        // Admin name
        JLabel adminNameLabel = new JLabel("System Administrator");
        adminNameLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        adminNameLabel.setForeground(Color.WHITE);
        adminNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Role
        JLabel roleLabel = new JLabel("Super Admin");
        roleLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Clock
        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to profile panel
        profilePanel.add(profileImageLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        profilePanel.add(adminNameLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(roleLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(clockLabel);
        
        // Menu section
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Create menu buttons with modern style
        JButton dashboardButton = createMenuButton("Dashboard", "🏠");
        JButton employeeListButton = createMenuButton("Employee List", "👥");
        JButton generatePayslipButton = createMenuButton("Generate Payslip", "💰");
        JButton attendanceButton = createMenuButton("Attendance Records", "📅");
        JButton manageEmployeesButton = createMenuButton("Manage Employees", "👤");
        JButton manageAttendanceButton = createMenuButton("Manage Attendance", "⏱️");
        JButton settingsButton = createMenuButton("System Settings", "⚙️");
        JButton logoutButton = createMenuButton("Logout", "🚪");
        
        // Add action listeners
        dashboardButton.addActionListener(e -> showDashboard());
        employeeListButton.addActionListener(e -> showEmployeeList());
        generatePayslipButton.addActionListener(e -> showPayrollGeneration());
        attendanceButton.addActionListener(e -> showAttendanceManagement());
        manageEmployeesButton.addActionListener(e -> showEmployeeManagement());
        manageAttendanceButton.addActionListener(e -> showAttendanceManagement());
        settingsButton.addActionListener(e -> showSystemSettings());
        logoutButton.addActionListener(e -> logout());
        
        // Add buttons to menu panel with spacing
        menuPanel.add(dashboardButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(employeeListButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(generatePayslipButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(attendanceButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(manageEmployeesButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(manageAttendanceButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(settingsButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        menuPanel.add(logoutButton);
        
        // Add a glue component that will push the remaining components to the top
        menuPanel.add(Box.createVerticalGlue());
        
        // Add sections to sidebar
        sidebarPanel.add(profilePanel, BorderLayout.NORTH);
        sidebarPanel.add(new JSeparator(), BorderLayout.CENTER);
        sidebarPanel.add(menuPanel, BorderLayout.CENTER);
        
        return sidebarPanel;
    }
    
    private JButton createMenuButton(String text, String emoji) {
        JButton button = new JButton(emoji + " " + text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 45));
        button.setPreferredSize(new Dimension(220, 45));
        button.setFont(new Font("Montserrat", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(41, 128, 185, 180));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185, 180));
            }
        });
        
        return button;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel versionLabel = new JLabel(AppConstants.APP_COPYRIGHT + " | " + AppConstants.APP_VERSION);
        versionLabel.setFont(new Font("Montserrat", Font.PLAIN, 12));
        versionLabel.setForeground(Color.WHITE);
        
        footerPanel.add(versionLabel);
        
        return footerPanel;
    }
    
    private void initializeClockTimer() {
        clockTimer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            clockLabel.setText(sdf.format(new Date()));
        });
        clockTimer.start();
    }
    
    private void showDashboard() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create dashboard panel
        RoundedPanel dashboardPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        dashboardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        // Date panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        datePanel.add(dateLabel);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(datePanel, BorderLayout.EAST);
        
        // Dashboard content
        JPanel dashboardContent = new JPanel(new BorderLayout());
        dashboardContent.setOpaque(false);
        
        // Summary cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);
        
        // Get stats
        List<Employee> allEmployees = employeeController.getAllEmployees();
        int totalEmployees = allEmployees.size();
        
        // Cards
        DashboardCard employeesCard = new DashboardCard("Total Employees", String.valueOf(totalEmployees), new Color(52, 152, 219));
        DashboardCard presentTodayCard = new DashboardCard("Present Today", "18", new Color(46, 204, 113));
        DashboardCard lateEmployeesCard = new DashboardCard("Late Employees", "3", new Color(231, 76, 60));
        DashboardCard pendingApprovalsCard = new DashboardCard("Pending Approvals", "5", new Color(155, 89, 182));
        
        cardsPanel.add(employeesCard);
        cardsPanel.add(presentTodayCard);
        cardsPanel.add(lateEmployeesCard);
        cardsPanel.add(pendingApprovalsCard);
        
        // Quick links
        RoundedPanel quickLinksPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        quickLinksPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel quickLinksTitle = new JLabel("Quick Actions");
        quickLinksTitle.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        JPanel linksPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        linksPanel.setOpaque(false);
        linksPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Quick link buttons
        ModernButton addEmployeeButton = new ModernButton("Add Employee");
        addEmployeeButton.addActionListener(e -> showAddEmployeeForm());
        
        ModernButton generatePayslipButton = new ModernButton("Generate Payslip");
        generatePayslipButton.addActionListener(e -> showPayrollGeneration());
        
        ModernButton viewAttendanceButton = new ModernButton("View Attendance");
        viewAttendanceButton.addActionListener(e -> showAttendanceManagement());
        
        ModernButton exportReportButton = new ModernButton("Export Report");
        exportReportButton.addActionListener(e -> exportReport());
        
        ModernButton systemSettingsButton = new ModernButton("System Settings");
        systemSettingsButton.addActionListener(e -> showSystemSettings());
        
        ModernButton backupButton = new ModernButton("Backup Data");
        backupButton.addActionListener(e -> backupData());
        
        linksPanel.add(addEmployeeButton);
        linksPanel.add(generatePayslipButton);
        linksPanel.add(viewAttendanceButton);
        linksPanel.add(exportReportButton);
        linksPanel.add(systemSettingsButton);
        linksPanel.add(backupButton);
        
        quickLinksPanel.add(quickLinksTitle, BorderLayout.NORTH);
        quickLinksPanel.add(linksPanel, BorderLayout.CENTER);
        
        // Recent activity
        RoundedPanel recentActivityPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        recentActivityPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        JPanel activityContentPanel = new JPanel();
        activityContentPanel.setLayout(new BoxLayout(activityContentPanel, BoxLayout.Y_AXIS));
        activityContentPanel.setOpaque(false);
        activityContentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Sample activities
        addActivity(activityContentPanel, "Employee ID 1001 clocked in", "10 minutes ago");
        addActivity(activityContentPanel, "Generated payslip for Employee ID 1005", "25 minutes ago");
        addActivity(activityContentPanel, "Updated attendance record for Employee ID 1003", "1 hour ago");
        addActivity(activityContentPanel, "Added new employee: Juan Dela Cruz", "2 hours ago");
        addActivity(activityContentPanel, "System backup completed", "Yesterday at 11:00 PM");
        
        recentActivityPanel.add(activityTitle, BorderLayout.NORTH);
        recentActivityPanel.add(activityContentPanel, BorderLayout.CENTER);
        
        // Add panels to dashboard content
        dashboardContent.add(cardsPanel, BorderLayout.NORTH);
        
        // Create a panel for the two bottom sections
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        bottomPanel.add(quickLinksPanel);
        bottomPanel.add(recentActivityPanel);
        
        dashboardContent.add(bottomPanel, BorderLayout.CENTER);
        
        // Add components to dashboard panel
        dashboardPanel.add(titlePanel, BorderLayout.NORTH);
        dashboardPanel.add(dashboardContent, BorderLayout.CENTER);
        
        // Add dashboard panel to content panel
        contentPanel.add(dashboardPanel, BorderLayout.CENTER);
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void addActivity(JPanel container, String message, String timeAgo) {
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setOpaque(false);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 0, 5, 0),
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230))));
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Montserrat", Font.PLAIN, 13));
        
        JLabel timeLabel = new JLabel(timeAgo);
        timeLabel.setFont(new Font("Montserrat", Font.ITALIC, 11));
        timeLabel.setForeground(Color.GRAY);
        
        activityPanel.add(msgLabel, BorderLayout.CENTER);
        activityPanel.add(timeLabel, BorderLayout.EAST);
        
        activityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        activityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(activityPanel);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private void showEmployeeList() {
        setCurrentView("employeeList");
        // Clear content panel
        contentPanel.removeAll();
        
        // Create employee list panel
        RoundedPanel employeeListPanelContainer = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        employeeListPanelContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Employee List");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Montserrat", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        
        ModernButton searchButton = new ModernButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 30));
        
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Get all employees
        List<Employee> employees = employeeController.getAllEmployees();
        
        // Create table model
        String[] columnNames = {"ID", "Last Name", "First Name", "Position", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add employees to table
        for (Employee employee : employees) {
            Object[] rowData = {
                employee.getEmployeeId(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getPosition(),
                employee.getStatus()
            };
            model.addRow(rowData);
        }
        
        // Create table
        JTable employeeTable = new JTable(model);
        employeeTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
        employeeTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 12));
        employeeTable.setRowHeight(25);
        employeeTable.setFillsViewportHeight(true);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add sorting capability
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        employeeTable.setRowSorter(sorter);
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        ModernButton viewButton = new ModernButton("View Details");
        viewButton.setPreferredSize(new Dimension(120, 35));
        viewButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
                viewEmployeeDetails(employeeId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an employee to view details.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        ModernButton addButton = new ModernButton("Add Employee");
        addButton.setPreferredSize(new Dimension(120, 35));
        addButton.addActionListener(e -> showAddEmployeeForm());
        
        ModernButton editButton = new ModernButton("Edit");
        editButton.setPreferredSize(new Dimension(120, 35));
        editButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
                showEditEmployeeForm(employeeId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an employee to edit.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        ModernButton deleteButton = new ModernButton("Delete");
        deleteButton.setPreferredSize(new Dimension(120, 35));
        deleteButton.setButtonColors(new Color(231, 76, 60), new Color(192, 57, 43));
        deleteButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
                deleteEmployee(employeeId);
                
                // Refresh table
                model.removeRow(employeeTable.convertRowIndexToModel(selectedRow));
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an employee to delete.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        actionPanel.add(viewButton);
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        
        // Add to employee list panel container
        employeeListPanelContainer.add(titlePanel, BorderLayout.NORTH);
        employeeListPanelContainer.add(scrollPane, BorderLayout.CENTER);
        employeeListPanelContainer.add(actionPanel, BorderLayout.SOUTH);
        
        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (searchText.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
            }
        });
        
        // Add employee list panel to content panel
        contentPanel.add(employeeListPanelContainer, BorderLayout.CENTER);
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showPayrollGeneration() {
        setCurrentView("payrollGeneration");

        // Clear content panel
        contentPanel.removeAll();

        // Create payroll panel
        RoundedPanel payrollPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        payrollPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Generate Payslips");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));

        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Create tabbed pane for different generation options
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Montserrat", Font.PLAIN, 14));

        // Tab 1: Single Employee Payslip
        JPanel singleEmployeePanel = createSingleEmployeePayslipPanel();
        tabbedPane.addTab("Single Employee", singleEmployeePanel);

        // Tab 2: Bulk Payslip Generation
        JPanel bulkGenerationPanel = createBulkPayslipPanel();
        tabbedPane.addTab("Bulk Generation", bulkGenerationPanel);

        // Add components to payroll panel
        payrollPanel.add(titlePanel, BorderLayout.NORTH);
        payrollPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add payroll panel to content panel
        contentPanel.add(payrollPanel, BorderLayout.CENTER);

        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createSingleEmployeePayslipPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Employee ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel employeeIdLabel = new JLabel("Employee ID:");
        employeeIdLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        formPanel.add(employeeIdLabel, gbc);

        gbc.gridx = 1;
        JTextField employeeIdField = new JTextField(15);
        employeeIdField.setFont(new Font("Montserrat", Font.PLAIN, 14));
        formPanel.add(employeeIdField, gbc);

        // Pay Period
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        formPanel.add(periodLabel, gbc);

        gbc.gridx = 1;
        String[] periodOptions = {
            "First Half (1-15) of Current Month",
            "Second Half (16-30/31) of Current Month",
            "Custom Date Range"
        };
        JComboBox<String> periodComboBox = new JComboBox<>(periodOptions);
        periodComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        periodComboBox.setPreferredSize(new Dimension(300, 25));
        formPanel.add(periodComboBox, gbc);

        // Generate button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        ModernButton generateButton = new ModernButton("Generate Payslip");
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.addActionListener(e -> generateSinglePayslip(employeeIdField.getText(), periodComboBox));
        formPanel.add(generateButton, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createBulkPayslipPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setOpaque(false);

        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));

        String[] periodOptions = {
            "First Half (1-15) of Current Month",
            "Second Half (16-30/31) of Current Month",
            "Previous Month"
        };
        JComboBox<String> bulkPeriodComboBox = new JComboBox<>(periodOptions);
        bulkPeriodComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));

        controlsPanel.add(periodLabel);
        controlsPanel.add(bulkPeriodComboBox);

        // Employee selection
        JPanel employeeSelectionPanel = new JPanel(new BorderLayout());
        employeeSelectionPanel.setOpaque(false);
        employeeSelectionPanel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JLabel selectEmployeeLabel = new JLabel("Select Employees:");
        selectEmployeeLabel.setFont(new Font("Montserrat", Font.BOLD, 16));

        // Get all employees
        List<Employee> employees = employeeController.getAllEmployees();

        // Create table model
        String[] columnNames = {"Select", "ID", "Last Name", "First Name", "Position"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only checkbox column is editable
            }
        };

        // Add employees to table
        for (Employee employee : employees) {
            Object[] rowData = {
                Boolean.FALSE, // Checkbox
                employee.getEmployeeId(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getPosition()
            };
            model.addRow(rowData);
        }

        // Create table
        JTable employeeTable = new JTable(model);
        employeeTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
        employeeTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 12));
        employeeTable.setRowHeight(25);
        employeeTable.setFillsViewportHeight(true);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.setPreferredSize(new Dimension(700, 300));

        employeeSelectionPanel.add(selectEmployeeLabel, BorderLayout.NORTH);
        employeeSelectionPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JCheckBox selectAllCheckBox = new JCheckBox("Select All");
        selectAllCheckBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        selectAllCheckBox.setOpaque(false);
        selectAllCheckBox.addActionListener(e -> {
            boolean selected = selectAllCheckBox.isSelected();
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(selected, i, 0);
            }
        });

        ModernButton generateBulkButton = new ModernButton("Generate Selected Payslips");
        generateBulkButton.setPreferredSize(new Dimension(220, 40));
        generateBulkButton.addActionListener(e -> generateBulkPayslips(model, bulkPeriodComboBox));

        buttonPanel.add(selectAllCheckBox);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(generateBulkButton);

        // Add components to panel
        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(employeeSelectionPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void generateSinglePayslip(String employeeIdText, JComboBox<String> periodComboBox) {
        // Validate employee ID
        if (employeeIdText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an Employee ID",
                    "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int employeeId;
        try {
            employeeId = Integer.parseInt(employeeIdText.trim());
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
            DateRangeDialog dialog = new DateRangeDialog(this);
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
            PayrollSummary payrollSummary = payrollController.calculatePayroll(employee, startDate, endDate);

            if (payrollSummary.getAttendanceRecords().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No attendance records found for the specified period.",
                        "No Data",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Display payroll summary in a new window
            displayPayslipWindow(payrollSummary);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error generating payslip: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateBulkPayslips(DefaultTableModel model, JComboBox<String> periodComboBox) {
        // Count selected employees
        java.util.List<Integer> selectedEmployeeIds = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                selectedEmployeeIds.add((Integer) model.getValueAt(i, 1));
            }
        }

        if (selectedEmployeeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one employee.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
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
            // Previous month
            LocalDate now = LocalDate.now();
            startDate = now.minusMonths(1).withDayOfMonth(1);
            endDate = now.withDayOfMonth(1).minusDays(1);
        }

        // Show confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
                "Generate payslips for " + selectedEmployeeIds.size() + " employee(s)?\n"
                + "Pay Period: " + startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                + " to " + endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                "Confirm Generation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (Integer employeeId : selectedEmployeeIds) {
                try {
                    Employee employee = employeeController.getEmployeeById(employeeId);
                    PayrollSummary payrollSummary = payrollController.calculatePayroll(employee, startDate, endDate);

                    // Save to file automatically
                    String fileName = String.format("Payslip_%d_%s_%s.txt",
                            employeeId,
                            startDate.format(DateTimeFormatter.ofPattern("MMddyyyy")),
                            endDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));

                    if (payrollController.savePayslipToFile(payrollSummary, fileName)) {
                        successCount++;
                    } else {
                        errorCount++;
                        errorMessages.append("Failed to save payslip for Employee ").append(employeeId).append("\n");
                    }
                } catch (Exception e) {
                    errorCount++;
                    errorMessages.append("Error for Employee ").append(employeeId).append(": ").append(e.getMessage()).append("\n");
                }
            }

            // Show results
            String message = String.format("Payslip Generation Complete!\n\nSuccessful: %d\nErrors: %d",
                    successCount, errorCount);

            if (errorCount > 0) {
                message += "\n\nErrors:\n" + errorMessages.toString();
                JOptionPane.showMessageDialog(this, message, "Generation Results", JOptionPane.WARNING_MESSAGE);
            } else {
                message += "\n\nAll payslips saved to project directory.";
                JOptionPane.showMessageDialog(this, message, "Generation Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void displayPayslipWindow(PayrollSummary payrollSummary) {
        // Create a new window to display the payslip
        JFrame payslipWindow = new JFrame("Payslip - " + payrollSummary.getEmployee().getFullName());
        payslipWindow.setSize(800, 900);
        payslipWindow.setLocationRelativeTo(this);
        payslipWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        payslipWindow.setAlwaysOnTop(true);

        // Use the same payslip display code as in PayrollGenerationPanel
        // ... (copy the payslip display code from PayrollGenerationPanel)
        // For now, create a simple payslip display
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create payslip content
        JTextArea payslipText = new JTextArea();
        payslipText.setEditable(false);
        payslipText.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Employee employee = payrollSummary.getEmployee();
        StringBuilder sb = new StringBuilder();
        sb.append("================ MOTORPH PAYROLL SYSTEM ================\n");
        sb.append("                    PAYSLIP DETAIL                     \n");
        sb.append("====================================================\n\n");
        sb.append("Pay Period: ").append(payrollSummary.getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                .append(" - ").append(payrollSummary.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n\n");

        sb.append("Employee Details:\n");
        sb.append("ID: ").append(employee.getEmployeeId()).append("\n");
        sb.append("Name: ").append(employee.getLastName()).append(", ").append(employee.getFirstName()).append("\n");
        sb.append("Position: ").append(employee.getPosition()).append("\n");
        sb.append("Status: ").append(employee.getStatus()).append("\n\n");

        sb.append("Attendance Summary:\n");
        sb.append(String.format("Days Present:       %d days\n", payrollSummary.getDaysPresent()));
        sb.append(String.format("Total Hours Worked: %.2f hours\n", payrollSummary.getTotalHours()));
        sb.append(String.format("Overtime Hours:     %.2f hours\n", payrollSummary.getOvertimeHours()));
        sb.append(String.format("Late Minutes:       %.2f minutes\n\n", payrollSummary.getLateMinutes()));

        sb.append("Earnings:\n");
        sb.append(String.format("Basic Salary:       PHP %,.2f\n", employee.getBasicSalary()));
        sb.append(String.format("Rice Subsidy:       PHP %,.2f\n", employee.getRiceSubsidy()));
        sb.append(String.format("Phone Allowance:    PHP %,.2f\n", employee.getPhoneAllowance()));
        sb.append(String.format("Clothing Allowance: PHP %,.2f\n", employee.getClothingAllowance()));
        sb.append(String.format("Gross Pay:          PHP %,.2f\n\n", payrollSummary.getGrossPay()));

        sb.append("Deductions:\n");
        sb.append(String.format("SSS:               PHP %,.2f\n", payrollSummary.getSssDeduction()));
        sb.append(String.format("PhilHealth:        PHP %,.2f\n", payrollSummary.getPhilhealthDeduction()));
        sb.append(String.format("Pag-IBIG:          PHP %,.2f\n", payrollSummary.getPagibigDeduction()));
        sb.append(String.format("Total Deductions:  PHP %,.2f\n\n", payrollSummary.getTotalDeductions()));

        sb.append("----------------------------------------------------\n");
        sb.append(String.format("NET PAY:           PHP %,.2f\n", payrollSummary.getNetPay()));
        sb.append("====================================================\n");

        payslipText.setText(sb.toString());

        JScrollPane scrollPane = new JScrollPane(payslipText);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton saveButton = new JButton("Save to File");
        saveButton.addActionListener(e -> {
            String fileName = String.format("Payslip_%d_%s_%s.txt",
                    employee.getEmployeeId(),
                    payrollSummary.getStartDate().format(DateTimeFormatter.ofPattern("MMddyyyy")),
                    payrollSummary.getEndDate().format(DateTimeFormatter.ofPattern("MMddyyyy")));

            if (payrollController.savePayslipToFile(payrollSummary, fileName)) {
                JOptionPane.showMessageDialog(payslipWindow,
                        "Payslip saved to: " + System.getProperty("user.dir") + "/" + fileName,
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(payslipWindow,
                        "Error saving payslip. Please try again.",
                        "Save Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            payslipWindow.setAlwaysOnTop(false);
            payslipWindow.dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        payslipWindow.add(mainPanel);
        payslipWindow.setVisible(true);

        // Remove always on top after 2 seconds
        Timer timer = new Timer(2000, e -> payslipWindow.setAlwaysOnTop(false));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showAttendanceManagement() {
        setCurrentView("attendanceManagement");

        // Clear content panel
        contentPanel.removeAll();

        // Create attendance management panel
        AttendanceManagementPanel attendanceManagementPanel = new AttendanceManagementPanel(attendanceController, employeeController);

        // Add attendance panel to content panel
        contentPanel.add(attendanceManagementPanel, BorderLayout.CENTER);

        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showEmployeeManagement() {
        showEmployeeList(); // Use the employee list which has add/edit/delete functionality
    }
    
    private void showSystemSettings() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create settings panel
        RoundedPanel settingsPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        settingsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("System Settings");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Settings content - For now, just a placeholder
        RoundedPanel settingsContent = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        settingsContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel settingsForm = new JPanel(new GridLayout(0, 2, 20, 15));
        settingsForm.setOpaque(false);
        
        // Company settings
        JLabel companyNameLabel = new JLabel("Company Name:");
        JTextField companyNameField = new JTextField("MotorPH Corporation");
        
        JLabel companyAddressLabel = new JLabel("Company Address:");
        JTextField companyAddressField = new JTextField("123 Main Street, Manila, Philippines");
        
        JLabel taxIdLabel = new JLabel("Company Tax ID:");
        JTextField taxIdField = new JTextField("123-456-789-000");
        
        JLabel phoneLabel = new JLabel("Contact Number:");
        JTextField phoneField = new JTextField("+63 2 123 4567");
        
        JLabel emailLabel = new JLabel("Email Address:");
        JTextField emailField = new JTextField("info@motorph.com");
        
        // Payroll settings
        JLabel payPeriodLabel = new JLabel("Pay Period:");
        String[] payPeriodOptions = {"Semi-monthly", "Monthly", "Weekly"};
        JComboBox<String> payPeriodComboBox = new JComboBox<>(payPeriodOptions);
        
        JLabel taxRateLabel = new JLabel("Default Tax Rate (%):");
        JTextField taxRateField = new JTextField("10");
        
        JLabel sssRateLabel = new JLabel("SSS Rate (%):");
        JTextField sssRateField = new JTextField("4.5");
        
        JLabel philhealthRateLabel = new JLabel("PhilHealth Rate (%):");
        JTextField philhealthRateField = new JTextField("1.5");
        
        JLabel pagibigRateLabel = new JLabel("Pag-IBIG Rate (%):");
        JTextField pagibigRateField = new JTextField("1.2");
        
        // Add components to settings form
        settingsForm.add(companyNameLabel);
        settingsForm.add(companyNameField);
        settingsForm.add(companyAddressLabel);
        settingsForm.add(companyAddressField);
        settingsForm.add(taxIdLabel);
        settingsForm.add(taxIdField);
        settingsForm.add(phoneLabel);
        settingsForm.add(phoneField);
        settingsForm.add(emailLabel);
        settingsForm.add(emailField);
        
        settingsForm.add(new JSeparator());
        settingsForm.add(new JSeparator());
        
        settingsForm.add(payPeriodLabel);
        settingsForm.add(payPeriodComboBox);
        settingsForm.add(taxRateLabel);
        settingsForm.add(taxRateField);
        settingsForm.add(sssRateLabel);
        settingsForm.add(sssRateField);
        settingsForm.add(philhealthRateLabel);
        settingsForm.add(philhealthRateField);
        settingsForm.add(pagibigRateLabel);
        settingsForm.add(pagibigRateField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton saveButton = new ModernButton("Save Settings");
        saveButton.setPreferredSize(new Dimension(150, 35));
        
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setButtonColors(new Color(150, 150, 150), new Color(120, 120, 120));
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add form and buttons to settings content
        settingsContent.add(settingsForm, BorderLayout.CENTER);
        settingsContent.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add components to settings panel
        settingsPanel.add(titlePanel, BorderLayout.NORTH);
        settingsPanel.add(settingsContent, BorderLayout.CENTER);
        
        // Add settings panel to content panel
        contentPanel.add(settingsPanel, BorderLayout.CENTER);
        
        // Add action listeners
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Settings saved successfully!", 
                "Settings Saved", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        cancelButton.addActionListener(e -> showDashboard());
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void viewEmployeeDetails(int employeeId) {
        try {
            Employee employee = employeeController.getEmployeeById(employeeId);
            
            // Create a dialog to show employee details
            JDialog dialog = new JDialog(this, "Employee Details", true);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);
            
            // Create content panel with gradient background
            GradientPanel contentPanel = new GradientPanel(new BorderLayout());
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Create employee details panel
            RoundedPanel detailsPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
            detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Title panel
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            
            JLabel titleLabel = new JLabel("Employee Details");
            titleLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
            
            titlePanel.add(titleLabel, BorderLayout.WEST);
            
            // Details content
            JPanel detailsContent = new JPanel();
            detailsContent.setLayout(new BoxLayout(detailsContent, BoxLayout.Y_AXIS));
            detailsContent.setOpaque(false);
            
            // Employee header (photo + basic info)
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            // Profile photo
            String initials = "";
            if (employee.getFirstName() != null && !employee.getFirstName().isEmpty()) {
                initials += employee.getFirstName().charAt(0);
            }
            if (employee.getLastName() != null && !employee.getLastName().isEmpty()) {
                initials += employee.getLastName().charAt(0);
            }
            ImageIcon profileIcon = ImageHelper.createInitialsProfileImage(initials, 100, 
                                                                     new Color(52, 152, 219), 
                                                                     Color.WHITE);
            JLabel photoLabel = new JLabel(profileIcon);
            photoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            
            JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            photoPanel.setOpaque(false);
            photoPanel.add(photoLabel);
            
            // Basic info
            JPanel basicInfoPanel = new JPanel();
            basicInfoPanel.setLayout(new BoxLayout(basicInfoPanel, BoxLayout.Y_AXIS));
            basicInfoPanel.setOpaque(false);
            basicInfoPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            
            JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
            nameLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel positionLabel = new JLabel(employee.getPosition());
            positionLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
            positionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Using String.valueOf to handle null value
            var departmentText = employee.getDepartment() != null ? employee.getDepartment() : "Not specified";
                JLabel departmentLabel = new JLabel("Department: " + departmentText);
            departmentLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            departmentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel statusLabel = new JLabel("Status: " + employee.getStatus());
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            basicInfoPanel.add(nameLabel);
            basicInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            basicInfoPanel.add(positionLabel);
            basicInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            basicInfoPanel.add(departmentLabel);
            basicInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            basicInfoPanel.add(statusLabel);
            
            headerPanel.add(photoPanel, BorderLayout.WEST);
            headerPanel.add(basicInfoPanel, BorderLayout.CENTER);
            
            // Create tabbed pane for detailed information
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Montserrat", Font.PLAIN, 14));
            
            // Personal information tab
            JPanel personalPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            personalPanel.setOpaque(false);
            personalPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            addLabelValuePair(personalPanel, "Employee ID:", String.valueOf(employee.getEmployeeId()));
            addLabelValuePair(personalPanel, "Birthday:", employee.getBirthday());
            addLabelValuePair(personalPanel, "Address:", employee.getAddress());
            addLabelValuePair(personalPanel, "Phone Number:", employee.getPhoneNumber());
            addLabelValuePair(personalPanel, "Supervisor:", employee.getSupervisor());
            
            // Government IDs tab
            JPanel governmentPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            governmentPanel.setOpaque(false);
            governmentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            addLabelValuePair(governmentPanel, "SSS Number:", employee.getSssNumber());
            addLabelValuePair(governmentPanel, "PhilHealth Number:", employee.getPhilhealthNumber());
            addLabelValuePair(governmentPanel, "TIN Number:", employee.getTinNumber());
            addLabelValuePair(governmentPanel, "Pag-IBIG Number:", employee.getPagibigNumber());
            
            // Compensation tab
            JPanel compensationPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            compensationPanel.setOpaque(false);
            compensationPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            addLabelValuePair(compensationPanel, "Basic Salary:", String.format("₱%,.2f", employee.getBasicSalary()));
            addLabelValuePair(compensationPanel, "Rice Subsidy:", String.format("₱%,.2f", employee.getRiceSubsidy()));
            addLabelValuePair(compensationPanel, "Phone Allowance:", String.format("₱%,.2f", employee.getPhoneAllowance()));
            addLabelValuePair(compensationPanel, "Clothing Allowance:", String.format("₱%,.2f", employee.getClothingAllowance()));
            addLabelValuePair(compensationPanel, "Gross Semi-Monthly Rate:", String.format("₱%,.2f", employee.getGrossSemiMonthlyRate()));
            addLabelValuePair(compensationPanel, "Hourly Rate:", String.format("₱%,.2f", employee.getHourlyRate()));
            
            // Add tabs to tabbed pane
            tabbedPane.addTab("Personal Information", personalPanel);
            tabbedPane.addTab("Government IDs", governmentPanel);
            tabbedPane.addTab("Compensation", compensationPanel);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
            
            ModernButton editButton = new ModernButton("Edit Employee");
            editButton.setPreferredSize(new Dimension(150, 35));
            editButton.addActionListener(e -> {
                dialog.dispose();
                showEditEmployeeForm(employeeId);
            });
            
            ModernButton closeButton = new ModernButton("Close");
            closeButton.setPreferredSize(new Dimension(100, 35));
            closeButton.setButtonColors(new Color(150, 150, 150), new Color(120, 120, 120));
            closeButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(editButton);
            buttonPanel.add(closeButton);
            
            // Add components to details content
            detailsContent.add(headerPanel);
            detailsContent.add(tabbedPane);
            detailsContent.add(buttonPanel);
            
            // Add components to details panel
            detailsPanel.add(titlePanel, BorderLayout.NORTH);
            detailsPanel.add(detailsContent, BorderLayout.CENTER);
            
            // Add details panel to content panel
            contentPanel.add(detailsPanel, BorderLayout.CENTER);
            
            // Add content panel to dialog
            dialog.add(contentPanel);
            
            // Display dialog
            dialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading employee details: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addLabelValuePair(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Montserrat", Font.BOLD, 14));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    private void showAddEmployeeForm() {
        // Create employee form dialog
        EmployeeFormDialog dialog = new EmployeeFormDialog(
                this,
                employeeController,
                null // null means adding new employee
        );

        dialog.setVisible(true);

        // Refresh employee list after dialog closes
        if (getCurrentView().equals("employeeList")) {
            showEmployeeList();
        }
    }

    private String currentView = "dashboard";

    private String getCurrentView() {
        return currentView;
    }

    private void setCurrentView(String view) {
        this.currentView = view;
    }
    
    // Helper method to parse double values safely
    private double parseDoubleOrDefault(String text, double defaultValue) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // Helper method to calculate gross semi-monthly rate
    private double calculateGrossSemiMonthly(double basicSalary) {
        return basicSalary / 2;
    }
    
    // Helper method to calculate hourly rate
    private double calculateHourlyRate(double basicSalary) {
        // Assuming 22 working days per month, 8 hours per day
        return basicSalary / (22 * 8);
    }
    
    private void showEditEmployeeForm(int employeeId) {
        try {
            // Get employee details
            Employee employee = employeeController.getEmployeeById(employeeId);
            
            // Create dialog similar to add employee form, but pre-populated
            JDialog dialog = new JDialog(this, "Edit Employee", true);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            
            // Create content panel with gradient background
            GradientPanel contentPanel = new GradientPanel(new BorderLayout());
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Create form panel
            RoundedPanel formPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            // Title panel
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            
            JLabel titleLabel = new JLabel("Edit Employee: " + employee.getFirstName() + " " + employee.getLastName());
            titleLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
            
            titlePanel.add(titleLabel, BorderLayout.WEST);
            
            // Create form content with tabs
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Montserrat", Font.PLAIN, 14));
            
            // Personal Information tab - pre-populated
            JPanel personalPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            personalPanel.setOpaque(false);
            personalPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            personalPanel.add(new JLabel("First Name:"));
            JTextField firstNameField = new JTextField(employee.getFirstName());
            personalPanel.add(firstNameField);
            
            personalPanel.add(new JLabel("Last Name:"));
            JTextField lastNameField = new JTextField(employee.getLastName());
            personalPanel.add(lastNameField);
            
            personalPanel.add(new JLabel("Birthday:"));
            JTextField birthdayField = new JTextField(employee.getBirthday());
            personalPanel.add(birthdayField);
            
            personalPanel.add(new JLabel("Address:"));
            JTextField addressField = new JTextField(employee.getAddress());
            personalPanel.add(addressField);
            
            personalPanel.add(new JLabel("Phone Number:"));
            JTextField phoneField = new JTextField(employee.getPhoneNumber());
            personalPanel.add(phoneField);
            
            personalPanel.add(new JLabel("Status:"));
            String[] statusOptions = {"Regular", "Probationary", "Contractual"};
            JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
            statusComboBox.setSelectedItem(employee.getStatus());
            personalPanel.add(statusComboBox);
            
            personalPanel.add(new JLabel("Position:"));
            JTextField positionField = new JTextField(employee.getPosition());
            personalPanel.add(positionField);
            
            personalPanel.add(new JLabel("Department:"));
            
            personalPanel.add(new JLabel("Supervisor:"));
            JTextField supervisorField = new JTextField(employee.getSupervisor());
            personalPanel.add(supervisorField);
            
            // Government IDs tab
            JPanel governmentPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            governmentPanel.setOpaque(false);
            governmentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            governmentPanel.add(new JLabel("SSS Number:"));
            JTextField sssField = new JTextField(employee.getSssNumber());
            governmentPanel.add(sssField);
            
            governmentPanel.add(new JLabel("PhilHealth Number:"));
            JTextField philhealthField = new JTextField(employee.getPhilhealthNumber());
            governmentPanel.add(philhealthField);
            
            governmentPanel.add(new JLabel("TIN Number:"));
            JTextField tinField = new JTextField(employee.getTinNumber());
            governmentPanel.add(tinField);
            
            governmentPanel.add(new JLabel("Pag-IBIG Number:"));
            JTextField pagibigField = new JTextField(employee.getPagibigNumber());
            governmentPanel.add(pagibigField);
            
            // Compensation tab
            JPanel compensationPanel = new JPanel(new GridLayout(0, 2, 15, 10));
            compensationPanel.setOpaque(false);
            compensationPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            compensationPanel.add(new JLabel("Basic Salary:"));
            JTextField salaryField = new JTextField(String.format("%.2f", employee.getBasicSalary()));
            compensationPanel.add(salaryField);
            
            compensationPanel.add(new JLabel("Rice Subsidy:"));
            JTextField riceField = new JTextField(String.format("%.2f", employee.getRiceSubsidy()));
            compensationPanel.add(riceField);
            
            compensationPanel.add(new JLabel("Phone Allowance:"));
            JTextField phoneAllowanceField = new JTextField(String.format("%.2f", employee.getPhoneAllowance()));
            compensationPanel.add(phoneAllowanceField);
            
            compensationPanel.add(new JLabel("Clothing Allowance:"));
            JTextField clothingField = new JTextField(String.format("%.2f", employee.getClothingAllowance()));
            compensationPanel.add(clothingField);
            
            // Add tabs to tabbed pane
            tabbedPane.addTab("Personal Information", personalPanel);
            tabbedPane.addTab("Government IDs", governmentPanel);
            tabbedPane.addTab("Compensation", compensationPanel);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
            
            ModernButton saveButton = new ModernButton("Save Changes");
            saveButton.setPreferredSize(new Dimension(150, 35));
            saveButton.addActionListener(e -> {
                // Basic validation
                if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "First name and last name are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update employee
                employee.setFirstName(firstNameField.getText().trim());
                employee.setLastName(lastNameField.getText().trim());
                employee.setBirthday(birthdayField.getText().trim());
                employee.setAddress(addressField.getText().trim());
                employee.setPhoneNumber(phoneField.getText().trim());
                employee.setStatus((String) statusComboBox.getSelectedItem());
                employee.setPosition(positionField.getText().trim());
                employee.setSupervisor(supervisorField.getText().trim());
                
                // Government IDs
                employee.setSssNumber(sssField.getText().trim());
                employee.setPhilhealthNumber(philhealthField.getText().trim());
                employee.setTinNumber(tinField.getText().trim());
                employee.setPagibigNumber(pagibigField.getText().trim());
                
                // Compensation
                try {
                    if (!salaryField.getText().trim().isEmpty()) {
                        employee.setBasicSalary(Double.parseDouble(salaryField.getText().trim()));
                    }
                    if (!riceField.getText().trim().isEmpty()) {
                        employee.setRiceSubsidy(Double.parseDouble(riceField.getText().trim()));
                    }
                    if (!phoneAllowanceField.getText().trim().isEmpty()) {
                        employee.setPhoneAllowance(Double.parseDouble(phoneAllowanceField.getText().trim()));
                    }
                    if (!clothingField.getText().trim().isEmpty()) {
                        employee.setClothingAllowance(Double.parseDouble(clothingField.getText().trim()));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please enter valid numbers for salary and allowances.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update employee in controller/database
                employeeController.updateEmployee(employee);
                
                JOptionPane.showMessageDialog(dialog,
                    "Employee updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                dialog.dispose();
                showEmployeeList(); // Refresh the list
            });
            
            ModernButton cancelButton = new ModernButton("Cancel");
            cancelButton.setPreferredSize(new Dimension(100, 35));
            cancelButton.setButtonColors(new Color(150, 150, 150), new Color(120, 120, 120));
            cancelButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            // Add components to form panel
            formPanel.add(titleLabel, BorderLayout.NORTH);
            formPanel.add(tabbedPane, BorderLayout.CENTER);
            formPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // Add form panel to content panel
            contentPanel.add(formPanel, BorderLayout.CENTER);
            
            // Add content panel to dialog
            dialog.add(contentPanel);
            
            // Display dialog
            dialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteEmployee(int employeeId) {
        try {
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this employee?", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Delete employee
                employeeController.deleteEmployee(employeeId);
                
                // Save changes
                employeeController.saveEmployees();
                
                JOptionPane.showMessageDialog(this, 
                    "Employee deleted successfully", 
                    "Delete Employee", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Delete Employee", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddAttendanceForm() {
        // Create a dialog for adding attendance
        JDialog dialog = new JDialog(this, "Add Attendance Record", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create content panel with gradient background
        GradientPanel contentPanel = new GradientPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        RoundedPanel formPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Add Attendance Record");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Form content
        JPanel formContent = new JPanel(new GridLayout(0, 2, 15, 10));
        formContent.setOpaque(false);
        formContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Employee selection
        formContent.add(new JLabel("Employee:"));
        List<Employee> employees = employeeController.getAllEmployees();
        String[] employeeOptions = new String[employees.size()];
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            employeeOptions[i] = employee.getEmployeeId() + " - " + employee.getFirstName() + " " + employee.getLastName();
        }
        JComboBox<String> employeeComboBox = new JComboBox<>(employeeOptions);
        formContent.add(employeeComboBox);
        
        // Date
        formContent.add(new JLabel("Date:"));
        JTextField dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        formContent.add(dateField);
        
        // Time In
        formContent.add(new JLabel("Time In:"));
        JTextField timeInField = new JTextField("08:00 AM");
        formContent.add(timeInField);
        
        // Time Out
        formContent.add(new JLabel("Time Out:"));
        JTextField timeOutField = new JTextField("05:00 PM");
        formContent.add(timeOutField);
        
        // Status
        formContent.add(new JLabel("Status:"));
        String[] statusOptions = {"Present", "Late", "Absent", "Half Day"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        formContent.add(statusComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        ModernButton saveButton = new ModernButton("Save Record");
        saveButton.setPreferredSize(new Dimension(150, 35));
        saveButton.addActionListener(e -> {
            // Validation and saving would happen here
            JOptionPane.showMessageDialog(dialog,
                "Attendance record added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
                
            dialog.dispose();
            showAttendanceManagement(); // Refresh the view
        });
        
        ModernButton cancelButton = new ModernButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setButtonColors(new Color(150, 150, 150), new Color(120, 120, 120));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add components to form panel
        formPanel.add(titlePanel, BorderLayout.NORTH);
        formPanel.add(formContent, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add form panel to content panel
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add content panel to dialog
        dialog.add(contentPanel);
        
        // Display dialog
        dialog.setVisible(true);
    }
    
    private void showEditAttendanceForm(int rowIndex) {
        // Similar to add attendance form but pre-populated
        JOptionPane.showMessageDialog(this,
            "Edit attendance functionality would be implemented here.",
            "Edit Attendance",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportAttendanceReport() {
        JOptionPane.showMessageDialog(this,
            "Attendance report export functionality would be implemented here.",
            "Export Report",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportReport() {
        JOptionPane.showMessageDialog(this, 
            "Report export functionality would be implemented here.", 
            "Export Report", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void backupData() {
        JOptionPane.showMessageDialog(this, 
            "Data backup functionality would be implemented here.", 
            "Backup Data", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Stop timers
            if (clockTimer != null) {
                clockTimer.stop();
            }
            
            dispose();
            
            // Create and show login form
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm(employeeController);
                loginForm.setVisible(true);
            });
        }
    }
}