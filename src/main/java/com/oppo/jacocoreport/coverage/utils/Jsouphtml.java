package com.oppo.jacocoreport.coverage.utils;

import com.oppo.jacocoreport.coverage.entity.CoverageData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.annotation.Documented;

public class Jsouphtml {
    private File totalhtmlreport;
    private File diffhtmlreport;
    public Jsouphtml(File totalhtmlreport,File diffhtmlreport){
      this.totalhtmlreport = totalhtmlreport;
      this.diffhtmlreport = diffhtmlreport;
    }

    public CoverageData getCoverageData(Long taskid){
        CoverageData coverageData = null;
        String totalinstructions = "";
        String totalbranches = "";
        String methodpercentStr = "";
        String diffinstructions = "";
        String diffbranches = "";
        String diffmethodpercentStr = "";
        String totalcoverageReportPath  = "";
        String diffcoverageReportPath  = "";
        try {
            //解析整体覆盖率报告
            if(this.totalhtmlreport.exists()) {
                Document document = Jsoup.parse(this.totalhtmlreport, "UTF-8");
                Elements elements = document.select("tfoot").select("td");
                totalinstructions = elements.get(2).text();
                totalbranches = elements.get(4).text();
                int totalmissedmethod = Integer.parseInt(elements.get(9).text());
                int totalmethod = Integer.parseInt(elements.get(10).text());
                float methodpercent = (float) totalmissedmethod * 100 / (float) totalmethod;
                methodpercentStr = String.format("%.2f", methodpercent) + "%";
                totalcoverageReportPath = Config.ReportBaseUrl+taskid+"/coveragereport/index.html";
            }

            //解析差异化覆盖率
            if(this.diffhtmlreport.exists()) {
                Document diffdocument = Jsoup.parse(this.diffhtmlreport, "UTF-8");
                Elements diffelements = diffdocument.select("tfoot").select("td");
                diffinstructions = diffelements.get(2).text();
                diffbranches = diffelements.get(4).text();
                int diffmissedmethod = Integer.parseInt(diffelements.get(9).text());
                int diffmethod = Integer.parseInt(diffelements.get(10).text());
                float diffmethodpercent = (float) diffmissedmethod * 100 / (float) diffmethod;
                diffmethodpercentStr = String.format("%.2f", diffmethodpercent) + "%";
                diffcoverageReportPath = Config.ReportBaseUrl+taskid+"/coveragediffreport/index.html";
            }
            coverageData = new CoverageData(taskid,totalinstructions,totalbranches,totalcoverageReportPath,methodpercentStr,diffinstructions,diffbranches,diffmethodpercentStr,diffcoverageReportPath);
            return coverageData;
        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageData;
    }

    public static void main(String[] args){
        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoreport\\123456789\\coveragereport\\index.html"),new File("D:\\jacocoreport\\123456789\\coveragediffreport\\index.html"));
        System.out.println(jsouphtml.getCoverageData(Long.parseLong("123456789")));

    }
}
