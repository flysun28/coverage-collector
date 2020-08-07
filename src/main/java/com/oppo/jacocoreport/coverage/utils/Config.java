package com.oppo.jacocoreport.coverage.utils;

public class Config {
    public final  static String MAVENPATH = "/usr/local/maven";
    public final  static String CodePath = "codeCoverage";
    public final static  int Port = 8098;
    public final  static String GitName = "80289528";
    public final  static String GitPassword = "Zhc_172520";
    public final  static String ResourceName = "testenvironment.yml";

    public final static String ReportBasePath = "/app/coveragebackend/2qpiyetftazy";  //测试环境
//    public final static String ReportBasePath = "/app/coveragebackend/fawoknqovs7v";
    public final static String ReportBaseUrl = "http://10.84.24.29:8888/";
    public final static String SEND_COVERAGE_URL = "http://atms-test.itest.adc.com/api/codeCoverage/execution/result";
    public static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";
    public static String SEND_ERRORMESSAGE_URL = "http://atms-test.itest.adc.com/api/codeCoverage/execution/failed";
}
