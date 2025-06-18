package emo.chen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import emo.chen.entity.Order;
import emo.chen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.sql.SQLIntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestParam Integer userId,
                                       @RequestParam String receiverName,
                                       @RequestParam String receiverPhone,
                                       @RequestParam String receiverAddress,
                                       @RequestParam(required = false) String remark,
                                       @RequestParam List<Integer> cartIds) {
        Order order = orderService.createOrder(userId, receiverName, receiverPhone, 
                                            receiverAddress, remark, cartIds);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/cancel/{orderNo}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNo) {
        boolean result = orderService.cancelOrder(orderNo);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pay/{orderNo}")
    public ResponseEntity<?> payOrder(@PathVariable String orderNo) {
        boolean result = orderService.payOrder(orderNo);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deliver/{orderNo}")
    public ResponseEntity<?> deliverOrder(@PathVariable String orderNo) {
        boolean result = orderService.deliverOrder(orderNo);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/confirm/{orderNo}")
    public ResponseEntity<?> confirmOrder(@PathVariable String orderNo) {
        boolean result = orderService.confirmOrder(orderNo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/detail/{orderNo}")
    public ResponseEntity<?> getOrderDetail(@PathVariable String orderNo) {
        Order order = orderService.getOrderDetail(orderNo);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Integer userId,
                                         @RequestParam(required = false) Integer orderState,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.getUserOrders(userId, orderState, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/list")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Integer orderState,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.getAllOrders(orderState, page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取今日订单统计信息
     */
    @GetMapping("/today/stats")
    public ResponseEntity<?> getTodayOrderStats() {
        Map<String, Object> stats = orderService.getTodayOrderStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取今日订单列表
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.getTodayOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * 删除订单
     * 只能删除已完成或已取消的订单
     */
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderNo) {
        try {
            boolean result = orderService.deleteOrder(orderNo);
            if (result) {
                return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "success", true,
                    "message", "订单删除成功"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "code", 400,
                    "success", false,
                    "message", "订单删除失败，可能是订单不存在或订单状态不允许删除"
                ));
            }
        } catch (Exception e) {
            // 获取具体的错误信息
            String errorMessage = e.getMessage();
            Throwable cause = e.getCause();
            if (cause != null) {
                // 如果是外键约束错误
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    errorMessage = "删除失败：订单存在关联数据无法删除";
                }
                // 如果有更具体的错误信息，使用它
                else if (cause.getMessage() != null) {
                    errorMessage = cause.getMessage();
                }
            }
            
            logger.error("删除订单时发生错误，订单号: {}, 错误: {}", orderNo, errorMessage, e);
            
            return ResponseEntity.ok(Map.of(
                "code", 500,
                "success", false,
                "message", "删除订单失败：" + errorMessage
            ));
        }
    }
} 