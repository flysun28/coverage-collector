package com.oppo.jacocoreport.coverage.utils;

import com.oppo.jacocoreport.coverage.entity.BranchCoverageData;
import com.oppo.jacocoreport.coverage.entity.CoverageData;
import com.oppo.jacocoreport.coverage.entity.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;

public class Jsouphtml {
    private File totalhtmlreport;
    private File diffhtmlreport;
    String totalCoverageReportPath = "";
    String diffCoverageReportPath = "";

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

    public Jsouphtml(File totalhtmlreport,File diffhtmlreport){
      this.totalhtmlreport = totalhtmlreport;
      this.diffhtmlreport = diffhtmlreport;
    }

//    public BranchCoverageData getBranchCoverageData(Long taskid,int isBranchTask,String appCode,String testedBranch,String basicBranch){
//        CoverageData coverageData = getCoverageData(taskid,isBranchTask);
//        BranchCoverageData branchCoverageData = new BranchCoverageData(coverageData);
//        branchCoverageData.setAppCode(appCode);
//        branchCoverageData.setTestedBranch(testedBranch);
//        branchCoverageData.setBasicBranch(basicBranch);
//        return branchCoverageData;
//    }
    public CoverageData getCoverageData(Long taskid,String appCode,String testedBranch,String basicBranch){
        CoverageData coverageData = new CoverageData();
        coverageData.setId(taskid);
        try {
            //解析整体覆盖率报告
            if(this.totalhtmlreport.exists()) {
                Document document = Jsoup.parse(this.totalhtmlreport, "UTF-8");
                if(!document.select("tfoot").isEmpty()) {
                    Elements elements = document.select("tfoot").select("td");
                    String instructionsStr = elements.get(1).text().replace(",", "");
                    missedInstructions = instructionsStr.split("of")[0].trim();
                    coverageData.setMissedInstructions(missedInstructions);
                    totalInstructions = instructionsStr.split("of")[1].trim();
                    coverageData.setTotalInstructions(totalInstructions);
                    String branchesStr = elements.get(3).text().replace(",", "");
                    missedBranches = branchesStr.split("of")[0].trim();
                    coverageData.setMissedBranches(missedBranches);
                    totalBranches = branchesStr.split("of")[1].trim();
                    coverageData.setTotalBranches(totalBranches);

                    missedCxty = elements.get(5).text().replace(",", "");
                    coverageData.setMissedCxty(missedCxty);
                    totalCxty = elements.get(6).text().replace(",", "");
                    coverageData.setTotalCxty(totalCxty);
                    missedLines = elements.get(7).text().replace(",", "");
                    coverageData.setMissedLines(missedLines);
                    totalLines = elements.get(8).text().replace(",", "");
                    coverageData.setTotalLines(totalLines);
                    missedMethods = elements.get(9).text().replace(",", "");
                    coverageData.setMissedMethods(missedMethods);
                    totalMethods = elements.get(10).text().replace(",", "");
                    coverageData.setTotalMethods(totalMethods);
                    missedClasses = elements.get(11).text().replace(",", "");
                    coverageData.setMissedClasses(missedClasses);
                    totalClasses = elements.get(12).text().replace(",", "");
                    coverageData.setTotalClasses(totalClasses);

                    totalCoverageReportPath = Config.ReportBaseUrl + taskid +"/"+ totalhtmlreport.getParent().toString()+"/"+totalhtmlreport.getName().toString();
                    coverageData.setTotalCoverageReportPath(totalCoverageReportPath);
                }

            }

            //解析差异化覆盖率
            if(this.diffhtmlreport.exists()) {
                Document diffdocument = Jsoup.parse(this.diffhtmlreport, "UTF-8");
                if(!diffdocument.select("tfoot").isEmpty()) {
                    Elements diffelements = diffdocument.select("tfoot").select("td");
                    String instructionsStr = diffelements.get(1).text().replace(",", "");

                    diffMissedInstructions = instructionsStr.split("of")[0].trim();
                    coverageData.setDiffMissedInstructions(diffMissedInstructions);
                    diffTotalInstructions = instructionsStr.split("of")[1].trim();
                    coverageData.setDiffTotalInstructions(diffTotalInstructions);
                    String branchesStr = diffelements.get(3).text().replace(",", "");
                    diffMissedBranches = branchesStr.split("of")[0].trim();
                    coverageData.setDiffMissedBranches(diffMissedBranches);
                    diffTotalBranches = branchesStr.split("of")[1].trim();
                    coverageData.setDiffTotalBranches(diffTotalBranches);

                    diffMissedCxty = diffelements.get(5).text().replace(",", "");
                    coverageData.setDiffMissedCxty(diffMissedCxty);
                    diffTotalCxty = diffelements.get(6).text().replace(",", "");
                    coverageData.setDiffTotalCxty(diffTotalCxty);
                    diffMissedLines = diffelements.get(7).text().replace(",", "");
                    coverageData.setDiffMissedLines(diffMissedLines);
                    diffTotalLines = diffelements.get(8).text().replace(",", "");
                    coverageData.setDiffTotalLines(diffTotalLines);
                    diffMissedMethods = diffelements.get(9).text().replace(",", "");
                    coverageData.setDiffMissedMethods(diffMissedMethods);
                    diffTotalMethods = diffelements.get(10).text().replace(",", "");
                    coverageData.setDiffTotalMethods(diffTotalMethods);
                    diffMissedClasses = diffelements.get(11).text().replace(",", "");
                    coverageData.setDiffMissedClasses(diffMissedClasses);
                    diffTotalClasses = diffelements.get(12).text().replace(",", "");
                    coverageData.setDiffTotalClasses(diffTotalClasses);

                    diffCoverageReportPath = Config.ReportBaseUrl + taskid +"/"+diffhtmlreport.getParent().toString()+"/"+diffhtmlreport.getName().toString();
                    coverageData.setDiffCoverageReportPath(diffCoverageReportPath);
                }
            }
            coverageData.setAppCode(appCode);
            coverageData.setTestedBranch(testedBranch);
            coverageData.setBasicBranch(basicBranch);
            return coverageData;
        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageData;
    }
    /**
     * 上传覆盖率报告
     */
    public static void sendcoveragedata(){
        File coverageReportPath = new File("D:\\jacocoCov\\9");
        try {
            CoverageData coverageData = new CoverageData();
            File coveragereport = new File(coverageReportPath, "coveragereport");
            if (coveragereport.exists()) {
                coveragereport = new File(coveragereport, "index.html");
            }
            File diffcoveragereport = new File(coverageReportPath, "coveragediffreport");
            if (diffcoveragereport.exists()) {
                diffcoveragereport = new File(diffcoveragereport, "index.html");
            }

            Jsouphtml jsouphtml = new Jsouphtml(coveragereport, diffcoveragereport);
            coverageData = jsouphtml.getCoverageData(9L,"","","");
            System.out.println(coverageData.toString());
            String requstUrl = Config.SEND_COVERAGE_URL;
            Data data = HttpUtils.sendPostRequest(requstUrl, coverageData);
            System.out.println("send coveragedata" + data.getCode());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
//        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoreport\\20200702102328\\coveragereport\\index.html"),new File("D:\\jacocoreport\\20200702102328\\coveragediffreport\\index.html"));
        sendcoveragedata();

    }
}
