package com.oppo.jacocoreport.coverage.entity;

public class Data {

    private int code;
    private String data;

    public int getCode() {
        return code;
    }

    public Data setCode(int code) {
        this.code = code;
        return this;
    }

    public String getData() {
        return data;
    }

    public Data setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Data{" +
                "code=" + code +
                ", data='" + data + '\'' +
                '}';
    }
}
