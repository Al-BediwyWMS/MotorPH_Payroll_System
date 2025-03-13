package com.motorph.payroll.service.test;

import com.motorph.payroll.dao.EmployeeDao;
import com.motorph.payroll.dao.FileEmployeeDao;
import com.motorph.payroll.exception.EmployeeNotFoundException;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.service.EmployeeService;
import com.motorph.payroll.service.EmployeeServiceImpl;
import com.motorph.payroll.util.AppConstants;
import com.motorph.payroll.util.test.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class EmployeeServiceImplTest {
    
    private EmployeeService employeeService;
    private String testFilePath;
    private final String TEMP_FILE = "temp-employees.csv";
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary copy of the test file
        testFilePath = TestUtil.createTempTestFile(TestUtil.TEST_EMPLOYEES_CSV, TEMP_FILE);
        EmployeeDao employeeDao = new FileEmployeeDao(testFilePath);
        employeeService = new EmployeeServiceImpl(employeeDao);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up the temporary file
        TestUtil.cleanupTempTestFile(TEMP_FILE);
    }
    
    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        
        // Verify we have the expected number of employees
        assertEquals(2, employees.size());
    }
    
    @Test
    public void testGetEmployeeById() {
        // Get existing employee
        Employee employee = employeeService.getEmployeeById(1);
        
        assertNotNull(employee);
        assertEquals(1, employee.getEmployeeId());
        
        // Get non-existent employee - should throw exception
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.getEmployeeById(999);
        });
    }
    
    @Test
    public void testLogin() {
        // Test successful employee login
        String username = AppConstants.EMPLOYEE_USERNAME_PREFIX + "1";
        String password = "1";
        
        Employee employee = employeeService.login(username, password);
        
        assertNotNull(employee);
        assertEquals(1, employee.getEmployeeId());
        
        // Test failed login - wrong password
        assertNull(employeeService.login(username, "wrong"));
        
        // Test failed login - wrong username format
        assertNull(employeeService.login("notAnEmployee", password));
        
        // Test failed login - non-existent employee
        assertNull(employeeService.login(AppConstants.EMPLOYEE_USERNAME_PREFIX + "999", "999"));
    }
    
    @Test
    public void testGenerateNewEmployeeId() {
        // Should return the highest id + 1
        int newId = employeeService.generateNewEmployeeId();
        assertEquals(3, newId);
    }
}