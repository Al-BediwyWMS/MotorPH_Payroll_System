package com.motorph.payroll;

import com.motorph.payroll.controller.AttendanceController;
import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.controller.PayrollController;
import com.motorph.payroll.dao.AttendanceDao;
import com.motorph.payroll.dao.DaoFactory;
import com.motorph.payroll.dao.EmployeeDao;
import com.motorph.payroll.service.AttendanceService;
import com.motorph.payroll.service.AttendanceServiceImpl;
import com.motorph.payroll.service.EmployeeService;
import com.motorph.payroll.service.EmployeeServiceImpl;
import com.motorph.payroll.service.PayrollService;
import com.motorph.payroll.service.PayrollServiceImpl;
import com.motorph.payroll.view.PayrollView;

public class Main {
    public static void main(String[] args) {
        // Create DAOs
        EmployeeDao employeeDao = DaoFactory.createEmployeeDao();
        AttendanceDao attendanceDao = DaoFactory.createAttendanceDao();
        
        // Create Services
        EmployeeService employeeService = new EmployeeServiceImpl(employeeDao);
        AttendanceService attendanceService = new AttendanceServiceImpl(attendanceDao);
        PayrollService payrollService = new PayrollServiceImpl(attendanceDao);
        
        // Create Controllers
        EmployeeController employeeController = new EmployeeController(employeeService);
        AttendanceController attendanceController = new AttendanceController(attendanceService);
        PayrollController payrollController = new PayrollController(payrollService, employeeService);
        
        // Create View
        PayrollView view = new PayrollView();
        
        // Start the application
        PayrollApp app = new PayrollApp(view, employeeController, attendanceController, payrollController);
        app.start();
    }
}