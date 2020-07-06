package com.oppo.jacocoreport.coverage.service;

import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import com.oppo.jacocoreport.coverage.utils.HttpUtils;

public class CoverageServiceImpl{

    public void sendCoverageData(String url,CoverageData coverageData){
        String requstUrl = url;
        Data data = HttpUtils.sendPostRequest(requstUrl,coverageData);
        System.out.println(data.getCode());
    }
}
