package com.oppo.test.coverage.backend.model.response;

import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = DefinitionException.class)
    public Result bizExceptionHandler(DefinitionException e){
        return Result.defineError(e);
    }

    @ExceptionHandler(value = Exception.class)
    public Result exceptionHandler(Exception e){
       return Result.otherError(ErrorEnum.OTHER_ERROR);
    }
}
