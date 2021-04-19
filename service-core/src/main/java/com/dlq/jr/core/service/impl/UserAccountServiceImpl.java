package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
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
import com.dlq.jr.core.service.UserBindService;
import com.dlq.jr.core.service.UserInfoService;
import com.dlq.jr.core.util.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private TransFlowService transFlowService;
    @Autowired
    private UserBindService userBindService;
    @Autowired
    private UserAccountService userAccountService;

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
//        try {
//            TimeUnit.SECONDS.sleep(110);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);
        return userAccount.getAmount();
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {

        //健壮性校验----判断当前用户提现金额不能大于 用户的余额
        BigDecimal account = userAccountService.getAccount(userId);
        Assert.isTrue(account.doubleValue() >= fetchAmt.doubleValue(), ResponseEnum.NOT_SUFFICIENT_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        //接口幂等性
        log.info("提现成功");
        String agentBillNo = (String) paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) {
            log.warn("幂等性返回");
            return;
        }

        //账户同步
        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));
        //baseMapper.updateAccount(bindCode, (new BigDecimal(fetchAmt)).negate(), new BigDecimal(0));

        //交易流水
        //记录账户流水
        TransFlowBo transFlowBo = new TransFlowBo(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "提现");
        transFlowService.saveTransFlow(transFlowBo);
    }
}
