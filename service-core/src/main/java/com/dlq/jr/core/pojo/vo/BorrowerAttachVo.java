package com.dlq.jr.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-08 23:48
 */
@Data
@ApiModel(value="借款人附件资料")
public class BorrowerAttachVo {

    @ApiModelProperty(value = "图片类型（idCard1：身份证正面，idCard2：身份证反面，house：房产证，car：车）")
    private String imageType;

    @ApiModelProperty(value = "图片路径")
    private String imageUrl;
}
