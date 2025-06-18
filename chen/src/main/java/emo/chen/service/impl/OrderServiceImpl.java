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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

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
        // 检查订单是否存在且状态为待付款或待发货
        if (order == null || (order.getOrderState() != 0 && order.getOrderState() != 1)) {
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
    @Transactional
    public boolean confirmOrder(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (order == null || order.getOrderState() != 2) {
            return false;
        }
        
        // 获取订单项并更新商品销量
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        for (OrderItem item : items) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods != null) {
                // 更新商品销量
                goods.setSales(goods.getSales() + item.getQuantity());
                goodsMapper.updateById(goods);
            }
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
        
        // 使用MyBatis-Plus的分页查询
        Page<Order> orderPage = page(pageParam, wrapper);
        
        // 获取订单项信息
        if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
            for (Order order : orderPage.getRecords()) {
                // 获取订单项
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_no", order.getOrderNo());
                List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
                order.setOrderItems(items);
            }
        }
        
        return orderPage;
    }

    @Override
    public Page<Order> getAllOrders(Integer orderState, int page, int size) {
        // 确保页码从1开始
        if (page < 1) {
            page = 1;
        }
        // 确保每页大小合理
        if (size < 1) {
            size = 10;
        }
        
        // 创建分页对象
        Page<Order> pageParam = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (orderState != null) {
            wrapper.eq("order_state", orderState);
        }
        wrapper.orderByDesc("create_time");
        
        // 执行分页查询
        Page<Order> orderPage = page(pageParam, wrapper);
        
        // 获取订单项信息
        if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
            for (Order order : orderPage.getRecords()) {
                // 获取订单项
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_no", order.getOrderNo());
                List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
                order.setOrderItems(items);
            }
        }
        
        return orderPage;
    }

    @Override
    public Map<String, Object> getTodayOrderStats() {
        // 获取今日开始和结束时间
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();
        
        // 创建查询条件
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", todayStart, todayEnd);
        
        // 获取今日订单数量
        long orderCount = count(wrapper);
        
        // 获取今日订单总额
        BigDecimal totalAmount = baseMapper.getTodayTotalAmount(todayStart, todayEnd);
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        
        // 获取今日销售商品总数
        Integer totalProducts = baseMapper.getTodayTotalProducts(todayStart, todayEnd);
        if (totalProducts == null) {
            totalProducts = 0;
        }
        
        // 封装结果
        Map<String, Object> stats = new HashMap<>();
        stats.put("orderCount", orderCount);
        stats.put("totalAmount", totalAmount);
        stats.put("totalProducts", totalProducts);
        
        return stats;
    }

    @Override
    public Page<Order> getTodayOrders(int page, int size) {
        // 获取今天的开始和结束时间
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        
        // 构建分页查询
        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.between("create_time", todayStart, todayEnd)
              .orderByDesc("create_time");
        
        // 执行分页查询
        Page<Order> orderPage = page(pageParam, wrapper);
        
        // 获取订单项信息
        if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
            for (Order order : orderPage.getRecords()) {
                // 获取订单项
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_no", order.getOrderNo());
                List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
                order.setOrderItems(items);
            }
        }
        
        return orderPage;
    }

    @Override
    public boolean canDeleteUser(Integer userId) {
        logger.info("检查用户是否可以删除，用户ID: {}", userId);
        
        // 查询用户的所有订单
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Order> orders = list(wrapper);
        
        // 如果用户没有订单，可以直接删除
        if (orders.isEmpty()) {
            logger.info("用户没有订单，可以删除");
            return true;
        }
        
        // 检查所有订单是否都是已完成或已取消状态
        for (Order order : orders) {
            // orderState: 3-已完成, 4-已取消
            if (order.getOrderState() != 3 && order.getOrderState() != 4) {
                logger.info("用户存在未完成且未取消的订单，订单号: {}, 订单状态: {}", 
                    order.getOrderNo(), order.getOrderState());
                return false;
            }
        }
        
        logger.info("用户的所有订单都已完成或已取消，可以删除");
        return true;
    }

    @Override
    public boolean verifyOrderAmount(String orderNo, String amount) {
        try {
            // 获取订单信息
            Order order = this.getOrderDetail(orderNo);
            if (order == null) {
                logger.error("订单不存在，订单号：{}", orderNo);
                return false;
            }

            // 将字符串金额转换为BigDecimal进行精确比较
            BigDecimal payAmount = new BigDecimal(amount);
            BigDecimal orderAmount = order.getTotalAmount();

            // 比较金额是否相等
            if (orderAmount.compareTo(payAmount) == 0) {
                return true;
            } else {
                logger.error("订单金额不匹配，订单号：{}，订单金额：{}，支付金额：{}", 
                    orderNo, orderAmount, payAmount);
                return false;
            }
        } catch (Exception e) {
            logger.error("验证订单金额时发生错误，订单号：{}", orderNo, e);
            return false;
        }
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