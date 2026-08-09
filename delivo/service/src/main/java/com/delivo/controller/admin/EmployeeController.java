package com.delivo.controller.admin;

import com.delivo.constant.JwtClaimsConstant;
import com.delivo.constant.MessageConstant;
import com.delivo.context.BaseContext;
import com.delivo.dto.EmployeeDTO;
import com.delivo.dto.EmployeeLoginDTO;
import com.delivo.dto.EmployeePageQueryDTO;
import com.delivo.entity.Employee;
import com.delivo.exception.BaseException;
import com.delivo.properties.JwtProperties;
import com.delivo.result.PageResult;
import com.delivo.result.Result;
import com.delivo.service.EmployeeService;
import com.delivo.utils.JwtUtil;
import com.delivo.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Employee Management
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "Employee API")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * Login
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "Employee Login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        // After successful login, generate a JWT token.
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        claims.put(JwtClaimsConstant.ROLE, employee.getRole());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .role(employee.getRole())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * Exit
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("Employee Logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * 
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("Create Employee")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        checkAdmin();
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * Employee Paging Query
     * 
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("Employee Pagination Query")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * Enable/Disable Employee Account
     * 
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("Enable/Disable Employee Account")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        checkAdmin();
        log.info("启用禁用员工账号：{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * Query employee information by ID
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("Get Employee by ID")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * Edit Employee Information
     * 
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("Edit Employee Info")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        checkAdmin();
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 校验当前操作者是否为管理员
     */
    private void checkAdmin() {
        Integer role = BaseContext.getCurrentRole();
        if (role == null || role != 1) {
            throw new BaseException(MessageConstant.PERMISSION_DENIED());
        }
    }
}
