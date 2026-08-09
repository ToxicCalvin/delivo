package com.delivo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.delivo.constant.MessageConstant;
import com.delivo.constant.PasswordConstant;
import com.delivo.constant.StatusConstant;
import com.delivo.context.BaseContext;
import com.delivo.dto.EmployeeDTO;
import com.delivo.dto.EmployeeLoginDTO;
import com.delivo.dto.EmployeePageQueryDTO;
import com.delivo.entity.Employee;
import com.delivo.exception.AccountLockedException;
import com.delivo.exception.AccountNotFoundException;
import com.delivo.exception.PasswordErrorException;
import com.delivo.mapper.EmployeeMapper;
import com.delivo.result.PageResult;
import com.delivo.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * Employee Login
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1. Query database data by username
        Employee employee = employeeMapper.getByUsername(username);

        // 2. Handle various exceptions (username does not exist, incorrect password,
        // account locked)
        if (employee == null) {
            // Account does not exist
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND());
        }

        // Password verification
        // Perform MD5 encryption on the plaintext password transmitted from the
        // frontend.
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // Incorrect password
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR());
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // Account locked
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED());
        }

        // 3、Return the entity object
        return employee;
    }

    /**
     * Newly hired employees
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建时间和修改时间
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录创建人id和修改人id
        // employee.setCreateUser(BaseContext.getCurrentId());
        // employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * Pagination Query
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit 0,10
        // 开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * Enable/Disable Employee Account
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?

        /*
         * Employee employee = new Employee();
         * employee.setStatus(status);
         * employee.setId(id);
         */

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * Search employees by ID
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * Edit Employee Information
     *
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }
}
