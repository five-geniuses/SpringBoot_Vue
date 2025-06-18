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

    // 统一响应格式
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = (Integer) params.get("userId");
            Integer goodsId = (Integer) params.get("goodsId");
            Integer quantity = (Integer) params.get("quantity");

            if (userId == null || goodsId == null || quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().body(
                    createResponse(false, "参数错误", null)
                );
            }

            Cart result = cartService.addToCart(userId, goodsId, quantity);
            return ResponseEntity.ok(
                createResponse(true, "添加成功", result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                createResponse(false, e.getMessage(), null)
            );
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> params) {
        try {
            Integer cartId = (Integer) params.get("cartId");
            Integer quantity = (Integer) params.get("quantity");

            if (cartId == null || quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().body(
                    createResponse(false, "参数错误", null)
                );
            }

            Cart result = cartService.updateQuantity(cartId, quantity);
            return ResponseEntity.ok(
                createResponse(true, "更新成功", result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                createResponse(false, e.getMessage(), null)
            );
        }
    }

    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer cartId) {
        try {
            boolean result = cartService.removeFromCart(cartId);
            return ResponseEntity.ok(
                createResponse(true, "删除成功", result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                createResponse(false, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getUserCart(@PathVariable Integer userId) {
        try {
            List<Cart> cartList = cartService.getUserCart(userId);
            return ResponseEntity.ok(
                createResponse(true, "获取成功", cartList)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                createResponse(false, e.getMessage(), null)
            );
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            boolean result = cartService.clearCart(userId);
            return ResponseEntity.ok(
                createResponse(true, "清空成功", result)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                createResponse(false, e.getMessage(), null)
            );
        }
    }
} 