package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.AttendanceController;
import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.controller.PayrollController;
import com.motorph.payroll.dao.DaoFactory;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
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
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmployeeDashboard extends JFrame {

    private final EmployeeController employeeController;
    private AttendanceController attendanceController;
    private PayrollController payrollController;
    private final Employee employee;
    
    private JPanel contentPanel;
    private JLabel profileImageLabel;
    private JLabel clockLabel;
    private Timer clockTimer;
    private JLabel notificationCountLabel;
    private NotificationPopup notificationPopup;
    private JLabel notificationLabel;
    
    public EmployeeDashboard(EmployeeController employeeController, Employee employee) {
        this.employeeController = employeeController;
        this.employee = employee;
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
        setTitle("MotorPH Payroll System - Employee Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));
        
        // Create notification popup
        notificationPopup = new NotificationPopup();
        
        // Create main panel with gradient background
        GradientPanel mainPanel = new GradientPanel(new BorderLayout());
        
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
        
        // Show default view (dashboard)
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
        
        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
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
        notificationCountLabel = new JLabel("3");
        notificationCountLabel.setFont(new Font("Montserrat", Font.BOLD, 10));
        notificationCountLabel.setForeground(Color.WHITE);
        notificationCountLabel.setBackground(Color.RED);
        notificationCountLabel.setOpaque(true);
        notificationCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notificationCountLabel.setVerticalAlignment(SwingConstants.CENTER);
        notificationCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        notificationPanel.add(notificationCountLabel, BorderLayout.EAST);
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + employee.getFirstName());
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
        
        userPanel.add(notificationPanel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(welcomeLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(logoutButton);
        
        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
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
        
        // Profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setOpaque(false);
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Profile image with proper dimensions
        ImageIcon profileIcon = ImageHelper.loadImage("/images/profile_default.png", 
                                                   "resources/images/profile_default.png", 
                                                   120, 120);
        
        if (profileIcon == null) {
            // Create default profile image with initials
            String initials = "";
            if (employee.getFirstName() != null && !employee.getFirstName().isEmpty()) {
                initials += employee.getFirstName().charAt(0);
            }
            if (employee.getLastName() != null && !employee.getLastName().isEmpty()) {
                initials += employee.getLastName().charAt(0);
            }
            profileIcon = ImageHelper.createInitialsProfileImage(initials, 120, 
                                                              new Color(52, 152, 219), 
                                                              Color.WHITE);
        }
        
        profileImageLabel = new JLabel(profileIcon);
        profileImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        // User name
        JLabel userNameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        userNameLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Position
        JLabel positionLabel = new JLabel(employee.getPosition());
        positionLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        positionLabel.setForeground(Color.WHITE);
        positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Clock
        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to profile panel
        profilePanel.add(profileImageLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        profilePanel.add(userNameLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(positionLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(clockLabel);
        
        // Menu section
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Create menu buttons with modern style
        JButton dashboardButton = createMenuButton("Dashboard", "🏠");
        JButton clockInOutButton = createMenuButton("Clock In/Out", "⏱️");
        JButton attendanceButton = createMenuButton("My Attendance", "📅");
        JButton payslipButton = createMenuButton("My Payslip", "💰");
        JButton profileButton = createMenuButton("My Profile", "👤");
        JButton logoutButton = createMenuButton("Logout", "🚪");
        
        // Add action listeners
        dashboardButton.addActionListener(e -> showDashboard());
        clockInOutButton.addActionListener(e -> showClockInOut());
        attendanceButton.addActionListener(e -> showAttendance());
        payslipButton.addActionListener(e -> showPayslip());
        profileButton.addActionListener(e -> showProfile());
        logoutButton.addActionListener(e -> logout());
        
        // Add buttons to menu panel with spacing
        menuPanel.add(dashboardButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(clockInOutButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(attendanceButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(payslipButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(profileButton);
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
        
        JLabel titleLabel = new JLabel("Employee Dashboard");
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
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        
        // Get today's attendance
        Attendance todayAttendance = attendanceController.getTodayAttendance(employee.getEmployeeId());
        String hoursToday = todayAttendance != null ? String.format("%.2f hrs", todayAttendance.getTotalHours()) : "0.00 hrs";
        
        // Cards
        DashboardCard hoursCard = new DashboardCard("Hours Today", hoursToday, new Color(52, 152, 219));
        DashboardCard attendanceCard = new DashboardCard("Attendance This Month", "22 days", new Color(46, 204, 113));
        DashboardCard salaryCard = new DashboardCard("Monthly Salary", String.format("₱%,.2f", employee.getBasicSalary()), new Color(155, 89, 182));
        
        cardsPanel.add(hoursCard);
        cardsPanel.add(attendanceCard);
        cardsPanel.add(salaryCard);
        
        // Attendance status
        JPanel attendanceStatusPanel = createAttendanceStatusPanel(todayAttendance);
        
        // Notifications panel
        JPanel notificationsPanel = createNotificationsPanel();
        
        // Add panels to dashboard content
        dashboardContent.add(cardsPanel, BorderLayout.NORTH);
        
        // Create a panel for the two bottom sections
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        bottomPanel.add(attendanceStatusPanel);
        bottomPanel.add(notificationsPanel);
        
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
    
    private JPanel createAttendanceStatusPanel(Attendance todayAttendance) {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Today's Attendance");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        if (todayAttendance == null) {
            JLabel statusLabel = new JLabel("You have not clocked in today.");
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            ModernButton clockInButton = new ModernButton("Clock In Now");
            clockInButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            clockInButton.setMaximumSize(new Dimension(200, 40));
            clockInButton.addActionListener(e -> {
                attendanceController.clockIn(employee.getEmployeeId());
                JOptionPane.showMessageDialog(this, 
                    "Successfully clocked in!",
                    "Clock In",
                    JOptionPane.INFORMATION_MESSAGE);
                attendanceController.saveAttendance();
                showDashboard(); // Refresh
            });
            
            contentPanel.add(statusLabel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(clockInButton);
        } else if (todayAttendance.getTimeOut() == null) {
            JLabel statusLabel = new JLabel("You clocked in at: " + todayAttendance.getFormattedTimeIn());
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            ModernButton clockOutButton = new ModernButton("Clock Out Now");
            clockOutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            clockOutButton.setMaximumSize(new Dimension(200, 40));
            clockOutButton.addActionListener(e -> {
                attendanceController.clockOut(employee.getEmployeeId());
                JOptionPane.showMessageDialog(this, 
                    "Successfully clocked out!",
                    "Clock Out",
                    JOptionPane.INFORMATION_MESSAGE);
                attendanceController.saveAttendance();
                showDashboard(); // Refresh
            });
            
            contentPanel.add(statusLabel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(clockOutButton);
        } else {
            JLabel statusLabel = new JLabel("Your attendance for today is complete.");
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JPanel timePanel = new JPanel(new GridLayout(4, 2, 10, 5));
            timePanel.setOpaque(false);
            
            timePanel.add(new JLabel("Time In:"));
            timePanel.add(new JLabel(todayAttendance.getFormattedTimeIn()));
            
            timePanel.add(new JLabel("Time Out:"));
            timePanel.add(new JLabel(todayAttendance.getFormattedTimeOut()));
            
            timePanel.add(new JLabel("Total Hours:"));
            timePanel.add(new JLabel(String.format("%.2f", todayAttendance.getTotalHours())));
            
            if (todayAttendance.getOvertimeHours() > 0) {
                timePanel.add(new JLabel("Overtime Hours:"));
                timePanel.add(new JLabel(String.format("%.2f", todayAttendance.getOvertimeHours())));
            }
            
            contentPanel.add(statusLabel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(timePanel);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNotificationsPanel() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        JPanel notificationsContent = new JPanel();
        notificationsContent.setLayout(new BoxLayout(notificationsContent, BoxLayout.Y_AXIS));
        notificationsContent.setOpaque(false);
        notificationsContent.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Sample notifications
        addNotification(notificationsContent, "Welcome to MotorPH Payroll System!", "Just now");
        addNotification(notificationsContent, "Your attendance for yesterday has been recorded.", "1 day ago");
        addNotification(notificationsContent, "New payslip available for May 2025", "2 days ago");
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JButton viewAllButton = new JButton("View All");
        viewAllButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        viewAllButton.setBorderPainted(false);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setContentAreaFilled(false);
        viewAllButton.setForeground(new Color(52, 152, 219));
        viewAllButton.addActionListener(e -> {
            notificationLabel.dispatchEvent(new java.awt.event.MouseEvent(
                notificationLabel,
                java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                notificationLabel.getX(),
                notificationLabel.getY(),
                1,
                false));
        });
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(viewAllButton, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(notificationsContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addNotification(JPanel container, String message, String timeAgo) {
        JPanel notifPanel = new JPanel(new BorderLayout());
        notifPanel.setOpaque(false);
        notifPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 0, 5, 0),
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230))));
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Montserrat", Font.PLAIN, 13));
        
        JLabel timeLabel = new JLabel(timeAgo);
        timeLabel.setFont(new Font("Montserrat", Font.ITALIC, 11));
        timeLabel.setForeground(Color.GRAY);
        
        notifPanel.add(msgLabel, BorderLayout.CENTER);
        notifPanel.add(timeLabel, BorderLayout.SOUTH);
        
        notifPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        notifPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        container.add(notifPanel);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private void showClockInOut() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create clock in/out panel
        RoundedPanel clockPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        clockPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Clock In / Clock Out");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        // Current date and time
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dateTimePanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel(today.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        dateTimePanel.add(dateLabel);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(dateTimePanel, BorderLayout.EAST);
        
        // Clock in/out content
        JPanel clockContent = new JPanel(new BorderLayout());
        clockContent.setOpaque(false);
        clockContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Current time display
        JPanel timeDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timeDisplayPanel.setOpaque(false);
        
        JLabel currentTimeLabel = new JLabel(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        currentTimeLabel.setFont(new Font("Montserrat", Font.BOLD, 48));
        timeDisplayPanel.add(currentTimeLabel);
        
        // Update the time label every second
        Timer timeTimer = new Timer(1000, e -> {
            currentTimeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        timeTimer.start();
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Get today's attendance
        Attendance todayAttendance = attendanceController.getTodayAttendance(employee.getEmployeeId());
        
        // Create rounded panel for status
        RoundedPanel statusContentPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        statusContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel statusInfoPanel = new JPanel();
        statusInfoPanel.setLayout(new BoxLayout(statusInfoPanel, BoxLayout.Y_AXIS));
        statusInfoPanel.setOpaque(false);
        statusInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel statusTitle = new JLabel("Attendance Status");
        statusTitle.setFont(new Font("Montserrat", Font.BOLD, 20));
        statusTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusInfoPanel.add(statusTitle);
        statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        
        if (todayAttendance == null) {
            JLabel statusLabel = new JLabel("You have not clocked in today.");
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            ModernButton clockInButton = new ModernButton("Clock In");
            clockInButton.setPreferredSize(new Dimension(150, 50));
            clockInButton.setFont(new Font("Montserrat", Font.BOLD, 16));
            
            clockInButton.addActionListener(e -> {
                attendanceController.clockIn(employee.getEmployeeId());
                JOptionPane.showMessageDialog(this, 
                    "Successfully clocked in!",
                    "Clock In",
                    JOptionPane.INFORMATION_MESSAGE);
                attendanceController.saveAttendance();
                showClockInOut(); // Refresh
            });
            
            statusInfoPanel.add(statusLabel);
            statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            buttonPanel.add(clockInButton);
            statusInfoPanel.add(buttonPanel);
            
        } else if (todayAttendance.getTimeOut() == null) {
            JLabel statusLabel = new JLabel("You clocked in today at " + todayAttendance.getFormattedTimeIn());
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Calculate time since clock in
            LocalTime timeIn = todayAttendance.getTimeIn();
            LocalTime currentTime = LocalTime.now();
            long hoursSince = java.time.Duration.between(timeIn, currentTime).toHours();
            long minutesSince = java.time.Duration.between(timeIn, currentTime).toMinutes() % 60;
            
            JLabel durationLabel = new JLabel(String.format("Time elapsed: %d hours, %d minutes", hoursSince, minutesSince));
            durationLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            ModernButton clockOutButton = new ModernButton("Clock Out");
            clockOutButton.setPreferredSize(new Dimension(150, 50));
            clockOutButton.setFont(new Font("Montserrat", Font.BOLD, 16));
            
            clockOutButton.addActionListener(e -> {
                attendanceController.clockOut(employee.getEmployeeId());
                JOptionPane.showMessageDialog(this, 
                    "Successfully clocked out!",
                    "Clock Out",
                    JOptionPane.INFORMATION_MESSAGE);
                attendanceController.saveAttendance();
                showClockInOut(); // Refresh
            });
            
            statusInfoPanel.add(statusLabel);
            statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            statusInfoPanel.add(durationLabel);
            statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            buttonPanel.add(clockOutButton);
            statusInfoPanel.add(buttonPanel);
            
        } else {
            JLabel statusLabel = new JLabel("Your attendance for today is complete.");
            statusLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel timeInfoPanel = new JPanel(new GridLayout(3, 2, 20, 10));
            timeInfoPanel.setOpaque(false);
            timeInfoPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
            
            JLabel timeInLabel = new JLabel("Time In:");
            timeInLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
            
            JLabel timeInValueLabel = new JLabel(todayAttendance.getFormattedTimeIn());
            timeInValueLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            
            JLabel timeOutLabel = new JLabel("Time Out:");
            timeOutLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
            
            JLabel timeOutValueLabel = new JLabel(todayAttendance.getFormattedTimeOut());
            timeOutValueLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            
            JLabel totalHoursLabel = new JLabel("Total Hours:");
            totalHoursLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
            
            JLabel totalHoursValueLabel = new JLabel(String.format("%.2f", todayAttendance.getTotalHours()));
            totalHoursValueLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
            
            timeInfoPanel.add(timeInLabel);
            timeInfoPanel.add(timeInValueLabel);
            timeInfoPanel.add(timeOutLabel);
            timeInfoPanel.add(timeOutValueLabel);
            timeInfoPanel.add(totalHoursLabel);
            timeInfoPanel.add(totalHoursValueLabel);
            
            statusInfoPanel.add(statusLabel);
            statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            statusInfoPanel.add(timeInfoPanel);
            
            // Add completed message
            JLabel completedLabel = new JLabel("You've completed your work for today!");
            completedLabel.setFont(new Font("Montserrat", Font.ITALIC, 14));
            completedLabel.setForeground(new Color(46, 204, 113));
            completedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            statusInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            statusInfoPanel.add(completedLabel);
        }
        
        statusContentPanel.add(statusInfoPanel, BorderLayout.CENTER);
        statusPanel.add(statusContentPanel, BorderLayout.CENTER);
        
        clockContent.add(timeDisplayPanel, BorderLayout.NORTH);
        clockContent.add(statusPanel, BorderLayout.CENTER);
        
        // Add components to clock panel
        clockPanel.add(titlePanel, BorderLayout.NORTH);
        clockPanel.add(clockContent, BorderLayout.CENTER);
        
        // Add clock panel to content panel
        contentPanel.add(clockPanel, BorderLayout.CENTER);
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showAttendance() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create attendance panel
        RoundedPanel attendancePanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        attendancePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Attendance Records");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        
        JLabel periodLabel = new JLabel("View Period:");
        periodLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        String[] periodOptions = {
            "Current Month", 
            "Previous Month", 
            "Custom Date Range"
        };
        JComboBox<String> periodComboBox = new JComboBox<>(periodOptions);
        periodComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        ModernButton viewButton = new ModernButton("View");
        viewButton.setPreferredSize(new Dimension(100, 30));
        
        controlPanel.add(periodLabel);
        controlPanel.add(periodComboBox);
        controlPanel.add(viewButton);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(controlPanel, BorderLayout.EAST);
        
        // Attendance content
        JPanel attendanceContent = new JPanel(new BorderLayout());
        attendanceContent.setOpaque(false);
        attendanceContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Date range panel
        JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateRangePanel.setOpaque(false);
        
        LocalDate startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
        LocalDate endDate = DateTimeUtil.getLastDayOfCurrentMonth();
        
        JLabel dateRangeLabel = new JLabel("Date Range: " + 
            startDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " to " + 
            endDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateRangeLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        
        dateRangePanel.add(dateRangeLabel);
        
        // Get attendance records
        List<Attendance> records = attendanceController.getAttendanceByDateRange(
            employee.getEmployeeId(), startDate, endDate);
        
        // If no records, create sample data for display
        if (records == null || records.isEmpty()) {
            // Sample data for demonstration
            records = createSampleAttendanceRecords();
        }
        
        // Create table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Create table model with attendance data
        String[] columnNames = {"Date", "Time In", "Time Out", "Total Hours", "OT Hours", "Late (min)", "Status"};
        Object[][] data = new Object[records.size()][7];
        
        for (int i = 0; i < records.size(); i++) {
            Attendance record = records.get(i);
            data[i][0] = record.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            data[i][1] = record.getFormattedTimeIn();
            data[i][2] = record.getFormattedTimeOut();
            data[i][3] = String.format("%.2f", record.getTotalHours());
            data[i][4] = String.format("%.2f", record.getOvertimeHours());
            data[i][5] = String.format("%.0f", record.getLateMinutes());
            data[i][6] = record.getStatus();
        }
        
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable attendanceTable = new JTable(tableModel);
        attendanceTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
        attendanceTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 12));
        attendanceTable.setRowHeight(25);
        attendanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        attendanceTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Summary panel
        RoundedPanel summaryPanel = new RoundedPanel(new GridLayout(1, 4, 10, 0), new Color(255, 255, 255, 220));
        summaryPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Calculate totals
        double totalHours = 0.0;
        double totalOvertimeHours = 0.0;
        double totalLateMinutes = 0.0;
        
        for (Attendance record : records) {
            totalHours += record.getTotalHours();
            totalOvertimeHours += record.getOvertimeHours();
            totalLateMinutes += record.getLateMinutes();
        }
        
        JLabel recordsLabel = new JLabel("Records: " + records.size());
        recordsLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        
        JLabel hoursLabel = new JLabel("Total Hours: " + String.format("%.2f", totalHours));
        hoursLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        JLabel overtimeLabel = new JLabel("OT Hours: " + String.format("%.2f", totalOvertimeHours));
        overtimeLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        JLabel lateLabel = new JLabel("Late Minutes: " + String.format("%.0f", totalLateMinutes));
        lateLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        summaryPanel.add(recordsLabel);
        summaryPanel.add(hoursLabel);
        summaryPanel.add(overtimeLabel);
        summaryPanel.add(lateLabel);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        ModernButton exportButton = new ModernButton("Export to PDF");
        exportButton.setPreferredSize(new Dimension(150, 35));
        exportButton.addActionListener(e -> exportAttendance());
        
        actionPanel.add(exportButton);
        
        // Add components to table panel
        tablePanel.add(dateRangePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(summaryPanel, BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);
        
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add components to attendance content
        attendanceContent.add(tablePanel, BorderLayout.CENTER);
        
        // Add components to attendance panel
        attendancePanel.add(titlePanel, BorderLayout.NORTH);
        attendancePanel.add(attendanceContent, BorderLayout.CENTER);
        
        // Add attendance panel to content panel
        contentPanel.add(attendancePanel, BorderLayout.CENTER);
        
        // Add action listener to view button
        viewButton.addActionListener(e -> {
            String selectedPeriod = (String) periodComboBox.getSelectedItem();
            LocalDate newStartDate;
            LocalDate newEndDate;
            
            if (selectedPeriod.equals("Current Month")) {
                newStartDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                newEndDate = DateTimeUtil.getLastDayOfCurrentMonth();
                updateAttendanceView(newStartDate, newEndDate, dateRangeLabel, tableModel);
            } else if (selectedPeriod.equals("Previous Month")) {
                LocalDate now = LocalDate.now();
                newStartDate = now.minusMonths(1).withDayOfMonth(1);
                newEndDate = now.withDayOfMonth(1).minusDays(1);
                updateAttendanceView(newStartDate, newEndDate, dateRangeLabel, tableModel);
            } else {
                // Show date picker dialog for custom range
                showDateRangeDialog(dateRangeLabel, tableModel);
            }
        });
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private List<Attendance> createSampleAttendanceRecords() {
        List<Attendance> records = new ArrayList<>();
        
        // Get the current month's dates for sample data
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        
        // Add records for the first few days of the month
        for (int day = 1; day <= 5; day++) {
            LocalDate date = startOfMonth.plusDays(day - 1);
            
            // Skip weekends
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            
            // Create sample times
            LocalTime timeIn = LocalTime.of(8, (day % 2 == 0) ? 15 : 0);
            LocalTime timeOut = LocalTime.of(17, (day % 2 == 0) ? 15 : 0);
            
            // Create attendance record with constructor
            Attendance attendance = new Attendance(
                employee.getEmployeeId(), 
                date, 
                timeIn, 
                timeOut
            );
            
            // Calculate total hours (simple approximation)
            double totalHours = 8.0;
            double overtimeHours = (day % 3 == 0) ? 1.0 : 0.0;
            double lateMinutes = (day % 2 == 0) ? 15.0 : 0.0;
            
            // Add calculated values using reflection (fix for lack of setters)
            try {
                java.lang.reflect.Field totalHoursField = Attendance.class.getDeclaredField("totalHours");
                totalHoursField.setAccessible(true);
                totalHoursField.set(attendance, totalHours);
                
                java.lang.reflect.Field overtimeHoursField = Attendance.class.getDeclaredField("overtimeHours");
                overtimeHoursField.setAccessible(true);
                overtimeHoursField.set(attendance, overtimeHours);
                
                java.lang.reflect.Field lateMinutesField = Attendance.class.getDeclaredField("lateMinutes");
                lateMinutesField.setAccessible(true);
                lateMinutesField.set(attendance, lateMinutes);
                
                java.lang.reflect.Field statusField = Attendance.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(attendance, (lateMinutes > 0) ? "Late" : "Present");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            records.add(attendance);
        }
        
        return records;
    }
    
    private void updateAttendanceView(LocalDate startDate, LocalDate endDate, JLabel dateRangeLabel, DefaultTableModel tableModel) {
        // Update date range label
        dateRangeLabel.setText("Date Range: " + 
            startDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " to " + 
            endDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        // Get attendance records for new date range
        List<Attendance> records = attendanceController.getAttendanceByDateRange(
            employee.getEmployeeId(), startDate, endDate);
        
        // If no records, create sample data for display
        if (records == null || records.isEmpty()) {
            // Sample data for demonstration
            records = createSampleAttendanceRecords();
        }
        
        // Clear existing table data
        tableModel.setRowCount(0);
        
        // Add new data
        for (Attendance record : records) {
            Object[] rowData = {
                record.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                record.getFormattedTimeIn(),
                record.getFormattedTimeOut(),
                String.format("%.2f", record.getTotalHours()),
                String.format("%.2f", record.getOvertimeHours()),
                String.format("%.0f", record.getLateMinutes()),
                record.getStatus()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void showDateRangeDialog(JLabel dateRangeLabel, DefaultTableModel tableModel) {
        // Create dialog for date range selection
        JDialog dialog = new JDialog(this, "Select Date Range", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel startLabel = new JLabel("Start Date (MM/DD/YYYY):");
        JTextField startField = new JTextField(10);
        startField.setText(LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        
        JLabel endLabel = new JLabel("End Date (MM/DD/YYYY):");
        JTextField endField = new JTextField(10);
        endField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        
        formPanel.add(startLabel);
        formPanel.add(startField);
        formPanel.add(endLabel);
        formPanel.add(endField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            try {
                // Parse dates
                LocalDate startDate = LocalDate.parse(startField.getText(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                LocalDate endDate = LocalDate.parse(endField.getText(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                
                // Update attendance view
                updateAttendanceView(startDate, endDate, dateRangeLabel, tableModel);
                
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid date format. Please use MM/DD/YYYY format.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to content panel
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add content panel to dialog
        dialog.add(contentPanel);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    private void exportAttendance() {
        JOptionPane.showMessageDialog(this,
            "Attendance export functionality would be implemented here.",
            "Export Attendance",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showPayslip() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create payslip panel
        RoundedPanel payslipPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        payslipPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Payslip");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        
        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        String[] periodOptions = {
            "First Half (1-15) of Current Month", 
            "Second Half (16-30/31) of Current Month", 
            "Previous Month",
            "Custom Date Range"
        };
        JComboBox<String> periodComboBox = new JComboBox<>(periodOptions);
        periodComboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        ModernButton viewButton = new ModernButton("View Payslip");
        viewButton.setPreferredSize(new Dimension(150, 30));
        
        controlPanel.add(periodLabel);
        controlPanel.add(periodComboBox);
        controlPanel.add(viewButton);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(controlPanel, BorderLayout.EAST);
        
        // Payslip list content
        JPanel payslipContent = new JPanel(new BorderLayout());
        payslipContent.setOpaque(false);
        payslipContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Create a list of available payslips
        RoundedPanel payslipListPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        payslipListPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel listTitleLabel = new JLabel("Available Payslips");
        listTitleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        // Create table model for payslips
        String[] columnNames = {"Pay Period", "Date Issued", "Net Pay", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add sample payslips
        model.addRow(new Object[]{"May 1-15, 2025", "May 15, 2025", "₱24,200.00", "Issued"});
        model.addRow(new Object[]{"April 16-30, 2025", "April 30, 2025", "₱24,500.00", "Issued"});
        model.addRow(new Object[]{"April 1-15, 2025", "April 15, 2025", "₱24,200.00", "Issued"});
        model.addRow(new Object[]{"March 16-31, 2025", "March 31, 2025", "₱24,500.00", "Issued"});
        
        JTable payslipTable = new JTable(model);
        payslipTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
        payslipTable.getTableHeader().setFont(new Font("Montserrat", Font.BOLD, 12));
        payslipTable.setRowHeight(30);
        payslipTable.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(payslipTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        ModernButton viewSelectedButton = new ModernButton("View Selected");
        viewSelectedButton.setPreferredSize(new Dimension(150, 35));
        viewSelectedButton.addActionListener(e -> {
            int selectedRow = payslipTable.getSelectedRow();
            if (selectedRow >= 0) {
                String payPeriod = (String) payslipTable.getValueAt(selectedRow, 0);
                showPayslipDetail(payPeriod);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a payslip to view.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        ModernButton downloadButton = new ModernButton("Download PDF");
        downloadButton.setPreferredSize(new Dimension(150, 35));
        downloadButton.addActionListener(e -> {
            int selectedRow = payslipTable.getSelectedRow();
            if (selectedRow >= 0) {
                JOptionPane.showMessageDialog(this,
                    "Payslip PDF download functionality would be implemented here.",
                    "Download Payslip",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a payslip to download.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        actionPanel.add(viewSelectedButton);
        actionPanel.add(downloadButton);
        
        // Add components to payslip list panel
        payslipListPanel.add(listTitleLabel, BorderLayout.NORTH);
        payslipListPanel.add(scrollPane, BorderLayout.CENTER);
        payslipListPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Add payslip list panel to content panel
        payslipContent.add(payslipListPanel, BorderLayout.CENTER);
        
        // Add components to payslip panel
        payslipPanel.add(titlePanel, BorderLayout.NORTH);
        payslipPanel.add(payslipContent, BorderLayout.CENTER);
        
        // Add payslip panel to content panel
        contentPanel.add(payslipPanel, BorderLayout.CENTER);
        
        // Add action listener to view button
        viewButton.addActionListener(e -> {
            String selectedPeriod = (String) periodComboBox.getSelectedItem();
            if (selectedPeriod.equals("First Half (1-15) of Current Month")) {
                showPayslipDetail("May 1-15, 2025");
            } else if (selectedPeriod.equals("Second Half (16-30/31) of Current Month")) {
                showPayslipDetail("May 16-31, 2025");
            } else if (selectedPeriod.equals("Previous Month")) {
                showPayslipDetail("April 1-30, 2025");
            } else {
                // Show date picker dialog for custom range
                JOptionPane.showMessageDialog(this,
                    "Custom date range selection would be implemented here.",
                    "Custom Date Range",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showPayslipDetail(String payPeriod) {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create payslip view panel
        RoundedPanel payslipViewPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        payslipViewPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Payslip: " + payPeriod);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        JButton backButton = new JButton("Back to Payslip List");
        backButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        backButton.addActionListener(e -> showPayslip());
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(backButton, BorderLayout.EAST);
        
        // Payslip content
        RoundedPanel payslipContent = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 230));
        payslipContent.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel companyLabel = new JLabel("MOTORPH CORPORATION");
        companyLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        companyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel payslipTitleLabel = new JLabel("EMPLOYEE PAYSLIP");
        payslipTitleLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        payslipTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel periodLabel = new JLabel("Pay Period: " + payPeriod);
        periodLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        periodLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setOpaque(false);
        headingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        companyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        payslipTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        periodLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headingPanel.add(companyLabel);
        headingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headingPanel.add(payslipTitleLabel);
        headingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headingPanel.add(periodLabel);
        
        headerPanel.add(headingPanel, BorderLayout.CENTER);
        
        // Employee details
        JPanel employeeDetailsPanel = new JPanel(new GridLayout(4, 4, 15, 10));
        employeeDetailsPanel.setOpaque(false);
        employeeDetailsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        addLabelValuePair(employeeDetailsPanel, "Employee ID:", String.valueOf(employee.getEmployeeId()));
        addLabelValuePair(employeeDetailsPanel, "Name:", employee.getFirstName() + " " + employee.getLastName());
        addLabelValuePair(employeeDetailsPanel, "Position:", employee.getPosition());
        var departmentText = null != employee.getDepartment() ? employee.getDepartment() : "IT Department";
        addLabelValuePair(employeeDetailsPanel, "Department:", (String) departmentText);
        
        // Main content divided into two columns
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContentPanel.setOpaque(false);
        
        // Left column - Earnings
        JPanel earningsPanel = new JPanel();
        earningsPanel.setLayout(new BoxLayout(earningsPanel, BoxLayout.Y_AXIS));
        earningsPanel.setOpaque(false);
        
        JLabel earningsTitle = new JLabel("Earnings");
        earningsTitle.setFont(new Font("Montserrat", Font.BOLD, 16));
        earningsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel earningsTablePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        earningsTablePanel.setOpaque(false);
        earningsTablePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        addLabelValuePair(earningsTablePanel, "Basic Salary:", "₱25,000.00");
        addLabelValuePair(earningsTablePanel, "Rice Subsidy:", "₱1,500.00");
        addLabelValuePair(earningsTablePanel, "Phone Allowance:", "₱1,000.00");
        addLabelValuePair(earningsTablePanel, "Clothing Allowance:", "₱1,000.00");
        addLabelValuePair(earningsTablePanel, "Overtime Pay:", "₱0.00");
        addLabelValuePair(earningsTablePanel, "Gross Earnings:", "₱28,500.00", true);
        
        earningsPanel.add(earningsTitle);
        earningsPanel.add(earningsTablePanel);
        
        // Right column - Deductions
        JPanel deductionsPanel = new JPanel();
        deductionsPanel.setLayout(new BoxLayout(deductionsPanel, BoxLayout.Y_AXIS));
        deductionsPanel.setOpaque(false);
        
        JLabel deductionsTitle = new JLabel("Deductions");
        deductionsTitle.setFont(new Font("Montserrat", Font.BOLD, 16));
        deductionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel deductionsTablePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        deductionsTablePanel.setOpaque(false);
        deductionsTablePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        addLabelValuePair(deductionsTablePanel, "SSS Contribution:", "₱1,125.00");
        addLabelValuePair(deductionsTablePanel, "PhilHealth:", "₱375.00");
        addLabelValuePair(deductionsTablePanel, "Pag-IBIG Fund:", "₱300.00");
        addLabelValuePair(deductionsTablePanel, "Withholding Tax:", "₱2,500.00");
        addLabelValuePair(deductionsTablePanel, "Other Deductions:", "₱0.00");
        addLabelValuePair(deductionsTablePanel, "Total Deductions:", "₱4,300.00", true);
        
        deductionsPanel.add(deductionsTitle);
        deductionsPanel.add(deductionsTablePanel);
        
        mainContentPanel.add(earningsPanel);
        mainContentPanel.add(deductionsPanel);
        
        // Net pay section
        JPanel netPayPanel = new JPanel(new BorderLayout());
        netPayPanel.setOpaque(false);
        netPayPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        RoundedPanel netPayContentPanel = new RoundedPanel(new BorderLayout(), new Color(41, 128, 185, 220));
        netPayContentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel netPayLabel = new JLabel("NET PAY:");
        netPayLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        netPayLabel.setForeground(Color.WHITE);
        
        JLabel netPayAmountLabel = new JLabel("₱24,200.00");
        netPayAmountLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        netPayAmountLabel.setForeground(Color.WHITE);
        
        netPayContentPanel.add(netPayLabel, BorderLayout.WEST);
        netPayContentPanel.add(netPayAmountLabel, BorderLayout.EAST);
        
        netPayPanel.add(netPayContentPanel, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        ModernButton printButton = new ModernButton("Print Payslip");
        printButton.setPreferredSize(new Dimension(150, 35));
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Print functionality would be implemented here.",
                "Print Payslip",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        ModernButton downloadButton = new ModernButton("Download PDF");
        downloadButton.setPreferredSize(new Dimension(150, 35));
        downloadButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "PDF download functionality would be implemented here.",
                "Download Payslip",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        actionPanel.add(printButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(downloadButton);
        
        // Add components to payslip content
        JPanel payslipMainContent = new JPanel();
        payslipMainContent.setLayout(new BoxLayout(payslipMainContent, BoxLayout.Y_AXIS));
        payslipMainContent.setOpaque(false);
        
        payslipMainContent.add(headerPanel);
        payslipMainContent.add(employeeDetailsPanel);
        payslipMainContent.add(mainContentPanel);
        payslipMainContent.add(netPayPanel);
        payslipMainContent.add(actionPanel);
        
        payslipContent.add(payslipMainContent, BorderLayout.CENTER);
        
        // Add components to payslip panel
        payslipViewPanel.add(titlePanel, BorderLayout.NORTH);
        payslipViewPanel.add(payslipContent, BorderLayout.CENTER);
        
        // Add payslip panel to content panel
        contentPanel.add(payslipViewPanel, BorderLayout.CENTER);
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void addLabelValuePair(JPanel panel, String label, String value) {
        addLabelValuePair(panel, label, value, false);
    }
    
    private void addLabelValuePair(JPanel panel, String label, String value, boolean isBold) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Montserrat", Font.BOLD, 13));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Montserrat", isBold ? Font.BOLD : Font.PLAIN, 13));
        
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    private void showProfile() {
        // Clear content panel
        contentPanel.removeAll();
        
        // Create profile panel
        RoundedPanel profilePanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 180));
        profilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        
        JButton editButton = new JButton("Edit Profile");
        editButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        editButton.addActionListener(e -> showEditProfileForm());
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(editButton, BorderLayout.EAST);
        
        // Profile content in a scroll pane
        JPanel profileContent = new JPanel(new BorderLayout());
        profileContent.setOpaque(false);
        profileContent.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Profile header with photo and basic info
        JPanel profileHeaderPanel = new JPanel(new BorderLayout());
        profileHeaderPanel.setOpaque(false);
        profileHeaderPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Profile photo panel
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.setOpaque(false);
        
        // Use the same profile image from sidebar
        ImageIcon profileIcon = ImageHelper.loadImage("/images/profile_default.png", 
                                                   "resources/images/profile_default.png", 
                                                   120, 120);
        
        if (profileIcon == null) {
            // Create default profile image with initials
            String initials = "";
            if (employee.getFirstName() != null && !employee.getFirstName().isEmpty()) {
                initials += employee.getFirstName().charAt(0);
            }
            if (employee.getLastName() != null && !employee.getLastName().isEmpty()) {
                initials += employee.getLastName().charAt(0);
            }
            profileIcon = ImageHelper.createInitialsProfileImage(initials, 120, 
                                                             new Color(52, 152, 219), 
                                                             Color.WHITE);
        }
        
        JLabel profilePhotoLabel = new JLabel(profileIcon);
        profilePhotoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        photoPanel.add(profilePhotoLabel);
        
        // Basic info panel
        JPanel basicInfoPanel = new JPanel();
        basicInfoPanel.setLayout(new BoxLayout(basicInfoPanel, BoxLayout.Y_AXIS));
        basicInfoPanel.setOpaque(false);
        basicInfoPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        nameLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel positionLabel = new JLabel(employee.getPosition());
        positionLabel.setFont(new Font("Montserrat", Font.PLAIN, 18));
        positionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel employeeIdLabel = new JLabel("Employee ID: " + employee.getEmployeeId());
        employeeIdLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        employeeIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        basicInfoPanel.add(nameLabel);
        basicInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        basicInfoPanel.add(positionLabel);
        basicInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        basicInfoPanel.add(employeeIdLabel);
        
        profileHeaderPanel.add(photoPanel, BorderLayout.WEST);
        profileHeaderPanel.add(basicInfoPanel, BorderLayout.CENTER);
        
        // Profile details in tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Montserrat", Font.PLAIN, 14));
        
        // Personal Information panel
        RoundedPanel personalInfoPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        personalInfoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel personalDetailsPanel = new JPanel(new GridLayout(5, 2, 20, 15));
        personalDetailsPanel.setOpaque(false);
        
        addLabelValuePair(personalDetailsPanel, "Birthday:", employee.getBirthday());
        addLabelValuePair(personalDetailsPanel, "Address:", employee.getAddress());
        addLabelValuePair(personalDetailsPanel, "Phone Number:", employee.getPhoneNumber());
        addLabelValuePair(personalDetailsPanel, "Status:", employee.getStatus());
        addLabelValuePair(personalDetailsPanel, "Supervisor:", employee.getSupervisor());
        
        personalInfoPanel.add(personalDetailsPanel, BorderLayout.NORTH);
        
        // Government IDs panel
        RoundedPanel governmentIdsPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        governmentIdsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel governmentDetailsPanel = new JPanel(new GridLayout(4, 2, 20, 15));
        governmentDetailsPanel.setOpaque(false);
        
        addLabelValuePair(governmentDetailsPanel, "SSS Number:", employee.getSssNumber());
        addLabelValuePair(governmentDetailsPanel, "PhilHealth Number:", employee.getPhilhealthNumber());
        addLabelValuePair(governmentDetailsPanel, "TIN Number:", employee.getTinNumber());
        addLabelValuePair(governmentDetailsPanel, "Pag-IBIG Number:", employee.getPagibigNumber());
        
        governmentIdsPanel.add(governmentDetailsPanel, BorderLayout.NORTH);
        
        // Compensation panel
        RoundedPanel compensationPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        compensationPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel compensationDetailsPanel = new JPanel(new GridLayout(6, 2, 20, 15));
        compensationDetailsPanel.setOpaque(false);
        
        addLabelValuePair(compensationDetailsPanel, "Basic Salary:", String.format("₱%,.2f", employee.getBasicSalary()));
        addLabelValuePair(compensationDetailsPanel, "Rice Subsidy:", String.format("₱%,.2f", employee.getRiceSubsidy()));
        addLabelValuePair(compensationDetailsPanel, "Phone Allowance:", String.format("₱%,.2f", employee.getPhoneAllowance()));
        addLabelValuePair(compensationDetailsPanel, "Clothing Allowance:", String.format("₱%,.2f", employee.getClothingAllowance()));
        addLabelValuePair(compensationDetailsPanel, "Gross Semi-Monthly Rate:", String.format("₱%,.2f", employee.getGrossSemiMonthlyRate()));
        addLabelValuePair(compensationDetailsPanel, "Hourly Rate:", String.format("₱%,.2f", employee.getHourlyRate()));
        
        compensationPanel.add(compensationDetailsPanel, BorderLayout.NORTH);
        
        // Account Settings panel
        RoundedPanel accountPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        accountPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel accountDetailsPanel = new JPanel(new GridLayout(2, 2, 20, 15));
        accountDetailsPanel.setOpaque(false);
        
        addLabelValuePair(accountDetailsPanel, "Username:", AppConstants.EMPLOYEE_USERNAME_PREFIX + employee.getEmployeeId());
        addLabelValuePair(accountDetailsPanel, "Password:", "********");
        
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setOpaque(false);
        passwordPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(new Font("Montserrat", Font.PLAIN, 12));
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        
        passwordPanel.add(changePasswordButton);
        
        accountPanel.add(accountDetailsPanel, BorderLayout.NORTH);
        accountPanel.add(passwordPanel, BorderLayout.CENTER);
        
        // Add panels to tabs
        tabbedPane.addTab("Personal Information", personalInfoPanel);
        tabbedPane.addTab("Government IDs", governmentIdsPanel);
        tabbedPane.addTab("Compensation", compensationPanel);
        tabbedPane.addTab("Account Settings", accountPanel);
        
        // Add components to profile content
        profileContent.add(profileHeaderPanel, BorderLayout.NORTH);
        profileContent.add(tabbedPane, BorderLayout.CENTER);
        
        // Add components to profile panel
        profilePanel.add(titlePanel, BorderLayout.NORTH);
        profilePanel.add(profileContent, BorderLayout.CENTER);
        
        // Add profile panel to content panel
        contentPanel.add(profilePanel, BorderLayout.CENTER);
        
        // Update UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showEditProfileForm() {
        // Create a dialog for editing profile
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create content panel
        GradientPanel contentPanel = new GradientPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Form panel
        RoundedPanel formPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Edit Personal Information");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        fieldsPanel.add(new JLabel("Phone Number:"));
        JTextField phoneField = new JTextField(employee.getPhoneNumber());
        fieldsPanel.add(phoneField);
        
        fieldsPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField(employee.getAddress());
        fieldsPanel.add(addressField);
        
        fieldsPanel.add(new JLabel("Emergency Contact:"));
        JTextField emergencyContactField = new JTextField();
        fieldsPanel.add(emergencyContactField);
        
        fieldsPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        fieldsPanel.add(emailField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            // Save changes
            employee.setPhoneNumber(phoneField.getText().trim());
            employee.setAddress(addressField.getText().trim());
            
            // Update employee in controller
            employeeController.updateEmployee(employee);
            
            JOptionPane.showMessageDialog(dialog,
                "Profile updated successfully!",
                "Profile Updated",
                JOptionPane.INFORMATION_MESSAGE);
                
            dialog.dispose();
            showProfile(); // Refresh profile view
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add components to form panel
        formPanel.add(titleLabel, BorderLayout.NORTH);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add form panel to content panel
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add content panel to dialog
        dialog.add(contentPanel);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    private void showChangePasswordDialog() {
        // Create a dialog for changing password
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        // Create content panel
        GradientPanel contentPanel = new GradientPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Form panel
        RoundedPanel formPanel = new RoundedPanel(new BorderLayout(), new Color(255, 255, 255, 220));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        fieldsPanel.add(new JLabel("Current Password:"));
        JPasswordField currentPasswordField = new JPasswordField();
        fieldsPanel.add(currentPasswordField);
        
        fieldsPanel.add(new JLabel("New Password:"));
        JPasswordField newPasswordField = new JPasswordField();
        fieldsPanel.add(newPasswordField);
        
        fieldsPanel.add(new JLabel("Confirm Password:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        fieldsPanel.add(confirmPasswordField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Change Password");
        saveButton.addActionListener(e -> {
            // Get password values
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "All fields are required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog,
                    "New password and confirm password do not match.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // In a real app, this would validate the current password and update it
            JOptionPane.showMessageDialog(dialog,
                "Password changed successfully!",
                "Password Changed",
                JOptionPane.INFORMATION_MESSAGE);
                
            dialog.dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add components to form panel
        formPanel.add(titleLabel, BorderLayout.NORTH);
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add form panel to content panel
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add content panel to dialog
        dialog.add(contentPanel);
        
        // Show dialog
        dialog.setVisible(true);
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