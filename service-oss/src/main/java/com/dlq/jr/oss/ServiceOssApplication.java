package com.dlq.jr.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 21:04
 */
@ComponentScan({"com.dlq.jr", "com.dlq.jr.common"})
@SpringBootApplication
public class ServiceOssApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOssApplication.class, args);
    }
}
