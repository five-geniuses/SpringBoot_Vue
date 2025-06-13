package emo.chen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import emo.chen.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    // 创建订单
    Order createOrder(Integer userId, String receiverName, String receiverPhone, 
                     String receiverAddress, String remark, List<Integer> cartIds);
    
    // 取消订单
    boolean cancelOrder(String orderNo);
    
    // 支付订单
    boolean payOrder(String orderNo);
    
    // 发货
    boolean deliverOrder(String orderNo);
    
    // 确认收货
    boolean confirmOrder(String orderNo);
    
    // 获取订单详情
    Order getOrderDetail(String orderNo);
    
    // 获取用户订单列表
    Page<Order> getUserOrders(Integer userId, Integer orderState, int page, int size);
    
    // 获取所有订单（管理员）
    Page<Order> getAllOrders(Integer orderState, int page, int size);
} 