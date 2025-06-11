package emo.chen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import emo.chen.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
} 