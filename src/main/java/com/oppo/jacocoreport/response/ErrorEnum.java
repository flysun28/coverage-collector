package com.oppo.jacocoreport.response;

public enum ErrorEnum {
    SUCCESS(200,"success"),
    CLONE_FAILED(1001,"clone代码失败"),
    GET_EVIRONMENTIP(1002,"获取测试环境IP失败"),
    BUILD_MAVEN(1003,"MAVEN编译失败"),
    PRODUCT_REPORT(1004,"生成覆盖率报告失败"),
    OTHER_ERROR(1005,"其他异常"),
    ;
    private Integer errorCode;
    private String errorMsg;

    ErrorEnum(Integer errorCode,String errorMsg){
           this.errorCode = errorCode;
           this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }



}
