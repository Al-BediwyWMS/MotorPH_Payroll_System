package com.motorph.payroll;

import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.dao.DaoFactory;
import com.motorph.payroll.service.EmployeeService;
import com.motorph.payroll.service.EmployeeServiceImpl;
import com.motorph.payroll.view.gui.LoginForm;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Check if required CSV files exist
        checkRequiredFiles();
        
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Load custom fonts
            loadCustomFonts();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create EmployeeController for login
        EmployeeService employeeService = new EmployeeServiceImpl(DaoFactory.createEmployeeDao());
        EmployeeController employeeController = new EmployeeController(employeeService);
        
        // Launch the GUI application
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm(employeeController);
            loginForm.setVisible(true);
        });
    }
    
    private static void checkRequiredFiles() {
        String[] requiredFiles = {"employees.csv", "attendance.csv", "user.csv"};
        boolean allFilesExist = true;
        
        for (String fileName : requiredFiles) {
            File file = new File(fileName);
            if (!file.exists()) {
                System.err.println("Required file missing: " + fileName);
                allFilesExist = false;
            }
        }
        
        if (!allFilesExist) {
            JOptionPane.showMessageDialog(null, 
                "Required CSV files are missing. Please ensure employees.csv, attendance.csv, and user.csv exist in the project root directory.",
                "Missing Files",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private static void loadCustomFonts() {
        try {
            // Try to load Montserrat from system fonts first
            String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            boolean montserratExists = false;
            
            for (String fontName : fontNames) {
                if (fontName.equals("Montserrat")) {
                    montserratExists = true;
                    break;
                }
            }
            
            // If Montserrat is not in system fonts, try to load from file
            if (!montserratExists) {
                // Try to load from resources first
                try {
                    Font montserratRegular = Font.createFont(Font.TRUETYPE_FONT, 
                        Main.class.getResourceAsStream("/fonts/Montserrat-Regular.ttf"));
                    Font montserratBold = Font.createFont(Font.TRUETYPE_FONT, 
                        Main.class.getResourceAsStream("/fonts/Montserrat-Bold.ttf"));
                    
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(montserratRegular);
                    ge.registerFont(montserratBold);
                } catch (Exception e) {
                    // If resources fail, try direct file path
                    try {
                        Font montserratRegular = Font.createFont(Font.TRUETYPE_FONT, 
                            new File("resources/fonts/Montserrat-Regular.ttf"));
                        Font montserratBold = Font.createFont(Font.TRUETYPE_FONT, 
                            new File("resources/fonts/Montserrat-Bold.ttf"));
                        
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        ge.registerFont(montserratRegular);
                        ge.registerFont(montserratBold);
                    } catch (Exception ex) {
                        System.out.println("Could not load Montserrat font from file: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load Montserrat font: " + e.getMessage());
            // Fallback to system fonts
        }
    }
}