package com.dlq.jr.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.core.listener.ExcelDictDTOListener;
import com.dlq.jr.core.pojo.dto.ExcelDictDTO;
import com.dlq.jr.core.pojo.entity.Dict;
import com.dlq.jr.core.mapper.DictMapper;
import com.dlq.jr.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {

        List<Dict> dictList = baseMapper.selectList(null);
        //创建ExcelDictDTO列表，将Dict列表转换成ExcelDictDTO列表
        List<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict,excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {

        try {
            //首先查询redis中是否存在数据列表
            List<Dict> dictList = (List<Dict>) redisTemplate.opsForValue().get("jr:core:dictList:" + parentId);
            if (dictList != null) {
                log.info("从redis中直接返回数据列表");
                //如果存在则从redis中直接返回数据列表
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis服务器异常："+ ExceptionUtils.getStackTrace(e));
        }

        //如果不存在则直接查询数据库
        log.info("从redis中直接返回数据列表");
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", parentId);
        List<Dict> dictList = baseMapper.selectList(dictQueryWrapper);
        //填充 hasChildren 字段
        dictList.forEach(dict -> {
            //判断当前节点是否有子节点，找到当前的dict下级有没有子节点
            dict.setHasChildren(this.hasChildren(dict.getId()));
        });

        try {
            log.info("将数据存入redis");
            //将数据存入redis
            redisTemplate.opsForValue().set("jr:core:dictList:" + parentId, dictList,5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis服务器异常："+ ExceptionUtils.getStackTrace(e));
        }

        //返回数据列表
        return dictList;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        return this.listByParentId(dict.getId());
    }

    private boolean hasChildren(Long id){
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        Integer integer = baseMapper.selectCount(dictQueryWrapper);
        return integer > 0;
    }
}
