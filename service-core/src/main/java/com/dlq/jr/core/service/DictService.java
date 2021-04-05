package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.dto.ExcelDictDTO;
import com.dlq.jr.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface DictService extends IService<Dict> {

    void importData(InputStream inputStream);

    List<ExcelDictDTO> listDictData();
}
