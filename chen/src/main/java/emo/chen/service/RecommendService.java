package emo.chen.service;

import emo.chen.entity.Goods;
import java.util.List;

public interface RecommendService {
    /**
     * 为指定用户推荐商品
     * @param userId 用户ID
     * @param topN 推荐商品数量
     * @return 推荐商品列表
     */
    List<Goods> recommendForUser(Integer userId, int topN);
} 