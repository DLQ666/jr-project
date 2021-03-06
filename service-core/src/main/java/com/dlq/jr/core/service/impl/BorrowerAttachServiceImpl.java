package com.dlq.jr.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.jr.core.pojo.entity.BorrowerAttach;
import com.dlq.jr.core.mapper.BorrowerAttachMapper;
import com.dlq.jr.core.pojo.vo.BorrowerAttachVo;
import com.dlq.jr.core.service.BorrowerAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {

    @Override
    public List<BorrowerAttachVo> selectBorrowerAttachVoList(Long borrowerId) {
        QueryWrapper<BorrowerAttach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("borrower_id", borrowerId);
        List<BorrowerAttach> borrowerAttachList = baseMapper.selectList(queryWrapper);
        List<BorrowerAttachVo> borrowerAttachVoList = new ArrayList<>();
        borrowerAttachList.forEach(borrowerAttach -> {
            BorrowerAttachVo borrowerAttachVo = new BorrowerAttachVo();
            borrowerAttachVo.setImageUrl(borrowerAttach.getImageUrl());
            borrowerAttachVo.setImageType(borrowerAttach.getImageType());
            borrowerAttachVoList.add(borrowerAttachVo);
        });
        return borrowerAttachVoList;
    }
}
