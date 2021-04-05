package com.dlq.jr.core;

import com.dlq.jr.core.mapper.DictMapper;
import com.dlq.jr.core.pojo.entity.Dict;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 16:15
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTests {

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private DictMapper dictMapper;

    @Test
    public void saveDict() {
        Dict dict = dictMapper.selectById(1);
        redisTemplate.opsForValue().set("dict", dict, 5, TimeUnit.MINUTES);
    }

    @Test
    public void getDict() {
        Dict dict = (Dict) redisTemplate.opsForValue().get("dict");
        System.out.println(dict);
        //Dict(id=1, parentId=0, name=全部分类, value=null, dictCode=ROOT, createTime=2021-04-05T14:28:25,
        // updateTime=2021-04-05T14:28:25, deleted=false, hasChildren=false)
    }
}
