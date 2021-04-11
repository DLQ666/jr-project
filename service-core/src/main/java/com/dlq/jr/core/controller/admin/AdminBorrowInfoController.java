package com.dlq.jr.core.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.dlq.jr.core.pojo.entity.Borrower;
import com.dlq.jr.core.pojo.query.BorrowInfoQuery;
import com.dlq.jr.core.pojo.vo.BorrowInfoApprovalVo;
import com.dlq.jr.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-10 17:36
 */
@Api(tags = "借款管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/borrowInfo")
public class AdminBorrowInfoController {

    @Autowired
    private BorrowInfoService borrowInfoService;

    @ApiOperation("借款信息列表")
    @GetMapping("/list/{page}/{limit}")
    public R list(@PathVariable Long page, @PathVariable Long limit, BorrowInfoQuery borrowInfoQuery) {
        Page<BorrowInfo> pageParam = new Page<>(page, limit);
        Page<BorrowInfo> pageModel = borrowInfoService.selectList(pageParam, borrowInfoQuery);
        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation("借款信息详情")
    @GetMapping("/show/{id}")
    public R show(@PathVariable("id") Long id) {
        Map<String, Object> borrowInfoDetail = borrowInfoService.getBorrowInfoDetail(id);
        return R.ok().data("borrowInfoDetail", borrowInfoDetail);
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowInfoApprovalVo borrowInfoApprovalVo) {
        borrowInfoService.approval(borrowInfoApprovalVo);
        return R.ok().message("审批完成");
    }
}
