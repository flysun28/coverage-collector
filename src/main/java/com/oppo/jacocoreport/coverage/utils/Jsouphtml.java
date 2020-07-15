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
        CoverageData coverageData = new CoverageData();
        coverageData.setId(taskid);
        String totalCoverageReportPath;
        String diffCoverageReportPath;

        String missedInstructions;
        String totalInstructions;
        String diffMissedInstructions;
        String diffTotalInstructions;
        /**
         * 分支覆盖率
         * */
        String missedBranches;
        String totalBranches;
        String diffMissedBranches;
        String diffTotalBranches;

        /**
         * 圈复杂度
         * */
        String missedCxty;
        String totalCxty;
        String diffMissedCxty;
        String diffTotalCxty;

        /**
         * 代码行
         * */
        String missedLines;
        String totalLines;
        String diffMissedLines;
        String diffTotalLines;

        /**
         * 方法
         * */
        String missedMethods;
        String totalMethods;
        String diffMissedMethods;
        String diffTotalMethods;

        /**
         * 类
         * */
        String missedClasses;
        String totalClasses;
        String diffMissedClasses;
        String diffTotalClasses;
        try {
            //解析整体覆盖率报告
            if(this.totalhtmlreport.exists()) {
                Document document = Jsoup.parse(this.totalhtmlreport, "UTF-8");
                Elements elements = document.select("tfoot").select("td");
                String instructionsStr = elements.get(1).text().replace(",","");
                missedInstructions = instructionsStr.split("of")[0].trim();
                coverageData.setMissedInstructions(missedInstructions);
                totalInstructions = instructionsStr.split("of")[1].trim();
                coverageData.setTotalInstructions(totalInstructions);
                String branchesStr = elements.get(3).text().replace(",","");
                missedBranches = branchesStr.split("of")[0].trim();
                coverageData.setMissedBranches(missedBranches);
                totalBranches = branchesStr.split("of")[1].trim();
                coverageData.setTotalBranches(totalBranches);

                missedCxty = elements.get(5).text().replace(",","");
                coverageData.setMissedCxty(missedCxty);
                totalCxty = elements.get(6).text().replace(",","");
                coverageData.setTotalCxty(totalCxty);
                missedLines = elements.get(7).text().replace(",","");
                coverageData.setMissedLines(missedLines);
                totalLines = elements.get(8).text().replace(",","");
                coverageData.setTotalLines(totalLines);
                missedMethods = elements.get(9).text().replace(",","");
                coverageData.setMissedMethods(missedMethods);
                totalMethods = elements.get(10).text().replace(",","");
                coverageData.setTotalMethods(totalMethods);
                missedClasses = elements.get(11).text().replace(",","");
                coverageData.setMissedClasses(missedClasses);
                totalClasses = elements.get(12).text().replace(",","");
                coverageData.setTotalClasses(totalClasses);

                totalCoverageReportPath = Config.ReportBaseUrl+taskid+"/coveragereport/index.html";
                coverageData.setTotalCoverageReportPath(totalCoverageReportPath);

            }

            //解析差异化覆盖率
            if(this.diffhtmlreport.exists()) {
                Document diffdocument = Jsoup.parse(this.diffhtmlreport, "UTF-8");
                Elements diffelements = diffdocument.select("tfoot").select("td");
                String instructionsStr = diffelements.get(1).text().replace(",","");

                diffMissedInstructions = instructionsStr.split("of")[0].trim();
                coverageData.setDiffMissedInstructions(diffMissedInstructions);
                diffTotalInstructions = instructionsStr.split("of")[1].trim();
                coverageData.setDiffTotalInstructions(diffTotalInstructions);
                String branchesStr = diffelements.get(3).text().replace(",","");
                diffMissedBranches = branchesStr.split("of")[0].trim();
                coverageData.setDiffMissedBranches(diffMissedBranches);
                diffTotalBranches = branchesStr.split("of")[1].trim();
                coverageData.setDiffTotalBranches(diffTotalBranches);

                diffMissedCxty = diffelements.get(5).text().replace(",","");
                coverageData.setDiffMissedCxty(diffMissedCxty);
                diffTotalCxty = diffelements.get(6).text().replace(",","");
                coverageData.setDiffTotalCxty(diffTotalCxty);
                diffMissedLines = diffelements.get(7).text().replace(",","");
                coverageData.setDiffMissedLines(diffMissedLines);
                diffTotalLines = diffelements.get(8).text().replace(",","");
                coverageData.setDiffTotalLines(diffTotalLines);
                diffMissedMethods = diffelements.get(9).text().replace(",","");
                coverageData.setDiffMissedMethods(diffMissedMethods);
                diffTotalMethods = diffelements.get(10).text().replace(",","");
                coverageData.setDiffTotalMethods(diffTotalMethods);
                diffMissedClasses = diffelements.get(11).text().replace(",","");
                coverageData.setDiffMissedClasses(diffMissedClasses);
                diffTotalClasses = diffelements.get(12).text().replace(",","");
                coverageData.setDiffTotalClasses(diffTotalClasses);

                diffCoverageReportPath = Config.ReportBaseUrl+taskid+"/coveragediffreport/index.html";
                coverageData.setDiffCoverageReportPath(diffCoverageReportPath);
            }
            return coverageData;
        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageData;
    }

    public static void main(String[] args){
        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoreport\\20200702102328\\coveragereport\\index.html"),new File("D:\\jacocoreport\\20200702102328\\coveragediffreport\\index.html"));
        System.out.println(jsouphtml.getCoverageData(Long.parseLong("20200702102328")));

    }
}
