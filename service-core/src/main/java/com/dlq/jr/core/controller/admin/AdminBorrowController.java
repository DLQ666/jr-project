package com.dlq.jr.core.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.Borrower;
import com.dlq.jr.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-08 21:10
 */
@Api(tags = "借款人管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/borrower")
public class AdminBorrowController {

    @Autowired
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(@PathVariable Long page, @PathVariable Long limit, @RequestParam String keyword) {
        //这里的@RequestParam其实是可以省略的，但是在目前的swagger版本中（2.9.2）不能省略，
        //否则默认将没有注解的参数解析为body中的传递的数据
        Page<Borrower> pageParam = new Page<>(page, limit);
        IPage<Borrower> pageModel = borrowerService.listPage(pageParam, keyword);
        return R.ok().data("pageModel", pageModel);
    }
}
