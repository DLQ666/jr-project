package com.dlq.jr.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
//@ToString
public enum UserStatusEnum {

    LOCK(0, "锁定"),
    NORMAL(1, "正常"),
    ;

    private Integer status;
    private String msg;
}
