package emo.chen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import emo.chen.entity.User;
import emo.chen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() == 0;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.badRequest().body("无权限访问");
        }
        Page<User> userPage = userService.getUserList(pageNum, pageSize);
        return ResponseEntity.ok(userPage);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.badRequest().body("无权限访问");
        }
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.badRequest().body("用户不存在");
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.badRequest().body("无权限访问");
        }
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("删除成功");
        }
        return ResponseEntity.badRequest().body("删除失败，可能是管理员账号或用户不存在");
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User user, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.badRequest().body("无权限访问");
        }
        user.setId(id);
        if (userService.updateUser(user)) {
            return ResponseEntity.ok("更新成功");
        }
        return ResponseEntity.badRequest().body("更新失败");
    }

    @GetMapping("/user/search")
    public ResponseEntity<?> searchUser(
            @RequestParam(required = false) String username,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.badRequest().body("无权限访问");
        }
        if (username != null && !username.isEmpty()) {
            User user = userService.getUserByUsername(username);
            return user != null ? ResponseEntity.ok(user) : ResponseEntity.badRequest().body("用户不存在");
        }
        return ResponseEntity.badRequest().body("请提供搜索条件");
    }
} 