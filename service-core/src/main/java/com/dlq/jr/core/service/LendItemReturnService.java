package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.entity.LendItemReturn;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface LendItemReturnService extends IService<LendItemReturn> {

    List<LendItemReturn> selectByLendId(Long lendId, Long userId);

    List<Map<String, Object>> addReturnDetail(Long lendReturnId);

    /**
     * 根据还款记录的id 查询 对应的回款记录
     */
    List<LendItemReturn> selectLendItemReturnList(Long lendReturnId);
}
