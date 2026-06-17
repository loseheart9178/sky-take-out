package com.sky.service.impl;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.utils.RequestContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 向购物车添加商品
     *
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //组装shoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(RequestContextUtil.getCurrentUserId());

        //获取商品在购物车中数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断当前商品是否在购物车中,如果在，则增加数量，update
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }
        //如果不在，则添加到购物车，insert

        //判断本次添加的商品是菜品还是套餐
        if (shoppingCartDTO.getDishId() != null) {
            //添加的是菜品
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            //添加的是套餐
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        //设置公共数据
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());


        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     * @return 购物车数据
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前用户id
        Long userId = RequestContextUtil.getCurrentUserId();
        return shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = RequestContextUtil.getCurrentUserId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //组装shoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(RequestContextUtil.getCurrentUserId());

        //获取商品在购物车中数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断当前商品是否在购物车中
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            //如果数量大于1，则减少数量
            if (cart.getNumber() > 1) {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                //如果数量等于1，则删除该商品记录
                shoppingCartMapper.delete(shoppingCart);
            }
        }
    }
}
