package emo.chen.service;

import emo.chen.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
 
public interface CategoryService extends IService<Category> {
    Category findById(Integer cateId);
    String getCategoryName(Integer cateId);
} 