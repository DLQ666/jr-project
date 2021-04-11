package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dlq.jr.core.enums.LendStatusEnum;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.mapper.LendMapper;
import com.dlq.jr.core.pojo.vo.BorrowInfoApprovalVo;
import com.dlq.jr.core.service.DictService;
import com.dlq.jr.core.service.LendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.util.LendNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Autowired
    private DictService dictService;

    @Override
    public void createlend(BorrowInfoApprovalVo borrowInfoApprovalVo, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setTitle(borrowInfoApprovalVo.getTitle());
        lend.setAmount(borrowInfo.getAmount()); //标的金额
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setLendYearRate(borrowInfoApprovalVo.getLendYearRate().divide(new BigDecimal(100)));
        lend.setServiceRate(borrowInfoApprovalVo.getServiceRate().divide(new BigDecimal(100)));
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100)); //最低投资金额
        lend.setInvestAmount(new BigDecimal(0)); //已投金额
        lend.setInvestNum(0); //已投人数
        lend.setPublishDate(LocalDateTime.now());

        //起息日期
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVo.getLendStartDate(), dateTimeFormatter);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVo.getLendInfo()); //标的描述

        //平台预期收益率  =  标的金额 * （年化 / 12  *  期数）
        BigDecimal mouthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        BigDecimal expectAmount = lend.getAmount().multiply(mouthRate.multiply(new BigDecimal(lend.getPeriod())));
        lend.setExpectAmount(expectAmount);

        //实际收益
        lend.setRealAmount(new BigDecimal(0)); //实际收益
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus()); //标的状态
        lend.setCheckTime(LocalDateTime.now()); //审核时间
        lend.setCheckAdminId(1L); //审核用户id
        //todo  放款时间 放款人id

        //存入数据库
        baseMapper.insert(lend);
    }

    @Override
    public IPage<Lend> selectList(IPage<Lend> pageParam) {
        IPage<Lend> lendIPage = baseMapper.selectPage(pageParam, null);
        lendIPage.getRecords().forEach(this::packgeLend);
        return lendIPage;
    }

    private void packgeLend(Lend lend) {
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", LendStatusEnum.getMsgByStatus(lend.getStatus()));
    }

}
