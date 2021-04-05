package com.dlq.jr.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.dlq.jr.common.exception.BusinessException;
import com.dlq.jr.common.result.R;
import com.dlq.jr.common.result.ResponseEnum;
import com.dlq.jr.core.pojo.dto.ExcelDictDTO;
import com.dlq.jr.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 13:18
 */
@Api(tags = "数据字典管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/dict")
public class AdminDictController {

    @Autowired
    private DictService dictService;

    @ApiOperation("Excel批量导入数据字典")
    @PostMapping("/import")
    public R batchImport(@ApiParam(value = "excel数据字典文件",required = true)
                         @RequestParam("file") MultipartFile file){

        try {
            InputStream inputStream = file.getInputStream();
            dictService.importData(inputStream);
            return R.ok().message("数据字典数据批量导入成功");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR,e);
        }
    }

    @ApiOperation("Excel批量导出数据字典")
    @GetMapping("/export")
    public void batchExport(HttpServletResponse response){
        try {
            // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());

        } catch (IOException e) {
            //EXPORT_DATA_ERROR(104, "数据导出失败"),
            throw  new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }


}
