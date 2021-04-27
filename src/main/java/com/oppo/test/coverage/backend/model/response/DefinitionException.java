package com.oppo.test.coverage.backend.model.response;

import com.oppo.test.coverage.backend.model.constant.ErrorEnum;

public class DefinitionException extends RuntimeException {
    private Integer errorCode;
    private String errorMsg;

    private ErrorEnum errorEnum;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    public DefinitionException(){

    }

    public DefinitionException(ErrorEnum errorEnum){
        this.errorEnum = errorEnum;
        this.errorCode = errorEnum.getErrorCode();
        this.errorMsg = errorEnum.getErrorMsg();
    }

}
