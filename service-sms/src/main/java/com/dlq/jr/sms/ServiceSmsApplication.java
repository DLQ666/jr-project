package com.dlq.jr.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 17:54
 */
@EnableFeignClients
@SpringBootApplication
@ComponentScan({"com.dlq.jr", "com.dlq.jr.common"})
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class, args);
    }
}
