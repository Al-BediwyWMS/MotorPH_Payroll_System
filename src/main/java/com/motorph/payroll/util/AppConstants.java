package com.motorph.payroll.util;

/**
 * Application-wide constants
 */
public class AppConstants {
    // Authentication constants
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String EMPLOYEE_USERNAME_PREFIX = "Employee";
    
    // Payroll calculation constants
    public static final double SSS_RATE = 0.045;
    public static final double PHILHEALTH_RATE = 0.04;
    public static final double PAGIBIG_RATE = 0.02;
    public static final double OVERTIME_RATE = 1.25;
    public static final double REGULAR_HOURS_PER_DAY = 8.0;
    public static final int WORKING_DAYS_PER_MONTH = 22;
    
    // Max deduction limits
    public static final double MAX_SSS_CONTRIBUTION = 1125.0;
    public static final double MAX_PHILHEALTH_CONTRIBUTION = 1800.0;
    public static final double MAX_PAGIBIG_CONTRIBUTION = 100.0;
    
    // Standard work hours
    public static final int STANDARD_START_HOUR = 8;
    public static final int STANDARD_START_MINUTE = 0;
    public static final int STANDARD_END_HOUR = 17;
    public static final int STANDARD_END_MINUTE = 0;
    
    // Tax brackets (annual)
    public static final double TAX_BRACKET_1 = 250000.0; // 0%
    public static final double TAX_BRACKET_2 = 400000.0; // 20%
    public static final double TAX_BRACKET_3 = 800000.0; // 25%
    public static final double TAX_BRACKET_4 = 2000000.0; // 30%
    public static final double TAX_BRACKET_5 = 8000000.0; // 32%
    
    // Pay periods per year
    public static final int PAY_PERIODS_PER_YEAR = 24;
    
    // File naming patterns
    public static final String PAYSLIP_FILENAME_PATTERN = "Payslip_%d_%s_%s.txt";
    
    // Application info
    public static final String APP_NAME = "MotorPH Payroll System";
    public static final String APP_VERSION = "1.0";
    public static final String APP_COPYRIGHT = "© 2024 MotorPH";
}