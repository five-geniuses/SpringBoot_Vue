package emo.chen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import emo.chen.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {
    // 添加商品到购物车
    boolean addToCart(Integer userId, Integer goodsId, Integer quantity);
    
    // 更新购物车商品数量
    boolean updateQuantity(Integer cartId, Integer quantity);
    
    // 删除购物车商品
    boolean removeFromCart(Integer cartId);
    
    // 获取用户购物车列表
    List<Cart> getUserCart(Integer userId);
    
    // 清空用户购物车
    boolean clearCart(Integer userId);
    
    // 检查商品是否在购物车中
    Cart checkExist(Integer userId, Integer goodsId);
} 