package com.lctking.shardingtest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lctking.shardingtest.dao.entity.UserDO;
import com.lctking.shardingtest.dto.req.UserRegisterReqDTO;

public interface UserService extends IService<UserDO> {
    void userRegister(UserRegisterReqDTO Params);
}
