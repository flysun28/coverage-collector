package com.oppo.test.coverage.backend.record.request.req;

import lombok.Data;

@Data
public class GetCaseIdListReq {
    private String appId;
    private String method;
}
