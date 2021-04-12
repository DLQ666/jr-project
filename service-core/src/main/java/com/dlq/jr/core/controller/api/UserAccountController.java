package com.dlq.jr.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.dlq.jr.common.result.R;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.service.UserAccountService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "会员账户")
@Slf4j
@RestController
@RequestMapping("/api/core/userAccount")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @ApiOperation("充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public R commitCharge(@PathVariable BigDecimal chargeAmt, HttpServletRequest request) {
        //获取当前用户登录id
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        //组装表单字符串，用于远程提交数据
        String formStr = userAccountService.commitCharge(chargeAmt, userId);
        return R.ok().data("formStr", formStr);
    }

    @ApiOperation(value = "用户充值异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        //验签
        if (RequestHelper.isSignEquals(paramMap)){
            //判断业务是否成功
            if ("0001".equals(paramMap.get("resultCode"))){
                //同步账户数据
                return userAccountService.notify(paramMap);
            }else {
                log.info("用户充值异步回调充值失败，代码不是0001：" + JSON.toJSONString(paramMap));
                return "success";
            }
        }else {
            log.info("用户充值异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
    }
}

