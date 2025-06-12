package emo.chen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import emo.chen.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自定义方法
}
