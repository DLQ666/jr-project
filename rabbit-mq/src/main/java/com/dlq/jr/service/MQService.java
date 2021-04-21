package com.dlq.jr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-21 14:15
 */
@Slf4j
@Service
public class MQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendMessage(String exchange, String routingKey, Object message){
        log.info("发送消息");
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return true;
    }

}
