package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GoodsMapper {
    
    @Select("SELECT * FROM goods WHERE goods_id = #{goodsId}")
    Goods findById(Integer goodsId);
    
    @Select("SELECT * FROM goods WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Goods> findByName(String name);
    
    @Select("SELECT * FROM goods")
    List<Goods> findAll();
    
    @Insert("INSERT INTO goods(name, desc, cate_id, price, num, kgs, size, creationdate, expirationdate, storagemethod, addtime, state, img_url) " +
            "VALUES(#{name}, #{desc}, #{cateId}, #{price}, #{num}, #{kgs}, #{size}, #{creationdate}, #{expirationdate}, #{storagemethod}, #{addtime}, #{state}, #{imgUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "goodsId")
    int insert(Goods goods);
    
    @Update("UPDATE goods SET name=#{name}, desc=#{desc}, cate_id=#{cateId}, price=#{price}, " +
            "num=#{num}, kgs=#{kgs}, size=#{size}, creationdate=#{creationdate}, " +
            "expirationdate=#{expirationdate}, storagemethod=#{storagemethod}, " +
            "addtime=#{addtime}, state=#{state}, img_url=#{imgUrl} " +
            "WHERE goods_id=#{goodsId}")
    int update(Goods goods);
    
    @Delete("DELETE FROM goods WHERE goods_id = #{goodsId}")
    int deleteById(Integer goodsId);
} 