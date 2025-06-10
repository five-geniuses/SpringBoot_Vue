package emo.chen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import emo.chen.entity.Goods;
import emo.chen.mapper.GoodsMapper;
import emo.chen.service.GoodsService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public Goods findById(Integer goodsId) {
        return getById(goodsId);
    }

    @Override
    public List<Goods> findByName(String name) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Goods::getName, name);
        return list(queryWrapper);
    }

    @Override
    public List<Goods> findAll() {
        return list();
    }

    @Override
    public Goods insert(Goods goods, MultipartFile file) {
        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            String fileName = handleFileUpload(file);
            goods.setImgUrl(fileName);
        }
        
        // 设置创建时间
        goods.setAddtime(new Date());
        goods.setCreationdate(new Date());
        
        // 设置默认状态
        if (goods.getState() == null) {
            goods.setState(1); // 1表示正常状态
        }
        
        save(goods);
        return goods;
    }

    @Override
    public boolean update(Goods goods, MultipartFile file) {
        // 如果有新文件上传，先删除旧文件
        if (file != null && !file.isEmpty()) {
            Goods oldGoods = getById(goods.getGoodsId());
            if (oldGoods != null && oldGoods.getImgUrl() != null) {
                deleteOldFile(oldGoods.getImgUrl());
            }
            String fileName = handleFileUpload(file);
            goods.setImgUrl(fileName);
        }
        
        return updateById(goods);
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