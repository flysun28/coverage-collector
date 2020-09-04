package com.oppo.jacocoreport.coverage.utils;

public class Config {
    public final  static String MAVENPATH = "/usr/local/maven";
    public final  static String CodePath = "codeCoverage";
    public final static  int Port = 8098;
    public final  static String GitName = "80289528";
    public final  static String GitPassword = "Zhc_172520";
    public final  static String ResourceName = "testenvironment.yml";

//    public final static String ReportBasePath = "/home/service/app/coveragebackend/2qpiyetftazy";  //测试环境
    public final static String ReportBasePath = "/home/service/app/coveragebackend/fawoknqovs7v"; //线上环境
//    public final static String ReportBasePath = "D:\\codeCoverage";
    public final static String ReportBaseUrl = "http://report.jacoco.wanyol.com/";
    public final static String SEND_COVERAGE_URL = "http://atms.itest.wanyol.com/api/codeCoverage/execution/result";
    public static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";
    public static String SEND_ERRORMESSAGE_URL = "http://atms.itest.wanyol.com/api/codeCoverage/execution/failed";
    public static String SEND_STOPTIMERTASK_URL ="http://atms.itest.wanyol.com/api/codeCoverage/execution/stop/timerTask?id=";
    public static String RECOVER_TIMERTASK_URL = "http://atms.itest.wanyol.com/api/codeCoverage/execution/recover/timer/task";
}
