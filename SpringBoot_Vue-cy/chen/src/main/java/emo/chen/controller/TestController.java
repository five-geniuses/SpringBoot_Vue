package emo.chen.controller;

import emo.chen.entity.Order;
import emo.chen.service.OrderService;
import emo.chen.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayService payService;

    @Value("${alipay.return-url}")
    private String returnUrl;

    /**
     * 创建测试订单
     */
    @PostMapping("/createTestOrder")
    public String createTestOrder() {
        // 生成测试订单号
        String orderNo = "TEST" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        
        // 创建支付宝订单（金额0.01元）
        return payService.createAlipayOrder(
            orderNo,
            "0.01",
            "测试订单-" + orderNo,
            returnUrl
        );
    }

    /**
     * 生成测试订单号
     */
    private String generateTestOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%03d", new Random().nextInt(1000));
        return "TEST" + timestamp + random;
    }
} 