package com.motorph.payroll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileHandler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static List<Employee> readEmployees(String filePath) {
        List<Employee> employees = new ArrayList<>();
        File file = new File(filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            // Skip header line
            if (scanner.hasNextLine()) {
                String header = scanner.nextLine();
                System.out.println("File header: " + header);
            }
            
            int lineCount = 0;
            int successCount = 0;
            int errorCount = 0;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineCount++;
                
                // Skip empty lines
                if (line.isEmpty()) {
                    System.out.println("Line " + lineCount + ": Empty line skipped");
                    continue;
                }
                
                try {
                    // Better CSV parsing for lines with quoted fields and commas in values
                    String[] data = parseCSVLine(line);
                    
                    System.out.println("Processing line " + lineCount + ": ID=" + data[0] + ", " + data.length + " columns");
                    
                    if (data.length < 13) { // Check for minimum required columns
                        System.err.println("WARNING: Line " + lineCount + " has fewer than expected columns: " + data.length);
                        System.err.println("Line content: " + line);
                        errorCount++;
                        continue;
                    }
                    
                    int employeeId = Integer.parseInt(data[0].trim());
                    String lastName = data[1].trim();
                    String firstName = data[2].trim();
                    String birthday = data[3].trim();
                    String address = data[4].trim();
                    String phoneNumber = data[5].trim();
                    String sssNumber = data[6].trim();
                    String philhealthNumber = data[7].trim();
                    String tinNumber = data[8].trim();
                    String pagibigNumber = data[9].trim();
                    String status = data[10].trim();
                    String position = data[11].trim();
                    String supervisor = data[12].trim();
                    
                    // Parse numeric fields safely
                    double basicSalary = parseAmount(data[13].trim());
                    double riceSubsidy = parseAmount(data[14].trim());
                    double phoneAllowance = parseAmount(data[15].trim());
                    double clothingAllowance = parseAmount(data[16].trim());
                    
                    // Handle optional fields
                    double grossSemiMonthlyRate = (data.length > 17) ? parseAmount(data[17].trim()) : basicSalary / 2;
                    double hourlyRate = (data.length > 18) ? parseAmount(data[18].trim()) : (basicSalary / 22) / 8;
                    
                    Employee employee = new Employee(
                        employeeId, lastName, firstName, birthday, address, phoneNumber, 
                        sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, 
                        position, supervisor, basicSalary, riceSubsidy, phoneAllowance, 
                        clothingAllowance, grossSemiMonthlyRate, hourlyRate
                    );
                    
                    employees.add(employee);
                    successCount++;
                    System.out.println("Successfully added employee " + employeeId + ": " + lastName + ", " + firstName);
                    
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing employee data at line " + lineCount + ": " + e.getMessage());
                    System.err.println("Line content: " + line);
                    errorCount++;
                } catch (Exception e) {
                    System.err.println("Unexpected error at line " + lineCount + ": " + e.getMessage());
                    System.err.println("Line content: " + line);
                    errorCount++;
                }
            }
            
            System.out.println("=============== EMPLOYEE LOADING SUMMARY ===============");
            System.out.println("Read " + lineCount + " lines from employee file");
            System.out.println("Successfully loaded " + successCount + " employees");
            System.out.println("Encountered " + errorCount + " errors");
            System.out.println("Total employees in list: " + employees.size());
            System.out.println("=======================================================");
            
        } catch (FileNotFoundException e) {
            System.err.println("Employee data file not found: " + e.getMessage());
            System.err.println("File path: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Unexpected error reading employee file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return employees;
    }
    
    public static List<Attendance> readAttendance(String filePath) {
        List<Attendance> attendanceRecords = new ArrayList<>();
        File file = new File(filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            // Skip header line
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            int lineCount = 0;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineCount++;
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    String[] data = parseCSVLine(line);
                    
                    // Check if we have the minimum required columns
                    if (data.length < 6) {
                        System.err.println("Invalid attendance data at line " + lineCount + ": " + line);
                        continue;
                    }
                    
                    // Parse employee ID from first column
                    int employeeId = Integer.parseInt(data[0].trim());
                    
                    // Parse date from column 3
                    LocalDate date = LocalDate.parse(data[3].trim(), DATE_FORMATTER);
                    
                    // Parse time in from column 4 with proper formatting
                    String timeInStr = data[4].trim();
                    LocalTime timeIn = parseTime(timeInStr);
                    
                    // Parse time out from column 5 with proper formatting
                    String timeOutStr = data[5].trim();
                    LocalTime timeOut = parseTime(timeOutStr);
                    
                    Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut);
                    attendanceRecords.add(attendance);
                } catch (Exception e) {
                    System.err.println("Error at line " + lineCount + ": " + e.getMessage());
                    System.err.println("Line content: " + line);
                }
            }
            
            System.out.println("Read " + lineCount + " lines from attendance file, loaded " + attendanceRecords.size() + " records");
            
        } catch (FileNotFoundException e) {
            System.err.println("Attendance data file not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error reading attendance file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceRecords;
    }
    
    public static void writeEmployeesCsv(List<Employee> employees, String filePath) {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            // Write header
            writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate");
            
            // Write data
            for (Employee emp : employees) {
                writer.printf("%d,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    emp.getEmployeeId(), emp.getLastName(), emp.getFirstName(), emp.getBirthday(),
                    emp.getAddress(), emp.getPhoneNumber(), emp.getSssNumber(), 
                    emp.getPhilhealthNumber(), emp.getTinNumber(), emp.getPagibigNumber(),
                    emp.getStatus(), emp.getPosition(), emp.getSupervisor(), emp.getBasicSalary(),
                    emp.getRiceSubsidy(), emp.getPhoneAllowance(), emp.getClothingAllowance(),
                    emp.getGrossSemiMonthlyRate(), emp.getHourlyRate());
            }
            
            System.out.println("Employee data saved successfully to: " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error writing employee data: " + e.getMessage());
        }
    }
    
    public static void writeAttendanceCsv(List<Attendance> attendanceRecords, String filePath) {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            // Write header
            writer.println("Employee #,Last Name,First Name,Date,Time In,Time Out");
            
            // Write data
            for (Attendance att : attendanceRecords) {
                writer.printf("%d,,%s,%s,%s,%s\n",
                    att.getEmployeeId(), 
                    "",
                    att.getDate().format(DATE_FORMATTER),
                    att.getTimeIn() != null ? att.getTimeIn().format(TIME_FORMATTER) : "",
                    att.getTimeOut() != null ? att.getTimeOut().format(TIME_FORMATTER) : "");
            }
            
            System.out.println("Attendance data saved successfully to: " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error writing attendance data: " + e.getMessage());
        }
    }
    
    // Helper method to handle quoted fields and commas within fields
    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // Toggle the inQuotes flag
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // If we're not in quotes, add the current token to the list
                tokens.add(sb.toString());
                sb = new StringBuilder();
            } else {
                // Add the character to the current token
                sb.append(c);
            }
        }
        
        // Add the last token
        tokens.add(sb.toString());
        
        return tokens.toArray(new String[0]);
    }
    
    // Helper method to properly parse amounts that may contain commas
    private static double parseAmount(String amount) {
        try {
            // Remove quotes and commas from the amount string
            String cleanAmount = amount.replace("\"", "").replace(",", "").trim();
            if (cleanAmount.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(cleanAmount);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing amount: " + amount);
            return 0.0;
        }
    }
    
    // Helper method to parse time strings with various formats
    private static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try direct parsing
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // If single digit hour (e.g., "8:30"), add leading zero
                if (timeStr.matches("^\\d:\\d\\d$")) {
                    return LocalTime.parse("0" + timeStr, TIME_FORMATTER);
                }
                
                // Split by colon and handle different formats
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    String hour = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
                    String minute = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                    return LocalTime.parse(hour + ":" + minute, TIME_FORMATTER);
                }
                
                // Fall back to parsing as best we can
                return LocalTime.parse(timeStr);
            } catch (DateTimeParseException e2) {
                System.err.println("Could not parse time: " + timeStr);
                return null;
            }
        }
    }
}