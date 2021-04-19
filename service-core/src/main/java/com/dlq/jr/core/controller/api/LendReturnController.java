package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.LendReturn;
import com.dlq.jr.core.service.LendReturnService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    @ApiOperation("用户还款")
    @PostMapping("/auth/commitReturn/{lendReturnId}")
    public R commitReturn(@ApiParam(value = "还款计划id", required = true)
                          @PathVariable Long lendReturnId, HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = lendReturnService.commitReturn(lendReturnId, userId);
        return R.ok().data("formStr", formStr);
    }
}

