package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.RequestContextUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;


    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //1.处理各种业务异常（地址为空，购物车为空）

        //判断地址是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //判断购物车是否为空
        Long userId = RequestContextUtil.getCurrentUserId();
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart.builder().userId(userId).build());
        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2.向订单表插入一条数据

        //封装订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(RequestContextUtil.getCurrentUserId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));   //订单号
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());//收货人
        orderMapper.insert(orders);
        //3.向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : list) {
            //封装订单明细数据
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置订单明细的订单id
            //添加到订单明细集合中，方便批量插入
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //4.清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //5.组装VO进行返回

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        /*
         * 原微信支付逻辑：需要微信支付商户号、证书、APIv3密钥和公网回调地址。
         * 目前没有营业执照，无法正常开通商户号，所以先保留原代码用于后续对比和恢复。
         */
//        // 当前登录用户id
//        Long userId = RequestContextUtil.getCurrentUserId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

        /*
         * 模拟支付逻辑：跳过真实微信下单和支付回调，直接复用 paySuccess 更新订单状态。
         * 这样订单会从“待付款/未支付”变为“待接单/已支付”，方便本地开发和课程演示。
         */
        paySuccess(ordersPaymentDTO.getOrderNumber());

        return OrderPaymentVO.builder()
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .nonceStr("mock-pay")
                .packageStr("mock-pay")
                .signType("MOCK")
                .paySign("mock-pay")
                .build();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    /**
     * 订单列表
     *
     * @param ordersPageQueryDTO 订单查询参数
     * @return PageResult
     */
    public PageResult pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置用户id
        ordersPageQueryDTO.setUserId(RequestContextUtil.getCurrentUserId());
        //开启分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查询订单信息
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //将Orders对象转换为OrderVO对象并返回
        List<OrderVO> orderVOList = getOrderVOList(page, false);
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 历史订单查询
     *
     * @param orderId
     * @return OrderVO
     */
    @Override
    public OrderVO detail(Long orderId) {
        //
        Orders orders = orderMapper.getById(orderId);

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void userCancelById(Long id) throws Exception {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态不为代付款或待接单时不允许直接取消
        if (order.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单状态为待接单时取消，需要进行退款
        if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
           /* //调用微信支付退款接口
            weChatPayUtil.refund(
                    order.getNumber(), //商户订单号
                    order.getNumber(), //商户退款单号
                    new BigDecimal("0.01"),//退款金额，单位 元
                    new BigDecimal("0.01"));//原订单金额
*/
            //支付状态修改为退款
            order.setPayStatus(Orders.REFUND);
        }

        //更新订单状态、取消原因、取消时间并更新到数据库
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void repetition(Long orderId) {
        //获取当前用户id
        Long userId = RequestContextUtil.getCurrentUserId();

        //查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

        //将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            //将订单详情里的菜品信息复制到购物车对象中并补全缺少的信息
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        //批量添加到购物车
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return PageResult
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //将Orders对象转换为OrderVO对象并返回
        List<OrderVO> orderVOList = getOrderVOList(page, true);
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 统计订单数据
     *
     * @return OrderStatisticsVO
     */
    @Override
    public OrderStatisticsVO statistics() {

        //分别查询待接单、待派送，派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);


        //封装结果并返回
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO 接单参数
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO 拒单参数
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //查询订单
        Orders orders=orderMapper.getById(ordersRejectionDTO.getId());
        //只有订单存在，且订单状态为待接单，才可接单
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //获取订单状态
        Integer status = orders.getStatus();
        //若用户已付款，则需要退款
        if (status.equals(Orders.PAID)) {
           /* //调用微信支付退款接口
            String refund = weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal("0.01"),//退款金额，单位 元
                    new BigDecimal("0.01"));//原订单金额
            log.info("申请退款：{}", refund);*/

            //支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }
        //根据订单id更新订单状态、拒单原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO 取消订单参数
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders=orderMapper.getById(ordersCancelDTO.getId());

        if (orders == null ) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer payStatus = orders.getPayStatus(); //支付状态
        //若用户已支付，则需要退款
        if (payStatus.equals(Orders.PAID)) {
          /*  //调用微信支付退款接口
            String refund = weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal("0.01"),
                    new BigDecimal("0.01")
            );
            log.info("申请退款：{}", refund);*/

            //支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        //根据订单id更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param orderId 订单id
     */
    @Override
    public void delivery(Long orderId) {
        //根据id查询订单
        Orders orders = orderMapper.getById(orderId);

        //只有订单存在且状态为待派送才可以派送
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //更新订单状态
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param orderId 订单id
     */
    @Override
    public void complete(Long orderId) {
        //根据id查询订单
        Orders orders = orderMapper.getById(orderId);

        //只有订单存在且状态为派送中才可以完成
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //更新订单状态、送达时间
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page, boolean isAdmin) {
        List<OrderVO> orderVOList = new ArrayList<>();

        //查询订单明细，并封装入OrderVO进行响应
        if (page != null && !page.isEmpty()) {
            for (Orders order : page) {
                //封装入OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                //查询订单明细
                Long orderId = order.getId(); //订单id
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

                //如果是管理端的查询请求，就拼接菜品信息字符串(格式：菜品名字*菜品数量; )
                if (isAdmin) {
                    StringBuffer orderDishes = new StringBuffer();
                    for (OrderDetail orderDetail : orderDetailList) {
                        orderDishes.append(orderDetail.getName()).append('*').append(orderDetail.getNumber()).append("; ");
                        orderVO.setOrderDishes(orderDishes.toString());
                    }
                } else {
                    //如果是用户端的查询请求，就把订单明细封装入OrderVO
                    orderVO.setOrderDetailList(orderDetailList);
                }
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
}
