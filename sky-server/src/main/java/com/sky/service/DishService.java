package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO 菜品数据
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
      * @param dishPageQueryDTO 分页查询参数
      * @return PageResult
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids 菜品id
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id 菜品id
     * @return DishVO
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 更新菜品信息，同时更新对应的口味数据
     * @param dishDTO 菜品数据
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售停售
     * @param status 状态
     * @param id 菜品id
     */
    void updateStatus(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId 分类id
      * @return 菜品列表
     */
    List<Dish> listByCategory(Long categoryId);

    /**
     * 根据条件查询菜品和口味
     * @param dish 筛选条件
     * @return List<DishVO>
     */
    List<DishVO> listWithFlavor(Dish dish);
}
