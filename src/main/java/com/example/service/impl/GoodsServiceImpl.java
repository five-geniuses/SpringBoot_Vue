package com.example.service.impl;

import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public Goods findById(Integer goodsId) {
        return goodsMapper.findById(goodsId);
    }

    @Override
    public List<Goods> findByName(String name) {
        return goodsMapper.findByName(name);
    }

    @Override
    public List<Goods> findAll() {
        return goodsMapper.findAll();
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
        
        goodsMapper.insert(goods);
        return goods;
    }

    @Override
    public boolean update(Goods goods, MultipartFile file) {
        // 如果有新文件上传，先删除旧文件
        if (file != null && !file.isEmpty()) {
            Goods oldGoods = goodsMapper.findById(goods.getGoodsId());
            if (oldGoods != null && oldGoods.getImgUrl() != null) {
                deleteOldFile(oldGoods.getImgUrl());
            }
            String fileName = handleFileUpload(file);
            goods.setImgUrl(fileName);
        }
        
        return goodsMapper.update(goods) > 0;
    }

    @Override
    public boolean deleteById(Integer goodsId) {
        // 删除文件
        Goods goods = goodsMapper.findById(goodsId);
        if (goods != null && goods.getImgUrl() != null) {
            deleteOldFile(goods.getImgUrl());
        }
        
        return goodsMapper.deleteById(goodsId) > 0;
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