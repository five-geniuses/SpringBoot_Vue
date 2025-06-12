package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Comment;
import emo.chen.entity.Goods;
import emo.chen.entity.User;
import emo.chen.mapper.CommentMapper;
import emo.chen.service.CommentService;
import emo.chen.service.GoodsService;
import emo.chen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private UserService userService;

    @Override
    public Comment addComment(Comment comment) {
        // 验证评分范围
        if (comment.getRating() < 1 || comment.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        
        // 设置创建时间
        comment.setCreateTime(LocalDateTime.now());
        
        // 保存评论
        save(comment);
        
        return comment;
    }

    @Override
    public Page<Comment> getGoodsComments(Integer goodsId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id", goodsId)
               .orderByDesc("create_time");
        
        Page<Comment> commentPage = page(pageParam, wrapper);
        
        // 获取商品信息
        Goods goods = goodsService.findById(goodsId);
        if (goods != null) {
            String goodsName = goods.getName();
            // 设置商品名称和用户名称
            for (Comment comment : commentPage.getRecords()) {
                comment.setGoodsName(goodsName);
                // 获取并设置用户名
                User user = userService.getUserById(comment.getUserId());
                if (user != null) {
                    comment.setUserName(user.getUsername());
                } else {
                    comment.setUserName("用户" + comment.getUserId());
                }
            }
        }
        
        return commentPage;
    }

    @Override
    public Page<Comment> getUserComments(Integer userId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");
        
        return page(pageParam, wrapper);
    }

    @Override
    public double calculateAverageRating(Integer goodsId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_id", goodsId)
               .select("AVG(rating) as avg_rating");
        
        List<Object> result = listObjs(wrapper);
        if (result != null && !result.isEmpty() && result.get(0) != null) {
            // 保留一位小数
            return Math.round(((Number) result.get(0)).doubleValue() * 10.0) / 10.0;
        }
        return 0.0;
    }

    @Override
    public boolean deleteComment(Integer commentId, Integer userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id", commentId)
               .eq("user_id", userId);  // 确保只能删除自己的评论
        
        return remove(wrapper);
    }
} 