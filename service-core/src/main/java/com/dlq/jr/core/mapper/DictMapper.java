package com.dlq.jr.core.mapper;

import com.dlq.jr.core.pojo.dto.ExcelDictDTO;
import com.dlq.jr.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface DictMapper extends BaseMapper<Dict> {

    void insertBath(@Param("list") List<ExcelDictDTO> list);
}
