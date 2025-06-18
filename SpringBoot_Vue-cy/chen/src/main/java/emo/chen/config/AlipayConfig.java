package emo.chen.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {
    private static final Logger logger = LoggerFactory.getLogger(AlipayConfig.class);

    // 支付宝网关，沙箱环境使用
    @Value("${alipay.gateway-url}")
    private String gatewayUrl;
    
    // 应用ID
    @Value("${alipay.app-id}")
    private String appId;
    
    // 商户私钥
    @Value("${alipay.merchant-private-key}")
    private String merchantPrivateKey;
    
    // 支付宝公钥
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    
    // 异步通知地址
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    
    // 同步返回地址
    @Value("${alipay.return-url}")
    private String returnUrl;

    @Bean
    public AlipayClient alipayClient() {
        logger.info("初始化支付宝客户端...");
        logger.info("网关地址: {}", gatewayUrl);
        logger.info("应用ID: {}", appId);
        logger.info("异步通知地址: {}", notifyUrl);
        logger.info("同步返回地址: {}", returnUrl);

        try {
            DefaultAlipayClient client = new DefaultAlipayClient(
                gatewayUrl,    // 支付宝网关
                appId,         // APPID
                merchantPrivateKey,    // 商户私钥
                "json",        // 参数格式
                "UTF-8",       // 编码
                alipayPublicKey,      // 支付宝公钥
                "RSA2"        // 签名类型
            );
            
            logger.info("支付宝客户端初始化成功");
            return client;
        } catch (Exception e) {
            logger.error("支付宝客户端初始化失败", e);
            throw new RuntimeException("支付宝客户端初始化失败: " + e.getMessage());
        }
    }

    // Getter 方法
    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getAppId() {
        return appId;
    }

    public String getMerchantPrivateKey() {
        return merchantPrivateKey;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
} 