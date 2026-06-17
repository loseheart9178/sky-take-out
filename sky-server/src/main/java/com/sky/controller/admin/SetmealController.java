package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    /**
     * 新增套餐
     * @param setmealDTO 套餐DTO
     * @return Result
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO 分页查询参数
     * @return Result
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery( SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids ids
     * @return Result
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐和关联的菜品数据
     * @param id 套餐id
     * @return Result
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getByIdWithDish(@PathVariable Long id) {
        log.info("查询套餐：{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }
    /**
     * 修改套餐
     * @param setmealDTO 套餐DTO
     * @return Result
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }
    /**
     * 起售停售套餐
     * @param status 状态
     * @param id id
     * @return Result
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("起售停售套餐：{}", status);
        log.info("套餐id：{}", id);
        setmealService.updateStatus(status, id);
        return Result.success();
    }

}
