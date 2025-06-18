package emo.chen.controller;

import emo.chen.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private PayService payService;

    /**
     * 创建支付宝支付订单
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAlipayOrder(@RequestBody Map<String, String> params) {
        try {
            String form = payService.createAlipayOrder(
                params.get("outTradeNo"),
                params.get("totalAmount"),
                params.get("subject"),
                params.get("returnUrl")
            );
            return ResponseEntity.ok().body(Map.of(
                "code", 200,
                "data", form,
                "message", "支付表单生成成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 支付宝支付结果通知
     */
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
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
        
        return payService.handleAlipayNotify(params);
    }
} 