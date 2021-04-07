package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.pojo.entity.UserAccount;
import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.mapper.UserInfoMapper;
import com.dlq.jr.core.pojo.entity.UserLoginRecord;
import com.dlq.jr.core.pojo.query.UserInfoQuery;
import com.dlq.jr.core.pojo.vo.LoginVo;
import com.dlq.jr.core.pojo.vo.RegisterVo;
import com.dlq.jr.core.pojo.vo.UserInfoVo;
import com.dlq.jr.core.service.UserAccountService;
import com.dlq.jr.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.jr.core.service.UserLoginRecordService;
import com.dlq.jr.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserLoginRecordService userLoginRecordService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterVo registerVo) {

        //判断用户是否已经被注册过
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile", registerVo.getMobile());
        Integer count = baseMapper.selectCount(wrapper);
        Assert.isTrue(count == 0, ResponseEnum.MOBILE_EXIST_ERROR);

        //插入用户信息记录：user_info
        UserInfo userInfo = new UserInfo();
        userInfo.setUserType(registerVo.getUserType());
        userInfo.setNickName(registerVo.getMobile());
        userInfo.setName(registerVo.getMobile());
        userInfo.setMobile(registerVo.getMobile());
        //保存密码采取BCrypt加密存储
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registerVo.getPassword());
        userInfo.setPassword(encode);
        userInfo.setStatus(UserInfo.STATUS_NORMAL);
        userInfo.setHeadImg(UserInfo.USER_AVATAR);
        baseMapper.insert(userInfo);

        //插入用户账户记录：user_account
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountService.save(userAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserInfoVo login(LoginVo loginVo, String ip) {
        //判断登陆用户是否存在
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper
                .eq("mobile", loginVo.getMobile())
                .eq("user_type", loginVo.getUserType());
        UserInfo userInfo = baseMapper.selectOne(userInfoQueryWrapper);

        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);

        //密码是否正确
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(loginVo.getPassword(), userInfo.getPassword());
        Assert.isTrue(matches, ResponseEnum.LOGIN_PASSWORD_ERROR);

        //用户是否被禁用
        Assert.equals(userInfo.getStatus(), UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);

        //记录用户登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(ip);
        userLoginRecordService.save(userLoginRecord);

        //生成token
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());

        //组装 UserInfoVo
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setToken(token);
        userInfoVo.setName(userInfo.getName());
        userInfoVo.setNickName(userInfo.getNickName());
        userInfoVo.setHeadImg(userInfo.getHeadImg());
        userInfoVo.setMobile(userInfo.getMobile());
        userInfoVo.setUserType(userInfo.getUserType());

        //返回
        return userInfoVo;
    }

    @Override
    public IPage<UserInfo> listPage(IPage<UserInfo> pageParam, UserInfoQuery userInfoQuery) {

        if (userInfoQuery == null) {
            return baseMapper.selectPage(pageParam, null);
        }

        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper
                .eq(StringUtils.isNotBlank(mobile), "mobile", mobile)
                .eq(status != null, "status", status)
                .eq(userType != null, "user_type", userType);
        //有点麻烦
        /*if (StringUtils.isNotBlank(mobile)){
            userInfoVoQueryWrapper.eq("mobile", mobile);
        }
        if (status != null){
            userInfoVoQueryWrapper.eq("status", status);
        }
        if (userType != null){
            userInfoVoQueryWrapper.eq("user_type", userType);
        }*/

        return baseMapper.selectPage(pageParam, userInfoQueryWrapper);
    }
}
