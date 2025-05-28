package com.motorph.payroll;

import com.motorph.payroll.controller.AttendanceController;
import com.motorph.payroll.controller.EmployeeController;
import com.motorph.payroll.controller.PayrollController;
import com.motorph.payroll.exception.EmployeeNotFoundException;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import com.motorph.payroll.util.AppConstants;
import com.motorph.payroll.util.DateTimeUtil;
import com.motorph.payroll.view.PayrollView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class that coordinates the controllers and view
 */
public class PayrollApp {
    private PayrollView view;
    private EmployeeController employeeController;
    private AttendanceController attendanceController;
    private PayrollController payrollController;
    private Scanner scanner;
    private boolean isAdminLogin = false;
    private Employee currentEmployee = null;
    
    public PayrollApp(PayrollView view, EmployeeController employeeController, 
                     AttendanceController attendanceController, PayrollController payrollController) {
        this.view = view;
        this.employeeController = employeeController;
        this.attendanceController = attendanceController;
        this.payrollController = payrollController;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Start the application
     */
    public void start() {
        // Display welcome screen
        view.displayWelcomeScreen();
        
        // Attempt login
        if (!login()) {
            view.displayMessage("Too many failed login attempts. System will now exit.");
            view.displayGoodbye();
            return;
        }
        
        // Main application loop
        mainLoop();
        
        // Clean up resources
        view.close();
        view.displayGoodbye();
    }
    
    /**
     * Handle the login process
     * @return true if login is successful
     */
    private boolean login() {
        int attempts = 3;
        
        while (attempts > 0) {
            // Get credentials from view
            String[] credentials = view.promptForLogin();
            String username = credentials[0];
            String password = credentials[1];
            
            // Check admin login
            if (username.equals(AppConstants.ADMIN_USERNAME) && password.equals(AppConstants.ADMIN_PASSWORD)) {
                view.displayMessage("\nLogin successful! Welcome, Administrator.");
                isAdminLogin = true;
                currentEmployee = null;
                return true;
            } 
            // Check employee login
            else {
                Employee employee = employeeController.login(username, password);
                if (employee != null) {
                    view.displayMessage("\nLogin successful! Welcome, " + 
                        employee.getFirstName() + " " + employee.getLastName());
                    isAdminLogin = false;
                    currentEmployee = employee;
                    return true;
                }
            }
            
            // Login failed
            attempts--;
            if (attempts > 0) {
                view.displayMessage("Invalid credentials. You have " + attempts + " attempts remaining.");
            }
        }
        return false;
    }
    
    /**
     * Main application loop
     */
    private void mainLoop() {
        boolean running = true;
        
        while (running) {
            try {
                // Display appropriate menu based on user type
                int choice;
                if (isAdminLogin) {
                    choice = view.displayAdminMenu();
                    if (choice > 0) {
                        processAdminChoice(choice);
                    }
                    // Exit condition
                    if (choice == 7) {
                        running = false;
                    }
                } else {
                    choice = view.displayEmployeeMenu(currentEmployee);
                    if (choice > 0) {
                        processEmployeeChoice(choice);
                    }
                    // Exit condition
                    if (choice == 6) {
                        running = false;
                    }
                }
            } catch (Exception e) {
                view.displayError("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Process admin menu choices
     * @param choice The menu choice
     */
    private void processAdminChoice(int choice) {
        switch (choice) {
            case 1:
                displayEmployeeList();
                break;
            case 2:
                generatePayslip();
                break;
            case 3:
                viewAttendanceRecords();
                break;
            case 4:
                manageEmployees();
                break;
            case 5:
                manageAttendance();
                break;
            case 6:
                // Logout
                if (view.confirmLogout()) {
                    view.displayMessage("\nLogging out admin user...");
                    if (!login()) {
                        view.displayMessage("Login failed after logout.");
                        System.exit(1);
                    }
                }
                break;
            case 7:
                // Exit handled in mainLoop
                break;
            default:
                view.displayMessage("Invalid choice. Please try again.");
        }
    }
    
    /**
     * Process employee menu choices
     * @param choice The menu choice
     */
    private void processEmployeeChoice(int choice) {
        switch (choice) {
            case 1:
                clockInOut();
                break;
            case 2:
                viewMyAttendance();
                break;
            case 3:
                viewMyPayslip();
                break;
            case 4:
                viewMyProfile();
                break;
            case 5:
                // Logout
                if (view.confirmLogout()) {
                    view.displayMessage("\nLogging out employee " + currentEmployee.getEmployeeId() + "...");
                    if (!login()) {
                        view.displayMessage("Login failed after logout.");
                        System.exit(1);
                    }
                }
                break;
            case 6:
                // Exit handled in mainLoop
                break;
            default:
                view.displayMessage("Invalid choice. Please try again.");
        }
    }
    
    /**
     * Display the employee list
     */
    private void displayEmployeeList() {
        List<Employee> employees = employeeController.getAllEmployees();
        view.displayEmployeeList(employees);
        
        String input = view.promptForString("\nOptions: [D]etail View | [B]ack to Main Menu\nEnter your choice: ");
        if (input.equalsIgnoreCase("D")) {
            int empId = view.promptForInt("Enter Employee ID to view details: ");
            try {
                Employee employee = employeeController.getEmployeeById(empId);
                view.displayEmployeeDetails(employee);
            } catch (EmployeeNotFoundException e) {
                view.displayMessage("Employee not found with ID: " + empId);
            }
        }
    }
    
    /**
     * Generate payslip for an employee
     */
    private void generatePayslip() {
        int empId = view.promptForInt("\nEnter Employee ID: ");
        
        try {
            Employee employee = employeeController.getEmployeeById(empId);
            
            // Allow user to specify pay period
            view.displayMessage("\nSelect pay period:");
            view.displayMessage("1. First Half (1-15)");
            view.displayMessage("2. Second Half (16-30/31)");
            view.displayMessage("3. Custom Date Range");
            
            int periodChoice = view.promptForInt("Enter your choice: ");
            
            LocalDate startDate, endDate;
            
            switch (periodChoice) {
                case 1:
                    startDate = DateTimeUtil.getFirstHalfStart();
                    endDate = DateTimeUtil.getFirstHalfEnd();
                    break;
                case 2:
                    startDate = DateTimeUtil.getSecondHalfStart();
                    endDate = DateTimeUtil.getSecondHalfEnd();
                    break;
                case 3:
                    startDate = view.promptForDate("Enter Start Date (MM/DD/YYYY): ");
                    endDate = view.promptForDate("Enter End Date (MM/DD/YYYY): ");
                    
                    if (startDate == null || endDate == null) {
                        view.displayMessage("Error parsing dates. Using current month.");
                        startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                        endDate = DateTimeUtil.getLastDayOfCurrentMonth();
                    }
                    break;
                default:
                    view.displayMessage("Invalid choice. Using current month.");
                    startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                    endDate = DateTimeUtil.getLastDayOfCurrentMonth();
            }
            
            // Calculate payroll for the specified period
            PayrollSummary payrollSummary = payrollController.calculatePayroll(employee, startDate, endDate);
            
            if (payrollSummary.getAttendanceRecords().isEmpty()) {
                view.displayMessage("No attendance records found for the specified period.");
                return;
            }
            
            view.displayPayslip(payrollSummary);
            
            // Option to save payslip
            String saveChoice = view.promptForString("\nDo you want to save this payslip to a file? (Y/N): ");
            
            if (saveChoice.equalsIgnoreCase("Y")) {
                String fileName = String.format(AppConstants.PAYSLIP_FILENAME_PATTERN, 
                    employee.getEmployeeId(),
                    startDate.format(DateTimeFormatter.ofPattern("MMddyyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
                    
                payrollController.savePayslipToFile(payrollSummary, fileName);
            }
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        }
    }
    
    /**
     * View attendance records for an employee
     */
    private void viewAttendanceRecords() {
        int empId = view.promptForInt("\nEnter Employee ID to view attendance: ");
        
        try {
            Employee employee = employeeController.getEmployeeById(empId);
            
            view.displayMessage("\nAttendance Options:");
            view.displayMessage("1. View Attendance for a Specific Period");
            view.displayMessage("2. View All Attendance Records");
            
            int viewChoice = view.promptForInt("Enter your choice: ");
            
            List<Attendance> employeeAttendance = attendanceController.getAttendanceByEmployeeId(empId);
                
            if (employeeAttendance.isEmpty()) {
                view.displayMessage("No attendance records found for this employee.");
                return;
            }
            
            if (viewChoice == 1) {
                // Allow filtering by date range
                view.displayMessage("\nSelect date range:");
                view.displayMessage("1. Current Month");
                view.displayMessage("2. Custom Date Range");
                
                int rangeChoice = view.promptForInt("Enter your choice: ");
                
                LocalDate startDate, endDate;
                
                if (rangeChoice == 2) {
                    startDate = view.promptForDate("Enter Start Date (MM/DD/YYYY): ");
                    endDate = view.promptForDate("Enter End Date (MM/DD/YYYY): ");
                    
                    if (startDate == null || endDate == null) {
                        view.displayMessage("Error parsing dates. Using current month.");
                        startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                        endDate = DateTimeUtil.getLastDayOfCurrentMonth();
                    }
                } else {
                    // Default to current month
                    startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                    endDate = DateTimeUtil.getLastDayOfCurrentMonth();
                }
                
                // Get filtered attendance records
                List<Attendance> filteredAttendance = attendanceController.getAttendanceByDateRange(
                    empId, startDate, endDate);
                
                // Calculate payroll summary with attendance info
                PayrollSummary payrollSummary = payrollController.calculatePayroll(employee, startDate, endDate);
                view.displayAttendanceStatus(payrollSummary);
            } else if (viewChoice == 2) {
                // View all attendance records
                LocalDate minDate = employeeAttendance.stream()
                    .map(Attendance::getDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
                
                LocalDate maxDate = employeeAttendance.stream()
                    .map(Attendance::getDate)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
                
                PayrollSummary payrollSummary = payrollController.calculatePayroll(employee, minDate, maxDate);
                view.displayAllAttendanceRecords(payrollSummary);
            } else {
                view.displayMessage("Invalid choice. Returning to main menu.");
            }
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        }
    }
    
    /**
     * Manage employees (add, edit, delete)
     */
    private void manageEmployees() {
        boolean returnToMain = false;
        
        while (!returnToMain) {
            view.displayEmployeeManagementMenu();
            
            int choice = view.promptForInt("");
            
            switch (choice) {
                case 1:
                    addEmployee();
                    break;
                case 2:
                    editEmployee();
                    break;
                case 3:
                    deleteEmployee();
                    break;
                case 4:
                    saveEmployees();
                    break;
                case 5:
                    returnToMain = true;
                    break;
                default:
                    view.displayMessage("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Add a new employee
     */
    private void addEmployee() {
        int newId = employeeController.generateNewEmployeeId();
        Employee newEmployee = view.promptForNewEmployee(newId);
        employeeController.addEmployee(newEmployee);
        view.displayMessage("Employee added successfully!");
    }
    
    /**
     * Edit an existing employee
     */
    private void editEmployee() {
        int empId = view.promptForInt("\nEnter Employee ID to edit: ");
        
        try {
            Employee employee = employeeController.getEmployeeById(empId);
            
            view.displayMessage("\n================ EDIT EMPLOYEE ================");
            view.displayMessage("Employee: " + employee.getLastName() + ", " + employee.getFirstName());
            view.displayMessage("ID: " + employee.getEmployeeId());
            
            String input = view.promptForString("Last Name [" + employee.getLastName() + "]: ");
            if (!input.isEmpty()) {
                employee.setLastName(input);
            }
            
            input = view.promptForString("First Name [" + employee.getFirstName() + "]: ");
            if (!input.isEmpty()) {
                employee.setFirstName(input);
            }
            
            input = view.promptForString("Birthday [" + employee.getBirthday() + "]: ");
            if (!input.isEmpty()) {
                employee.setBirthday(input);
            }
            
            input = view.promptForString("Address [" + employee.getAddress() + "]: ");
            if (!input.isEmpty()) {
                employee.setAddress(input);
            }
            
            input = view.promptForString("Phone Number [" + employee.getPhoneNumber() + "]: ");
            if (!input.isEmpty()) {
                employee.setPhoneNumber(input);
            }
            
            input = view.promptForString("Position [" + employee.getPosition() + "]: ");
            if (!input.isEmpty()) {
                employee.setPosition(input);
            }
            
            input = view.promptForString("Status [" + employee.getStatus() + "]: ");
            if (!input.isEmpty()) {
                employee.setStatus(input);
            }
            
            input = view.promptForString("Basic Salary [" + employee.getBasicSalary() + "]: ");
            if (!input.isEmpty()) {
                try {
                    double newSalary = Double.parseDouble(input);
                    employee.setBasicSalary(newSalary);
                    
                    // Update derived values
                    employee.setGrossSemiMonthlyRate(newSalary / 2);
                    employee.setHourlyRate((newSalary / AppConstants.WORKING_DAYS_PER_MONTH) / 
                                          AppConstants.REGULAR_HOURS_PER_DAY);
                } catch (NumberFormatException e) {
                    view.displayMessage("Invalid salary format. This field was not updated.");
                }
            }
            
            employeeController.updateEmployee(employee);
            view.displayMessage("Employee information updated successfully!");
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        }
    }
    
    /**
     * Delete an employee
     */
    private void deleteEmployee() {
        int empId = view.promptForInt("\nEnter Employee ID to delete: ");
        
        try {
            Employee employee = employeeController.getEmployeeById(empId);
            
            String confirm = view.promptForString("Are you sure you want to delete " + 
                employee.getFirstName() + " " + employee.getLastName() + "? (Y/N): ");
            
            if (confirm.equalsIgnoreCase("Y")) {
                employeeController.deleteEmployee(empId);
                view.displayMessage("Employee deleted successfully!");
            } else {
                view.displayMessage("Deletion cancelled.");
            }
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        }
    }
    
    /**
     * Save employee data to file
     */
    private void saveEmployees() {
        boolean saved = employeeController.saveEmployees();
        if (saved) {
            view.displayMessage("Employee data saved successfully!");
        } else {
            view.displayMessage("Failed to save employee data. Please check file permissions.");
        }
    }
    
    /**
     * Manage attendance records
     */
    private void manageAttendance() {
        boolean returnToMain = false;
        
        while (!returnToMain) {
            view.displayAttendanceManagementMenu();
            
            int choice = view.promptForInt("");
            
            switch (choice) {
                case 1:
                    addAttendance();
                    break;
                case 2:
                    editAttendance();
                    break;
                case 3:
                    deleteAttendance();
                    break;
                case 4:
                    saveAttendance();
                    break;
                case 5:
                    returnToMain = true;
                    break;
                default:
                    view.displayMessage("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Add a new attendance record
     */
    private void addAttendance() {
        view.displayMessage("\n================ ADD ATTENDANCE RECORD ================");
        
        int empId = view.promptForInt("Employee ID: ");
        
        try {
            // Verify employee exists
            employeeController.getEmployeeById(empId);
            
            LocalDate date = view.promptForDate("Date (MM/DD/YYYY): ");
            if (date == null) {
                view.displayMessage("Invalid date format. Operation cancelled.");
                return;
            }
            
            LocalTime timeIn = view.promptForTime("Time In (HH:MM): ");
            LocalTime timeOut = view.promptForTime("Time Out (HH:MM): ");
            
            if (timeIn == null || timeOut == null) {
                view.displayMessage("Invalid time format. Operation cancelled.");
                return;
            }
            
            // Check if record already exists
            List<Attendance> existingRecords = attendanceController.getAttendanceByDateRange(empId, date, date);
                
            if (!existingRecords.isEmpty()) {
                String overwrite = view.promptForString("Attendance record already exists for this date. Overwrite? (Y/N): ");
                
                if (!overwrite.equalsIgnoreCase("Y")) {
                    view.displayMessage("Operation cancelled.");
                    return;
                }
            }
            
            attendanceController.addAttendance(empId, date, timeIn, timeOut);
            view.displayMessage("Attendance record added successfully!");
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        } catch (Exception e) {
            view.displayMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Edit an existing attendance record
     */
    private void editAttendance() {
        int empId = view.promptForInt("\nEnter Employee ID: ");
        
        try {
            // Verify employee exists
            employeeController.getEmployeeById(empId);
            
            LocalDate date = view.promptForDate("Enter Date (MM/DD/YYYY): ");
            if (date == null) {
                view.displayMessage("Invalid date format. Operation cancelled.");
                return;
            }
            
            List<Attendance> records = attendanceController.getAttendanceByDateRange(empId, date, date);
            
            if (records.isEmpty()) {
                view.displayMessage("Attendance record not found!");
                return;
            }
            
            Attendance record = records.get(0);
            
            view.displayMessage("\n================ EDIT ATTENDANCE ================");
            view.displayMessage("Employee ID: " + record.getEmployeeId());
            view.displayMessage("Date: " + record.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            
            LocalTime timeIn = view.promptForTime("Time In [" + record.getFormattedTimeIn() + "]: ");
            LocalTime timeOut = view.promptForTime("Time Out [" + record.getFormattedTimeOut() + "]: ");
            
            if (timeIn == null) timeIn = record.getTimeIn();
            if (timeOut == null) timeOut = record.getTimeOut();
            
            attendanceController.updateAttendance(empId, date, timeIn, timeOut);
            view.displayMessage("Attendance record updated successfully!");
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        } catch (Exception e) {
            view.displayMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Delete an attendance record
     */
    private void deleteAttendance() {
        int empId = view.promptForInt("\nEnter Employee ID: ");
        
        try {
            // Verify employee exists
            employeeController.getEmployeeById(empId);
            
            LocalDate date = view.promptForDate("Enter Date (MM/DD/YYYY): ");
            if (date == null) {
                view.displayMessage("Invalid date format. Operation cancelled.");
                return;
            }
            
            List<Attendance> records = attendanceController.getAttendanceByDateRange(empId, date, date);
            
            if (records.isEmpty()) {
                view.displayMessage("Attendance record not found!");
                return;
            }
            
            String confirm = view.promptForString("Are you sure you want to delete this attendance record? (Y/N): ");
            
            if (confirm.equalsIgnoreCase("Y")) {
                attendanceController.deleteAttendance(empId, date);
                view.displayMessage("Attendance record deleted successfully!");
            } else {
                view.displayMessage("Deletion cancelled.");
            }
        } catch (EmployeeNotFoundException e) {
            view.displayMessage("Employee not found! Please check the ID and try again.");
        } catch (Exception e) {
            view.displayMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Save attendance data to file
     */
    private void saveAttendance() {
        boolean saved = attendanceController.saveAttendance();
        if (saved) {
            view.displayMessage("Attendance data saved successfully!");
        } else {
            view.displayMessage("Failed to save attendance data. Please check file permissions.");
        }
    }
    
    /**
     * Employee clock in/out function
     */
    private void clockInOut() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        view.displayMessage("\n================ CLOCK IN/OUT ================");
        view.displayMessage("Employee: " + currentEmployee.getFirstName() + " " + currentEmployee.getLastName());
        view.displayMessage("Today's Date: " + today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        view.displayMessage("Current Time: " + currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // Check employee's attendance status
        Attendance todayRecord = attendanceController.getTodayAttendance(currentEmployee.getEmployeeId());
        
        if (todayRecord == null) {
            // No record for today, offer to clock in
            view.displayMessage("\nYou have not clocked in today.");
            String choice = view.promptForString("Do you want to clock in now? (Y/N): ");
            
            if (choice.equalsIgnoreCase("Y")) {
                attendanceController.clockIn(currentEmployee.getEmployeeId());
                view.displayMessage("Successfully clocked in at " + currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                attendanceController.saveAttendance(); // Auto-save after clock in
            } else {
                view.displayMessage("Clock in cancelled.");
            }
        } else if (todayRecord.getTimeOut() == null) {
            // Record exists but no time out, offer to clock out
            view.displayMessage("\nYou clocked in today at " + todayRecord.getFormattedTimeIn());
            String choice = view.promptForString("Do you want to clock out now? (Y/N): ");
            
            if (choice.equalsIgnoreCase("Y")) {
                attendanceController.clockOut(currentEmployee.getEmployeeId());
                
                // Get updated record to display accurate information
                Attendance updatedRecord = attendanceController.getTodayAttendance(currentEmployee.getEmployeeId());
                
                view.displayMessage("Successfully clocked out at " + currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                if (updatedRecord != null) {
                    view.displayMessage(String.format("Total hours worked today: %.2f hours", updatedRecord.getTotalHours()));
                    if (updatedRecord.getOvertimeHours() > 0) {
                        view.displayMessage(String.format("Overtime hours: %.2f hours", updatedRecord.getOvertimeHours()));
                    }
                    if (updatedRecord.getLateMinutes() > 0) {
                        view.displayMessage(String.format("You were late by %.0f minutes", updatedRecord.getLateMinutes()));
                    }
                }
                
                attendanceController.saveAttendance(); // Auto-save after clock out
            } else {
                view.displayMessage("Clock out cancelled.");
            }
        } else {
            // Complete record for today already exists
            view.displayMessage("\nYou have already completed your attendance record for today:");
            view.displayMessage("Time In: " + todayRecord.getFormattedTimeIn());
            view.displayMessage("Time Out: " + todayRecord.getFormattedTimeOut());
            view.displayMessage(String.format("Total Hours: %.2f hours", todayRecord.getTotalHours()));
            
            if (todayRecord.getOvertimeHours() > 0) {
                view.displayMessage(String.format("Overtime Hours: %.2f hours", todayRecord.getOvertimeHours()));
            }
            
            if (todayRecord.getLateMinutes() > 0) {
                view.displayMessage(String.format("Late Minutes: %.0f minutes", todayRecord.getLateMinutes()));
            }
        }
        
        view.promptForString("\nPress Enter to continue...");
    }
    
    /**
     * View employee's own attendance records
     */
    private void viewMyAttendance() {
        List<Attendance> employeeAttendance = attendanceController.getAttendanceByEmployeeId(currentEmployee.getEmployeeId());
            
        if (employeeAttendance.isEmpty()) {
            view.displayMessage("\nNo attendance records found.");
            view.promptForString("\nPress Enter to continue...");
            return;
        }
        
        view.displayMessage("\n================ MY ATTENDANCE RECORDS ================");
        view.displayMessage("1. View Current Month");
        view.displayMessage("2. View Specific Date Range");
        view.displayMessage("3. View All Records");
        
        int choice = view.promptForInt("Enter your choice: ");
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        if (choice == 1) {
            // Current month
            startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
            endDate = DateTimeUtil.getLastDayOfCurrentMonth();
        } else if (choice == 2) {
            // Specific date range
            startDate = view.promptForDate("Enter Start Date (MM/DD/YYYY): ");
            endDate = view.promptForDate("Enter End Date (MM/DD/YYYY): ");
            
            if (startDate == null || endDate == null) {
                view.displayMessage("Error parsing dates. Showing all records instead.");
                choice = 3; // Fall back to showing all records
            }
        }
        // Choice 3 or fallback: All records
        
        // Get records based on choice
        PayrollSummary payrollSummary;
        if (choice == 3) {
            // Find min and max dates for all records
            LocalDate minDate = employeeAttendance.stream()
                .map(Attendance::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            LocalDate maxDate = employeeAttendance.stream()
                .map(Attendance::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            startDate = minDate;
            endDate = maxDate;
            payrollSummary = payrollController.calculatePayroll(currentEmployee, minDate, maxDate);
        } else {
            payrollSummary = payrollController.calculatePayroll(currentEmployee, startDate, endDate);
        }
        
        if (startDate != null && endDate != null) {
            view.displayMessage("\nDate Range: " + 
                startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + " to " + 
                endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }
        
        view.displayAllAttendanceRecords(payrollSummary);
        view.promptForString("\nPress Enter to continue...");
    }
    
    /**
     * View employee's own payslip
     */
    private void viewMyPayslip() {
        view.displayMessage("\n================ MY PAYSLIP ================");
        view.displayMessage("Select pay period:");
        view.displayMessage("1. First Half (1-15) of Current Month");
        view.displayMessage("2. Second Half (16-30/31) of Current Month");
        view.displayMessage("3. Custom Date Range");
        
        int periodChoice = view.promptForInt("Enter your choice: ");
        
        LocalDate startDate, endDate;
        
        switch (periodChoice) {
            case 1:
                startDate = DateTimeUtil.getFirstHalfStart();
                endDate = DateTimeUtil.getFirstHalfEnd();
                break;
            case 2:
                startDate = DateTimeUtil.getSecondHalfStart();
                endDate = DateTimeUtil.getSecondHalfEnd();
                break;
            case 3:
                startDate = view.promptForDate("Enter Start Date (MM/DD/YYYY): ");
                endDate = view.promptForDate("Enter End Date (MM/DD/YYYY): ");
                
                if (startDate == null || endDate == null) {
                    view.displayMessage("Error parsing dates. Using current month.");
                    startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                    endDate = DateTimeUtil.getLastDayOfCurrentMonth();
                }
                break;
            default:
                view.displayMessage("Invalid choice. Using current month.");
                startDate = DateTimeUtil.getFirstDayOfCurrentMonth();
                endDate = DateTimeUtil.getLastDayOfCurrentMonth();
        }
        
        // Calculate payroll for the specified period
        PayrollSummary payrollSummary = payrollController.calculatePayroll(
            currentEmployee, startDate, endDate);
        
        if (payrollSummary.getAttendanceRecords().isEmpty()) {
            view.displayMessage("No attendance records found for the specified period.");
            view.promptForString("\nPress Enter to continue...");
            return;
        }
        
        view.displayPayslip(payrollSummary);
        
        // Option to save payslip
        String saveChoice = view.promptForString("\nDo you want to save this payslip to a file? (Y/N): ");
        
        if (saveChoice.equalsIgnoreCase("Y")) {
            String fileName = String.format(AppConstants.PAYSLIP_FILENAME_PATTERN, 
                currentEmployee.getEmployeeId(),
                startDate.format(DateTimeFormatter.ofPattern("MMddyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
                
            if (payrollController.savePayslipToFile(payrollSummary, fileName)) {
                view.displayMessage("Payslip saved successfully!");
            } else {
                view.displayMessage("Error saving payslip. Please try again.");
            }
        }
        
        view.promptForString("\nPress Enter to continue...");
    }
    
    /**
     * View employee's own profile
     */
    private void viewMyProfile() {
        view.displayEmployeeDetails(currentEmployee);
        
        view.displayMessage("\nLogin Information:");
        view.displayMessage("Username: " + AppConstants.EMPLOYEE_USERNAME_PREFIX + currentEmployee.getEmployeeId());
        view.displayMessage("Password: " + currentEmployee.getEmployeeId());
        view.displayMessage("====================================================");
        
        view.promptForString("\nPress Enter to continue...");
    }
}