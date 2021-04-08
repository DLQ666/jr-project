package com.dlq.jr.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.R;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.vo.UserBindVo;
import com.dlq.jr.core.service.UserBindService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "会员账号绑定")
@Slf4j
@RestController
@RequestMapping("/api/core/userBind")
public class UserBindController {

    @Autowired
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(@RequestBody UserBindVo userBindVo, HttpServletRequest request) {
        //从header中获取token，并对token进行校验，确保用户已经登录，从token中提取userId
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        //根据userId做账户绑定,生成一个动态表单字符串
        String formStr = userBindService.commitBindUser(userBindVo, userId);
        return R.ok().data("formStr", formStr);
    }

    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {

        //汇付宝向尚融宝发起回调请求时携带的参数
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("账户绑定异步回调接收的参数如下：{}", JSON.toJSONString(paramMap));

        //验签
        if (!RequestHelper.isSignEquals(paramMap)) {
            log.error("用户账号绑定异步回调签名验证错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        log.info("验签成功！开始账户绑定");
        userBindService.notify(paramMap);

        return "success";
    }
}

