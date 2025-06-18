package emo.chen.service;

import java.util.Map;

public interface PayService {
    /**
     * 创建支付宝支付订单
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param subject 订单标题
     * @param returnUrl 同步跳转地址
     * @return 支付表单HTML
     */
    String createAlipayOrder(String outTradeNo, String totalAmount, String subject, String returnUrl);

    /**
     * 处理支付宝异步通知
     * @param params 通知参数
     * @return 处理结果
     */
    String handleAlipayNotify(Map<String, String> params);
} 