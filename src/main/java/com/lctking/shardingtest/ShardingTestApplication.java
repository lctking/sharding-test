package com.lctking.shardingtest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.lctking.shardingtest.dao.mapper")
public class ShardingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingTestApplication.class, args);
    }

}
