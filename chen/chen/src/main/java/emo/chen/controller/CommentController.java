package emo.chen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import emo.chen.entity.Comment;
import emo.chen.entity.Goods;
import emo.chen.service.CommentService;
import emo.chen.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Comment comment) {
        try {
            Comment savedComment = commentService.addComment(comment);
            return ResponseEntity.ok(savedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/goods/{goodsId}")
    public ResponseEntity<?> getGoodsComments(
            @PathVariable Integer goodsId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 获取商品信息
        Goods goods = goodsService.findById(goodsId);
        if (goods == null) {
            return ResponseEntity.badRequest().body("商品不存在");
        }

        // 获取评论列表
        Page<Comment> comments = commentService.getGoodsComments(goodsId, page, size);
        
        // 获取平均评分
        double avgRating = commentService.calculateAverageRating(goodsId);

        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("avgRating", avgRating);
        response.put("goods", goods);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserComments(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> comments = commentService.getUserComments(userId, page, size);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer commentId,
            @RequestParam(required = true) Integer userId) {
        try {
            // 验证评论是否存在
            Comment comment = commentService.getById(commentId);
            if (comment == null) {
                return ResponseEntity.badRequest().body("评论不存在");
            }
            
            // 验证是否是评论作者
            if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body("只能删除自己的评论");
            }
            
            boolean result = commentService.deleteComment(commentId, userId);
            if (!result) {
                return ResponseEntity.badRequest().body("删除评论失败");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除评论失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 