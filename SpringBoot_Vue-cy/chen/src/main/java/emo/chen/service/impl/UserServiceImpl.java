package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.User;
import emo.chen.entity.Cart;
import emo.chen.entity.Comment;
import emo.chen.mapper.UserMapper;
import emo.chen.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private CommentService commentService;

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("password", password);
        return getOne(queryWrapper);
    }

    @Override
    public boolean register(User user) {
        if (checkUsernameExists(user.getUsername())) {
            return false;
        }
        user.setRole(1); // Set default role as regular user
        return save(user);
    }

    @Override
    public Page<User> getUserList(int pageNum, int pageSize) {
        return page(new Page<>(pageNum, pageSize));
    }

    @Override
    public User getUserById(Integer id) {
        return getById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public boolean updateUser(User user) {
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Integer id) {
        logger.info("开始删除用户，ID: {}", id);
        
        // 检查用户是否存在
        User user = getById(id);
        if (user == null) {
            logger.warn("要删除的用户不存在，ID: {}", id);
            return false;
        }
        
        // 不能删除管理员用户
        if (user.getRole() == 0) {
            logger.warn("不能删除管理员用户，ID: {}", id);
            return false;
        }
        
        // 检查用户订单状态
        if (!orderService.canDeleteUser(id)) {
            logger.warn("用户还有未完成的订单，不能删除，ID: {}", id);
            return false;
        }

        try {
            // 删除用户的购物车数据
            QueryWrapper<Cart> cartWrapper = new QueryWrapper<>();
            cartWrapper.eq("user_id", id);
            cartService.remove(cartWrapper);
            logger.info("已删除用户的购物车数据");

            // 删除用户的评论数据
            QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("user_id", id);
            commentService.remove(commentWrapper);
            logger.info("已删除用户的评论数据");

            // 最后删除用户本身
            boolean result = removeById(id);
            if (result) {
                logger.info("用户删除成功，ID: {}", id);
            } else {
                logger.error("用户删除失败，ID: {}", id);
                throw new RuntimeException("删除用户失败");
            }
            return result;
        } catch (Exception e) {
            logger.error("删除用户及相关数据时发生错误", e);
            throw new RuntimeException("删除用户及相关数据时发生错误", e);
        }
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user != null && user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            return updateById(user);
        }
        return false;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }
} 