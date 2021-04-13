package com.oppo.jacocoreport.controller;


import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.entity.RecordReq;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRecordController {


    public Data startTest(@RequestBody RecordReq recordReq){


        return new Data();
    }

}
