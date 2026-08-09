package com.delivo.service;

import com.delivo.dto.EmployeeDTO;
import com.delivo.dto.EmployeeLoginDTO;
import com.delivo.dto.EmployeePageQueryDTO;
import com.delivo.entity.Employee;
import com.delivo.result.PageResult;

public interface EmployeeService {

    /**
     * Employee Login
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * Newly hired employees
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * Pagination Query
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * Enable/Disable Employee Account
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * Search employees by ID
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * Edit Employee Information
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
