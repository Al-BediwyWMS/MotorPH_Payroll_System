package com.motorph.payroll.dao.test;

import com.motorph.payroll.dao.AttendanceDao;
import com.motorph.payroll.dao.FileAttendanceDao;
import com.motorph.payroll.model.Attendance;
import com.motorph.payroll.util.test.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FileAttendanceDaoTest {
    
    private AttendanceDao attendanceDao;
    private String testFilePath;
    private final String TEMP_FILE = "temp-attendance.csv";
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary copy of the test file
        testFilePath = TestUtil.createTempTestFile(TestUtil.TEST_ATTENDANCE_CSV, TEMP_FILE);
        attendanceDao = new FileAttendanceDao(testFilePath);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up the temporary file
        TestUtil.cleanupTempTestFile(TEMP_FILE);
    }
    
    @Test
    public void testGetAllAttendance() {
        List<Attendance> attendance = attendanceDao.getAllAttendance();
        
        // Verify we have the expected number of records
        assertEquals(3, attendance.size());
    }
    
    @Test
    public void testGetAttendanceByEmployeeId() {
        // Get attendance for employee 1
        List<Attendance> employee1Attendance = attendanceDao.getAttendanceByEmployeeId(1);
        
        // Verify we have the expected number of records
        assertEquals(2, employee1Attendance.size());
        
        // Get attendance for employee 2
        List<Attendance> employee2Attendance = attendanceDao.getAttendanceByEmployeeId(2);
        
        // Verify we have the expected number of records
        assertEquals(1, employee2Attendance.size());
        
        // Get attendance for non-existent employee
        List<Attendance> nonExistentAttendance = attendanceDao.getAttendanceByEmployeeId(999);
        
        // Verify we have no records
        assertTrue(nonExistentAttendance.isEmpty());
    }
    
    @Test
    public void testGetAttendanceByDateRange() {
        // Get attendance for employee 1 on March 1, 2024
        List<Attendance> attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 1)
        );
        
        // Verify we have one record
        assertEquals(1, attendance.size());
        
        // Get attendance for employee 1 for the entire month
        attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 31)
        );
        
        // Verify we have two records
        assertEquals(2, attendance.size());
        
        // Get attendance for a date range with no records
        attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 4, 1),
            LocalDate.of(2024, 4, 30)
        );
        
        // Verify we have no records
        assertTrue(attendance.isEmpty());
    }
    
    @Test
    public void testAddAttendance() {
        // Create a new attendance record
        Attendance newAttendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 3),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        // Add the record
        attendanceDao.addAttendance(newAttendance);
        
        // Verify it was added
        List<Attendance> attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 3),
            LocalDate.of(2024, 3, 3)
        );
        
        assertEquals(1, attendance.size());
        assertEquals(LocalTime.of(8, 0), attendance.get(0).getTimeIn());
        assertEquals(LocalTime.of(17, 0), attendance.get(0).getTimeOut());
    }
    
    @Test
    public void testUpdateAttendance() {
        // Add a new attendance record
        Attendance newAttendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 3),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        attendanceDao.addAttendance(newAttendance);
        
        // Create an updated record
        Attendance updatedAttendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 3),
            LocalTime.of(8, 30),  // Updated time in
            LocalTime.of(17, 30)  // Updated time out
        );
        
        // Update the record
        attendanceDao.updateAttendance(updatedAttendance);
        
        // Verify it was updated
        List<Attendance> attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 3),
            LocalDate.of(2024, 3, 3)
        );
        
        assertEquals(1, attendance.size());
        assertEquals(LocalTime.of(8, 30), attendance.get(0).getTimeIn());
        assertEquals(LocalTime.of(17, 30), attendance.get(0).getTimeOut());
    }
    
    @Test
    public void testDeleteAttendance() {
        // Delete an attendance record
        attendanceDao.deleteAttendance(1, LocalDate.of(2024, 3, 1));
        
        // Verify it was deleted
        List<Attendance> attendance = attendanceDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 1)
        );
        
        assertTrue(attendance.isEmpty());
        
        // Verify we have one less record overall
        assertEquals(2, attendanceDao.getAllAttendance().size());
    }
    
    @Test
    public void testSaveAttendance() {
        // Add a new attendance record
        Attendance newAttendance = new Attendance(
            1,
            LocalDate.of(2024, 3, 3),
            LocalTime.of(8, 0),
            LocalTime.of(17, 0)
        );
        
        attendanceDao.addAttendance(newAttendance);
        
        // Save the changes
        boolean result = attendanceDao.saveAttendance();
        
        // Verify save was successful
        assertTrue(result);
        
        // Create a new DAO instance to load from the saved file
        AttendanceDao newDao = new FileAttendanceDao(testFilePath);
        
        // Verify the new record was saved
        List<Attendance> attendance = newDao.getAttendanceByDateRange(
            1, 
            LocalDate.of(2024, 3, 3),
            LocalDate.of(2024, 3, 3)
        );
        
        assertEquals(1, attendance.size());
    }
}