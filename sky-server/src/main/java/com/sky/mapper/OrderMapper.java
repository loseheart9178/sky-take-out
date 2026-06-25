package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders 订单数据
     */
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders 订单数据
     */
    void update(Orders orders);

    /**
     * 分页查询历史订单信息
     * @param ordersPageQueryDTO 查询参数
     * @return 订单分页数据
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param orderId 订单id
     * @return 订单数据
     */
    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    /**
     * 各个状态的订单数量统计
     * @param status
     * @return 订单数量
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 查询超时的订单
     * @param pendingPayment
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{pendingPayment} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);



    /**
     * 取消支付超时且仍处于待支付、未支付状态的订单
     */
    int cancelTimeoutOrders(@Param("time") LocalDateTime time,
                            @Param("cancelTime") LocalDateTime cancelTime,
                            @Param("pendingPaymentStatus") Integer pendingPaymentStatus,
                            @Param("unPaidStatus") Integer unPaidStatus,
                            @Param("cancelledStatus") Integer cancelledStatus);

    /**
     * 完成超时且仍处于派送中的订单
     */
    int completeDeliveryOrders(@Param("time") LocalDateTime time,
                               @Param("deliveryTime") LocalDateTime deliveryTime,
                               @Param("deliveryInProgressStatus") Integer deliveryInProgressStatus,
                               @Param("completedStatus") Integer completedStatus);
}
