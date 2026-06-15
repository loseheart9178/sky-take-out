package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId 分类id
     * @return 菜品数量
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish 菜品数据
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 分页查询参数
     * @return 菜品分页数据列表
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品数据
     * @param id 菜品id
     * @return 菜品数据
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据id删除菜品数据
     * @param id 菜品id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量id删除菜品
      * @param ids 菜品id
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改菜品数据
     * @param dish 菜品数据
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId 分类id
     * @return 菜品列表
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> listByCategory(Long categoryId);

    /**
     * 检查是否有菜品处于启用状态
     * @param ids 菜品id列表
     * @return 启用状态的菜品数量
     */
    Integer countEnabledStatus(List<Long> ids);

    /**
     * 根据套餐id查询菜品
     * @param setmealId 套餐id
     * @return List<Dish>
     */
    @Select("select d.* from dish d left join setmeal_dish sd on d.id = sd.dish_id where setmeal_id = " +
            "#{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

}
