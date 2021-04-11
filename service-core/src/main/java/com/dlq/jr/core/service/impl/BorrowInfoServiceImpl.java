package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.exception.BusinessException;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.enums.*;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.dlq.jr.core.mapper.BorrowInfoMapper;
import com.dlq.jr.core.pojo.entity.Borrower;
import com.dlq.jr.core.pojo.entity.IntegralGrade;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.pojo.query.BorrowInfoQuery;
import com.dlq.jr.core.pojo.vo.BorrowerDetailVo;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private IntegralGradeService integralGradeService;
    @Autowired
    private DictService dictService;
    @Autowired
    private BorrowerService borrowerService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        //获取用户积分
        Integer integral = userInfo.getIntegral();

        //根据积分查询额度
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper.le("integral_start", integral).ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeService.getOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }
        return integralGrade.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        //判断用户是否锁定
        Assert.isTrue(UserStatusEnum.NORMAL.getStatus().equals(userInfo.getStatus()), ResponseEnum.LOGIN_LOKED_ERROR);
        //判断用户是否绑定状态
        Assert.isTrue(
                UserBindEnum.BIND_OK.getStatus().intValue() == userInfo.getBindStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);

        //判断借款人额度申请状态
        Assert.isTrue(
                BorrowerStatusEnum.AUTH_OK.getStatus().intValue() == userInfo.getBorrowAuthStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        //判断借款人额度是否充足
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        //存储borrowInfo数据
        borrowInfo.setUserId(userId);
        //百分比转小数
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        //设置借款申请状态
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);
        if (objects.size() == 0) {
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer) objects.get(0);
    }

    @Override
    public Page<BorrowInfo> selectList(Page<BorrowInfo> pageParam, BorrowInfoQuery borrowInfoQuery) {
        if (borrowInfoQuery.getBorrowYearRate() != null) {
            BigDecimal borrowYearRate = borrowInfoQuery.getBorrowYearRate().divide(new BigDecimal(100));
            borrowInfoQuery.setBorrowYearRate(borrowYearRate);
        }
        Page<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList(pageParam, borrowInfoQuery);
        borrowInfoList.getRecords().forEach(this::packgeBorrowInfo);
        return borrowInfoList;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        //查询借款对象  BorrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        if (borrowInfo == null) {
            return null;
        }
        packgeBorrowInfo(borrowInfo);
        //查询借款人对象 Borrower（BorrowerDetailVo）
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerService.getOne(borrowerQueryWrapper);
        BorrowerDetailVo borrowerDetailVo = borrowerService.getBorrowerDetailVoById(borrower.getId());
        //组装集合结果
        HashMap<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVo);
        return result;
    }

    private BorrowInfo packgeBorrowInfo(BorrowInfo borrowInfo) {
        String returnMethod = dictService.getNameByParentDictCodeAndValue(
                "returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue(
                "moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());

        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);
        return borrowInfo;
    }
}
