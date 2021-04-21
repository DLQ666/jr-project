package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.bo.TransFlowBo;
import com.dlq.jr.core.pojo.entity.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface TransFlowService extends IService<TransFlow> {

    void saveTransFlow(TransFlowBo TransFlowBo);

    boolean isSaveTransFlow(String agentBillNo);

    List<TransFlow> selectByUserId(Long userId);
}
