package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.vo.InvestVo;
import com.dlq.jr.core.service.LendItemService;
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

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "标的的投资")
@Slf4j
@RestController
@RequestMapping("/api/core/lendItem")
public class LendItemController {

    @Autowired
    private LendItemService lendItemService;

    @ApiOperation("会员投资提交数据")
    @PostMapping("/auth/commitInvest")
    public R commitInvest(@RequestBody InvestVo investVo, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String userName = JwtUtils.getUserName(token);
        investVo.setInvestUserId(userId);
        investVo.setInvestName(userName);

        //构建充值自动提交表单
        String formStr = lendItemService.commitInvest(investVo);
        return R.ok().data("formStr", formStr);
    }
}

