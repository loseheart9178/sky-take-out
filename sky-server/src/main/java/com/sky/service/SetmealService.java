package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO 套餐DTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO 分页查询参数
     * @return 套餐分页查询结果
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids 套餐id数组
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐和对应的菜品数据
     * @param id 套餐id
     * @return SetmealVO
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO 套餐DTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 起售停售套餐
     * @param status 状态
     * @param id 套餐id
     */
    void updateStatus(Integer status, Long id);
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

}
