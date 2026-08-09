package com.delivo.service.impl;

import com.delivo.constant.PasswordConstant;
import com.delivo.constant.StatusConstant;
import com.delivo.dto.EmployeeDTO;
import com.delivo.dto.EmployeeLoginDTO;
import com.delivo.entity.Employee;
import com.delivo.exception.AccountLockedException;
import com.delivo.exception.AccountNotFoundException;
import com.delivo.exception.PasswordErrorException;
import com.delivo.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeMapper employeeMapper;

    /**
     * Test US 8.1 - AC 1: Valid Login
     * TC-EMP-01: Call login with valid credentials and ENABLED status.
     */
    @Test
    public void testLogin_ValidCredentials_ReturnsEmployee() {
        // [Given]
        String passwordPlain = "123456";
        String passwordMd5 = DigestUtils.md5DigestAsHex(passwordPlain.getBytes());

        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword(passwordPlain);

        Employee mockEmployee = new Employee();
        mockEmployee.setUsername("admin");
        mockEmployee.setPassword(passwordMd5);
        mockEmployee.setStatus(StatusConstant.ENABLE); // Enabled

        when(employeeMapper.getByUsername("admin")).thenReturn(mockEmployee);

        // [When]
        Employee result = employeeService.login(loginDTO);

        // [Then]
        assertEquals(mockEmployee, result);
    }

    /**
     * Test US 8.1 - AC 2: Account Not Found
     * TC-EMP-02: Call login. Mock getByUsername returning null.
     */
    @Test
    public void testLogin_AccountNotFound_ThrowsException() {
        // [Given]
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("unknown");
        loginDTO.setPassword("123456");

        when(employeeMapper.getByUsername("unknown")).thenReturn(null);

        // [When & Then]
        assertThrows(AccountNotFoundException.class, () -> {
            employeeService.login(loginDTO);
        });
    }

    /**
     * Test US 8.1 - AC 3: Incorrect Password
     * TC-EMP-03: Call login with wrong password. Mock returning user with different
     * password hash.
     */
    @Test
    public void testLogin_IncorrectPassword_ThrowsException() {
        // [Given]
        String correctPasswordMd5 = DigestUtils.md5DigestAsHex("correctPassword".getBytes());

        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrongPassword"); // Plaintext wrong password

        Employee mockEmployee = new Employee();
        mockEmployee.setUsername("admin");
        mockEmployee.setPassword(correctPasswordMd5); // Hash of correct password

        when(employeeMapper.getByUsername("admin")).thenReturn(mockEmployee);

        // [When & Then]
        assertThrows(PasswordErrorException.class, () -> {
            employeeService.login(loginDTO);
        });
    }

    /**
     * Test US 8.1 - AC 4: Account Locked
     * TC-EMP-04: Call login with correct password, but user status=DISABLE (0).
     */
    @Test
    public void testLogin_AccountLocked_ThrowsException() {
        // [Given]
        String passwordPlain = "123456";
        String passwordMd5 = DigestUtils.md5DigestAsHex(passwordPlain.getBytes());

        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword(passwordPlain);

        Employee mockEmployee = new Employee();
        mockEmployee.setUsername("admin");
        mockEmployee.setPassword(passwordMd5);
        mockEmployee.setStatus(StatusConstant.DISABLE); // Disabled (Locked)

        when(employeeMapper.getByUsername("admin")).thenReturn(mockEmployee);

        // [When & Then]
        assertThrows(AccountLockedException.class, () -> {
            employeeService.login(loginDTO);
        });
    }

    /**
     * Test US 8.2 - AC 1: Default Settings
     * TC-EMP-05: Call save with basic DTO. employeeMapper.insert() is called.
     * verify default password and status=1.
     */
    @Test
    public void testSave_NewEmployee_SetsDefaultPasswordAndStatus() {
        // [Given]
        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("newuser");
        dto.setName("New User");

        // [When]
        employeeService.save(dto);

        // [Then]
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper, times(1)).insert(employeeCaptor.capture());

        Employee savedEmployee = employeeCaptor.getValue();
        assertEquals("newuser", savedEmployee.getUsername());
        assertEquals("New User", savedEmployee.getName());
        assertEquals(StatusConstant.ENABLE, savedEmployee.getStatus()); // Verify status is 1

        String expectedDefaultPasswordHash = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        assertEquals(expectedDefaultPasswordHash, savedEmployee.getPassword()); // Verify default password hash
    }
}
