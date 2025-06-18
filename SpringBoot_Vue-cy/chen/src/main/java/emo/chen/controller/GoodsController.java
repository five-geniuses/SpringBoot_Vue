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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true", 
    allowedHeaders = {"Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method",
        "Access-Control-Request-Headers"},
    exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class GoodsController {
    
    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private GoodsService goodsService;

    @Value("${file.upload-path}")
    private String uploadPath;

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
    public ResponseEntity<Goods> create(@RequestBody Goods goods) {
        logger.info("创建商品: {}", goods);
        try {
            return ResponseEntity.ok(goodsService.insert(goods));
        } catch (Exception e) {
            logger.error("创建商品失败: {}", goods, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("id") Integer id,
            @RequestBody Goods goods) {
        logger.info("更新商品，ID: {}, 商品信息: {}", id, goods);
        try {
            goods.setGoodsId(id);
            return goodsService.update(goods)
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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("开始上传商品图片");
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "请选择要上传的文件"
                ));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "只能上传图片文件"
                ));
            }

            // 检查文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "图片大小不能超过2MB"
                ));
            }

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
            File dest = new File(uploadPath + File.separator + fileName);
            file.transferTo(dest);

            logger.info("图片上传成功：{}", fileName);

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "上传成功",
                "data", fileName
            ));

        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", 500,
                "message", "上传失败：" + e.getMessage()
            ));
        }
    }
} 