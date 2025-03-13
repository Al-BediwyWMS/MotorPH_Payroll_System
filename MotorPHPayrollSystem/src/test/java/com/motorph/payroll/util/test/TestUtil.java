package com.motorph.payroll.util.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for testing
 */
public class TestUtil {
    
    public static final String TEST_RESOURCES = "test-resources";
    public static final String TEST_EMPLOYEES_CSV = "test-employees.csv";
    public static final String TEST_ATTENDANCE_CSV = "test-attendance.csv";
    
    /**
     * Get the path to a test resource file
     * @param fileName The name of the file
     * @return The absolute path to the file
     */
    public static String getTestResourcePath(String fileName) {
        return System.getProperty("user.dir") + File.separator + 
               TEST_RESOURCES + File.separator + fileName;
    }
    
    /**
     * Create a temporary copy of a test file for modification during tests
     * @param originalFileName The original file name
     * @param tempFileName The temporary file name
     * @return The path to the temporary file
     * @throws Exception If the file cannot be copied
     */
    public static String createTempTestFile(String originalFileName, String tempFileName) throws Exception {
        String originalPath = getTestResourcePath(originalFileName);
        String tempPath = getTestResourcePath(tempFileName);
        
        Files.copy(Paths.get(originalPath), Paths.get(tempPath), StandardCopyOption.REPLACE_EXISTING);
        
        return tempPath;
    }
    
    /**
     * Clean up temporary test files
     * @param tempFileName The name of the temporary file to delete
     */
    public static void cleanupTempTestFile(String tempFileName) {
        try {
            Files.deleteIfExists(Paths.get(getTestResourcePath(tempFileName)));
        } catch (Exception e) {
            // Ignore exceptions during cleanup
        }
    }
}