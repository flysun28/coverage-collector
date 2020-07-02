package com.oppo.jacocoreport.coverage.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.annotation.Documented;

public class Jsouphtml {
    private File htmlreport;
    public Jsouphtml(File htmlreport){
      this.htmlreport = htmlreport;
    }

    public String getCoverageReport(){
        String coveragepercent = "";
        try {
            Document document = Jsoup.parse(this.htmlreport, "UTF-8");
            Elements elements = document.select("tfoot").select("td");
            coveragepercent = elements.get(2).text();
            System.out.println(coveragepercent);
            return coveragepercent;
        }catch (Exception e){
            e.printStackTrace();
        }
        return coveragepercent;
    }

    public static void main(String[] args){
        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoCov\\20200630211850\\coveragereport\\index.html"));
        System.out.println(jsouphtml.getCoverageReport());
        Jsouphtml jsouphtml2 = new Jsouphtml(new File("D:\\jacocoCov\\20200630211850\\coveragediffreport\\index.html"));
        System.out.println(jsouphtml2.getCoverageReport());

    }
}
