package com.dlq.jr.core.service.impl;

import com.dlq.jr.core.pojo.entity.UserInfo;
import com.dlq.jr.core.mapper.UserInfoMapper;
import com.dlq.jr.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
