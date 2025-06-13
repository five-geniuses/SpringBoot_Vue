package com.example.controller;

import com.example.entity.Goods;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/{id}")
    public ResponseEntity<Goods> getById(@PathVariable("id") Integer id) {
        Goods goods = goodsService.findById(id);
        return goods != null ? ResponseEntity.ok(goods) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Goods>> searchByName(@RequestParam(required = false) String name) {
        if (name != null && !name.trim().isEmpty()) {
            return ResponseEntity.ok(goodsService.findByName(name));
        }
        return ResponseEntity.ok(goodsService.findAll());
    }

    @GetMapping
    public ResponseEntity<List<Goods>> getAll() {
        return ResponseEntity.ok(goodsService.findAll());
    }

    @PostMapping
    public ResponseEntity<Goods> create(
            @RequestPart("goods") Goods goods,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(goodsService.insert(goods, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("id") Integer id,
            @RequestPart("goods") Goods goods,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        goods.setGoodsId(id);
        return goodsService.update(goods, file)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        return goodsService.deleteById(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
} 