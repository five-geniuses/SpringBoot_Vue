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
} 