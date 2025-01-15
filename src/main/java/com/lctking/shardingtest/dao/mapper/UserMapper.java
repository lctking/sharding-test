package com.lctking.shardingtest.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lctking.shardingtest.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
