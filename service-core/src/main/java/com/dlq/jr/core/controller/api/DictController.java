package com.dlq.jr.core.controller.api;

import com.dlq.jr.common.result.R;
import com.dlq.jr.core.pojo.entity.Dict;
import com.dlq.jr.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-08 19:35
 */
@Api(tags = "数据字典")
@Slf4j
@RestController
@RequestMapping("/api/core/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation("根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable("dictCode") String dictCode) {
        List<Dict> dictList = dictService.findByDictCode(dictCode);
        return R.ok().data("dictList", dictList);
    }
}
