package emo.chen.controller;

import emo.chen.entity.User;
import emo.chen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
        }
    }

    @PostMapping("/register")
    }

        session.invalidate();
    }
    }