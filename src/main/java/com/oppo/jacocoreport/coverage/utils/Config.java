package com.oppo.jacocoreport.coverage.utils;

public class Config {
    public final static  int Port = 8098;
    public final  static String GitName = "80289528";
    public final  static String GitPassword = "Zhc_001";

//    public final static String ReportBasePath = "/home/service/app/coveragebackend/2qpiyetftazy";  //测试环境
    public final static String ReportBasePath = "/home/service/app/coveragebackend/fawoknqovs7v"; //线上环境
//    public final static String ReportBasePath = "D:\\codeCoverage";
    public final  static String CodePath = ReportBasePath+"/codeCoverage";
    public final  static String ProjectCovPath = ReportBasePath+"/projectCovPath";
//    public final static String ReportBaseUrl = "http://report-test.jacoco.wanyol.com/";
//    public final static String ReportBaseUrl = "http://s3v2.ocs-cn-south.wanyol.com/code-coverage//home/service/app/coveragebackend/2qpiyetftazy/";
    public final static String ReportBaseUrl = "http://s3v2.ocs-cn-south.wanyol.com/code-coverage//home/service/app/coveragebackend/fawoknqovs7v/";
    public final static String BaseUrl = "http://atms.itest.wanyol.com/";
    public final static String SEND_COVERAGE_URL = BaseUrl+"api/codeCoverage/execution/result";
    public final static String SEND_BRANCHCOVERAGE_URL = BaseUrl+"api/codeCoverage/execution/result/branch";
    public final static String SEND_VERSIONCOVERAGE_URL = BaseUrl+"api/codeCoverage/record/project/result";
    public static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";
    public static String SEND_ERRORMESSAGE_URL = BaseUrl+"api/codeCoverage/execution/failed";
    public static String SEND_STOPTIMERTASK_URL =BaseUrl+"api/codeCoverage/execution/stop/timerTask?id=";
    public static String RECOVER_TIMERTASK_URL = BaseUrl+"api/codeCoverage/execution/recover/timer/task";

    public final static String TransferBaseIp = "10.35.174.197";
    public static String GET_TRANSFER_PORT_URL = "http://10.35.174.197:9099/java/port";
}
