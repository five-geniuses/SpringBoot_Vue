package emo.chen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import emo.chen.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取今日订单统计信息
     * @return 包含今日订单数量、今日营业额等信息的Map
     */
    Map<String, Object> getTodayOrderStats();

    /**
     * 获取今日订单列表
     * @param page 页码
     * @param size 每页大小
     * @return 今日订单分页列表
     */
    Page<Order> getTodayOrders(int page, int size);

    /**
     * 检查用户是否可以被删除
     * 只有当用户的所有订单都是已完成或已取消状态时，才能删除用户
     * @param userId 用户ID
     * @return true如果用户可以被删除，false如果不能
     */
    boolean canDeleteUser(Integer userId);

    /**
     * 验证订单金额
     * @param orderNo 订单号
     * @param amount 支付金额
     * @return 验证结果
     */
    boolean verifyOrderAmount(String orderNo, String amount);

    /**
     * 根据订单号获取订单信息
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order getOrderByOrderNo(String orderNo);

    /**
     * 更新订单信息
     * @param order 订单信息
     * @return 更新结果
     */
    boolean updateOrder(Order order);
} 