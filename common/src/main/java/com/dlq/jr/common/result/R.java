package com.dlq.jr.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-03 20:59
 */
@Data
public class R {

    private Integer code;
    private String message;
    private Map<String,Object> data = new HashMap<>();

    /**
     * 构造函数私有化
     */
    private R(){}

    /**
     * 返回成功结果
     * @return
     */
    public static R ok(){
        R r = new R();
        r.setCode(ResponseEnum.SUCCESS.getCode());
        r.setMessage(ResponseEnum.SUCCESS.getMessage());
        return r;
    }

    /**
     * 返回失败结果
     * @return
     */
    public static R error(){
        R r = new R();
        r.setCode(ResponseEnum.ERROR.getCode());
        r.setMessage(ResponseEnum.ERROR.getMessage());
        return r;
    }

    /**
     * 设置特定的结果
     * @param responseEnum
     * @return
     */
    public static R setResult(ResponseEnum responseEnum){
        R r = new R();
        r.setCode(responseEnum.getCode());
        r.setMessage(responseEnum.getMessage());
        return r;
    }

    public R data(String key,Object value){
        this.data.put(key, value);
        return this;
    }

    /**
     * 设置特定的消息
     * @param message
     * @return
     */
    public R message(String message){
        this.setMessage(message);
        return this;
    }
}
