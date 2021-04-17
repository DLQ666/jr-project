package com.dlq.jr.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dlq.jr.common.exception.BusinessException;
import com.dlq.jr.core.enums.LendItemStatusEnum;
import com.dlq.jr.core.enums.LendStatusEnum;
import com.dlq.jr.core.enums.ReturnMethodEnum;
import com.dlq.jr.core.enums.TransTypeEnum;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.mapper.UserAccountMapper;
import com.dlq.jr.core.pojo.bo.TransFlowBo;
import com.dlq.jr.core.pojo.entity.*;
import com.dlq.jr.core.mapper.LendMapper;
import com.dlq.jr.core.pojo.vo.BorrowInfoApprovalVo;
import com.dlq.jr.core.pojo.vo.BorrowerDetailVo;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Slf4j
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Autowired
    private DictService dictService;
    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private TransFlowService transFlowService;
    @Autowired
    private LendItemService lendItemService;
    @Autowired
    private LendItemReturnService lendItemReturnService;
    @Autowired
    private LendReturnService lendReturnService;

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

    @Override
    public Map<String, Object> getLendDetail(Long id) {

        //查询lend
        Lend lend = baseMapper.selectById(id);
        packgeLend(lend);

        //查询借款人对象 Borrower（BorrowerDetailVo）
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerService.getOne(borrowerQueryWrapper);
        BorrowerDetailVo borrowerDetailVo = borrowerService.getBorrowerDetailVoById(borrower.getId());

        //组装集合结果
        HashMap<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVo);
        return result;
    }

    @Override
    public List<Lend> selectList() {
        List<Lend> lendList = baseMapper.selectList(null);
        lendList.forEach(this::packgeLend);
        return lendList;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {
        BigDecimal interestCount;
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod().intValue()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod().intValue()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod().intValue()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }
        return interestCount;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long id) {
        //根据标的id获取标的信息
        Lend lend = baseMapper.selectById(id);

        //封装提交至汇付宝的参数============
        HashMap<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentProjectCode", lend.getLendNo());
        map.put("agentBillNo", LendNoUtils.getLoanNo());
        //计算 月年化
        BigDecimal mouthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //已投金额 * 月年化 * 投资时长
        BigDecimal realAmount = lend.getInvestAmount().multiply(mouthRate).multiply(new BigDecimal(lend.getPeriod()));
        map.put("mchFee", realAmount);
        map.put("timestamp", RequestHelper.getTimestamp());
        map.put("sign", RequestHelper.getSign(map));
        log.info("放款参数：" + JSONObject.toJSONString(map));

        //调用汇付宝放款接口---向汇付宝提交远程放款请求----并接收响应============
        JSONObject request = RequestHelper.sendRequest(map, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + request.toJSONString());

        //放款失败的处理
        if (!"0000".equals(request.getString("resultCode"))) {
            throw new BusinessException(request.getString("resultMsg"));
        }

        //放款成功
        //1、标的状态和标的平台收益：更新标的相关信息=================
        lend.setRealAmount(realAmount); //平台收益
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());  //标的状态
        lend.setPaymentTime(LocalDateTime.now()); //放款时间
        baseMapper.updateById(lend);

        //2、给借款账号转入金额======================
        //获取借款人bindCode
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();
        //获取放款金额
        BigDecimal voteAmt = new BigDecimal(request.getString("voteAmt"));
        //转账
        userAccountMapper.updateAccount(bindCode, voteAmt, new BigDecimal(0));

        //3、增加借款交易流水
        String agentBillNo = request.getString("agentBillNo");
        TransFlowBo transFlowBo = new TransFlowBo(
                agentBillNo,
                bindCode,
                voteAmt,
                TransTypeEnum.BORROW_BACK,
                "项目放款，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle() + "，放款金额：" + voteAmt + " 元"
        );
        //保存借款交易流水
        transFlowService.saveTransFlow(transFlowBo);

        //4、解冻并扣除投资人资金
        //获取这个标的 下的所有投资列表
        List<LendItem> lendItemList = lendItemService.selectByLendId(id, LendItemStatusEnum.PAYMENT.getStatus());
        for (LendItem lendItem : lendItemList) {
            //获取每个标的记录下---> 的每个投资记录中 的投资人userId
            Long investUserId = lendItem.getInvestUserId();
            //根据每个 投资人userId 查询 每个投资人信息
            UserInfo investUserInfo = userInfoService.getById(investUserId);
            //获取每个投资人的 绑定协议号 bindCode
            String investBindCode = investUserInfo.getBindCode();
            //解冻并扣除投资人资金
            BigDecimal investAmount = lendItem.getInvestAmount();
            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());
            //5、增加每个投资人 的 交易流水
            TransFlowBo investTransFlowBo = new TransFlowBo(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "项目放款，冻结资金转出，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle() + "，解冻转出金额：" + investAmount + " 元"
            );
            //保存每个投资人解冻转出交易流水
            transFlowService.saveTransFlow(investTransFlowBo);
        }

        //6、放款成功----生成借款人还款计划和出借人回款计划
        this.repaymentPlan(lend);
    }

    /**
     * 还款计划
     * @param lend
     */
    private void repaymentPlan(Lend lend) {

        // 创建还款接话列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();
        for (int i = 0; i < len; i++) {
            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            //填充基本属性
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());

            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);
            //判断是否是最后一期还款
            if (i == len){ //最后一期
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            // 设置还款状态
            lendReturn.setStatus(0);

            //将还款对象加入还款计划列表
            lendReturnList.add(lendReturn);
        }

        //批量保存还款计划
        lendReturnService.saveBatch(lendReturnList);
        //=====================================================================

        //生成期数和还款记录的id对应的键值对的集合
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        //创建所有投资的所有回款记录列表
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        //获取当前标的下的所有的已支付的投资
        List<LendItem> lendItems = lendItemService.selectByLendId(lend.getId(), 1);
        //遍历投资列表
        for (LendItem lendItem : lendItems) {
            //根据投资记录的id 调用回款计划生成的方法，得到当前这笔投资的回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem, lendReturnMap, lend);
            //将当前这笔投资的回款计划列表   放入    所有投资的所有回款记录列表
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        //遍历还款记录列表
        for (LendReturn lendReturn : lendReturnList) {
            //通过filter，map，reduce将相关的回款数据过滤出来
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().intValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().intValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().intValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //将当前的所有投资人的数据相加，就是当前期数的所有投资人的回款数据（本金、利息、总金额）
            //设置本金利息总金额
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }

        //批量更新还款计划列表
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划 （针对某一笔投资的回款计划）
     * @param lendItem
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(LendItem lendItem, Map<Integer, Long> lendReturnMap, Lend lend) {

        //投标信息 lendItem
        //调用工具类计算还款本金和利息，存储为集合
        //{key ：value} {期数 ：本金|利息}
        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();//当前期数
            //根据当前期数，获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            //创建回款计划记录
            LendItemReturn lendItemReturn = new LendItemReturn();
            //将还款记录关联到回款记录
            lendItemReturn.setLendReturnId(lendReturnId);
            //设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItem.getId());
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            // 设置回款本金、利息和总额（注意左后一个月的计算）
            if (currentPeriod.intValue() == lend.getPeriod()){ //最后一期
                //除掉最后一期 的 所有金额总和  本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一期的回款金额
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一期的回款金额
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);
            }else { //非最后一期
                //本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                //利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            //回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));

            //设置回款状态和是否逾期等其他属性
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            //是否逾期，默认未逾期
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            //将回款记录放入回款列表
            lendItemReturnList.add(lendItemReturn);
        }

        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }

    private void packgeLend(Lend lend) {
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", LendStatusEnum.getMsgByStatus(lend.getStatus()));
    }

}
