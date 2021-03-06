package com.dlq.jr.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlq.jr.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.jr.core.pojo.vo.BorrowerApprovalVo;
import com.dlq.jr.core.pojo.vo.BorrowerDetailVo;
import com.dlq.jr.core.pojo.vo.BorrowerVo;
import com.dlq.jr.core.pojo.vo.RevertBorrowerVo;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author D奇
 * @since 2021-04-03
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerVoByUserId(BorrowerVo borrowerVo, Long userId);

    Integer getStatusByUserId(Long userId);

    IPage<Borrower> listPage(Page<Borrower> pageParam, String keyword);

    BorrowerDetailVo getBorrowerDetailVoById(Long id);

    void approval(BorrowerApprovalVo borrowerApprovalVo);

    RevertBorrowerVo selectBorrowerVoByUserId(Long userId);
}
