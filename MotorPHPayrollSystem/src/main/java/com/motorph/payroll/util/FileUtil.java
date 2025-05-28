package com.motorph.payroll.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class for file operations
 */
public class FileUtil {
    
    /**
     * Read all lines from a file
     * @param filePath The path to the file
     * @return A list of lines from the file
     * @throws FileNotFoundException If the file cannot be found
     */
    public static List<String> readLines(String filePath) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        
        return lines;
    }
    
    /**
     * Check if a file exists
     * @param filePath The path to the file
     * @return True if the file exists
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
    
    /**
     * Create directory if it doesn't exist
     * @param directoryPath The path to the directory
     * @return True if the directory exists or was created successfully
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return directory.isDirectory();
    }
    
    /**
     * Get the default application data directory
     * @return The data directory path
     */
    public static String getAppDataDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Parse a CSV line with proper handling of quoted fields
     * @param line The CSV line to parse
     * @return An array of fields
     */
    public static String[] parseCSVLine(String line) {
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
}