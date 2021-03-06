package com.dlq.jr.sms.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.dlq.jr.common.exception.Assert;
import com.dlq.jr.common.exception.BusinessException;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.sms.service.SmsService;
import com.dlq.jr.sms.util.SmsProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 18:29
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void send(String mobile, String templateCode, Map<String, Object> param) {

        //创建远程连接客户端对象
        DefaultProfile profile = DefaultProfile.getProfile(
                SmsProperties.REGION_Id,
                SmsProperties.KEY_ID,
                SmsProperties.KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);

        //创建远程连接的请求参数
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
        request.putQueryParameter("TemplateCode", templateCode);
        Gson gson = new Gson();
        String jsonParam = gson.toJson(param);
        request.putQueryParameter("TemplateParam", jsonParam);

        try {
            //使用客户端对象携带请求参数向远程阿里云服务器发起远程调用 并得到响应结果
            CommonResponse response = client.getCommonResponse(request);
            System.out.println("response.getData()" + response.getData());

            //通信失败处理
            boolean success = response.getHttpResponse().isSuccess();
            Assert.isTrue(success, ResponseEnum.ALIYUN_RESPONSE_ERROR);

            //获取响应结果
            String data = response.getData();
            Map<String, String> resultMap = gson.fromJson(data, HashMap.class);
            Object code = resultMap.get("Code");
            String message = resultMap.get("Message");
            log.info("code:" + code + "message：" + message);

            //业务失败处理
            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL", code,
                    ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
            Assert.equals("OK", code, ResponseEnum.ALIYUN_SMS_ERROR);

        } catch (ServerException e) {
            log.error("阿里云短信发送sdk调用失败：" + e.getErrCode() + "，" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, e);
            //e.printStackTrace();
        } catch (ClientException e) {
            log.error("阿里云短信发送sdk调用失败：" + e.getErrCode() + "，" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR, e);
            //e.printStackTrace();
        }
    }
}
