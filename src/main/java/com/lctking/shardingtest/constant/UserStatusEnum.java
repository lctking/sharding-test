package com.lctking.shardingtest.constant;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    NORMAL(1),
    DELETED(0);

    private final int status;

    UserStatusEnum(int status) {
        this.status = status;
    }
}
