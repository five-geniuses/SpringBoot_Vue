package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Goods {
    private Integer goodsId;
    private String name;
    private String desc;
    private Integer cateId;
    private BigDecimal price;
    private Integer num;
    private Double kgs;
    private String size;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationdate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expirationdate;
    
    private String storagemethod;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addtime;
    
    private Integer state;
    private String imgUrl;
} 