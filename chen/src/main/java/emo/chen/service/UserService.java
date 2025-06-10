package emo.chen.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import emo.chen.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
    boolean register(User user);
    Page<User> getUserList(int pageNum, int pageSize);
    User getUserById(Integer id);
    User getUserByUsername(String username);
    boolean updateUser(User user);
    boolean deleteUser(Integer id);
    boolean changePassword(Integer userId, String oldPassword, String newPassword);
    boolean checkUsernameExists(String username);
} 