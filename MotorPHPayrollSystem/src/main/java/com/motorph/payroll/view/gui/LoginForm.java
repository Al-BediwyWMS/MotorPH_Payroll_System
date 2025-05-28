package com.motorph.payroll.view.gui;

import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.util.AppConstants;
import com.motorph.payroll.util.ImageHelper;
import com.motorph.payroll.view.gui.components.GradientPanel;
import com.motorph.payroll.view.gui.components.ModernButton;
import com.motorph.payroll.view.gui.components.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginForm extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private final EmployeeController employeeController;
    
    public LoginForm(EmployeeController employeeController) {
        this.employeeController = employeeController;
        initializeUI();
    }
    
    private void initializeUI() {
        // Set up the frame
        setTitle("MotorPH Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create main panel with gradient background
        GradientPanel mainPanel = new GradientPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create logo panel
        JPanel logoPanel = createLogoPanel();
        
        // Create login form panel with scrollable content to ensure it fits on smaller screens
        JPanel outerFormPanel = new JPanel(new BorderLayout());
        outerFormPanel.setOpaque(false);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 40, 0, 40));
        
        // Create the rounded panel for login form
        RoundedPanel loginPanel = new RoundedPanel(new GridBagLayout(), new Color(255, 255, 255, 230));
        loginPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Login title
        JLabel loginTitleLabel = new JLabel("Login to Your Account");
        loginTitleLabel.setFont(new Font("Montserrat", Font.BOLD, 20));
        loginTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(loginTitleLabel, gbc);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Montserrat", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        loginPanel.add(usernameLabel, gbc);
        
        gbc.gridy = 2;
        loginPanel.add(usernameField, gbc);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Montserrat", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        gbc.gridy = 3;
        loginPanel.add(passwordLabel, gbc);
        
        gbc.gridy = 4;
        loginPanel.add(passwordField, gbc);
        
        // Login button
        loginButton = new ModernButton("Login");
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setFont(new Font("Montserrat", Font.BOLD, 14));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 8, 8, 8);
        loginPanel.add(buttonPanel, gbc);
        
        // Exit button as text link
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Montserrat", Font.PLAIN, 14));
        exitButton.setForeground(new Color(41, 128, 185));
        exitButton.setContentAreaFilled(false);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exitPanel.setOpaque(false);
        exitPanel.add(exitButton);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 8, 8, 8);
        loginPanel.add(exitPanel, gbc);
        
        // Maintain aspect ratio of login panel
        Dimension loginSize = new Dimension(400, 380);
        loginPanel.setMinimumSize(loginSize);
        loginPanel.setPreferredSize(loginSize);
        
        // Add login panel to form panel with centering
        formPanel.add(Box.createVerticalGlue());
        
        JPanel centeringPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centeringPanel.setOpaque(false);
        centeringPanel.add(loginPanel);
        
        formPanel.add(centeringPanel);
        formPanel.add(Box.createVerticalGlue());
        
        // Make form panel scrollable if needed
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        outerFormPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        JLabel copyrightLabel = new JLabel(AppConstants.APP_COPYRIGHT + " | " + AppConstants.APP_VERSION);
        copyrightLabel.setFont(new Font("Montserrat", Font.PLAIN, 12));
        copyrightLabel.setForeground(Color.WHITE);
        footerPanel.add(copyrightLabel);
        
        // Add panels to main panel
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(outerFormPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Add event listeners
        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> System.exit(0));
        
        // Make Enter key work for login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        
        // Load logo with proper aspect ratio
        ImageIcon logoIcon = ImageHelper.loadImage("/images/motorph_logo.png", 
                                                 "resources/images/motorph_logo.png", 
                                                 300, 100);
        
        // Create logo label
        JLabel logoLabel;
        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon);
        } else {
            // If logo can't be loaded, create text-based logo
            ImageIcon textLogo = ImageHelper.createTextLogo("MOTORPH", 300, 80, 
                                                         Color.WHITE, 
                                                         new Color(0, 0, 0, 0), 
                                                         "Montserrat", 36, Font.BOLD);
            logoLabel = new JLabel(textLogo);
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create tagline
        JLabel taglineLabel = new JLabel("THE FILIPINO'S CHOICE");
        taglineLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        taglineLabel.setForeground(Color.WHITE);
        taglineLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to logo panel
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        logoPanel.add(taglineLabel, BorderLayout.SOUTH);
        
        return logoPanel;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password cannot be empty!", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check admin login
        if (username.equals(AppConstants.ADMIN_USERNAME) && 
            password.equals(AppConstants.ADMIN_PASSWORD)) {
            JOptionPane.showMessageDialog(this, 
                "Admin login successful!", 
                "Login Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open admin dashboard
            openAdminDashboard();
            return;
        }
        
        // Check employee login
        Employee employee = employeeController.login(username, password);
        if (employee != null) {
            JOptionPane.showMessageDialog(this, 
                "Welcome, " + employee.getFirstName() + " " + employee.getLastName() + "!", 
                "Login Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open employee dashboard
            openEmployeeDashboard(employee);
            return;
        }
        
        // Login failed
        JOptionPane.showMessageDialog(this, 
            "Invalid username or password!", 
            "Login Failed", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void openAdminDashboard() {
        // Hide login form
        setVisible(false);
        
        // Open admin dashboard
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard(employeeController);
            dashboard.setVisible(true);
        });
        
        // Dispose login form
        dispose();
    }
    
    private void openEmployeeDashboard(Employee employee) {
        // Hide login form
        setVisible(false);
        
        // Open employee dashboard
        SwingUtilities.invokeLater(() -> {
            EmployeeDashboard dashboard = new EmployeeDashboard(employeeController, employee);
            dashboard.setVisible(true);
        });
        
        // Dispose login form
        dispose();
    }
}