package com.motorph.payroll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String EMPLOYEE_USERNAME_PREFIX = "Employee";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private static boolean isAdminLogin = false;
    private static Employee currentEmployee = null;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        displayWelcomeScreen();
        
        String projectPath = System.getProperty("user.dir");
        String employeeFilePath = projectPath + File.separator + "employees.csv";
        String attendanceFilePath = projectPath + File.separator + "attendance.csv";
        
        List<Employee> employees = null;
        List<Attendance> attendanceRecords = null;
        
        try {
            // Redirect standard output and error to suppress loading messages
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            
            // Create a null output stream
            PrintStream nullOut = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    // Do nothing - discard all output
                }
            });
            
            // Redirect standard output and error
            System.setOut(nullOut);
            System.setErr(nullOut);
            
            // Load data silently
            employees = FileHandler.readEmployees(employeeFilePath);
            attendanceRecords = FileHandler.readAttendance(attendanceFilePath);
            
            // Restore original output streams
            System.setOut(originalOut);
            System.setErr(originalErr);
            
            // Just show minimal loading completion message
            if (employees.isEmpty()) {
                System.out.println("No employee data was loaded. Please check the data files.");
                System.exit(1);
            }
            
            if (attendanceRecords.isEmpty()) {
                System.out.println("Warning: No attendance records were loaded.");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            System.err.println("Please ensure the data files are in the correct location and format.");
            System.exit(1);
        }
        
        // Continue with login
        if (!login(scanner, employees)) {
            System.out.println("Too many failed login attempts. System will now exit.");
            System.exit(1);
        }

        while (true) {
            try {
                if (isAdminLogin) {
                    displayAdminMainMenu();
                } else {
                    displayEmployeeMainMenu();
                }
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer

                if (isAdminLogin) {
                    // Admin menu options
                    switch (choice) {
                        case 1:
                            displayEmployeeList(employees, scanner);
                            break;
                        case 2:
                            generatePayslip(employees, attendanceRecords, scanner);
                            break;
                        case 3:
                            viewAttendanceRecords(employees, attendanceRecords, scanner);
                            break;
                        case 4:
                            manageEmployees(employees, scanner);
                            break;
                        case 5:
                            manageAttendance(employees, attendanceRecords, scanner);
                            break;
                        case 6:
                            // Logout
                            if (confirmLogout(scanner)) {
                                System.out.println("\nLogging out admin user...");
                                if (login(scanner, employees)) {
                                    continue;
                                } else {
                                    System.out.println("Too many failed login attempts. System will now exit.");
                                    displayGoodbye();
                                    scanner.close();
                                    return;
                                }
                            }
                            break;
                        case 7:
                            System.out.println("\nThank you for using MotorPH Payroll System!");
                            displayGoodbye();
                            scanner.close();
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                    // Employee menu options
                    switch (choice) {
                        case 1:
                            clockInOut(currentEmployee, attendanceRecords, scanner);
                            saveAttendance(attendanceRecords); // Auto-save after clock in/out
                            break;
                        case 2:
                            viewMyAttendance(currentEmployee, attendanceRecords, scanner);
                            break;
                        case 3:
                            viewMyPayslip(currentEmployee, attendanceRecords, scanner);
                            break;
                        case 4:
                            viewMyProfile(currentEmployee, scanner);
                            break;
                        case 5:
                            // Logout
                            if (confirmLogout(scanner)) {
                                System.out.println("\nLogging out employee " + currentEmployee.getEmployeeId() + "...");
                                if (login(scanner, employees)) {
                                    continue;
                                } else {
                                    System.out.println("Too many failed login attempts. System will now exit.");
                                    displayGoodbye();
                                    scanner.close();
                                    return;
                                }
                            }
                            break;
                        case 6:
                            System.out.println("\nThank you for using MotorPH Payroll System!");
                            displayGoodbye();
                            scanner.close();
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
                scanner.nextLine(); // Clear scanner buffer
            }
        }
    }

    private static void displayWelcomeScreen() {
        System.out.println("\n=====================================================");
        System.out.println("||                 WELCOME TO                       ||");
        System.out.println("||            MOTORPH PAYROLL SYSTEM               ||");
        System.out.println("=====================================================");
        System.out.println("            © 2024 MotorPH. Version 1.0            \n");
    }

    private static void displayGoodbye() {
        System.out.println("\n=====================================================");
        System.out.println("||               THANK YOU FOR USING                ||");
        System.out.println("||            MOTORPH PAYROLL SYSTEM               ||");
        System.out.println("=====================================================");
        System.out.println("                    GOODBYE!                        \n");
    }

    private static void displayAdminMainMenu() {
        System.out.println("\n================ MOTORPH PAYROLL SYSTEM ================");
        System.out.println("                 ADMINISTRATOR MENU                    ");
        System.out.println("====================================================");
        System.out.println("1. View Employee List");
        System.out.println("2. Generate Payslip");
        System.out.println("3. View Attendance Records");
        System.out.println("4. Manage Employees");
        System.out.println("5. Manage Attendance");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
        System.out.println("====================================================");
        System.out.print("Enter your choice: ");
    }
    
    private static void displayEmployeeMainMenu() {
        System.out.println("\n================ MOTORPH PAYROLL SYSTEM ================");
        System.out.printf("                 EMPLOYEE PORTAL - %s %s           \n",
            currentEmployee.getFirstName(), currentEmployee.getLastName());
        System.out.println("====================================================");
        System.out.println("1. Clock In/Out");
        System.out.println("2. View My Attendance Records");
        System.out.println("3. View My Payslip");
        System.out.println("4. View My Profile");
        System.out.println("5. Logout");
        System.out.println("6. Exit");
        System.out.println("====================================================");
        System.out.print("Enter your choice: ");
    }

    private static boolean login(Scanner scanner, List<Employee> employees) {
        int attempts = 3;
        
        while (attempts > 0) {
            System.out.print("\nUsername: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            // Check for admin login
            if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                System.out.println("\nLogin successful! Welcome, Administrator.");
                isAdminLogin = true;
                currentEmployee = null;
                return true;
            } 
            // Check for employee login
            else if (username.startsWith(EMPLOYEE_USERNAME_PREFIX)) {
                String idStr = username.substring(EMPLOYEE_USERNAME_PREFIX.length());
                try {
                    int empId = Integer.parseInt(idStr);
                    // Employee password should match their ID
                    if (password.equals(idStr)) {
                        // Find the employee
                        Employee employee = employees.stream()
                            .filter(emp -> emp.getEmployeeId() == empId)
                            .findFirst()
                            .orElse(null);
                            
                        if (employee != null) {
                            System.out.println("\nLogin successful! Welcome, " + employee.getFirstName() + " " + employee.getLastName());
                            isAdminLogin = false;
                            currentEmployee = employee;
                            return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid employee username format
                }
            }
            
            // If we get here, login failed
            attempts--;
            if (attempts > 0) {
                System.out.println("Invalid credentials. You have " + attempts + " attempts remaining.");
            }
        }
        return false;
    }
    
    private static boolean confirmLogout(Scanner scanner) {
        System.out.print("\nAre you sure you want to logout? (Y/N): ");
        String choice = scanner.nextLine().trim().toUpperCase();
        return choice.equals("Y") || choice.equals("YES");
    }

    private static void displayEmployeeList(List<Employee> employees, Scanner scanner) {
        if (employees == null || employees.isEmpty()) {
            System.out.println("No employees found in the database.");
            return;
        }
        
        while (true) {
            displayEmployeePage(employees);
            
            System.out.println("\nOptions: [D]etail View | [B]ack to Main Menu");
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (input.equals("D") || input.equals("DETAIL")) {
                System.out.print("Enter Employee ID to view details: ");
                try {
                    int empId = scanner.nextInt();
                    scanner.nextLine(); // Clear buffer
                    displayEmployeeDetails(employees, empId);
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a valid Employee ID.");
                    scanner.nextLine(); // Clear buffer
                }
            } else if (input.equals("B") || input.equals("BACK")) {
                return;
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private static void displayEmployeePage(List<Employee> employees) {
        System.out.println("\n================ EMPLOYEE LIST ================");
        System.out.println("Total Employees: " + employees.size());
        System.out.println("====================================================");
        
        // Print table header with more spacing for Position
        System.out.printf("%-8s | %-15s | %-15s | %-30s | %-15s\n", 
            "ID", "Last Name", "First Name", "Position", "Status");
        System.out.println("----------------------------------------------------------------------");
        
        // Sort employees by ID for consistent display
        employees.sort((e1, e2) -> Integer.compare(e1.getEmployeeId(), e2.getEmployeeId()));
        
        // Display all employees
        for (Employee emp : employees) {
            System.out.printf("%-8d | %-15s | %-15s | %-30s | %-15s\n", 
                emp.getEmployeeId(), 
                truncateString(emp.getLastName(), 15),
                truncateString(emp.getFirstName(), 15),
                truncateString(emp.getPosition(), 30),
                truncateString(emp.getStatus(), 15));
        }
        
        System.out.println("====================================================");
        System.out.println("Enter 'D' to view detailed employee information");
    }
    
    private static void displayEmployeeDetails(List<Employee> employees, int employeeId) {
        Employee employee = employees.stream()
            .filter(emp -> emp.getEmployeeId() == employeeId)
            .findFirst()
            .orElse(null);
            
        if (employee == null) {
            System.out.println("Employee not found with ID: " + employeeId);
            return;
        }
        
        System.out.println("\n================ EMPLOYEE DETAILS ================");
        System.out.println("Employee ID: " + employee.getEmployeeId());
        System.out.println("Full Name: " + employee.getLastName() + ", " + employee.getFirstName());
        System.out.println("Birthday: " + employee.getBirthday());
        System.out.println("Address: " + employee.getAddress());
        System.out.println("Phone Number: " + employee.getPhoneNumber());
        
        System.out.println("\nEmployment Information:");
        System.out.println("Status: " + employee.getStatus());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Supervisor: " + employee.getSupervisor());
        
        System.out.println("\nGovernment IDs:");
        System.out.println("SSS Number: " + employee.getSssNumber());
        System.out.println("PhilHealth Number: " + employee.getPhilhealthNumber());
        System.out.println("TIN Number: " + employee.getTinNumber());
        System.out.println("Pag-IBIG Number: " + employee.getPagibigNumber());
        
        System.out.println("\nCompensation:");
        System.out.printf("Basic Salary: PHP %,.2f\n", employee.getBasicSalary());
        System.out.printf("Rice Subsidy: PHP %,.2f\n", employee.getRiceSubsidy());
        System.out.printf("Phone Allowance: PHP %,.2f\n", employee.getPhoneAllowance());
        System.out.printf("Clothing Allowance: PHP %,.2f\n", employee.getClothingAllowance());
        System.out.printf("Gross Semi-Monthly Rate: PHP %,.2f\n", employee.getGrossSemiMonthlyRate());
        System.out.printf("Hourly Rate: PHP %,.2f\n", employee.getHourlyRate());
        
        System.out.println("====================================================");
        System.out.print("Press Enter to continue...");
        new Scanner(System.in).nextLine();
    }
    
    // Helper method to truncate long strings for display
    private static String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private static void generatePayslip(List<Employee> employees, List<Attendance> attendanceRecords, Scanner scanner) {
        if (employees == null || employees.isEmpty() || attendanceRecords == null || attendanceRecords.isEmpty()) {
            System.out.println("No data available to generate payslip.");
            return;
        }

        System.out.print("\nEnter Employee ID: ");
        final int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer

        Employee employee = employees.stream()
            .filter(emp -> emp.getEmployeeId() == empId)
            .findFirst()
            .orElse(null);

        if (employee == null) {
            System.out.println("Employee not found! Please check the ID and try again.");
            return;
        }

        // Allow user to specify pay period
        System.out.println("\nSelect pay period:");
        System.out.println("1. First Half (1-15)");
        System.out.println("2. Second Half (16-30/31)");
        System.out.println("3. Custom Date Range");
        System.out.print("Enter your choice: ");
        
        int periodChoice = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        LocalDate startDate, endDate;
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        
        switch (periodChoice) {
            case 1:
                startDate = LocalDate.of(currentYear, currentMonth, 1);
                endDate = LocalDate.of(currentYear, currentMonth, 15);
                break;
            case 2:
                startDate = LocalDate.of(currentYear, currentMonth, 16);
                endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                break;
            case 3:
                System.out.print("Enter Start Date (MM/DD/YYYY): ");
                String startDateStr = scanner.nextLine();
                System.out.print("Enter End Date (MM/DD/YYYY): ");
                String endDateStr = scanner.nextLine();
                
                try {
                    startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                    endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                } catch (Exception e) {
                    System.out.println("Error parsing dates. Using current month.");
                    startDate = LocalDate.of(currentYear, currentMonth, 1);
                    endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                }
                break;
            default:
                System.out.println("Invalid choice. Using current month.");
                startDate = LocalDate.of(currentYear, currentMonth, 1);
                endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        }

        // Filter attendance records for the specified period
        // Fix lambda expression issue by making variables effectively final
        final LocalDate filterStartDate = startDate;
        final LocalDate filterEndDate = endDate;
        
        List<Attendance> filteredAttendance = attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == empId)
            .filter(a -> !a.getDate().isBefore(filterStartDate) && !a.getDate().isAfter(filterEndDate))
            .collect(Collectors.toList());
        
        if (filteredAttendance.isEmpty()) {
            System.out.println("No attendance records found for the specified period.");
            return;
        }

        // Calculate payroll for the specified period
        PayrollSummary payrollSummary = PayrollCalculator.calculatePayroll(employee, filteredAttendance);
        payrollSummary.printPayslip();
        
        // Option to save payslip
        System.out.print("\nDo you want to save this payslip to a file? (Y/N): ");
        String saveChoice = scanner.nextLine();
        
        if (saveChoice.equalsIgnoreCase("Y")) {
            String fileName = String.format("Payslip_%d_%s_%s.txt", 
                employee.getEmployeeId(),
                startDate.format(DateTimeFormatter.ofPattern("MMddyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
                
            savePayslipToFile(payrollSummary, fileName);
        }
    }
    
    private static void savePayslipToFile(PayrollSummary payslip, String fileName) {
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + File.separator + fileName;
        
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            
            writer.println("\n================ MOTORPH PAYROLL SYSTEM ================");
            writer.println("                    PAYSLIP DETAIL                     ");
            writer.println("====================================================");
            
            // Pay Period
            writer.println("Pay Period: " + payslip.getStartDate().format(dateFormatter) + " - " + 
                          payslip.getEndDate().format(dateFormatter));
            
            // Employee Details
            writer.println("\nEmployee Details:");
            writer.println("ID: " + payslip.getEmployee().getEmployeeId());
            writer.println("Name: " + payslip.getEmployee().getLastName() + ", " + payslip.getEmployee().getFirstName());
            writer.println("Position: " + payslip.getEmployee().getPosition());
            writer.println("Status: " + payslip.getEmployee().getStatus());
            
            writer.println("\nGovernment Numbers:");
            writer.println("SSS: " + payslip.getEmployee().getSssNumber());
            writer.println("PhilHealth: " + payslip.getEmployee().getPhilhealthNumber());
            writer.println("TIN: " + payslip.getEmployee().getTinNumber());
            writer.println("Pag-IBIG: " + payslip.getEmployee().getPagibigNumber());
            
            writer.println("\nAttendance Summary:");
            writer.printf("Days Present:       %d days\n", payslip.getDaysPresent());
            writer.printf("Total Hours Worked: %.2f hours\n", payslip.getTotalHours());
            writer.printf("Overtime Hours:     %.2f hours\n", payslip.getOvertimeHours());
            writer.printf("Late Minutes:       %.2f minutes\n", payslip.getLateMinutes());
            
            writer.println("\nEarnings:");
            writer.printf("Basic Salary:       PHP %-,12.2f\n", payslip.getEmployee().getBasicSalary());
            writer.printf("Rice Subsidy:       PHP %-,12.2f\n", payslip.getEmployee().getRiceSubsidy());
            writer.printf("Phone Allowance:    PHP %-,12.2f\n", payslip.getEmployee().getPhoneAllowance());
            writer.printf("Clothing Allowance: PHP %-,12.2f\n", payslip.getEmployee().getClothingAllowance());
            writer.printf("Gross Pay:          PHP %-,12.2f\n", payslip.getGrossPay());
            
            writer.println("\nDeductions:");
            writer.printf("SSS:               PHP %-,12.2f\n", payslip.getSssDeduction());
            writer.printf("PhilHealth:        PHP %-,12.2f\n", payslip.getPhilhealthDeduction());
            writer.printf("Pag-IBIG:          PHP %-,12.2f\n", payslip.getPagibigDeduction());
            writer.printf("Total Deductions:  PHP %-,12.2f\n", payslip.getTotalDeductions());
            
            writer.println("----------------------------------------------------");
            writer.printf("NET PAY:           PHP %-,12.2f\n", payslip.getNetPay());
            writer.println("====================================================");
            writer.println("          This is a system-generated payslip.        \n");
            
            System.out.println("Payslip saved to: " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error saving payslip: " + e.getMessage());
        }
    }

    private static void viewAttendanceRecords(List<Employee> employees, List<Attendance> attendanceRecords, Scanner scanner) {
        if (employees == null || employees.isEmpty() || attendanceRecords == null || attendanceRecords.isEmpty()) {
            System.out.println("No data available to display attendance records.");
            return;
        }

        System.out.print("\nEnter Employee ID to view attendance: ");
        final int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer

        Employee employee = employees.stream()
            .filter(emp -> emp.getEmployeeId() == empId)
            .findFirst()
            .orElse(null);

        if (employee == null) {
            System.out.println("Employee not found! Please check the ID and try again.");
            return;
        }

        System.out.println("\nAttendance Options:");
        System.out.println("1. View Attendance for a Specific Period");
        System.out.println("2. View All Attendance Records");
        System.out.print("Enter your choice: ");
        
        int viewChoice = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        List<Attendance> employeeAttendance = attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == empId)
            .collect(Collectors.toList());
            
        if (employeeAttendance.isEmpty()) {
            System.out.println("No attendance records found for this employee.");
            return;
        }
        
        if (viewChoice == 1) {
            // Allow filtering by date range
            System.out.println("\nSelect date range:");
            System.out.println("1. Current Month");
            System.out.println("2. Custom Date Range");
            System.out.print("Enter your choice: ");
            
            int rangeChoice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            
            LocalDate startDate, endDate;
            LocalDate currentDate = LocalDate.now();
            
            if (rangeChoice == 2) {
                System.out.print("Enter Start Date (MM/DD/YYYY): ");
                String startDateStr = scanner.nextLine();
                System.out.print("Enter End Date (MM/DD/YYYY): ");
                String endDateStr = scanner.nextLine();
                
                try {
                    startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                    endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                } catch (Exception e) {
                    System.out.println("Error parsing dates. Using current month.");
                    startDate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1);
                    endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                }
            } else {
                // Default to current month
                startDate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1);
                endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            }
            
            // Filter attendance records
            final LocalDate filterStartDate = startDate;
            final LocalDate filterEndDate = endDate;
            
            List<Attendance> filteredAttendance = employeeAttendance.stream()
                .filter(a -> !a.getDate().isBefore(filterStartDate) && !a.getDate().isAfter(filterEndDate))
                .collect(Collectors.toList());
            
            // Calculate payroll summary with attendance info
            PayrollSummary payrollSummary = PayrollCalculator.calculatePayroll(employee, filteredAttendance);
            payrollSummary.displayAttendanceStatus();
        } else if (viewChoice == 2) {
            // View all attendance records
            PayrollSummary payrollSummary = PayrollCalculator.calculatePayroll(employee, employeeAttendance);
            payrollSummary.displayAllAttendanceRecords();
        } else {
            System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    private static void manageEmployees(List<Employee> employees, Scanner scanner) {
        while (true) {
            System.out.println("\n================ EMPLOYEE MANAGEMENT ================");
            System.out.println("1. Add New Employee");
            System.out.println("2. Edit Employee");
            System.out.println("3. Delete Employee");
            System.out.println("4. Save Changes");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            
            switch (choice) {
                case 1:
                    addEmployee(employees, scanner);
                    break;
                case 2:
                    editEmployee(employees, scanner);
                    break;
                case 3:
                    deleteEmployee(employees, scanner);
                    break;
                case 4:
                    saveEmployees(employees);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addEmployee(List<Employee> employees, Scanner scanner) {
        System.out.println("\n================ ADD NEW EMPLOYEE ================");
        
        // Generate new employee ID (max existing ID + 1)
        int newId = employees.stream()
            .mapToInt(Employee::getEmployeeId)
            .max()
            .orElse(0) + 1;
            
        System.out.println("New Employee ID: " + newId);
        System.out.println("Employee Login Credentials:");
        System.out.println("Username: Employee" + newId);
        System.out.println("Password: " + newId);
        System.out.println("----------------------------------------------------");
        
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        
        System.out.print("Birthday (MM/DD/YYYY): ");
        String birthday = scanner.nextLine();
        
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine();
        
        System.out.print("SSS Number: ");
        String sssNumber = scanner.nextLine();
        
        System.out.print("Philhealth Number: ");
        String philhealthNumber = scanner.nextLine();
        
        System.out.print("TIN Number: ");
        String tinNumber = scanner.nextLine();
        
        System.out.print("Pag-IBIG Number: ");
        String pagibigNumber = scanner.nextLine();
        
        System.out.print("Status (Regular/Probationary): ");
        String status = scanner.nextLine();
        
        System.out.print("Position: ");
        String position = scanner.nextLine();
        
        System.out.print("Supervisor: ");
        String supervisor = scanner.nextLine();
        
        System.out.print("Basic Salary: ");
        double basicSalary = scanner.nextDouble();
        
        System.out.print("Rice Subsidy: ");
        double riceSubsidy = scanner.nextDouble();
        
        System.out.print("Phone Allowance: ");
        double phoneAllowance = scanner.nextDouble();
        
        System.out.print("Clothing Allowance: ");
        double clothingAllowance = scanner.nextDouble();
        
        // Calculate derived values
        double grossSemiMonthlyRate = basicSalary / 2;
        double hourlyRate = (basicSalary / 22) / 8;
        
        Employee newEmployee = new Employee(
            newId, lastName, firstName, birthday, address, phoneNumber, 
            sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, 
            position, supervisor, basicSalary, riceSubsidy, phoneAllowance, 
            clothingAllowance, grossSemiMonthlyRate, hourlyRate
        );
        
        employees.add(newEmployee);
        System.out.println("Employee added successfully!");
    }

    private static void editEmployee(List<Employee> employees, Scanner scanner) {
        System.out.print("\nEnter Employee ID to edit: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        Employee employee = employees.stream()
            .filter(emp -> emp.getEmployeeId() == empId)
            .findFirst()
            .orElse(null);
            
        if (employee == null) {
            System.out.println("Employee not found! Please check the ID and try again.");
            return;
        }
        
        System.out.println("\n================ EDIT EMPLOYEE ================");
        System.out.println("Employee: " + employee.getLastName() + ", " + employee.getFirstName());
        System.out.println("ID: " + employee.getEmployeeId());
        
        System.out.print("Last Name [" + employee.getLastName() + "]: ");
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setLastName(input);
        }
        
        System.out.print("First Name [" + employee.getFirstName() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setFirstName(input);
        }
        
        System.out.print("Birthday [" + employee.getBirthday() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setBirthday(input);
        }
        
        System.out.print("Address [" + employee.getAddress() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setAddress(input);
        }
        
        System.out.print("Phone Number [" + employee.getPhoneNumber() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setPhoneNumber(input);
        }
        
        System.out.print("Position [" + employee.getPosition() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setPosition(input);
        }
        
        System.out.print("Status [" + employee.getStatus() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            employee.setStatus(input);
        }
        
        System.out.print("Basic Salary [" + employee.getBasicSalary() + "]: ");
        input = scanner.nextLine();
        if (!input.isEmpty()) {
            double newSalary = Double.parseDouble(input);
            employee.setBasicSalary(newSalary);
            
            // Update derived values
            employee.setGrossSemiMonthlyRate(newSalary / 2);
            employee.setHourlyRate((newSalary / 22) / 8);
        }
        
        System.out.println("Employee information updated successfully!");
    }

    private static void deleteEmployee(List<Employee> employees, Scanner scanner) {
        System.out.print("\nEnter Employee ID to delete: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        Employee employee = employees.stream()
            .filter(emp -> emp.getEmployeeId() == empId)
            .findFirst()
            .orElse(null);
            
        if (employee == null) {
            System.out.println("Employee not found! Please check the ID and try again.");
            return;
        }
        
        System.out.print("Are you sure you want to delete " + 
            employee.getFirstName() + " " + employee.getLastName() + "? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            employees.removeIf(emp -> emp.getEmployeeId() == empId);
            System.out.println("Employee deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void saveEmployees(List<Employee> employees) {
        String projectPath = System.getProperty("user.dir");
        String employeeFilePath = projectPath + File.separator + "employees.csv";
        
        FileHandler.writeEmployeesCsv(employees, employeeFilePath);
    }

    private static void manageAttendance(List<Employee> employees, List<Attendance> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\n================ ATTENDANCE MANAGEMENT ================");
            System.out.println("1. Add Attendance Record");
            System.out.println("2. Edit Attendance Record");
            System.out.println("3. Delete Attendance Record");
            System.out.println("4. Save Changes");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            
            switch (choice) {
                case 1:
                    addAttendance(employees, attendanceRecords, scanner);
                    break;
                case 2:
                    editAttendance(attendanceRecords, scanner);
                    break;
                case 3:
                    deleteAttendance(attendanceRecords, scanner);
                    break;
                case 4:
                    saveAttendance(attendanceRecords);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addAttendance(List<Employee> employees, List<Attendance> attendanceRecords, Scanner scanner) {
        System.out.println("\n================ ADD ATTENDANCE RECORD ================");
        
        System.out.print("Employee ID: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        // Verify employee exists
        boolean employeeExists = employees.stream()
            .anyMatch(emp -> emp.getEmployeeId() == empId);
            
        if (!employeeExists) {
            System.out.println("Employee not found! Please check the ID and try again.");
            return;
        }
        
        System.out.print("Date (MM/DD/YYYY): ");
        String dateStr = scanner.nextLine();
        
        System.out.print("Time In (HH:MM): ");
        String timeInStr = scanner.nextLine();
        
        System.out.print("Time Out (HH:MM): ");
        String timeOutStr = scanner.nextLine();
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime timeOut = LocalTime.parse(timeOutStr, TIME_FORMATTER);
            
            // Check if record already exists
            boolean recordExists = attendanceRecords.stream()
                .anyMatch(a -> a.getEmployeeId() == empId && a.getDate().equals(date));
                
            if (recordExists) {
                System.out.print("Attendance record already exists for this date. Overwrite? (Y/N): ");
                String overwrite = scanner.nextLine();
                
                if (overwrite.equalsIgnoreCase("Y")) {
                    // Remove existing record
                    attendanceRecords.removeIf(a -> a.getEmployeeId() == empId && a.getDate().equals(date));
                } else {
                    System.out.println("Operation cancelled.");
                    return;
                }
            }
            
            Attendance newAttendance = new Attendance(empId, date, timeIn, timeOut);
            attendanceRecords.add(newAttendance);
            
            System.out.println("Attendance record added successfully!");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date or time format. Please use MM/DD/YYYY for date and HH:MM for time.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void editAttendance(List<Attendance> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee ID: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        System.out.print("Enter Date (MM/DD/YYYY): ");
        String dateStr = scanner.nextLine();
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            
            Attendance record = attendanceRecords.stream()
                .filter(att -> att.getEmployeeId() == empId && att.getDate().equals(date))
                .findFirst()
                .orElse(null);
                
            if (record == null) {
                System.out.println("Attendance record not found!");
                return;
            }
            
            System.out.println("\n================ EDIT ATTENDANCE ================");
            System.out.println("Employee ID: " + record.getEmployeeId());
            System.out.println("Date: " + record.getDate().format(DATE_FORMATTER));
            
            System.out.print("Time In [" + record.getFormattedTimeIn() + "]: ");
            String timeInStr = scanner.nextLine();
            
            System.out.print("Time Out [" + record.getFormattedTimeOut() + "]: ");
            String timeOutStr = scanner.nextLine();
            
            // Create new record with updated values (since Attendance doesn't have setters)
            LocalTime timeIn = timeInStr.isEmpty() ? record.getTimeIn() : 
                LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime timeOut = timeOutStr.isEmpty() ? record.getTimeOut() : 
                LocalTime.parse(timeOutStr, TIME_FORMATTER);
            
            // Remove old record and add updated one
            attendanceRecords.remove(record);
            attendanceRecords.add(new Attendance(empId, date, timeIn, timeOut));
            
            System.out.println("Attendance record updated successfully!");
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date or time format. Please use MM/DD/YYYY for date and HH:MM for time.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteAttendance(List<Attendance> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee ID: ");
        int empId = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        System.out.print("Enter Date (MM/DD/YYYY): ");
        String dateStr = scanner.nextLine();
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            
            boolean removed = attendanceRecords.removeIf(
                att -> att.getEmployeeId() == empId && att.getDate().equals(date));
                
            if (removed) {
                System.out.println("Attendance record deleted successfully!");
            } else {
                System.out.println("Attendance record not found!");
            }
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format. Please use MM/DD/YYYY.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void saveAttendance(List<Attendance> attendanceRecords) {
        String projectPath = System.getProperty("user.dir");
        String attendanceFilePath = projectPath + File.separator + "attendance.csv";
        
        FileHandler.writeAttendanceCsv(attendanceRecords, attendanceFilePath);
    }
    
    // Employee-specific methods
    
    private static void clockInOut(Employee employee, List<Attendance> attendanceRecords, Scanner scanner) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        System.out.println("\n================ CLOCK IN/OUT ================");
        System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
        System.out.println("Today's Date: " + today.format(DATE_FORMATTER));
        System.out.println("Current Time: " + currentTime.format(TIME_FORMATTER));
        
        // Check if employee already has an attendance record for today
        Attendance todayRecord = attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == employee.getEmployeeId() && a.getDate().equals(today))
            .findFirst()
            .orElse(null);
        
        if (todayRecord == null) {
            // No record for today, offer to clock in
            System.out.println("\nYou have not clocked in today.");
            System.out.print("Do you want to clock in now? (Y/N): ");
            String choice = scanner.nextLine().trim().toUpperCase();
            
            if (choice.equals("Y") || choice.equals("YES")) {
                // Create new attendance record with only time in
                Attendance newRecord = new Attendance(employee.getEmployeeId(), today, currentTime, null);
                attendanceRecords.add(newRecord);
                System.out.println("Successfully clocked in at " + currentTime.format(TIME_FORMATTER));
            } else {
                System.out.println("Clock in cancelled.");
            }
        } else if (todayRecord.getTimeOut() == null) {
            // Record exists but no time out, offer to clock out
            System.out.println("\nYou clocked in today at " + todayRecord.getFormattedTimeIn());
            System.out.print("Do you want to clock out now? (Y/N): ");
            String choice = scanner.nextLine().trim().toUpperCase();
            
            if (choice.equals("Y") || choice.equals("YES")) {
                // Create updated record with time out added
                attendanceRecords.remove(todayRecord);
                Attendance updatedRecord = new Attendance(
                    employee.getEmployeeId(), 
                    today, 
                    todayRecord.getTimeIn(), 
                    currentTime
                );
                attendanceRecords.add(updatedRecord);
                
                System.out.println("Successfully clocked out at " + currentTime.format(TIME_FORMATTER));
                System.out.printf("Total hours worked today: %.2f hours\n", updatedRecord.getTotalHours());
                if (updatedRecord.getOvertimeHours() > 0) {
                    System.out.printf("Overtime hours: %.2f hours\n", updatedRecord.getOvertimeHours());
                }
                if (updatedRecord.getLateMinutes() > 0) {
                    System.out.printf("You were late by %.0f minutes\n", updatedRecord.getLateMinutes());
                }
            } else {
                System.out.println("Clock out cancelled.");
            }
        } else {
            // Complete record for today already exists
            System.out.println("\nYou have already completed your attendance record for today:");
            System.out.println("Time In: " + todayRecord.getFormattedTimeIn());
            System.out.println("Time Out: " + todayRecord.getFormattedTimeOut());
            System.out.printf("Total Hours: %.2f hours\n", todayRecord.getTotalHours());
            
            if (todayRecord.getOvertimeHours() > 0) {
                System.out.printf("Overtime Hours: %.2f hours\n", todayRecord.getOvertimeHours());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private static void viewMyAttendance(Employee employee, List<Attendance> attendanceRecords, Scanner scanner) {
        List<Attendance> employeeAttendance = attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == employee.getEmployeeId())
            .collect(Collectors.toList());
            
        if (employeeAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\n================ MY ATTENDANCE RECORDS ================");
        System.out.println("1. View Current Month");
        System.out.println("2. View Specific Date Range");
        System.out.println("3. View All Records");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        List<Attendance> filteredAttendance;
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        if (choice == 1) {
            // Current month
            LocalDate currentDate = LocalDate.now();
            startDate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1);
            endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            
            final LocalDate finalStartDate = startDate;
            final LocalDate finalEndDate = endDate;
            
            filteredAttendance = employeeAttendance.stream()
                .filter(a -> !a.getDate().isBefore(finalStartDate) && !a.getDate().isAfter(finalEndDate))
                .collect(Collectors.toList());
                
        } else if (choice == 2) {
            // Specific date range
            System.out.print("Enter Start Date (MM/DD/YYYY): ");
            String startDateStr = scanner.nextLine();
            System.out.print("Enter End Date (MM/DD/YYYY): ");
            String endDateStr = scanner.nextLine();
            
            try {
                startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                
                final LocalDate finalStartDate = startDate;
                final LocalDate finalEndDate = endDate;
                
                filteredAttendance = employeeAttendance.stream()
                    .filter(a -> !a.getDate().isBefore(finalStartDate) && !a.getDate().isAfter(finalEndDate))
                    .collect(Collectors.toList());
            } catch (Exception e) {
                System.out.println("Error parsing dates. Showing all records instead.");
                filteredAttendance = employeeAttendance;
            }
        } else {
            // All records
            filteredAttendance = employeeAttendance;
        }
        
        // Calculate summary
        PayrollSummary payrollSummary = PayrollCalculator.calculatePayroll(employee, filteredAttendance);
        
        if (startDate != null && endDate != null) {
            System.out.println("\nDate Range: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
        }
        
        payrollSummary.displayAllAttendanceRecords();
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private static void viewMyPayslip(Employee employee, List<Attendance> attendanceRecords, Scanner scanner) {
        List<Attendance> employeeAttendance = attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == employee.getEmployeeId())
            .collect(Collectors.toList());
            
        if (employeeAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found. Cannot generate payslip.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\n================ MY PAYSLIP ================");
        System.out.println("Select pay period:");
        System.out.println("1. First Half (1-15) of Current Month");
        System.out.println("2. Second Half (16-30/31) of Current Month");
        System.out.println("3. Custom Date Range");
        System.out.print("Enter your choice: ");
        
        int periodChoice = scanner.nextInt();
        scanner.nextLine(); // Clear buffer
        
        LocalDate startDate, endDate;
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        
        switch (periodChoice) {
            case 1:
                startDate = LocalDate.of(currentYear, currentMonth, 1);
                endDate = LocalDate.of(currentYear, currentMonth, 15);
                break;
            case 2:
                startDate = LocalDate.of(currentYear, currentMonth, 16);
                endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                break;
            case 3:
                System.out.print("Enter Start Date (MM/DD/YYYY): ");
                String startDateStr = scanner.nextLine();
                System.out.print("Enter End Date (MM/DD/YYYY): ");
                String endDateStr = scanner.nextLine();
                
                try {
                    startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
                    endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
                } catch (Exception e) {
                    System.out.println("Error parsing dates. Using current month.");
                    startDate = LocalDate.of(currentYear, currentMonth, 1);
                    endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
                }
                break;
            default:
                System.out.println("Invalid choice. Using current month.");
                startDate = LocalDate.of(currentYear, currentMonth, 1);
                endDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        }
        
        // Filter attendance records for the specified period
        final LocalDate filterStartDate = startDate;
        final LocalDate filterEndDate = endDate;
        
        List<Attendance> filteredAttendance = employeeAttendance.stream()
            .filter(a -> !a.getDate().isBefore(filterStartDate) && !a.getDate().isAfter(filterEndDate))
            .collect(Collectors.toList());
        
        if (filteredAttendance.isEmpty()) {
            System.out.println("No attendance records found for the specified period.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Calculate payroll for the specified period
        PayrollSummary payrollSummary = PayrollCalculator.calculatePayroll(employee, filteredAttendance);
        payrollSummary.printPayslip();
        
        // Option to save payslip
        System.out.print("\nDo you want to save this payslip to a file? (Y/N): ");
        String saveChoice = scanner.nextLine();
        
        if (saveChoice.equalsIgnoreCase("Y")) {
            String fileName = String.format("Payslip_%d_%s_%s.txt", 
                employee.getEmployeeId(),
                startDate.format(DateTimeFormatter.ofPattern("MMddyyyy")),
                endDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
                
            savePayslipToFile(payrollSummary, fileName);
        }
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private static void viewMyProfile(Employee employee, Scanner scanner) {
        System.out.println("\n================ MY PROFILE ================");
        System.out.println("Employee ID: " + employee.getEmployeeId());
        System.out.println("Full Name: " + employee.getLastName() + ", " + employee.getFirstName());
        System.out.println("Birthday: " + employee.getBirthday());
        System.out.println("Address: " + employee.getAddress());
        System.out.println("Phone Number: " + employee.getPhoneNumber());
        
        System.out.println("\nEmployment Information:");
        System.out.println("Status: " + employee.getStatus());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Supervisor: " + employee.getSupervisor());
        
        System.out.println("\nGovernment IDs:");
        System.out.println("SSS Number: " + employee.getSssNumber());
        System.out.println("PhilHealth Number: " + employee.getPhilhealthNumber());
        System.out.println("TIN Number: " + employee.getTinNumber());
        System.out.println("Pag-IBIG Number: " + employee.getPagibigNumber());
        
        System.out.println("\nCompensation:");
        System.out.printf("Basic Salary: PHP %,.2f\n", employee.getBasicSalary());
        System.out.printf("Rice Subsidy: PHP %,.2f\n", employee.getRiceSubsidy());
        System.out.printf("Phone Allowance: PHP %,.2f\n", employee.getPhoneAllowance());
        System.out.printf("Clothing Allowance: PHP %,.2f\n", employee.getClothingAllowance());
        System.out.printf("Gross Semi-Monthly Rate: PHP %,.2f\n", employee.getGrossSemiMonthlyRate());
        System.out.printf("Hourly Rate: PHP %,.2f\n", employee.getHourlyRate());
        
        System.out.println("====================================================");
        System.out.println("\nLogin Information:");
        System.out.println("Username: Employee" + employee.getEmployeeId());
        System.out.println("Password: " + employee.getEmployeeId());
        System.out.println("====================================================");
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}