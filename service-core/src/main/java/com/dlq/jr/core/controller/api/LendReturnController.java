package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.LendReturn;
import com.dlq.jr.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "还款计划")
@RestController
@RequestMapping("/api/core/lendReturn")
public class LendReturnController {

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(@ApiParam(value = "标的id", required = true)@PathVariable Long lendId) {
        List<LendReturn> list = lendReturnService.selectByLendId(lendId);
        return R.ok().data("list", list);
    }
}

