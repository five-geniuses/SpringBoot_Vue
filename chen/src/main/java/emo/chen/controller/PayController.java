package emo.chen.controller;

import emo.chen.entity.Order;
import emo.chen.service.OrderService;
import emo.chen.service.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@CrossOrigin
public class PayController {
    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PayService payService;

    @Autowired
    private OrderService orderService;

    /**
     * 创建支付宝支付订单
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAlipayOrder(@RequestBody Map<String, String> params) {
        try {
            // 参数验证
            String orderNo = params.get("outTradeNo");
            String totalAmount = params.get("totalAmount");
            String subject = params.get("subject");
            String returnUrl = params.get("returnUrl");

            if (orderNo == null || totalAmount == null || subject == null || returnUrl == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "缺少必要参数"
                ));
            }

            // 验证订单是否存在
            Order order = orderService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "订单不存在"
                ));
            }

            // 验证订单状态
            if (order.getPayState() == 1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "订单已支付"
                ));
            }

            // 验证订单金额
            if (!order.getTotalAmount().toString().equals(totalAmount)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "订单金额不匹配"
                ));
            }

            String form = payService.createAlipayOrder(orderNo, totalAmount, subject, returnUrl);
            
            return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", form,
                "message", "支付表单生成成功"
            ));
        } catch (Exception e) {
            logger.error("创建支付订单失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "创建支付订单失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 支付宝支付结果通知
     */
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            
            logger.info("收到支付宝回调通知：{}", params);
            return payService.handleAlipayNotify(params);
        } catch (Exception e) {
            logger.error("处理支付回调失败", e);
            return "failure";
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    public ResponseEntity<?> getPayStatus(@PathVariable String orderNo) {
        try {
            Order order = orderService.getOrderByOrderNo(orderNo);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "订单不存在"
                ));
            }

            return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", Map.of(
                    "orderNo", order.getOrderNo(),
                    "payState", order.getPayState(),
                    "orderState", order.getOrderState(),
                    "payTime", order.getPayTime()
                ),
                "message", "获取支付状态成功"
            ));
        } catch (Exception e) {
            logger.error("获取支付状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "获取支付状态失败：" + e.getMessage()
            ));
        }
    }
} 