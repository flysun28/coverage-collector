package com.oppo.jacocoreport.coverage.entity;

public class Data<T> {

    private int code;
    private T data;


    public int getCode() {
        return code;
    }

    public Data setCode(int code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return data;
    }

    public Data setData(T data) {
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
