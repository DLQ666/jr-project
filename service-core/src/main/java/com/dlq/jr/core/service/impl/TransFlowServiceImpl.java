package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.core.pojo.bo.TransFlowBo;
import com.dlq.jr.core.pojo.entity.TransFlow;
import com.dlq.jr.core.mapper.TransFlowMapper;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.service.TransFlowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public void saveTransFlow(TransFlowBo transFlowBo) {
        String bindCode = transFlowBo.getBindCode();
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", bindCode);
        UserInfo userInfo = userInfoService.getOne(userInfoQueryWrapper);
        TransFlow transFlow = new TransFlow();
        transFlow.setTransAmount(transFlowBo.getAmount());
        transFlow.setMemo(transFlowBo.getMemo());
        transFlow.setTransTypeName(transFlowBo.getTransTypeEnum().getTransTypeName());
        transFlow.setTransType(transFlowBo.getTransTypeEnum().getTransType());
        transFlow.setTransNo(transFlowBo.getAgentBillNo()); //流水号
        transFlow.setUserId(userInfo.getId());
        transFlow.setUserName(userInfo.getName());
        baseMapper.insert(transFlow);
    }

    @Override
    public boolean isSaveTransFlow(String agentBillNo) {
        QueryWrapper<TransFlow> transFlowQueryWrapper = new QueryWrapper<>();
        transFlowQueryWrapper.eq("trans_no", agentBillNo);
        Integer count = baseMapper.selectCount(transFlowQueryWrapper);
        return count > 0;
    }
}
