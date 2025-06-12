package emo.chen.controller;

import emo.chen.entity.Goods;
import emo.chen.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin // 添加跨域支持
public class GoodsController {
    
    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/{id}")
    public ResponseEntity<Goods> getById(@PathVariable("id") Integer id) {
        logger.info("获取商品信息，ID: {}", id);
        try {
            Goods goods = goodsService.findById(id);
            return goods != null ? ResponseEntity.ok(goods) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("获取商品信息失败，ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Goods>> searchByName(@RequestParam(required = false) String name) {
        logger.info("搜索商品，关键词: {}", name);
        try {
            if (name != null && !name.trim().isEmpty()) {
                return ResponseEntity.ok(goodsService.findByName(name));
            }
            return ResponseEntity.ok(goodsService.findAll());
        } catch (Exception e) {
            logger.error("搜索商品失败，关键词: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Goods>> getAll() {
        logger.info("获取所有商品");
        try {
            return ResponseEntity.ok(goodsService.findAll());
        } catch (Exception e) {
            logger.error("获取所有商品失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Goods> create(
            @RequestPart("goods") Goods goods,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        logger.info("创建商品: {}", goods);
        try {
            return ResponseEntity.ok(goodsService.insert(goods, file));
        } catch (Exception e) {
            logger.error("创建商品失败: {}", goods, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("id") Integer id,
            @RequestPart("goods") Goods goods,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        logger.info("更新商品，ID: {}, 商品信息: {}", id, goods);
        try {
            goods.setGoodsId(id);
            return goodsService.update(goods, file)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("更新商品失败，ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        logger.info("删除商品，ID: {}", id);
        try {
            return goodsService.deleteById(id)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("删除商品失败，ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 