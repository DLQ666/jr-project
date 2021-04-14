package com.dlq.jr.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.dlq.jr.core.pojo.entity.Lend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.BorrowInfoApprovalVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface LendService extends IService<Lend> {

    void createlend(BorrowInfoApprovalVo borrowInfoApprovalVo, BorrowInfo borrowInfo);

    IPage<Lend> selectList(IPage<Lend> pageParam);

    Map<String, Object> getLendDetail(Long id);

    List<Lend> selectList();

    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod);

    void makeLoan(Long id);
}
