package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.enums.UserBindEnum;
import com.dlq.jr.core.hfb.FormHelper;
import com.dlq.jr.core.hfb.HfbConst;
import com.dlq.jr.core.hfb.RequestHelper;
import com.dlq.jr.core.pojo.entity.UserAccount;
import com.dlq.jr.core.pojo.entity.UserBind;
import com.dlq.jr.core.mapper.UserBindMapper;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.pojo.vo.UserBindVo;
import com.dlq.jr.core.service.UserAccountService;
import com.dlq.jr.core.service.UserBindService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserAccountService userAccountService;

    /**
     * 账户绑定提交数据
     */
    @Override
    public String commitBindUser(UserBindVo userBindVo, Long userId) {

        //不同的user_id，相同的身份证，如果存在，不允许
        QueryWrapper<UserBind> wrapper = new QueryWrapper<>();
        wrapper.eq("id_card", userBindVo.getIdCard())
                .ne("user_id", userId);
        UserBind userBind = baseMapper.selectOne(wrapper);
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

        //检查用户是否曾经绑定过
        wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        userBind = baseMapper.selectOne(wrapper);
        if (userBind == null) {
            //创建用户绑定记录
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVo, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);

            //创建用户账户记录
            UserAccount userAccount = new UserAccount();
            userAccount.setUserId(userId);
            userAccountService.save(userAccount);
        } else {
            //相同的user_id，如果存在，n那么就取出数据，进行更新
            BeanUtils.copyProperties(userBindVo, userBind);
            baseMapper.updateById(userBind);
        }

        //组装自动提交表单的参数
        Map<String, Object> map = new HashMap<>();
        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentUserId", userId);
        map.put("idCard", userBindVo.getIdCard());
        map.put("personalName", userBindVo.getName());
        map.put("bankType", userBindVo.getBankType());
        map.put("bankNo", userBindVo.getBankNo());
        map.put("mobile", userBindVo.getMobile());
        map.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        map.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        map.put("timestamp", RequestHelper.getTimestamp());
        map.put("sign", RequestHelper.getSign(map));

        //生成动态表单字符串
        return FormHelper.buildForm(HfbConst.USERBIND_URL, map);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(Map<String, Object> paramMap) {
        String bindCode = (String) paramMap.get("bindCode");
        String userId = (String) paramMap.get("agentUserId");

        //根据userId查询user_bind记录
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);

        //更新用户绑定表
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);

        //更新用户绑定表
        UserInfo userInfo = userInfoService.getById(userId);
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfoService.updateById(userInfo);
    }
}
