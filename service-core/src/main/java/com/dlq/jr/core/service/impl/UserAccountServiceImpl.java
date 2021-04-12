package com.dlq.jr.core.service.impl;

import com.dlq.jr.core.enums.TransTypeEnum;
import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.bo.TransFlowBo;
import com.dlq.jr.core.pojo.entity.UserAccount;
import com.dlq.jr.core.mapper.UserAccountMapper;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.service.TransFlowService;
import com.dlq.jr.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.UserInfoService;
import com.dlq.jr.core.util.LendNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private TransFlowService transFlowService;

    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {

        //获取充值人绑定账号
        UserInfo userInfo = userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();

        HashMap<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentBillNo", LendNoUtils.getChargeNo());
        map.put("bindCode", bindCode);
        map.put("chargeAmt", chargeAmt);
        map.put("feeAmt", new BigDecimal("0"));
        map.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);
        map.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        map.put("timestamp", RequestHelper.getTimestamp());
        map.put("sign", RequestHelper.getSign(map));

        return FormHelper.buildForm(HfbConst.RECHARGE_URL, map);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {
        //幂等性判断 ？标准 判断交易流水是否存在
        String agentBillNo = (String) paramMap.get("agentBillNo"); //商户充值订单号
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if (isSave) {
            log.warn("幂等性返回");
            return "success";
        }

        //处理账户
        String bindCode = (String) paramMap.get("bindCode");
        String chargeAmt = (String) paramMap.get("chargeAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        //记录账户流水
        TransFlowBo transFlowBo = new TransFlowBo(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值");
        transFlowService.saveTransFlow(transFlowBo);
        try {
            TimeUnit.SECONDS.sleep(110);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }
}
