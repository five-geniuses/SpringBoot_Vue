package emo.chen.controller;

import emo.chen.entity.Cart;
import emo.chen.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer userId,
                                     @RequestParam Integer goodsId,
                                     @RequestParam Integer quantity) {
        boolean result = cartService.addToCart(userId, goodsId, quantity);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestParam Integer cartId,
                                          @RequestParam Integer quantity) {
        boolean result = cartService.updateQuantity(cartId, quantity);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer cartId) {
        boolean result = cartService.removeFromCart(cartId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getUserCart(@PathVariable Integer userId) {
        List<Cart> cartList = cartService.getUserCart(userId);
        return ResponseEntity.ok(cartList);
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        boolean result = cartService.clearCart(userId);
        return ResponseEntity.ok(result);
    }
} 