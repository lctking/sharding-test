package com.lctking.shardingtest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lctking.shardingtest.constant.UserStatusEnum;
import com.lctking.shardingtest.dao.entity.UserDO;
import com.lctking.shardingtest.dao.mapper.UserMapper;
import com.lctking.shardingtest.dto.req.UserRegisterReqDTO;
import com.lctking.shardingtest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;

    @Transactional(rollbackFor = {Throwable.class})
    @Override
    public void userRegister(UserRegisterReqDTO Params) {
        UserDO userDO = UserDO.builder().username(Params.getUsername())
                .password(Params.getPassword())
                .region(Params.getRegion())
                .status(UserStatusEnum.NORMAL.getStatus()).build();
        userMapper.insert(userDO);
    }
}
