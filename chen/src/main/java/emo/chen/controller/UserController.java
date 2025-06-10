package emo.chen.controller;

import emo.chen.entity.User;
import emo.chen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginForm, HttpSession session) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        User user = userService.login(username, password);
        
        if (user != null) {
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("用户名或密码错误");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.register(user)) {
            return ResponseEntity.ok("注册成功");
        }
        return ResponseEntity.badRequest().body("用户名已存在");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user = userService.getUserById(user.getId());
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("未登录");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            updatedUser.setId(currentUser.getId());
            updatedUser.setRole(currentUser.getRole()); // Prevent role modification
            if (userService.updateUser(updatedUser)) {
                session.setAttribute("user", updatedUser);
                return ResponseEntity.ok("更新成功");
            }
        }
        return ResponseEntity.badRequest().body("更新失败");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            String oldPassword = passwordForm.get("oldPassword");
            String newPassword = passwordForm.get("newPassword");
            if (userService.changePassword(user.getId(), oldPassword, newPassword)) {
                return ResponseEntity.ok("密码修改成功");
            }
        }
        return ResponseEntity.badRequest().body("密码修改失败");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("登出成功");
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", userService.checkUsernameExists(username));
        return ResponseEntity.ok(response);
    }
} 