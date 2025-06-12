package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Cart;
import emo.chen.entity.Goods;
import emo.chen.entity.Order;
import emo.chen.entity.OrderItem;
import emo.chen.mapper.CartMapper;
import emo.chen.mapper.GoodsMapper;
import emo.chen.mapper.OrderItemMapper;
import emo.chen.mapper.OrderMapper;
import emo.chen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public Order createOrder(Integer userId, String receiverName, String receiverPhone, 
                           String receiverAddress, String remark, List<Integer> cartIds) {
        // 1. 生成订单号
        String orderNo = generateOrderNo();
        
        // 2. 获取购物车商品
        QueryWrapper<Cart> cartWrapper = new QueryWrapper<>();
        cartWrapper.in("cart_id", cartIds);
        List<Cart> cartList = cartMapper.selectList(cartWrapper);
        
        if (cartList.isEmpty()) {
            return null;
        }
        
        // 3. 计算总金额和总数量
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Cart cart : cartList) {
            // 检查商品库存
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods == null || goods.getNum() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足");
            }
            
            // 创建订单项
            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setGoodsId(cart.getGoodsId());
            item.setGoodsName(cart.getGoodsName());
            item.setImgUrl(cart.getImgUrl());
            item.setPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(cart.getTotalPrice());
            item.setCreateTime(LocalDateTime.now());
            orderItems.add(item);
            
            // 更新商品库存
            goods.setNum(goods.getNum() - cart.getQuantity());
            goodsMapper.updateById(goods);
            
            totalAmount = totalAmount.add(cart.getTotalPrice());
            totalQuantity += cart.getQuantity();
        }
        
        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setBuyNum(totalQuantity);
        order.setBuyPrice(totalAmount.divide(new BigDecimal(totalQuantity), 2, BigDecimal.ROUND_HALF_UP));
        order.setOrderState(0); // 待付款
        order.setPayState(0); // 未支付
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 5. 保存订单和订单项
        save(order);
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getOrderId());
            orderItemMapper.insert(item);
        }
        
        // 6. 清空购物车
        cartMapper.deleteBatchIds(cartIds);
        
        // 设置订单项
        order.setOrderItems(orderItems);
        
        return order;
    }

    @Override
    @Transactional
    public boolean cancelOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null || order.getOrderState() != 0) {
            return false;
        }
        
        // 恢复商品库存
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        for (OrderItem item : items) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            goods.setNum(goods.getNum() + item.getQuantity());
            goodsMapper.updateById(goods);
        }
        
        // 更新订单状态
        order.setOrderState(4); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean payOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null || order.getOrderState() != 0) {
            return false;
        }
        
        order.setOrderState(1); // 待发货
        order.setPayState(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean deliverOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null || order.getOrderState() != 1) {
            return false;
        }
        
        order.setOrderState(2); // 待收货
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean confirmOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null || order.getOrderState() != 2) {
            return false;
        }
        
        order.setOrderState(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public Order getOrderDetail(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order != null) {
            // 获取订单项
            QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
            wrapper.eq("order_no", orderNo);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);
            order.setOrderItems(items);
        }
        return order;
    }

    @Override
    public Page<Order> getUserOrders(Integer userId, Integer orderState, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (orderState != null) {
            wrapper.eq("order_state", orderState);
        }
        wrapper.orderByDesc("create_time");
        return page(pageParam, wrapper);
    }

    @Override
    public Page<Order> getAllOrders(Integer orderState, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (orderState != null) {
            wrapper.eq("order_state", orderState);
        }
        wrapper.orderByDesc("create_time");
        return page(pageParam, wrapper);
    }

    private Order getOrderByNo(String orderNo) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        return getOne(wrapper);
    }

    private String generateOrderNo() {
        // 生成订单号：时间戳 + UUID前8位
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + uuid;
    }
} 