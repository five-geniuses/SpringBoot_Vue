package emo.chen.service;

import emo.chen.entity.Goods;
import java.util.List;

public interface GoodsService {
    Goods findById(Integer goodsId);
    List<Goods> findByName(String name);
    List<Goods> findAll();
    Goods insert(Goods goods);
    boolean update(Goods goods);
    boolean deleteById(Integer goodsId);
    
    /**
     * 检查商品是否可以删除或下架
     * @param goodsId 商品ID
     * @return true如果商品可以删除或下架，false如果不能
     */
    boolean canDeleteOrDeactivate(Integer goodsId);
} 