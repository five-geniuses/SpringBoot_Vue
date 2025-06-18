package emo.chen.service;

import emo.chen.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
 
public interface CategoryService extends IService<Category> {
    Category findById(Integer cateId);
    String getCategoryName(Integer cateId);
    // 根据分类名称查询分类
    Category findByName(String name);
} 