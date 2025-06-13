package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Category;
import emo.chen.mapper.CategoryMapper;
import emo.chen.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Override
    public Category findById(Integer cateId) {
        return getById(cateId);
    }
    
    @Override
    public String getCategoryName(Integer cateId) {
        Category category = getById(cateId);
        return category != null ? category.getName() : null;
    }

    @Override
    public Category findByName(String name) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name);
        return getOne(queryWrapper);
    }
} 