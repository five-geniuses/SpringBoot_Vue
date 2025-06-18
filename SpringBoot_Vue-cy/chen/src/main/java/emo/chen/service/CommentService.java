package emo.chen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import emo.chen.entity.Comment;

public interface CommentService extends IService<Comment> {
    // 添加评论
    Comment addComment(Comment comment);
    
    // 获取商品的所有评论（分页）
    Page<Comment> getGoodsComments(Integer goodsId, int page, int size);
    
    // 获取用户的所有评论（分页）
    Page<Comment> getUserComments(Integer userId, int page, int size);
    
    // 计算商品的平均评分
    double calculateAverageRating(Integer goodsId);
    
    // 删除评论（用户只能删除自己的评论）
    boolean deleteComment(Integer commentId, Integer userId);
} 