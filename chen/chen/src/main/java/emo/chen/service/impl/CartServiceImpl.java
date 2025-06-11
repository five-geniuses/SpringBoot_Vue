package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Cart;
import emo.chen.entity.Goods;
import emo.chen.mapper.CartMapper;
import emo.chen.mapper.GoodsMapper;
import emo.chen.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional
    public boolean addToCart(Integer userId, Integer goodsId, Integer quantity) {
        // 检查商品是否存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getState() == 0) {
            return false;
        }

        // 检查是否已在购物车中
        Cart existCart = checkExist(userId, goodsId);
        if (existCart != null) {
            // 更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            existCart.setTotalPrice(goods.getPrice().multiply(new BigDecimal(existCart.getQuantity())));
            existCart.setUpdateTime(LocalDateTime.now());
            return updateById(existCart);
        }

        // 创建新购物车项
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setGoodsName(goods.getName());
        cart.setQuantity(quantity);
        cart.setPrice(goods.getPrice());
        cart.setTotalPrice(cart.getPrice().multiply(new BigDecimal(quantity)));
        cart.setImgUrl(goods.getImgUrl());
        cart.setAddTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());

        return save(cart);
    }

    @Override
    public boolean updateQuantity(Integer cartId, Integer quantity) {
        Cart cart = getById(cartId);
        if (cart == null) {
            return false;
        }

        cart.setQuantity(quantity);
        cart.setTotalPrice(cart.getPrice().multiply(new BigDecimal(quantity)));
        cart.setUpdateTime(LocalDateTime.now());
        return updateById(cart);
    }

    @Override
    public boolean removeFromCart(Integer cartId) {
        return removeById(cartId);
    }

    @Override
    public List<Cart> getUserCart(Integer userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("add_time");
        return list(wrapper);
    }

    @Override
    public boolean clearCart(Integer userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return remove(wrapper);
    }

    @Override
    public Cart checkExist(Integer userId, Integer goodsId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("goods_id", goodsId);
        return getOne(wrapper);
    }
} 