package com.motorph.payroll.dao;

import java.io.File;

public class DaoFactory {
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    
    public static EmployeeDao createEmployeeDao() {
        String filePath = PROJECT_PATH + File.separator + "employees.csv";
        return new FileEmployeeDao(filePath);
    }
    
    public static AttendanceDao createAttendanceDao() {
        String filePath = PROJECT_PATH + File.separator + "attendance.csv";
        return new FileAttendanceDao(filePath);
    }
    
    public static UserDao createUserDao() {
        String filePath = PROJECT_PATH + File.separator + "user.csv";
        return new FileUserDao(filePath);
    }
}