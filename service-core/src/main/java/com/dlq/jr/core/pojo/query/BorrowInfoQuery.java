package com.dlq.jr.core.pojo.query;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-10 18:25
 */
@Data
@ApiModel(description="借款搜索对象")
public class BorrowInfoQuery {
    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机")
    private String mobile;

    @ApiModelProperty(value = "借款期限")
    private Integer period;

    @ApiModelProperty(value = "状态（0：未提交，1：审核中， 2：审核通过， -1：审核不通过）")
    private Integer status;

    @ApiModelProperty(value = "还款方式 1-等额本息 2-等额本金 3-每月还息一次还本 4-一次还本")
    private Integer returnMethod;

    @ApiModelProperty(value = "年化利率")
    private BigDecimal borrowYearRate;

    @ApiModelProperty(value = "借款金额最低区间")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "借款金额最高区间")
    private BigDecimal maxAmount;


}
