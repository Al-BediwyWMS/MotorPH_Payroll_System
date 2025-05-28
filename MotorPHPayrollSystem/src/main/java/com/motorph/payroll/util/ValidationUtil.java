package com.motorph.payroll.util;

/**
 * Utility class for data validation
 */
public class ValidationUtil {
    
    /**
     * Check if a string is null or empty
     * @param str The string to check
     * @return True if the string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validate a phone number format
     * @param phoneNumber The phone number to validate
     * @return True if the format is valid
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (isEmpty(phoneNumber)) {
            return false;
        }
        
        // Simple validation - can be enhanced for specific formats
        return phoneNumber.matches("\\d{10,11}");
    }
    
    /**
     * Validate if a string is a positive number
     * @param str The string to validate
     * @return True if it's a positive number
     */
    public static boolean isPositiveNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        try {
            double value = Double.parseDouble(str);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate an SSS number format
     * @param sssNumber The SSS number to validate
     * @return True if the format is valid
     */
    public static boolean isValidSssNumber(String sssNumber) {
        if (isEmpty(sssNumber)) {
            return false;
        }
        
        // Basic pattern for SSS numbers
        return sssNumber.matches("\\d{2}-\\d{7}-\\d|\\d{10}");
    }
    
    /**
     * Truncate a string to a maximum length
     * @param str The string to truncate
     * @param maxLength The maximum length
     * @return The truncated string
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}