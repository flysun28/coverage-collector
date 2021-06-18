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

import com.oppo.jacocoreport.record.entity.InvokeRecord;
import com.oppo.jacocoreport.record.entity.InvokeRecordExample;
import com.oppo.jacocoreport.record.mapper.InvokeRecordMapper;
import com.oppo.jacocoreport.record.service.InvokeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class InvokeRecordServiceImpl implements InvokeRecordService {

    @Autowired
    private InvokeRecordMapper invokeRecordMapper;
    @Override
    public int saveInvokeRecord(InvokeRecord invokeRecord) {
        Date date = new Date();
        invokeRecord.setCreateTime(date);
        invokeRecord.setUpdateTime(date);
        try {
            return invokeRecordMapper.insertSelective(invokeRecord);
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public List<String> selectCaseId(String appId, String method) {
        InvokeRecordExample invokeRecordExample=new InvokeRecordExample();
        InvokeRecordExample.Criteria criteria=invokeRecordExample.createCriteria();
        criteria.andAppIdEqualTo(appId);
        criteria.andMethodEqualTo(method);
        List<InvokeRecord> invokeRecordList=invokeRecordMapper.selectByExample(invokeRecordExample);
        if(invokeRecordList.size()>0)
        {
            List<String> caseId=new ArrayList<>();
            for(InvokeRecord invokeRecord:invokeRecordList)
            {
                caseId.add(invokeRecord.getCaseId());
            }
            return caseId;
        }
        return null;
    }
}
