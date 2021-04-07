package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.LoginVo;
import com.dlq.jr.core.pojo.vo.RegisterVo;
import com.dlq.jr.core.pojo.vo.UserInfoVo;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVo registerVo);

    UserInfoVo login(LoginVo loginVo, String ip);
}
