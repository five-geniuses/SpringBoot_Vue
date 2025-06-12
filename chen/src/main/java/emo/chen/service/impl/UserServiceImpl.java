package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.User;
import emo.chen.mapper.UserMapper;
import emo.chen.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
    public boolean deleteUser(Integer id) {
        User user = getById(id);
        if (user != null && user.getRole() == 0) {
            return false; // Cannot delete admin users
        }
        return removeById(id);
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