package com.oppo.test.coverage.backend.record.request.res;

import lombok.Data;

import java.util.List;

@Data
public class GetCaseIdListRes {
    private List<String> caseId;
}
