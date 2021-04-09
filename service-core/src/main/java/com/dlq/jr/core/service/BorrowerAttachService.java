package com.dlq.jr.core.service;

import com.dlq.jr.core.pojo.entity.BorrowerAttach;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.BorrowerAttachVo;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {

    List<BorrowerAttachVo> selectBorrowerAttachVoList(Long borrowerId);
}
