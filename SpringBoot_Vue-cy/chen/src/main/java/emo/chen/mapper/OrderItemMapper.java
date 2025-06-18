package emo.chen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import emo.chen.entity.OrderItem;
import emo.chen.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    /**
     * 通过订单号获取订单信息
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Select("SELECT * FROM t_order WHERE order_no = #{orderNo}")
    Order getOrderByOrderNo(String orderNo);
} 