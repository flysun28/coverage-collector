package com.oppo.test.coverage.backend.model.response;

/**
 * @author 80264236
 * @date 2021/5/21 11:18
 */
public class CortResponse {

    /**
     * 错误码,0为正常响应
     * */
    private Integer errno;

    private String errormsg;

    private Object data;


    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CortResponse{" +
                "errno=" + errno +
                ", errormsg='" + errormsg + '\'' +
                ", data=" + data +
                '}';
    }
}
