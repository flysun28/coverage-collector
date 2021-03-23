package com.oppo.jacocoreport.response;

/**
 * @author 80264236
 * @date 2021/3/19 10:05
 */
public enum TimerTaskStopReasonEnum {
    /**
     * 轮询任务停止原因回调
     * */
    BASE(0,"&msg="),
    USER_HANDLE(1,"手动停止"),
    NEW_VERSION(2,"被测机器已部署新版本"),
    NO_UPDATE_ONE_DAY(3,"超过一天覆盖率数据无变化"),
    NO_JACOCO_ALL(4,"无法获取到覆盖率文件"),
    OTHER_ERROR(5,"轮询期间发生错误"),
    NO_UPDATE_NOTIFY(6,"${appCode}应用的覆盖率轮询统计任务:${id},版本:${version},覆盖率文件超过24H未更新,请关注并及时停止")
    ;

    private Integer code;
    private String reasonMsg;

    TimerTaskStopReasonEnum(Integer code,String reasonMsg){
        this.code = code;
        this.reasonMsg = reasonMsg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReasonMsg() {
        return reasonMsg;
    }

    public void setReasonMsg(String reasonMsg) {
        this.reasonMsg = reasonMsg;
    }
}
