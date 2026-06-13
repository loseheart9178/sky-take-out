package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

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
}
