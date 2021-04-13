package com.dlq.jr.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-13 11:16
 */
@AllArgsConstructor
@Getter
public enum UserTypeEnum {

    LENDERS(1, "出借人"),
    BORROWER(2, "借款人"),
    ;

    private Integer status;
    private String msg;
}
