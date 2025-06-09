package emo.chen.controller;

import emo.chen.entity.User;
import emo.chen.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            return "登录成功！";
        } else {
            return "用户名或密码错误！";
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        System.out.println(user.toString());
        boolean success = userService.register(user);
        return success ? "注册成功！" : "注册失败！";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "退出登录成功！";
    }
}
