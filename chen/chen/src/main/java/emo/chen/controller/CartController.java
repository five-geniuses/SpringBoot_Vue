package emo.chen.controller;

import emo.chen.entity.Cart;
import emo.chen.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            // 从请求体中获取参数
            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer goodsId = Integer.valueOf(request.get("goodsId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            // 参数验证
            if (userId == null || goodsId == null || quantity == null) {
                return ResponseEntity.badRequest().body(createResponse(false, "缺少必要参数"));
            }

            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(createResponse(false, "商品数量必须大于0"));
            }

            boolean result = cartService.addToCart(userId, goodsId, quantity);
            if (!result) {
                return ResponseEntity.badRequest().body(createResponse(false, "加入购物车失败"));
            }
            
            return ResponseEntity.ok(createResponse(true, "成功加入购物车"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "加入购物车失败：" + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestParam(required = true) Integer cartId,
                                          @RequestParam(required = true) Integer quantity) {
        try {
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(createResponse(false, "商品数量必须大于0"));
            }

            boolean result = cartService.updateQuantity(cartId, quantity);
            if (!result) {
                return ResponseEntity.badRequest().body(createResponse(false, "更新购物车失败"));
            }
            
            return ResponseEntity.ok(createResponse(true, "购物车更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "更新购物车失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer cartId) {
        try {
            boolean result = cartService.removeFromCart(cartId);
            if (!result) {
                return ResponseEntity.badRequest().body(createResponse(false, "删除购物车商品失败"));
            }
            
            return ResponseEntity.ok(createResponse(true, "成功删除购物车商品"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "删除购物车商品失败：" + e.getMessage()));
        }
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getUserCart(@PathVariable Integer userId) {
        try {
            List<Cart> cartList = cartService.getUserCart(userId);
            return ResponseEntity.ok(cartList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "获取购物车列表失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            boolean result = cartService.clearCart(userId);
            if (!result) {
                return ResponseEntity.badRequest().body(createResponse(false, "清空购物车失败"));
            }
            
            return ResponseEntity.ok(createResponse(true, "成功清空购物车"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createResponse(false, "清空购物车失败：" + e.getMessage()));
        }
    }

    // 创建统一的响应格式
    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
} 