package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.R;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.pojo.vo.RegisterVo;
import com.dlq.jr.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "会员接口")
@Slf4j
@RestController
@RequestMapping("/api/core/userInfo")
public class UserInfoController {

    @Autowired
    private RedisTemplate redisTemplate;
    //private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public R register(@RequestBody RegisterVo registerVo) {

        //校验验证码是否正确
        String mobile = registerVo.getMobile();
        String code = registerVo.getCode();
        String codeGen = (String) redisTemplate.opsForValue().get("jr:sms:code:" + mobile);
        //String codeMobile = (String) redisTemplate.opsForValue().get("jr:sms:code:" + mobile);
        //上边这种用泛型的不行 取出数据为  "\"1234\"" -----> 串中串
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        //注册
        userInfoService.register(registerVo);

        return R.ok().message("注册成功！");
    }
}
