package com.dlq.jr.core.pojo.bo;

import com.dlq.jr.core.enums.TransTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-11 21:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransFlowBo {

    private String agentBillNo;
    private String bindCode;
    private BigDecimal amount;
    private TransTypeEnum transTypeEnum;
    private String memo;
}
