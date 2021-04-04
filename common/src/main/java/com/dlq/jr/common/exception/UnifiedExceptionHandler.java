package com.dlq.jr.common.exception;

import com.dlq.jr.common.result.R;
import com.dlq.jr.common.result.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-04 10:40
 */
@Slf4j
@RestControllerAdvice
public class UnifiedExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error();
    }

    @ExceptionHandler(value = BadSqlGrammarException.class)
    public R handleException(BadSqlGrammarException e) {
        log.error(e.getMessage(), e);
        return R.setResult(ResponseEnum.BAD_SQL_GRAMMAR_ERROR);
    }

    @ExceptionHandler(value = BusinessException.class)
    public R handleException(BusinessException e) {
        log.error(e.getMessage(), e);
        return R.error().message(e.getMessage()).code(e.getCode());
    }
}
