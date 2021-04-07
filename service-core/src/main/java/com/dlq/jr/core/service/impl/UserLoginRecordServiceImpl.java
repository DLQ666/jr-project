package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.core.pojo.entity.UserLoginRecord;
import com.dlq.jr.core.mapper.UserLoginRecordMapper;
import com.dlq.jr.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    @Override
    public List<UserLoginRecord> listTop50(Long userId) {
        QueryWrapper<UserLoginRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("id")
                .last("limit 50");
        return baseMapper.selectList(wrapper);
    }
}
