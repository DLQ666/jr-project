package com.dlq.jr.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.enums.LendStatusEnum;
import com.dlq.jr.core.enums.TransTypeEnum;
import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.mapper.UserAccountMapper;
import com.dlq.jr.core.pojo.bo.TransFlowBo;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.pojo.entity.LendItem;
import com.dlq.jr.core.pojo.entity.LendItemReturn;
import com.dlq.jr.core.pojo.entity.LendReturn;
import com.dlq.jr.core.mapper.LendReturnMapper;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.util.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Slf4j
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
    @Autowired
    private TransFlowService transFlowService;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private LendItemService lendItemService;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {
        log.info("还款成功");

        //还款编号
        String agentBatchNo = (String) paramMap.get("agentBatchNo");

        //幂等性判断
        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if (result) {
            log.warn("幂等性返回");
            return;
        }

        //获取还款数据
        QueryWrapper<LendReturn> lendReturnQueryWrapper = new QueryWrapper<>();
        lendReturnQueryWrapper.eq("return_no", agentBatchNo);
        LendReturn lendReturn = baseMapper.selectOne(lendReturnQueryWrapper);

        //更新还款状态
        lendReturn.setStatus(1);
        String voteFeeAmt = (String) paramMap.get("voteFeeAmt");
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        lendReturn.setRealReturnTime(LocalDateTime.now());
        baseMapper.updateById(lendReturn);

        //更新标的信息
        Lend lend = lendService.getById(lendReturn.getLendId());
        //如果是最后一次还款 那么就更新标的信息
        if (lendReturn.getLast()) {
            //已结清
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendService.updateById(lend);
        }

        //还款账号转出金额
        BigDecimal totalAmt = new BigDecimal((String) paramMap.get("totalAmt"));
        //借款人bindCode
        String bindCode = userBindService.getBindCodeByUserId(lendReturn.getUserId());
        userAccountMapper.updateAccount(bindCode, totalAmt.negate(), new BigDecimal(0));

        //添加还款流水 -- 借款人流水
        TransFlowBo transFlowBO = new TransFlowBo(
                agentBatchNo,
                bindCode,
                totalAmt,
                TransTypeEnum.RETURN_DOWN,
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
                        + "还款金额：" + totalAmt);
        transFlowService.saveTransFlow(transFlowBO);

        //回款明细的获取
        List<LendItemReturn> lendItemReturnList = lendItemReturnService.selectLendItemReturnList(lendReturn.getId());
        lendItemReturnList.forEach(item -> {
            //更新回款状态
            item.setStatus(1);
            item.setRealReturnTime(LocalDateTime.now());
            lendItemReturnService.updateById(item);

            //更新出借信息
            LendItem lendItem = lendItemService.getById(item.getLendItemId());
            lendItem.setRealAmount(lendItem.getRealAmount().add(item.getInterest())); //动态的实际收益、
            lendItemService.updateById(lendItem);

            //获取投资人bindCode
            String investBindCode = userBindService.getBindCodeByUserId(item.getInvestUserId());
            //投资账号转入金额
            userAccountMapper.updateAccount(investBindCode, item.getTotal(), new BigDecimal(0));

            //添加回款流水 -- 投资人流水
            TransFlowBo investTransFlowBO = new TransFlowBo(
                    LendNoUtils.getReturnItemNo(),
                    investBindCode,
                    item.getTotal(),
                    TransTypeEnum.INVEST_BACK,
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle()
                            + "回款金额：" + lend.getLendNo());
            transFlowService.saveTransFlow(investTransFlowBO);
        });
    }
}
