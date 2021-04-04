package com.dlq.jr.core.controller.admin;

import com.dlq.jr.common.exception.BusinessException;
import com.dlq.jr.common.result.R;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.pojo.entity.IntegralGrade;
import com.dlq.jr.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-03 19:43
 */
@Api(tags = "积分等级管理")
@RestController
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {

    @Autowired
    private IntegralGradeService integralGradeService;

    @ApiOperation("积分等级列表")
    @GetMapping("/list")
    public R listAll(){
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation("根据id删除记录")
    @DeleteMapping("/remove/{id}")
    public R removeById(@PathVariable("id")Long id){
        boolean b = integralGradeService.removeById(id);
        if (b){
            return R.ok().message("删除成功");
        }else {
            return R.error().message("删除失败");
        }
    }

    @ApiOperation("新增积分等级")
    @PostMapping("/save")
    public R save(@RequestBody IntegralGrade integralGrade){
        if (integralGrade.getBorrowAmount() == null){
            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        }
        boolean save = integralGradeService.save(integralGrade);
        if (save){
            return R.ok().message("保存成功");
        }else {
            return R.error().message("保存失败");
        }
    }

    @ApiOperation("根据id获取积分等级")
    @GetMapping("/get/{id}")
    public R getById(@PathVariable("id")Long id){
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if (integralGrade != null){
            return R.ok().data("record",integralGrade);
        }else {
            return R.error().message("数据获取失败");
        }
    }

    @ApiOperation("新增积分等级")
    @PutMapping("/update")
    public R updateById(@RequestBody IntegralGrade integralGrade){
        boolean update = integralGradeService.updateById(integralGrade);
        if (update){
            return R.ok().message("更新成功");
        }else {
            return R.error().message("更新失败");
        }
    }
}
