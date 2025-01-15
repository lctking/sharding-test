package com.lctking.shardingtest.controller;

import com.lctking.shardingtest.dto.req.UserRegisterReqDTO;
import com.lctking.shardingtest.result.Result;
import com.lctking.shardingtest.result.Results;
import com.lctking.shardingtest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> userRegister(@RequestBody UserRegisterReqDTO Params){
        userService.userRegister(Params);
        return Results.success();
    }
}
