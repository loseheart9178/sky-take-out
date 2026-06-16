package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO 菜品和口味数据
     * @return Result
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清理缓存数据
        String key = "dish_"+dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品管理分页查询
     *
     * @param dishPageQueryDTO 分页查询参数
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     *
     * @param ids 菜品id数组
     * @return Result
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品，ids：{}", ids);
        dishService.deleteBatch(ids);
        // 清理所有菜品的缓存数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id 菜品id
     * @return Result<DishVO>
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品信息：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息，同时更新对应的口味信息
     *
     * @param dishDTO 菜品和口味数据
     * @return Result
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品信息：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 清理所有菜品的缓存数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 菜品起售停售
     *
     * @param status 状态
     * @param id     菜品id
     * @return Result
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改菜品状态：{}", id);
        dishService.updateStatus(status, id);
        //清理缓存
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
      * @param categoryId 分类id
     * @return Result<List < Dish>>
     */
    @GetMapping("/list")
    public Result<List<Dish>> listByCategory( Long categoryId) {
        log.info("查询：{}", categoryId);
        List<Dish> list = dishService.listByCategory(categoryId);
        return Result.success(list);
    }

    /**
     * 清理缓存数据
     *
     * @param pattern 缓存key的匹配模式
     */
    private void cleanCache(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
