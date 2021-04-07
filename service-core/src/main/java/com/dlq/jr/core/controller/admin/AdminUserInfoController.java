package com.dlq.jr.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.R;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.common.util.RegexValidateUtils;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.pojo.query.UserInfoQuery;
import com.dlq.jr.core.pojo.vo.LoginVo;
import com.dlq.jr.core.pojo.vo.RegisterVo;
import com.dlq.jr.core.pojo.vo.UserInfoVo;
import com.dlq.jr.core.service.UserInfoService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
@RequestMapping("/admin/core/userInfo")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(@PathVariable("page") Long page,
                      @PathVariable("limit") Long limit,
                      UserInfoQuery userInfoQuery) {

        IPage<UserInfo> pageParam = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.listPage(pageParam, userInfoQuery);
        return R.ok().data("pageModel", pageModel);
    }

}

