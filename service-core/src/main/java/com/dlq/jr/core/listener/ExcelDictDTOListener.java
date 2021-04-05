package com.dlq.jr.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.dlq.jr.core.mapper.DictMapper;
import com.dlq.jr.core.pojo.dto.ExcelDictDTO;
import com.dlq.jr.core.service.DictService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-05 13:12
 */
@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    private DictMapper dictMapper;

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //数据列表
    List<ExcelDictDTO> list = new ArrayList<>();

    //每隔5条记录批量存储一次数据
    //生产环境临界值调整为 3000  ，，，，太多了数据库不支持-sql太长
    private static final int BATCH_COUNT = 5;

    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext context) {
        log.info("解析到一条记录：{}", data);

        //将数据存入数据列表
        list.add(data);
        if (list.size() >= BATCH_COUNT) {
            saveData(); //每五条 保存数据库
            list.clear(); //清空list
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //当最后剩余的数据记录数不足BATCH_COUNT时，我们最终一次性存储剩余数据
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        log.info("{}：条数据被保存到数据库...", list.size());
        //调用mapper层的save方法：save --- list对象
        dictMapper.insertBath(list);
        log.info("{}：条记录被保存到数据库成功！", list.size());
    }
}
