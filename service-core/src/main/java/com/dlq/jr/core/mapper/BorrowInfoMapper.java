package com.dlq.jr.core.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlq.jr.core.pojo.query.BorrowInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

    Page<BorrowInfo> selectBorrowInfoList(Page page, @Param("borrowInfoQuery") BorrowInfoQuery borrowInfoQuery);
}
