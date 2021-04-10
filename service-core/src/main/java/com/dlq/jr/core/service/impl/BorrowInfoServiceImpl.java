package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.dlq.jr.core.mapper.BorrowInfoMapper;
import com.dlq.jr.core.pojo.entity.IntegralGrade;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.IntegralGradeService;
import com.dlq.jr.core.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        //获取用户积分
        Integer integral = userInfo.getIntegral();

        //根据积分查询额度
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper.le("integral_start",integral).ge("integral_end", integral);
        IntegralGrade integralGrade = integralGradeService.getOne(integralGradeQueryWrapper);
        if (integralGrade == null){
            return new BigDecimal("0");
        }
        return integralGrade.getBorrowAmount();
    }
}
