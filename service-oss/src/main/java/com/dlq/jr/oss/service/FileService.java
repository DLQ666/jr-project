package com.dlq.jr.oss.service;

import java.io.InputStream;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 21:07
 */
public interface FileService {

    /**
     * 文件上传至阿里云
     */
    String upload(InputStream inputStream, String module, String fileName);

    /**
     * 根据路径删除文件
     * @param url
     */
    void removeFile(String url);
}
