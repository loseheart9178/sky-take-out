package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.vo.EmployeeVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username 用户名
     * @return Employee
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee 员工对象
     */
    @Insert("insert into employee (username, name, password, phone, sex, id_number, create_time, update_time, create_user, update_user, status) " +
            "values (#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}, #{status})")
    void insert(Employee employee);

    /**
     * 分页查询
     * @param employeePageQueryDTO 分页查询参数
     * @return Page<Employee>
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 修改员工信息
     * @param employee 员工对象
     */
    void update(Employee employee);

    /**
     * 根据id查询员工信息
     * @param id 员工id
     * @return EmployeeVO
     */
    @Select("select id, username, name, phone, sex, id_number, status from employee where id = #{id}")
    EmployeeVO getById(Long id);
}
