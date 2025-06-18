package emo.chen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import emo.chen.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM t_order WHERE create_time BETWEEN #{todayStart} AND #{todayEnd}")
    BigDecimal getTodayTotalAmount(LocalDateTime todayStart, LocalDateTime todayEnd);
    
    @Select("SELECT COUNT(DISTINCT goods_id) FROM t_order o JOIN t_order_item i ON o.order_no = i.order_no WHERE o.create_time BETWEEN #{todayStart} AND #{todayEnd}")
    Integer getTodayTotalProducts(LocalDateTime todayStart, LocalDateTime todayEnd);
} 