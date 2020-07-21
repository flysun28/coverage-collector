package com.oppo.jacocoreport.coverage.entity;

public class ErrorMsg {
    /**
     * 对应的测试记录id
     * */
    private Long id;
    /**
     * 错误信息
     */
    private String msg;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }



}
