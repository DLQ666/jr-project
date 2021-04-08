package com.dlq.jr.core.controller.api;


import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.vo.BorrowerVo;
import com.dlq.jr.core.service.BorrowerService;
import com.dlq.jr.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "借款人")
@Slf4j
@RestController
@RequestMapping("/api/core/borrower")
public class BorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @ApiOperation("保存借款人信息")
    @PostMapping("/auth/save")
    public R save(@RequestBody BorrowerVo borrowerVo, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        borrowerService.saveBorrowerVoByUserId(borrowerVo, userId);
        return R.ok().message("信息提交成功");
    }
}

