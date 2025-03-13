package com.motorph.payroll.dao.test;

import com.motorph.payroll.dao.EmployeeDao;
import com.motorph.payroll.dao.FileEmployeeDao;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.util.test.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FileEmployeeDaoTest {
    
    private EmployeeDao employeeDao;
    private String testFilePath;
    private final String TEMP_FILE = "temp-employees.csv";
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary copy of the test file
        testFilePath = TestUtil.createTempTestFile(TestUtil.TEST_EMPLOYEES_CSV, TEMP_FILE);
        employeeDao = new FileEmployeeDao(testFilePath);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up the temporary file
        TestUtil.cleanupTempTestFile(TEMP_FILE);
    }
    
    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = employeeDao.getAllEmployees();
        
        // Verify we have the expected number of employees
        assertEquals(2, employees.size());
        
        // Verify first employee details
        Employee firstEmployee = employees.get(0);
        assertEquals(1, firstEmployee.getEmployeeId());
        assertEquals("Test", firstEmployee.getLastName());
        assertEquals("Employee", firstEmployee.getFirstName());
    }
    
    @Test
    public void testGetEmployeeById() {
        // Get existing employee
        Employee employee = employeeDao.getEmployeeById(1);
        
        assertNotNull(employee);
        assertEquals(1, employee.getEmployeeId());
        assertEquals("Test", employee.getLastName());
        
        // Get non-existent employee
        Employee nonExistent = employeeDao.getEmployeeById(999);
        assertNull(nonExistent);
    }
    
    @Test
    public void testAddEmployee() {
        // Create a new employee
        Employee newEmployee = new Employee(
            3, "New", "Employee", "03/03/1993", "New Address", "5555555555",
            "77-7777777-7", "77-777777777-7", "777-777-777-777", "7777-7777-7777",
            "Regular", "New Position", "New Supervisor", 
            28000.0, 1500.0, 1000.0, 1000.0, 14000.0, 159.09
        );
        
        // Add the employee
        employeeDao.addEmployee(newEmployee);
        
        // Verify it was added
        Employee retrieved = employeeDao.getEmployeeById(3);
        assertNotNull(retrieved);
        assertEquals("New", retrieved.getLastName());
        assertEquals("Employee", retrieved.getFirstName());
    }
    
    @Test
    public void testUpdateEmployee() {
        // Get an existing employee
        Employee employee = employeeDao.getEmployeeById(1);
        
        // Update some fields
        employee.setLastName("Updated");
        employee.setPosition("Updated Position");
        employee.setBasicSalary(22000.0);
        
        // Save the update
        employeeDao.updateEmployee(employee);
        
        // Verify the update
        Employee updated = employeeDao.getEmployeeById(1);
        assertEquals("Updated", updated.getLastName());
        assertEquals("Updated Position", updated.getPosition());
        assertEquals(22000.0, updated.getBasicSalary());
    }
    
    @Test
    public void testDeleteEmployee() {
        // Delete an employee
        employeeDao.deleteEmployee(2);
        
        // Verify it was deleted
        assertNull(employeeDao.getEmployeeById(2));
        
        // Verify we only have one employee left
        assertEquals(1, employeeDao.getAllEmployees().size());
    }
    
    @Test
    public void testSaveEmployees() {
        // Add a new employee
        Employee newEmployee = new Employee(
            3, "New", "Employee", "03/03/1993", "New Address", "5555555555",
            "77-7777777-7", "77-777777777-7", "777-777-777-777", "7777-7777-7777",
            "Regular", "New Position", "New Supervisor", 
            28000.0, 1500.0, 1000.0, 1000.0, 14000.0, 159.09
        );
        
        employeeDao.addEmployee(newEmployee);
        
        // Save the changes
        boolean result = employeeDao.saveEmployees();
        
        // Verify save was successful
        assertTrue(result);
        
        // Create a new DAO instance to load from the saved file
        EmployeeDao newDao = new FileEmployeeDao(testFilePath);
        
        // Verify the new employee was saved
        Employee retrieved = newDao.getEmployeeById(3);
        assertNotNull(retrieved);
        assertEquals("New", retrieved.getLastName());
    }
}