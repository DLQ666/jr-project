package com.dlq.jr.sms.service;

import java.util.Map;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 18:28
 */
public interface SmsService {

    void send(String mobile, String templateCode, Map<String, Object> param);
}
