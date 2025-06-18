package emo.chen.controller;

import emo.chen.entity.Goods;
import emo.chen.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    @Autowired
    private RecommendService recommendService;

    @GetMapping("/user/{userId}")
    public List<Goods> recommend(@PathVariable Integer userId,
                                 @RequestParam(defaultValue = "5") int topN) {
        return recommendService.recommendForUser(userId, topN);
    }
} 