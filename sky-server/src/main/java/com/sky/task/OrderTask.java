package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理支付超时订单，每分钟触发一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单: {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        int count = orderMapper.cancelTimeoutOrders(time, LocalDateTime.now(),
                Orders.PENDING_PAYMENT, Orders.UN_PAID, Orders.CANCELLED);
        log.info("支付超时订单处理完成，实际取消订单数量: {}", count);
    }
    /**
     * 定时处理一直处于派送中订单，每天凌晨一点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单: {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        int count = orderMapper.completeDeliveryOrders(time, LocalDateTime.now(),
                Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED);
        log.info("派送中订单处理完成，实际完成订单数量: {}", count);
    }
}
