package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.core.enums.BorrowerStatusEnum;
import com.dlq.jr.core.enums.DictEnum;
import com.dlq.jr.core.enums.IntegralEnum;
import com.dlq.jr.core.mapper.UserInfoMapper;
import com.dlq.jr.core.pojo.entity.Borrower;
import com.dlq.jr.core.mapper.BorrowerMapper;
import com.dlq.jr.core.pojo.entity.BorrowerAttach;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.pojo.entity.UserIntegral;
import com.dlq.jr.core.pojo.vo.BorrowerApprovalVo;
import com.dlq.jr.core.pojo.vo.BorrowerAttachVo;
import com.dlq.jr.core.pojo.vo.BorrowerDetailVo;
import com.dlq.jr.core.pojo.vo.BorrowerVo;
import com.dlq.jr.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private BorrowerAttachService borrowerAttachService;
    @Autowired
    private DictService dictService;
    @Autowired
    private UserIntegralService userIntegralService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVoByUserId(BorrowerVo borrowerVo, Long userId) {

        //获取用户基本信息
        UserInfo userInfo = userInfoService.getById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVo, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus()); //认证中
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVo.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachService.save(borrowerAttach);
        });

        // 更新userInfo中的借款人认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoService.updateById(userInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id",userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);
        if (objects.size() == 0){
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer)objects.get(0);
    }

    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return baseMapper.selectPage(pageParam, null);
        }
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.like("name", keyword)
                .or().like("mobile", keyword)
                .or().like("id_card", keyword)
                .orderByDesc("id");
        return baseMapper.selectPage(pageParam, borrowerQueryWrapper);
    }

    @Override
    public BorrowerDetailVo getBorrowerDetailVoById(Long id) {
        //根据id查询借款人信息
        Borrower borrower = baseMapper.selectById(id);
        if (borrower == null){
            return null;
        }
        BorrowerDetailVo borrowerDetailVo = new BorrowerDetailVo();
        //填充基本借款人信息
        BeanUtils.copyProperties(borrower, borrowerDetailVo);

        //性别
        borrowerDetailVo.setSex(borrower.getSex() == 1 ? "男" : "女");
        //婚否
        borrowerDetailVo.setMarry(borrower.getMarry() ? "是" : "否");
        //下拉列表内容
        borrowerDetailVo.setEducation(dictService.getNameByParentDictCodeAndValue(
                DictEnum.EDUCATION.getDictCode(), borrower.getEducation()));
        borrowerDetailVo.setIndustry(dictService.getNameByParentDictCodeAndValue(
                DictEnum.INDUSTRY.getDictCode(), borrower.getIndustry()));
        borrowerDetailVo.setIncome(dictService.getNameByParentDictCodeAndValue(
                DictEnum.INCOME.getDictCode(), borrower.getIncome()));
        borrowerDetailVo.setReturnSource(dictService.getNameByParentDictCodeAndValue(
                DictEnum.RETURN_SOURCE.getDictCode(), borrower.getReturnSource()));
        borrowerDetailVo.setContactsRelation(dictService.getNameByParentDictCodeAndValue(
                DictEnum.RELATION.getDictCode(), borrower.getContactsRelation()));

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVo.setStatus(status);

        //附件列表
        List<BorrowerAttachVo> borrowerAttachVoList = borrowerAttachService.selectBorrowerAttachVoList(id);
        borrowerDetailVo.setBorrowerAttachVoList(borrowerAttachVoList);
        return borrowerDetailVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVo borrowerApprovalVo) {
        //获取借款额度申请人id
        Long borrowerId = borrowerApprovalVo.getBorrowerId();
        //获取额度借款申请人对象
        Borrower borrower = baseMapper.selectById(borrowerId);
        //设置审核状态
        borrower.setStatus(borrowerApprovalVo.getStatus());
        baseMapper.updateById(borrower);

        //获取用户id
        Long userId = borrower.getUserId();
        //获取用户对象
        UserInfo userInfo = userInfoService.getById(userId);
        //获取用户原始积分
        Integer integral = userInfo.getIntegral();

        if (BorrowerStatusEnum.AUTH_OK.getStatus().equals(borrowerApprovalVo.getStatus())) {
            //查询是否已经添加过积分信息---添加过则不添加
            QueryWrapper<UserIntegral> userIntegralQueryWrapper = new QueryWrapper<>();
            userIntegralQueryWrapper.eq("user_id", userId);
            List<UserIntegral> userIntegrals = userIntegralService.list(userIntegralQueryWrapper);

            for (UserIntegral existIntegral : userIntegrals) {
                if (!IntegralEnum.BORROWER_INFO.getMsg().equals(existIntegral.getContent())) {
                    //给用户计算 基本信息积分
                    UserIntegral userIntegral = new UserIntegral();
                    userIntegral.setUserId(userId);
                    userIntegral.setIntegral(borrowerApprovalVo.getInfoIntegral());
                    userIntegral.setContent(IntegralEnum.BORROWER_INFO.getMsg());
                    userIntegralService.save(userIntegral);
                    integral += borrowerApprovalVo.getInfoIntegral();
                }
            }

            //身份证积分
            if (borrowerApprovalVo.getIsIdCardOk()) {
                for (UserIntegral existIntegral : userIntegrals) {
                    if (!IntegralEnum.BORROWER_IDCARD.getMsg().equals(existIntegral.getContent())) {
                        UserIntegral userIdcardIntegral = new UserIntegral();
                        userIdcardIntegral.setUserId(borrower.getUserId());
                        userIdcardIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
                        userIdcardIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
                        userIntegralService.save(userIdcardIntegral);
                        integral += IntegralEnum.BORROWER_IDCARD.getIntegral();
                    }
                }
            }
            //车辆积分
            if (borrowerApprovalVo.getIsCarOk()) {
                for (UserIntegral existIntegral : userIntegrals) {
                    if (!IntegralEnum.BORROWER_CAR.getMsg().equals(existIntegral.getContent())) {
                        UserIntegral userIntegral = new UserIntegral();
                        userIntegral.setUserId(borrower.getUserId());
                        userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
                        userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
                        userIntegralService.save(userIntegral);
                        integral += IntegralEnum.BORROWER_CAR.getIntegral();
                    }
                }
            }
            //房产信息
            if (borrowerApprovalVo.getIsHouseOk()) {
                for (UserIntegral existIntegral : userIntegrals) {
                    if (!IntegralEnum.BORROWER_HOUSE.getMsg().equals(existIntegral.getContent())) {
                        UserIntegral userIntegral = new UserIntegral();
                        userIntegral.setUserId(borrower.getUserId());
                        userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
                        userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
                        userIntegralService.save(userIntegral);
                        integral += IntegralEnum.BORROWER_HOUSE.getIntegral();
                    }
                }
            }
        }

        //设置用户总积分
        userInfo.setIntegral(integral);
        //修改审核状态
        userInfo.setBorrowAuthStatus(borrowerApprovalVo.getStatus());
        //更新userInfo
        userInfoService.updateById(userInfo);
    }
}
