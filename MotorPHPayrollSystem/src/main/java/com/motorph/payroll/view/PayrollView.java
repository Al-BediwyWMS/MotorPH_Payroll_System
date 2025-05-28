package com.motorph.payroll.view;

import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.PayrollSummary;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class PayrollView {
    private Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public PayrollView() {
        this.scanner = new Scanner(System.in);
    }
    
    public void displayWelcomeScreen() {
        System.out.println("\n=====================================================");
        System.out.println("||                 WELCOME TO                     ||");
        System.out.println("||            MOTORPH PAYROLL SYSTEM             ||");
        System.out.println("=====================================================");
        System.out.println("         © 2024 MotorPH. Version 1.0             \n");
    }
    
    public void displayGoodbye() {
        System.out.println("\n=====================================================");
        System.out.println("||               THANK YOU FOR USING              ||");
        System.out.println("||            MOTORPH PAYROLL SYSTEM             ||");
        System.out.println("=====================================================");
        System.out.println("                    GOODBYE!                      \n");
    }
    
    public String[] promptForLogin() {
        System.out.print("\nUsername: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        return new String[]{username, password};
    }
    
    public int displayAdminMenu() {
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
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            return choice;
        } catch (Exception e) {
            scanner.nextLine(); // Clear buffer
            return -1; // Invalid choice
        }
    }
    
    public int displayEmployeeMenu(Employee employee) {
        System.out.println("\n================ MOTORPH PAYROLL SYSTEM ================");
        System.out.printf("                 EMPLOYEE PORTAL - %s %s           \n",
            employee.getFirstName(), employee.getLastName());
        System.out.println("====================================================");
        System.out.println("1. Clock In/Out");
        System.out.println("2. View My Attendance Records");
        System.out.println("3. View My Payslip");
        System.out.println("4. View My Profile");
        System.out.println("5. Logout");
        System.out.println("6. Exit");
        System.out.println("====================================================");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            return choice;
        } catch (Exception e) {
            scanner.nextLine(); // Clear buffer
            return -1; // Invalid choice
        }
    }
    
    public void displayEmployeeList(List<Employee> employees) {
        System.out.println("\n================ EMPLOYEE LIST ================");
        System.out.println("Total Employees: " + employees.size());
        System.out.println("====================================================");
        
        // Print table header
        System.out.printf("%-8s | %-15s | %-15s | %-30s | %-15s\n", 
            "ID", "Last Name", "First Name", "Position", "Status");
        System.out.println("----------------------------------------------------------------------");
        
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
    }
    
    public void displayEmployeeDetails(Employee employee) {
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
        scanner.nextLine();
    }
    
    public void displayMessage(String message) {
        System.out.println(message);
    }
    
    public void displayError(String error) {
        System.err.println("ERROR: " + error);
    }
    
    public boolean confirmLogout() {
        System.out.print("\nAre you sure you want to logout? (Y/N): ");
        String choice = scanner.nextLine().trim().toUpperCase();
        return choice.equals("Y") || choice.equals("YES");
    }
    
    public int promptForInt(String prompt) {
        System.out.print(prompt);
        try {
            int value = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            return value;
        } catch (Exception e) {
            scanner.nextLine(); // Clear buffer
            return -1;
        }
    }
    
    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    public LocalDate promptForDate(String prompt) {
        System.out.print(prompt);
        String dateStr = scanner.nextLine().trim();
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public LocalTime promptForTime(String prompt) {
        System.out.print(prompt);
        String timeStr = scanner.nextLine().trim();
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public void displayPayslip(PayrollSummary payrollSummary) {
        payrollSummary.printPayslip();
    }
    
    public void displayAttendanceStatus(PayrollSummary payrollSummary) {
        payrollSummary.displayAttendanceStatus();
    }
    
    public void displayAllAttendanceRecords(PayrollSummary payrollSummary) {
        payrollSummary.displayAllAttendanceRecords();
    }
    
    public void displayEmployeeManagementMenu() {
        System.out.println("\n================ EMPLOYEE MANAGEMENT ================");
        System.out.println("1. Add New Employee");
        System.out.println("2. Edit Employee");
        System.out.println("3. Delete Employee");
        System.out.println("4. Save Changes");
        System.out.println("5. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }
    
    public void displayAttendanceManagementMenu() {
        System.out.println("\n================ ATTENDANCE MANAGEMENT ================");
        System.out.println("1. Add Attendance Record");
        System.out.println("2. Edit Attendance Record");
        System.out.println("3. Delete Attendance Record");
        System.out.println("4. Save Changes");
        System.out.println("5. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }
    
    public Employee promptForNewEmployee(int newId) {
        System.out.println("\n================ ADD NEW EMPLOYEE ================");
        
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
        
        double basicSalary = promptForDouble("Basic Salary: ");
        double riceSubsidy = promptForDouble("Rice Subsidy: ");
        double phoneAllowance = promptForDouble("Phone Allowance: ");
        double clothingAllowance = promptForDouble("Clothing Allowance: ");
        
        // Calculate derived values
        double grossSemiMonthlyRate = basicSalary / 2;
        double hourlyRate = (basicSalary / 22) / 8;
        
        return new Employee(
            newId, lastName, firstName, birthday, address, phoneNumber, 
            sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, 
            position, supervisor, basicSalary, riceSubsidy, phoneAllowance, 
            clothingAllowance, grossSemiMonthlyRate, hourlyRate
        );
    }
    
    public double promptForDouble(String prompt) {
        System.out.print(prompt);
        try {
            double value = scanner.nextDouble();
            scanner.nextLine(); // Clear buffer
            return value;
        } catch (Exception e) {
            scanner.nextLine(); // Clear buffer
            return 0.0;
        }
    }
    
    // Helper methods
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    // Close scanner when done
    public void close() {
        scanner.close();
    }
}