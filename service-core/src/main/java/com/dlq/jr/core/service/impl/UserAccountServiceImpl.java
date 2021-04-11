package com.dlq.jr.core.service.impl;

import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.entity.UserAccount;
import com.dlq.jr.core.mapper.UserAccountMapper;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.UserInfoService;
import com.dlq.jr.core.util.LendNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

}
