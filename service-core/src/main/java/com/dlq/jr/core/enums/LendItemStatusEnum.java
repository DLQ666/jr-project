package com.dlq.jr.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-13 14:13
 */
@AllArgsConstructor
@Getter
public enum LendItemStatusEnum {
    //0：默认 1：已支付 2：已还款
    DEFAULT(0, "默认"),
    PAYMENT(1, "已支付"),
    REPAYMENT(2, "已还款"),
    ;

    private Integer status;
    private String msg;
}
