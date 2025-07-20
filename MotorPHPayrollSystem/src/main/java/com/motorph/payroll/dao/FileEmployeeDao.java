package com.motorph.payroll.dao;

import com.motorph.payroll.exception.DataAccessException;
import com.motorph.payroll.model.Employee;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileEmployeeDao implements EmployeeDao {
    private String filePath;
    private List<Employee> employees;
    
    public FileEmployeeDao(String filePath) {
        this.filePath = filePath;
        this.employees = new ArrayList<>();
        loadEmployees();
    }
    
    private void loadEmployees() {
        File file = new File(filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            // Skip header line
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    // Parse CSV line
                    String[] data = parseCSVLine(line);
                    
                    if (data.length < 13) { // Check for minimum required columns
                        System.err.println("WARNING: Line has fewer than expected columns: " + data.length);
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
                    
                } catch (Exception e) {
                    System.err.println("Error parsing employee data: " + e.getMessage());
                }
            }
            
        } catch (FileNotFoundException e) {
            throw new DataAccessException("Employee data file not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error reading employee file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }
    
    @Override
    public Employee getEmployeeById(int id) {
        return employees.stream()
            .filter(emp -> emp.getEmployeeId() == id)
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void addEmployee(Employee employee) {
        // Ensure employee ID is unique
        if (getEmployeeById(employee.getEmployeeId()) != null) {
            throw new DataAccessException("Employee with ID " + employee.getEmployeeId() + " already exists");
        }
        employees.add(employee);
    }
    
    @Override
    public void updateEmployee(Employee employee) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId() == employee.getEmployeeId()) {
                employees.set(i, employee);
                return;
            }
        }
        throw new DataAccessException("Employee with ID " + employee.getEmployeeId() + " not found for update");
    }
    
    @Override
    public void deleteEmployee(int id) {
        boolean removed = employees.removeIf(emp -> emp.getEmployeeId() == id);
        if (!removed) {
            throw new DataAccessException("Employee with ID " + id + " not found for deletion");
        }
    }
    
    @Override
    public boolean saveEmployees() {
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
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Error writing employee data: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to handle quoted fields and commas within fields
    private String[] parseCSVLine(String line) {
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
    private double parseAmount(String amount) {
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
}