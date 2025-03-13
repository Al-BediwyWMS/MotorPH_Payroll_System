package com.motorph.payroll.dao;

import com.motorph.payroll.exception.DataAccessException;
import com.motorph.payroll.model.Attendance;
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
import java.util.stream.Collectors;

public class FileAttendanceDao implements AttendanceDao {
    private String filePath;
    private List<Attendance> attendanceRecords;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public FileAttendanceDao(String filePath) {
        this.filePath = filePath;
        this.attendanceRecords = new ArrayList<>();
        loadAttendance();
    }
    
    private void loadAttendance() {
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
                    if (data.length < 4) {
                        System.err.println("Invalid attendance data at line " + lineCount + ": " + line);
                        continue;
                    }
                    
                    // Parse employee ID from first column
                    int employeeId = Integer.parseInt(data[0].trim());
                    
                    // Parse date from column 3
                    LocalDate date = LocalDate.parse(data[3].trim(), DATE_FORMATTER);
                    
                    // Parse time in from column 4 with proper formatting
                    String timeInStr = data.length > 4 ? data[4].trim() : "";
                    LocalTime timeIn = parseTime(timeInStr);
                    
                    // Parse time out from column 5 with proper formatting
                    String timeOutStr = data.length > 5 ? data[5].trim() : "";
                    LocalTime timeOut = parseTime(timeOutStr);
                    
                    Attendance attendance = new Attendance(employeeId, date, timeIn, timeOut);
                    attendanceRecords.add(attendance);
                } catch (Exception e) {
                    System.err.println("Error at line " + lineCount + ": " + e.getMessage());
                }
            }
            
        } catch (FileNotFoundException e) {
            throw new DataAccessException("Attendance data file not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error reading attendance file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Attendance> getAllAttendance() {
        return new ArrayList<>(attendanceRecords);
    }
    
    @Override
    public List<Attendance> getAttendanceByEmployeeId(int employeeId) {
        return attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == employeeId)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecords.stream()
            .filter(a -> a.getEmployeeId() == employeeId)
            .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    @Override
    public void addAttendance(Attendance attendance) {
        // Check if record already exists for this employee and date
        boolean recordExists = attendanceRecords.stream()
            .anyMatch(a -> a.getEmployeeId() == attendance.getEmployeeId() && 
                     a.getDate().equals(attendance.getDate()));
                     
        if (recordExists) {
            // Remove existing record
            attendanceRecords.removeIf(a -> a.getEmployeeId() == attendance.getEmployeeId() && 
                              a.getDate().equals(attendance.getDate()));
        }
        
        attendanceRecords.add(attendance);
    }
    
    @Override
    public void updateAttendance(Attendance attendance) {
        boolean updated = false;
        
        for (int i = 0; i < attendanceRecords.size(); i++) {
            Attendance record = attendanceRecords.get(i);
            if (record.getEmployeeId() == attendance.getEmployeeId() && 
                record.getDate().equals(attendance.getDate())) {
                attendanceRecords.set(i, attendance);
                updated = true;
                break;
            }
        }
        
        if (!updated) {
            throw new DataAccessException("Attendance record not found for update");
        }
    }
    
    @Override
    public void deleteAttendance(int employeeId, LocalDate date) {
        boolean removed = attendanceRecords.removeIf(a -> 
            a.getEmployeeId() == employeeId && a.getDate().equals(date));
            
        if (!removed) {
            throw new DataAccessException("Attendance record not found for deletion");
        }
    }
    
    @Override
    public boolean saveAttendance() {
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
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Error writing attendance data: " + e.getMessage());
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
    
    // Helper method to parse time strings with various formats
    private LocalTime parseTime(String timeStr) {
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