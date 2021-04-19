package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.pojo.entity.LendItem;
import com.dlq.jr.core.pojo.entity.LendItemReturn;
import com.dlq.jr.core.mapper.LendItemReturnMapper;
import com.dlq.jr.core.pojo.entity.LendReturn;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn> implements LendItemReturnService {

    @Autowired
    private LendService lendService;
    @Autowired
    private LendReturnService lendReturnService;
    @Autowired
    private LendItemService lendItemService;
    @Autowired
    private UserBindService userBindService;

    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {
        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id", lendId)
                    .eq("invest_user_id", userId)
                    .orderByAsc("current_period");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 通过还款记录的id 找到对应的回款计划数据，组装data参数中需要的List<Map>
     * @param lendReturnId
     * @return
     */
    @Override
    public List<Map<String, Object>> addReturnDetail(Long lendReturnId) {
        //还款记录
        LendReturn lendReturn = lendReturnService.getById(lendReturnId);
        //获取标的
        Lend lend = lendService.getById(lendReturn.getLendId());

        //根据还款id 查询到对应 所有的回款列表lend_item_return
        List<LendItemReturn> lendItemReturnList = this.selectLendItemReturnList(lendReturnId);
        List<Map<String, Object>> lendItemReturnDetailList = new ArrayList<>();
        for (LendItemReturn lendItemReturn : lendItemReturnList) {
            //获取投资记录id
            Long lendItemId = lendItemReturn.getLendItemId();
            LendItem lendItem = lendItemService.getById(lendItemId);

            //获取投资人id
            Long investUserId = lendItem.getInvestUserId();
            String bindCode = userBindService.getBindCodeByUserId(investUserId);

            //组装参数
            Map<String, Object> map = new HashMap<>();
            map.put("agentProjectCode", lend.getLendNo()); //项目编号
            map.put("voteBillNo", lendItem.getLendItemNo());//投资编号
            map.put("toBindCode", bindCode); //收款人（投资人）bindCode
            map.put("transit_amt", lendItemReturn.getTotal()); //还款金额
            map.put("base_amt", lendItemReturn.getPrincipal()); //还款本金
            map.put("benifit_amt", lendItemReturn.getInterest()); // 还款利息
            map.put("fee_amt", new BigDecimal(0)); //商户手续费

            lendItemReturnDetailList.add(map);
        }
        System.out.println(lendItemReturnDetailList);
        return lendItemReturnDetailList;
    }

    @Override
    public List<LendItemReturn> selectLendItemReturnList(Long lendReturnId) {
        QueryWrapper<LendItemReturn> lendItemReturnQueryWrapper = new QueryWrapper<>();
        lendItemReturnQueryWrapper.eq("lend_return_id", lendReturnId);
        return baseMapper.selectList(lendItemReturnQueryWrapper);
    }
}
