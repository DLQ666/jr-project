package com.dlq.jr.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-13 10:48
 */
@Data
@ApiModel(description = "投标信息")
public class InvestVo {

    //标的id
    private Long lendId;

    //投标金额
    private String investAmount;

    //用户id
    private Long investUserId;

    //用户姓名
    private String investName;
}
