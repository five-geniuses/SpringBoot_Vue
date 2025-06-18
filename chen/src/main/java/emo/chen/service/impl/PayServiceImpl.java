package emo.chen.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import emo.chen.config.AlipayConfig;
import emo.chen.entity.Order;
import emo.chen.service.OrderService;
import emo.chen.service.PayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    private static final Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);

    private final AlipayConfig alipayConfig;
    private final OrderService orderService;

    @Autowired
    public PayServiceImpl(AlipayConfig alipayConfig, OrderService orderService) {
        this.alipayConfig = alipayConfig;
        this.orderService = orderService;
    }

    @Override
    public String createAlipayOrder(String outTradeNo, String totalAmount, String subject, String returnUrl) {
        try {
            // 验证订单信息
            Order order = orderService.getOrderByOrderNo(outTradeNo);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (order.getPayState() == 1) {
                throw new RuntimeException("订单已支付");
            }
            if (!order.getTotalAmount().toString().equals(totalAmount)) {
                throw new RuntimeException("订单金额不匹配");
            }

            // 创建AlipayClient
            AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
            );

            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(returnUrl);
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());

            // 构建请求参数
            String bizContent = "{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":\"" + totalAmount + "\"," +
                "\"subject\":\"" + subject + "\"" +
                "}";
            alipayRequest.setBizContent(bizContent);

            // 调用SDK生成表单
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            logger.info("生成支付宝支付表单成功，订单号：{}", outTradeNo);
            return form;

        } catch (AlipayApiException e) {
            logger.error("生成支付宝支付表单失败", e);
            throw new RuntimeException("创建支付订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                "UTF-8",
                "RSA2"
            );

            if (signVerified) {
                // 商户订单号
                String outTradeNo = params.get("out_trade_no");
                // 支付宝交易号
                String tradeNo = params.get("trade_no");
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 支付金额
                String totalAmount = params.get("total_amount");

                logger.info("收到支付宝回调通知 - 订单号：{}，支付宝交易号：{}，交易状态：{}", 
                    outTradeNo, tradeNo, tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    // 获取订单信息
                    Order order = orderService.getOrderByOrderNo(outTradeNo);
                    if (order == null) {
                        logger.error("订单不存在 - 订单号：{}", outTradeNo);
                        return "failure";
                    }

                    // 验证订单金额
                    if (!order.getTotalAmount().equals(new BigDecimal(totalAmount))) {
                        logger.error("订单金额不匹配 - 订单号：{}，支付金额：{}，订单金额：{}", 
                            outTradeNo, totalAmount, order.getTotalAmount());
                        return "failure";
                    }

                    // 更新订单状态
                    order.setPayState(1); // 设置为已支付
                    order.setOrderState(1); // 设置为待发货状态
                    order.setPayTime(LocalDateTime.now());
                    order.setUpdateTime(LocalDateTime.now());
                    
                    // 保存订单更新
                    orderService.updateOrder(order);
                    
                    logger.info("订单支付成功并更新状态 - 订单号：{}", outTradeNo);
                    return "success";
                }
            } else {
                logger.warn("支付宝回调通知签名验证失败");
            }
        } catch (AlipayApiException e) {
            logger.error("处理支付宝回调通知失败", e);
        }
        return "failure";
    }
} 