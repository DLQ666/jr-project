package com.dlq.jr.sms.feign.fallback;

import com.dlq.jr.sms.feign.CoreFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-07 20:57
 */
@Slf4j
@Service
public class CoreFeignServiceFallback implements CoreFeignService {

    @Override
    public boolean checkMobile(String mobile) {
        log.error("远程调用失败，checkMobile熔断方法执行！");
        return false; //手机号不重复
    }
}
