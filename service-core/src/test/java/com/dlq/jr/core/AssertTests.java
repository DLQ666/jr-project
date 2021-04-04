package com.dlq.jr.core;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-04 11:54
 */
public class AssertTests {

    @Test
    public void test1() {
        Object o = null;
        if (o == null){
            throw new IllegalArgumentException("参数错误");
        }
    }

    @Test
    public void test2() {
        Object o = null;
        //用断言替代if结构
        Assert.notNull(o, "参数错误");
    }
}
