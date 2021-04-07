package com.dlq.jr.sms.feign;

import com.dlq.jr.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-04-07 18:41
 */
@FeignClient("service-core")
public interface CoreFeignService {

    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable("mobile") String mobile);
}
