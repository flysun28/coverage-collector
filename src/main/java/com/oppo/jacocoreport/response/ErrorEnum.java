package com.oppo.jacocoreport.response;

public enum ErrorEnum {
    SUCCESS(200,"success"),
    CLONE_FAILED(1001,"clone代码失败"),
    GET_EVIRONMENTIP(1002,"获取测试环境IP失败"),
    BUILD_MAVEN(1003,"MAVEN编译项目代码失败"),
    PRODUCT_REPORT(1004,"生成覆盖率报告失败"),
    JACOCO_EXEC_FAILED(1005,"获取测试环境覆盖率文件失败,请检查jacoco服务是否正确部署"),
    OTHER_ERROR(1006,"其他异常"),
    GETDOWNLOADPACKAGE_RAILED(1007,"未找到本版本的部署包,请确认配置的版本是否部署测试环境"),
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
