package com.motorph.payroll.model;

/**
 * Enum representing the possible employment statuses
 */
public enum EmployeeStatus {
    REGULAR("Regular"),
    PROBATIONARY("Probationary"),
    CONTRACTUAL("Contractual"),
    PART_TIME("Part-Time");
    
    private final String displayName;
    
    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convert a string to an EmployeeStatus enum
     * @param status The status string
     * @return The matching EmployeeStatus or REGULAR as default
     */
    public static EmployeeStatus fromString(String status) {
        if (status == null) {
            return REGULAR;
        }
        
        for (EmployeeStatus employeeStatus : values()) {
            if (employeeStatus.name().equalsIgnoreCase(status) || 
                employeeStatus.getDisplayName().equalsIgnoreCase(status)) {
                return employeeStatus;
            }
        }
        
        return REGULAR; // Default if not found
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}