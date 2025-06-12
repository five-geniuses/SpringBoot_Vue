package emo.chen.service;

import emo.chen.entity.Goods;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface GoodsService {
    Goods findById(Integer goodsId);
    List<Goods> findByName(String name);
    List<Goods> findAll();
    Goods insert(Goods goods, MultipartFile file);
    boolean update(Goods goods, MultipartFile file);
    boolean deleteById(Integer goodsId);
} 