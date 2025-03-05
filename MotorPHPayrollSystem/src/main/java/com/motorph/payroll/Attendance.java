package com.motorph.payroll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Attendance {
    private int employeeId;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private double totalHours;
    private double overtimeHours;
    private double lateMinutes;
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public Attendance(int employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        calculateWorkingHours();
    }

    private void calculateWorkingHours() {
        if (timeIn != null && timeOut != null) {
            // Calculate total duration
            Duration workDuration = Duration.between(timeIn, timeOut);
            
            // Subtract 1 hour for lunch break if worked more than 5 hours
            double totalMinutes = workDuration.toMinutes();
            if (totalMinutes > 300) { // 5 hours = 300 minutes
                totalMinutes -= 60; // Subtract lunch break
            }
            
            this.totalHours = totalMinutes / 60.0;

            // Calculate late minutes
            if (timeIn.isAfter(STANDARD_START_TIME)) {
                this.lateMinutes = Duration.between(STANDARD_START_TIME, timeIn).toMinutes();
            }

            // Calculate overtime (after 5:00 PM)
            if (timeOut.isAfter(STANDARD_END_TIME)) {
                this.overtimeHours = Duration.between(STANDARD_END_TIME, timeOut).toMinutes() / 60.0;
            }
        }
    }

    // Getters
    public int getEmployeeId() { return employeeId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public double getTotalHours() { return totalHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getLateMinutes() { return lateMinutes; }
    
    public String getFormattedTimeIn() {
        return timeIn != null ? timeIn.format(TIME_FORMATTER) : "N/A";
    }
    
    public String getFormattedTimeOut() {
        return timeOut != null ? timeOut.format(TIME_FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return String.format("Attendance{employeeId=%d, date=%s, timeIn=%s, timeOut=%s, totalHours=%.2f, overtimeHours=%.2f}",
                           employeeId, date, getFormattedTimeIn(), getFormattedTimeOut(), totalHours, overtimeHours);
    }
}