package com.lctking.shardingtest.dto.req;

import lombok.Data;

@Data
public class UserRegisterReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    private String region;

}
