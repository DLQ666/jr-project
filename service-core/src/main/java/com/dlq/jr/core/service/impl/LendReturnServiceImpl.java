package com.dlq.jr.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.pojo.entity.LendReturn;
import com.dlq.jr.core.mapper.LendReturnMapper;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Autowired
    private LendService lendService;
    @Autowired
    private UserBindService userBindService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private LendItemReturnService lendItemReturnService;

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        //还款记录
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        //获取用户余额
        BigDecimal account = userAccountService.getAccount(userId);
        Assert.isTrue(account.doubleValue() >= lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_ERROR);

        //标的记录
        Lend lend = lendService.getById(lendReturn.getLendId());
        // 获取还款人的协议绑定号
        String bindCode = userBindService.getBindCodeByUserId(userId);

        //组装表单发送给---跳转汇付宝提交
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        //商户商品名称
        paramMap.put("agentGoodsName", lend.getTitle());
        //批次号
        paramMap.put("agentBatchNo", lendReturn.getReturnNo());
        //还款人绑定协议号
        paramMap.put("fromBindCode", bindCode);
        //还款总额
        paramMap.put("totalAmt", lendReturn.getTotal());
        paramMap.put("note", "");
        //还款明细
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);
        paramMap.put("data", JSONObject.toJSONString(lendItemReturnDetailList));

        paramMap.put("voteFeeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);
        return formStr;
    }
}
