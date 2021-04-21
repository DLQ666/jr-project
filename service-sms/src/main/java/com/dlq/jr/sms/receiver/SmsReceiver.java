package com.dlq.jr.sms.receiver;

import com.dlq.jr.dto.SmsDTO;
import com.dlq.jr.rabbitmq.MQConst;
import com.dlq.jr.sms.service.SmsService;
import com.dlq.jr.sms.util.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-21 14:53
 */
@Slf4j
@Component
public class SmsReceiver {

    @Autowired
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SMS_ITEM,durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
            key = {MQConst.ROUTING_SMS_ITEM}
    ))
    public void send(SmsDTO smsDTO){
        log.info("SmsReceiver 消息监听。。。。。。");
        HashMap<String, Object> param = new HashMap<>();
        param.put("checkcode", smsDTO.getMessage());
        smsService.send(smsDTO.getMobile(), SmsProperties.TEMPLATE_CODE, param);
    }
}
