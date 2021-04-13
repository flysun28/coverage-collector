package com.oppo.jacocoreport.coverage.entity;

import lombok.Data;

@Data
public class RecordReq {

    private String paasZoneCode;

    private String appId;

    private String caseId;

    private String remoteAddr;
}
