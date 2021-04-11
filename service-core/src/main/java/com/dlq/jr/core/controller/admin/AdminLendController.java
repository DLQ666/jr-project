package com.dlq.jr.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Api(tags = "标的管理")
@RestController
@RequestMapping("/admin/core/lend")
public class AdminLendController {

    @Autowired
    private LendService lendService;

    @ApiOperation("标的列表")
    @GetMapping("/list/{page}/{limit}")
    public R list(@PathVariable Long page, @PathVariable Long limit) {
        IPage<Lend> pageParam = new Page<>(page, limit);
        IPage<Lend> pageModel = lendService.selectList(pageParam);
        return R.ok().data("list", pageModel);
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public R show(@PathVariable Long id) {
        Map<String, Object> result = lendService.getLendDetail(id);
        return R.ok().data("lendDetail", result);
    }
}

