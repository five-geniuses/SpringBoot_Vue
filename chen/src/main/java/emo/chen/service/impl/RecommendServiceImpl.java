package emo.chen.service.impl;

import emo.chen.entity.Comment;
import emo.chen.entity.Goods;
import emo.chen.mapper.CommentMapper;
import emo.chen.mapper.GoodsMapper;
import emo.chen.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Goods> recommendForUser(Integer userId, int topN) {
        // 1. 查询所有用户-商品-评分
        List<Comment> allComments = commentMapper.selectList(null);
        if (allComments == null || allComments.isEmpty()) return Collections.emptyList();

        // 2. 构建用户-商品评分矩阵
        Map<Integer, Map<Integer, Integer>> userRatings = new HashMap<>();
        for (Comment c : allComments) {
            userRatings
                .computeIfAbsent(c.getUserId(), k -> new HashMap<>())
                .put(c.getGoodsId(), c.getRating());
        }
        if (!userRatings.containsKey(userId)) return Collections.emptyList();
        Map<Integer, Integer> targetRatings = userRatings.get(userId);

        // 3. 计算目标用户与其他用户的相似度
        Map<Integer, Double> similarity = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : userRatings.entrySet()) {
            Integer otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;
            double sim = cosineSimilarity(targetRatings, entry.getValue());
            similarity.put(otherUserId, sim);
        }

        // 4. 找到最相似的K个用户
        int K = 5;
        List<Integer> topUsers = similarity.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(K)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // 5. 推荐这些用户喜欢但目标用户未评分的商品
        Set<Integer> ratedGoods = targetRatings.keySet();
        Map<Integer, Double> recommendScore = new HashMap<>();
        for (Integer otherUserId : topUsers) {
            double sim = similarity.get(otherUserId);
            Map<Integer, Integer> otherRatings = userRatings.get(otherUserId);
            for (Map.Entry<Integer, Integer> e : otherRatings.entrySet()) {
                Integer goodsId = e.getKey();
                if (ratedGoods.contains(goodsId)) continue;
                recommendScore.merge(goodsId, sim * e.getValue(), Double::sum);
            }
        }

        // 6. 按得分排序，返回topN商品
        List<Integer> recommendGoodsIds = recommendScore.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(topN)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (recommendGoodsIds.isEmpty()) return Collections.emptyList();
        // 查询商品详情
        return goodsMapper.selectBatchIds(recommendGoodsIds);
    }

    // 余弦相似度
    private double cosineSimilarity(Map<Integer, Integer> a, Map<Integer, Integer> b) {
        Set<Integer> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());
        if (common.isEmpty()) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (Integer gid : common) {
            dot += a.get(gid) * b.get(gid);
        }
        for (Integer v : a.values()) normA += v * v;
        for (Integer v : b.values()) normB += v * v;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
} 