package com.motorph.payroll.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtil {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    
    /**
     * Parse a date string in MM/dd/yyyy format
     * @param dateStr The date string to parse
     * @return The parsed LocalDate or null if invalid
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parse a time string in HH:mm format
     * @param timeStr The time string to parse
     * @return The parsed LocalTime or null if invalid
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try direct parsing
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // If single digit hour (e.g., "8:30"), add leading zero
                if (timeStr.matches("^\\d:\\d\\d$")) {
                    return LocalTime.parse("0" + timeStr.trim(), TIME_FORMATTER);
                }
                
                // Split by colon and handle different formats
                String[] parts = timeStr.trim().split(":");
                if (parts.length == 2) {
                    String hour = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
                    String minute = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                    return LocalTime.parse(hour + ":" + minute, TIME_FORMATTER);
                }
                
                // Fall back to parsing as best we can
                return LocalTime.parse(timeStr.trim());
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
    
    /**
     * Get the first day of the current month
     * @return The first day of the current month
     */
    public static LocalDate getFirstDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return LocalDate.of(now.getYear(), now.getMonth(), 1);
    }
    
    /**
     * Get the last day of the current month
     * @return The last day of the current month
     */
    public static LocalDate getLastDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
    
    /**
     * Get the first day of the first half of the current month (1-15)
     * @return The first day of the first half
     */
    public static LocalDate getFirstHalfStart() {
        LocalDate now = LocalDate.now();
        return LocalDate.of(now.getYear(), now.getMonth(), 1);
    }
    
    /**
     * Get the last day of the first half of the current month (1-15)
     * @return The last day of the first half
     */
    public static LocalDate getFirstHalfEnd() {
        LocalDate now = LocalDate.now();
        return LocalDate.of(now.getYear(), now.getMonth(), 15);
    }
    
    /**
     * Get the first day of the second half of the current month (16-end)
     * @return The first day of the second half
     */
    public static LocalDate getSecondHalfStart() {
        LocalDate now = LocalDate.now();
        return LocalDate.of(now.getYear(), now.getMonth(), 16);
    }
    
    /**
     * Get the last day of the second half of the current month (16-end)
     * @return The last day of the second half
     */
    public static LocalDate getSecondHalfEnd() {
        return getLastDayOfCurrentMonth();
    }
}