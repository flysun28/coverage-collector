package com.oppo.test.coverage.backend.model.constant;

/**
 * @author 80264236
 */

public enum ErrorEnum {
    /**
     * 错误码
     * */
    SUCCESS(200,"success"),
    CLONE_FAILED(1001,"clone代码失败"),
    GET_EVIRONMENTIP(1002,"获取测试环境IP失败,请确认配置的版本是否部署测试环境"),
    BUILD_MAVEN(1003,"MAVEN编译项目代码失败"),
    PRODUCT_REPORT(1004,"生成覆盖率报告失败"),
    JACOCO_EXEC_FAILED(1005,"获取测试环境覆盖率文件失败,请检查jacoco服务是否正确部署"),
    OTHER_ERROR(1006,"其他异常"),
    GETDOWNLOADPACKAGE_RAILED(1007,"未找到本版本的部署包,请确认配置的版本是否部署测试环境"),
    DOWNLOAD_BUILDVERSION_FAILED(1008,"网络问题导致下载版本包失败，请重新执行一次"),
    DETECTED_NEW_VERSION(1009,"检测到测试环境部署版本和配置不一致，请重新配置版本"),
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
