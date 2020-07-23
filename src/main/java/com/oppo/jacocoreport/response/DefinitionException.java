package com.oppo.jacocoreport.response;

public class DefinitionException extends RuntimeException {
    protected Integer errorCode;
    protected String errorMsg;
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

    public DefinitionException(){

    }

    public DefinitionException(Integer errorCode,String errorMsg){
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

}
