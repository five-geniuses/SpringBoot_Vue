package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Goods;
import emo.chen.mapper.GoodsMapper;
import emo.chen.service.GoodsService;
import emo.chen.service.CategoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoodsServiceImpl.class);

    @Autowired
    private CategoryService categoryService;

    @Value("${file.upload-path}")
    private String uploadPath;

    private void setCategoryName(Goods goods) {
        if (goods != null && goods.getCateId() != null) {
            String categoryName = categoryService.getCategoryName(goods.getCateId());
            goods.setCategoryName(categoryName != null ? categoryName : "未知分类");
        }
    }

    @Override
    public Goods findById(Integer goodsId) {
        Goods goods = getById(goodsId);
        if (goods != null) {
            setCategoryName(goods);
        }
        return goods;
    }

    @Override
    public List<Goods> findByName(String name) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Goods::getName, name);
        List<Goods> goodsList = list(queryWrapper);
        goodsList.forEach(this::setCategoryName);
        return goodsList;
    }

    @Override
    public List<Goods> findAll() {
        List<Goods> goodsList = list();
        goodsList.forEach(this::setCategoryName);
        return goodsList;
    }

    // 添加验证商品数量的方法
    private void validateAndSetGoodsStatus(Goods goods) {
        // 确保商品数量不小于0
        if (goods.getNum() < 0) {
            goods.setNum(0);
        }
        
        // 当商品数量为0时，自动将状态设置为0
        if (goods.getNum() == 0) {
            goods.setState(0);
        } else if (goods.getState() == null) {
            goods.setState(1); // 如果数量大于0且状态为null，设置为正常状态
        }
    }

    @Override
    public Goods insert(Goods goods) {
        // 设置创建时间
        goods.setAddtime(new Date());
        goods.setCreationdate(new Date());
        
        // 验证并设置商品状态
        validateAndSetGoodsStatus(goods);
        
        save(goods);
        
        // 设置分类名称
        setCategoryName(goods);
        return goods;
    }

    @Override
    public boolean update(Goods goods) {
        // 验证并设置商品状态
        validateAndSetGoodsStatus(goods);
        
        boolean result = updateById(goods);
        if (result) {
            setCategoryName(goods);
        }
        return result;
    }

    @Override
    public boolean deleteById(Integer goodsId) {
        logger.info("开始删除商品，ID: {}", goodsId);
        
        // 删除文件
        Goods goods = getById(goodsId);
        if (goods == null) {
            logger.warn("要删除的商品不存在，ID: {}", goodsId);
            return false;
        }
         
        logger.info("找到商品: {}", goods);
        
        if (goods.getImgUrl() != null) {
            logger.info("删除商品图片: {}", goods.getImgUrl());
            deleteOldFile(goods.getImgUrl());
        }
        
        boolean result = removeById(goodsId);
        logger.info("删除商品结果: {}", result ? "成功" : "失败");
        
        return result;
    }

    private String handleFileUpload(MultipartFile file) {
        // 确保上传目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        try {
            file.transferTo(new File(uploadPath + File.separator + fileName));
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private void deleteOldFile(String fileName) {
        File file = new File(uploadPath + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
    }
} 