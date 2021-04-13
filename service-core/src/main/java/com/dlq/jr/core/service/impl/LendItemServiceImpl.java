package com.dlq.jr.core.service.impl;

import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.enums.LendItemStatusEnum;
import com.dlq.jr.core.enums.LendStatusEnum;
import com.dlq.jr.core.enums.UserStatusEnum;
import com.dlq.jr.core.enums.UserTypeEnum;
import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.entity.Lend;
import com.dlq.jr.core.pojo.entity.LendItem;
import com.dlq.jr.core.mapper.LendItemMapper;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.pojo.vo.InvestVo;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.util.LendNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Autowired
    private LendService lendService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserBindService userBindService;

    @Override
    public String commitInvest(InvestVo investVo) {

        //获取investVo属性
        //获取投资人 userId
        Long investUserId = investVo.getInvestUserId();
        //获取投资人名字
        String investName = investVo.getInvestName();
        //获取当前标的id
        Long lendId = investVo.getLendId();
        //获取当前投资金额
        BigDecimal investAmount = new BigDecimal(investVo.getInvestAmount());

        //健壮性的校验=================================================
        Lend lend = lendService.getById(lendId);
        UserInfo userInfo = userInfoService.getById(investUserId);

        //判断用户是否是投资人  -----  只有是投资人(出借人) 才能进行投标操作
        Integer userType = userInfo.getUserType();
        Assert.isTrue(userType.intValue() == UserTypeEnum.LENDERS.getStatus().intValue(),
                ResponseEnum.USER_TYPE_ERROR);

        //判断用户状态 ---- 只有未锁定 才能进行投标操作
        Assert.isTrue(userInfo.getStatus().intValue() == UserStatusEnum.NORMAL.getStatus().intValue(),
                ResponseEnum.LOGIN_LOKED_ERROR);

        //判断标的状态 ---- 只有在<募资中>的标的才能投标   ---否则 抛异常
        Assert.isTrue(lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus().intValue(),
                ResponseEnum.LEND_INVEST_ERROR);

        //判断是否超卖   ----  已投金额 + 当前投资金额 <= 标的金额（正常）
        BigDecimal sum = lend.getInvestAmount().add(investAmount);
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),
                ResponseEnum.LEND_FULL_SCALE_ERROR);

        //获取投资人账户余额
        BigDecimal account = userAccountService.getAccount(investUserId);
        //判断用户余额   当前用户的余额 >= 当前投资金额
        Assert.isTrue(account.doubleValue() >= investAmount.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        //获取paramMap中参数=================================================
        //生成 标的 下的投资记录
        LendItem lendItem = new LendItem();
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(lendId); //对应的标的id
        lendItem.setInvestUserId(investUserId); //投资用户id
        lendItem.setInvestName(investName); //投资人名称
        lendItem.setInvestAmount(investAmount);  //此笔投资金额
        lendItem.setLendYearRate(lend.getLendYearRate()); //年化
        lendItem.setInvestTime(LocalDateTime.now()); //投资时间
        lendItem.setLendStartDate(lend.getLendStartDate()); //开始时间
        lendItem.setLendEndDate(lend.getLendEndDate()); //结束时间
        //预期收益
        BigDecimal expectAmount = lendService.getInterestCount(lendItem.getInvestAmount(),
                lendItem.getLendYearRate(),
                lend.getPeriod(),
                lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmount);
        //实际收益
        lendItem.setRealAmount(new BigDecimal(0));
        //投资记录的状态
        lendItem.setStatus(LendItemStatusEnum.DEFAULT.getStatus()); //默认状态：刚刚创建
        baseMapper.insert(lendItem); //存入数据库

        //获取投资人的 bindCode 协议号
        String bindCode = userBindService.getBindCodeByUserId(investUserId);
        //获取借款人的 bindCode 协议号
        String benefitBindCode = userBindService.getBindCodeByUserId(lend.getUserId());

        //组装投资相关的参数，提交到汇付宝资金托管平台==========================================
        //在托管平台同步用户的投资信息，修改用户的账户资金信息==========================================
        //封装提交至汇付宝的参数=================================================
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", bindCode); //投资人的bindCode
        paramMap.put("benefitBindCode",benefitBindCode); //借款人的bindCode
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVo.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建充值自动提交表单
        return FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
    }
}
