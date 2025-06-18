package emo.chen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class UploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/goods/upload")
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