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

package com.oppo.test.coverage.backend.record.controller;


import com.oppo.test.coverage.backend.record.common.Response;
import com.oppo.test.coverage.backend.record.common.ResponseBuilder;
import com.oppo.test.coverage.backend.record.common.ResponseCode;
import com.oppo.test.coverage.backend.record.request.req.GetCaseIdListReq;
import com.oppo.test.coverage.backend.record.request.req.StartRecordReq;
import com.oppo.test.coverage.backend.record.request.req.StopRecordReq;
import com.oppo.test.coverage.backend.record.service.TestRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRecordController {

    @Autowired
    private TestRecordService testRecordService;

    @RequestMapping(value="/recording/start")
    public Response startTest(@RequestBody StartRecordReq startRecordReq){
        if (StringUtils.isBlank(startRecordReq.getAppId())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"appId is not blank");
        }
        if (StringUtils.isBlank(startRecordReq.getCaseId())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"caseId is not blank");
        }
        if (StringUtils.isBlank(startRecordReq.getPaasZoneCode())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"paasZoneCode is not blank");
        }
        if (StringUtils.isBlank(startRecordReq.getRemoteAddr())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"remoteAddr is not blank");
        }
        return testRecordService.startTest(startRecordReq);
    }


    @RequestMapping("/recording/stop")
    public Response stopTest(@RequestBody StopRecordReq stopRecordReq){

        if (StringUtils.isBlank(stopRecordReq.getFlowNo())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"flowNo is not blank");
        }
        return testRecordService.stopTest(stopRecordReq);
    }

    @RequestMapping("/recording/getCaseIdList")
    public Response getCaseIdListTest(@RequestBody GetCaseIdListReq getCaseIdListReq){

        if (StringUtils.isBlank(getCaseIdListReq.getAppId())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"appId is not blank");
        }
        if (StringUtils.isBlank(getCaseIdListReq.getMethod())){
            ResponseBuilder.buildFailRes(ResponseCode.PARAMS_ILlEGLE,"method is not blank");
        }
        return testRecordService.getCaseIdListTest(getCaseIdListReq);
    }

}
