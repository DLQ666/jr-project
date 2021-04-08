package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.UserBindVo;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface UserBindService extends IService<UserBind> {

    String commitBindUser(UserBindVo userBindVo, Long userId);

    void notify(Map<String, Object> paramMap);
}
