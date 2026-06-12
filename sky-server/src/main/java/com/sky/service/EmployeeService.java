package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeVO;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO   员工DTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO 分页查询参数
     * @return PageResult
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 设置员工状态
     * @param status 状态
     * @param id 员工id
     */
    void setEmployeeStatus(Integer status, Long id);

    /**
     * 根据id查询员工信息
     * @param id 员工id
     * @return EmployeeVO
     *
     */
    EmployeeVO getById(Long id);

    /**
     * 修改员工信息
     * @param employeeDTO 员工对象
     */
    void update(EmployeeDTO employeeDTO);
}
