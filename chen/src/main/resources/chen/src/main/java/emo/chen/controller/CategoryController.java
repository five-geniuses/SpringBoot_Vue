package emo.chen.controller;

import emo.chen.entity.Category;
import emo.chen.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        logger.info("获取所有分类");
        List<Category> categories = categoryService.list();
        logger.info("分类数量: {}", categories.size());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        logger.info("获取分类信息，ID: {}", id);
        Category category = categoryService.findById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        }
        logger.warn("分类不存在，ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<?> getCategoryByName(@RequestParam String name) {
        logger.info("根据名称查询分类: {}", name);
        Category category = categoryService.findByName(name);
        if (category != null) {
            logger.info("找到分类: {}", category.getCateId());
            return ResponseEntity.ok(category);
        }
        logger.warn("未找到分类: {}", name);
        return ResponseEntity.notFound().build();
    }
} 