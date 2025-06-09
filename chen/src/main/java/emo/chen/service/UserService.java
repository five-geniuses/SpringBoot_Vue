package emo.chen.service;

import emo.chen.entity.User;

public interface UserService {
    User login(String username, String password);
    boolean register(User user);
}
