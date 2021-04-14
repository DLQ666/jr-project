package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.entity.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.InvestVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface LendItemService extends IService<LendItem> {

    String commitInvest(InvestVo investVo);

    String notify(Map<String, Object> paramMap);

    List<LendItem> selectByLendId(Long lendId, Integer status);
}
