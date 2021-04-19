package com.dlq.jr.core.controller.admin;

import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.LendItem;
import com.dlq.jr.core.service.LendItemService;
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
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-19 19:57
 */
@Api(tags = "标的的投资")
@RestController
@RequestMapping("/admin/core/lendItem")
public class AdminLendItemController {

    @Resource
    private LendItemService lendItemService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(@ApiParam(value = "标的id", required = true)@PathVariable Long lendId) {
        List<LendItem> list = lendItemService.selectByLendId(lendId);
        return R.ok().data("list", list);
    }

}
