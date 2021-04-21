package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.TransFlow;
import com.dlq.jr.core.service.TransFlowService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.dsig.TransformService;
import java.util.List;

/**
 * <p>
 * 交易流水表 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "资金记录")
@RestController
@RequestMapping("/api/core/transFlow")
public class TransFlowController {

    @Autowired
    private TransFlowService transFlowService;

    @ApiOperation("获取列表")
    @GetMapping("/list")
    public R list(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        List<TransFlow> list = transFlowService.selectByUserId(userId);
        return R.ok().data("list", list);
    }
}

