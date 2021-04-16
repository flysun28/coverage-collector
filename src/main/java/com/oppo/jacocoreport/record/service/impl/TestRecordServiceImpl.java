/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.oppo.jacocoreport.record.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oppo.jacocoreport.record.config.CommonConfig;
import com.oppo.jacocoreport.record.entity.InvokeRecord;
import com.oppo.jacocoreport.record.entity.TestRecordExample;
import com.oppo.jacocoreport.record.request.req.StartRecordReq;
import com.oppo.jacocoreport.record.request.res.PaasRequestDetailRes;
import com.oppo.jacocoreport.record.service.InvokeRecordService;
import com.oppo.jacocoreport.record.utils.OkHttpClientUtil;
import com.oppo.jacocoreport.record.utils.SnowFlakeUtil;
import com.oppo.jacocoreport.record.entity.TestRecord;
import com.oppo.jacocoreport.record.mapper.TestRecordMapper;
import com.oppo.jacocoreport.record.request.req.StopRecordReq;
import com.oppo.jacocoreport.record.request.res.StartRecordRes;
import com.oppo.jacocoreport.record.service.TestRecordService;
import com.oppo.jacocoreport.record.common.Response;
import com.oppo.jacocoreport.record.common.ResponseBuilder;
import com.oppo.jacocoreport.record.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TestRecordServiceImpl implements TestRecordService {

    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private TestRecordMapper testRecordMapper;

    @Autowired
    private InvokeRecordService invokeRecordService;

    @Override
    public Response startTest(StartRecordReq startRecordReq) {

        TestRecord testRecord = new TestRecord();
        BeanUtils.copyProperties(startRecordReq,testRecord);
        testRecord.setBeginTime(new Date());
        String flowNo = SnowFlakeUtil.getSnowId(1l,1l);
        testRecord.setFlowNo(flowNo);
        try {
            save(testRecord);
            StartRecordRes startRecordRes = new StartRecordRes();
            startRecordRes.setFlowNo(flowNo);
            return ResponseBuilder.buildSuccessRes(startRecordRes);
        } catch (Exception e){
            return ResponseBuilder.buildFailRes(ResponseCode.BUSSINESS_ERR,e);
        }
    }

    @Override
    public Response stopTest(StopRecordReq stopRecordReq) {
        CompletableFuture.runAsync(()-> {
            TestRecord endRecord = new TestRecord();
            BeanUtils.copyProperties(stopRecordReq,endRecord);
            TestRecord beginRecord = selectBySelective(endRecord);
            if (Objects.isNull(beginRecord)){
                log.error("未查到开始测试记录");
            }
            beginRecord.setEndTime(new Date());
            testRecordMapper.updateByPrimaryKey(beginRecord);
            List<PaasRequestDetailRes> paasRequestDetailResList = requestIntfDetail(beginRecord);
            paasRequestDetailResList.forEach(paasRequestDetailRes -> {
                List<InvokeRecord> invokeRecordList = requestInvokeDetail(paasRequestDetailRes.getTraceId(),beginRecord.getBeginTime().getTime());
                if (invokeRecordList.size() > 0){
                    invokeRecordList.forEach(invokeRecord -> {
                        invokeRecord.setFlowNo(beginRecord.getFlowNo());
                        invokeRecordService.saveInvokeRecord(invokeRecord);
                    });
                }
            });
        });
        return ResponseBuilder.buildDefaultSuccessRes();
    }


    public int save(TestRecord testRecord) {
        Date date = new Date();
        testRecord.setCreateTime(date);
        testRecord.setUpdateTime(date);
        return testRecordMapper.insert(testRecord);
    }

    public int update(TestRecord testRecord) {
        TestRecordExample example = new TestRecordExample();
        TestRecordExample.Criteria criteria = example.createCriteria();
        testRecord.setUpdateTime(new Date());
        if (StringUtils.isNotBlank(testRecord.getFlowNo())){
            criteria.andFlowNoEqualTo(testRecord.getFlowNo());
        }
        if (StringUtils.isNotBlank(testRecord.getRemoteAddr())){
            criteria.andRemoteAddrEqualTo(testRecord.getRemoteAddr());
        }
        if (StringUtils.isNotBlank(testRecord.getPaasZoneCode())){
            criteria.andPaasZoneCodeEqualTo(testRecord.getPaasZoneCode());
        }
        if (StringUtils.isNotBlank(testRecord.getAppId())){
            criteria.andAppIdEqualTo(testRecord.getAppId());
        }
        if (StringUtils.isNotBlank(testRecord.getCaseId())){
            criteria.andCaseIdEqualTo(testRecord.getCaseId());
        }
        return testRecordMapper.updateByExampleSelective(testRecord,example);
    }



    public TestRecord selectBySelective(TestRecord testRecord){
        TestRecordExample example = new TestRecordExample();
        example.setOrderByClause("create_time desc");
        TestRecordExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(testRecord.getFlowNo())){
            criteria.andFlowNoEqualTo(testRecord.getFlowNo());
        }
        if (StringUtils.isNotBlank(testRecord.getAppId())){
            criteria.andAppIdEqualTo(testRecord.getAppId());
        }
        if (StringUtils.isNotBlank(testRecord.getCaseId())){
            criteria.andCaseIdEqualTo(testRecord.getCaseId());
        }
        if (StringUtils.isNotBlank(testRecord.getPaasZoneCode())){
            criteria.andPaasZoneCodeEqualTo(testRecord.getPaasZoneCode());
        }
        if (StringUtils.isNotBlank(testRecord.getRemoteAddr())){
            criteria.andRemoteAddrEqualTo(testRecord.getRemoteAddr());
        }
        List<TestRecord> testRecordList =  testRecordMapper.selectByExample(example);
        return testRecordList.size() > 0 ?testRecordList.get(0):null;
    }

    public List<InvokeRecord> requestInvokeDetail(String traceId,long startTime){
        Map<String,Object> body = new HashMap<>();
        body.put("traceId",traceId);
        body.put("startTime",startTime);
        List<Map<String,Object>> list = new ArrayList<>();
        list.add(body);
        List<InvokeRecord> invokeRecordList = new ArrayList<>();
        try {
            String result = OkHttpClientUtil.post(commonConfig.getInvokeUrl(), JSONObject.toJSONString(list),null);
            JSONObject traceObj = JSONObject.parseObject(result);
            if (Objects.nonNull(traceObj) &&
                    traceObj.getIntValue("status") == 0 &&
                    Objects.nonNull(traceObj.getJSONObject("data")) &&
                    traceObj.getJSONObject("data").getJSONObject(traceId).getIntValue("size") > 0){
                JSONArray jsonArray = traceObj.getJSONObject("data").getJSONObject(traceId).getJSONArray("data");
                jsonArray.forEach(json -> {
                    JSONObject traceDetail = (JSONObject)json;
                    String method = traceDetail.getString("method");
                    String appId = traceDetail.getString("appId");
                    String nodeIp = traceDetail.getString("nodeIp");
                    String remoteAddr = traceDetail.getString("remoteAddr");
                    InvokeRecord invokeRecord = new InvokeRecord();
                    invokeRecord.setAppId(appId);
                    invokeRecord.setMethod(method);
                    invokeRecord.setNodeIp(nodeIp);
                    invokeRecord.setRemoteAddr(remoteAddr);
                    invokeRecord.setTraceId(traceId);
                    invokeRecordList.add(invokeRecord);
                });
            }

        } catch (Exception e){
            log.error("request error :",commonConfig.getInvokeUrl(),e);
        }

        return invokeRecordList;
    }

    public List<PaasRequestDetailRes> requestIntfDetail(TestRecord record){
        int page = 1;
        int size = 100;
        Map<String,Object> body = new HashMap<>();
        body.put("appId",record.getAppId());
        body.put("startTime",record.getBeginTime().getTime());
        body.put("endTime",record.getEndTime().getTime());
        body.put("page",page);
        body.put("size",size);
        body.put("zoneCode",record.getPaasZoneCode());
        List<PaasRequestDetailRes> paasRequestDetailResList = new ArrayList<>();
        try {
            String traceResult = OkHttpClientUtil.post(commonConfig.getRequestUrl(), JSONObject.toJSONString(body),null);
            JSONObject traceObj = JSONObject.parseObject(traceResult);
            if (Objects.nonNull(traceObj) &&
                    traceObj.getIntValue("status") == 200 &&
                    Objects.nonNull(traceObj.getJSONObject("data")) &&
                    traceObj.getJSONObject("data").getIntValue("total") > 0){
                int total = traceObj.getJSONObject("data").getIntValue("total");
                JSONArray jsonArray = traceObj.getJSONObject("data").getJSONArray("list");
                if (total > size){
                    for (int i = 0; i < total/size; i++){
                        body.put("page",i + 2);
                        traceResult = OkHttpClientUtil.post(commonConfig.getRequestUrl(), JSONObject.toJSONString(body),null);
                        JSONObject moreTraceObj = JSONObject.parseObject(traceResult);
                        if (Objects.nonNull(traceObj) &&
                                traceObj.getIntValue("status") == 200 &&
                                Objects.nonNull(traceObj.getJSONObject("data")) &&
                                traceObj.getJSONObject("data").getIntValue("total") > 0){
                            jsonArray.addAll(moreTraceObj.getJSONObject("data").getJSONArray("list"));
                        }
                    }
                }
                jsonArray.forEach(json -> {
                    PaasRequestDetailRes paasRequestDetailRes = new PaasRequestDetailRes();
                    BeanUtils.copyProperties(record,paasRequestDetailRes);
                    JSONObject obj = (JSONObject) json;
                    if (obj.getString("remoteAddr").contains(record.getRemoteAddr())){
                        paasRequestDetailRes.setTraceId(obj.getString("traceId"));
                        paasRequestDetailResList.add(paasRequestDetailRes);
                    }
                });
            }
        } catch (Exception e){
            log.error("request error :",commonConfig.getRequestUrl(),e);
        }
        return paasRequestDetailResList;
    }
}
