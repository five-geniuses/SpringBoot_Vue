package emo.chen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import emo.chen.entity.Order;
import emo.chen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

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
} 