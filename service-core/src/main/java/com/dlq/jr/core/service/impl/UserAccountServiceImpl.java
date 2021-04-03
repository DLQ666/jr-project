package com.dlq.jr.core.service.impl;

import com.dlq.jr.core.pojo.entity.UserAccount;
import com.dlq.jr.core.mapper.UserAccountMapper;
import com.dlq.jr.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

}
