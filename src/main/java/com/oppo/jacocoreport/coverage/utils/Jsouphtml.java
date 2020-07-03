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
        try {
            //解析整体覆盖率报告
            Document document = Jsoup.parse(this.totalhtmlreport, "UTF-8");
            Elements elements = document.select("tfoot").select("td");
            String totalinstructions = elements.get(2).text();
            String totalbranches = elements.get(4).text();
            int totalmissedmethod = Integer.parseInt(elements.get(9).text());
            int totalmethod = Integer.parseInt(elements.get(10).text());
            float methodpercent = (float)totalmissedmethod / (float)totalmethod;

            //解析差异化覆盖率
            Document diffdocument = Jsoup.parse(this.diffhtmlreport, "UTF-8");
            Elements diffelements = document.select("tfoot").select("td");
            String diffinstructions = elements.get(2).text();
            String diffbranches = elements.get(4).text();
            int diffmissedmethod = Integer.parseInt(elements.get(9).text());
            int diffmethod = Integer.parseInt(elements.get(10).text());
            float diffmethodpercent = (float)diffmissedmethod / (float)diffmethod;
            coverageData = new CoverageData(taskid,totalinstructions,totalbranches,methodpercent+"",diffinstructions,diffbranches,diffmethodpercent+"");
            return coverageData;
        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageData;
    }

    public static void main(String[] args){
        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoCov\\20200630211850\\coveragereport\\index.html"),new File("D:\\jacocoCov\\20200630211850\\coveragediffreport\\index.html"));
        System.out.println(jsouphtml.getCoverageData(Long.parseLong("20200630211850")));

    }
}
