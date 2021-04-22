package com.oppo.test.coverage.backend.biz.jacoco;

import com.oppo.test.coverage.backend.model.entity.CoverageData;
import com.oppo.test.coverage.backend.util.SpringContextUtil;
import com.oppo.test.coverage.backend.util.SystemConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author :
 */
public class Jsouphtml {

    private SystemConfig systemConfig;

    private static final Logger logger = LoggerFactory.getLogger(Jsouphtml.class);

    private File totalHtmlReport;
    private File diffHtmlReport;

    private CoverageData coverageData = new CoverageData();


    public Jsouphtml(File totalhtmlreport, File diffhtmlreport) {
        this.totalHtmlReport = totalhtmlreport;
        this.diffHtmlReport = diffhtmlreport;
        this.systemConfig = (SystemConfig) SpringContextUtil.getBean("systemConfig");
    }


    public CoverageData getCoverageData(Long taskId, String appCode, String testedBranch, String basicBranch, Long versionId, String projectName) {
        Long reportId = taskId;
        if (versionId != null && versionId!=0) {
            reportId = versionId;
        }
        coverageData.setId(taskId);
        coverageData.setAppCode(appCode);
        coverageData.setTestedBranch(testedBranch);
        coverageData.setBasicBranch(basicBranch);
        coverageData.setVersionId(versionId);
        try {
            //解析整体覆盖率报告
            if (this.totalHtmlReport.exists()) {
                totalHtmlReportAnalyze(versionId, projectName, reportId);
            }
            //解析差异化覆盖率
            if (this.diffHtmlReport.exists()) {
                diffHtmlReportAnalyze(versionId, projectName, reportId);
            }
            return coverageData;
        } catch (Exception e) {
            logger.error("Jsouphtml error : {} , {}, {} , {} ,{} ,{}", taskId, appCode, totalHtmlReport.getAbsolutePath(), diffHtmlReport.getAbsolutePath(), e.getMessage(), e.getCause());
            e.printStackTrace();
        }
        return coverageData;
    }


    private void totalHtmlReportAnalyze(Long versionId, String projectName, Long reportId) throws IOException {
        Document document = Jsoup.parse(this.totalHtmlReport, "UTF-8");
        if (!document.select("tfoot").isEmpty()) {
            Elements elements = document.select("tfoot").select("td");

            String instructionsStr = elements.get(1).text().replace(",", "");
            coverageData.setMissedInstructions(instructionsStr.split("of")[0].trim());
            coverageData.setTotalInstructions(instructionsStr.split("of")[1].trim());

            String branchesStr = elements.get(3).text().replace(",", "");
            coverageData.setMissedBranches(branchesStr.split("of")[0].trim());
            coverageData.setTotalBranches(branchesStr.split("of")[1].trim());

            coverageData.setMissedCxty(elements.get(5).text().replace(",", ""));
            coverageData.setTotalCxty(elements.get(6).text().replace(",", ""));

            coverageData.setMissedLines(elements.get(7).text().replace(",", ""));
            coverageData.setTotalLines(elements.get(8).text().replace(",", ""));

            coverageData.setMissedMethods(elements.get(9).text().replace(",", ""));
            coverageData.setTotalMethods(elements.get(10).text().replace(",", ""));

            coverageData.setMissedClasses(elements.get(11).text().replace(",", ""));
            coverageData.setTotalClasses(elements.get(12).text().replace(",", ""));

            if (versionId != null && versionId!=0) {
                coverageData.setTotalCoverageReportPath(systemConfig.reportBaseUrl + "projectCovPath/" + projectName + "/" + totalHtmlReport.toString().substring(totalHtmlReport.toString().indexOf(reportId + "")).replace("\\", "/"));
            } else {
                coverageData.setTotalCoverageReportPath(systemConfig.reportBaseUrl + "taskID/" + totalHtmlReport.toString().substring(totalHtmlReport.toString().indexOf(reportId + "")).replace("\\", "/"));
            }
        }
    }

    private void diffHtmlReportAnalyze(Long versionId, String projectName, Long reportId) throws IOException {
        Document diffDocument = Jsoup.parse(this.diffHtmlReport, "UTF-8");
        if (!diffDocument.select("tfoot").isEmpty()) {

            Elements diffElements = diffDocument.select("tfoot").select("td");

            String instructionsStr = diffElements.get(1).text().replace(",", "");

            coverageData.setDiffMissedInstructions(instructionsStr.split("of")[0].trim());
            coverageData.setDiffTotalInstructions(instructionsStr.split("of")[1].trim());

            String branchesStr = diffElements.get(3).text().replace(",", "");
            coverageData.setDiffMissedBranches(branchesStr.split("of")[0].trim());
            coverageData.setDiffTotalBranches(branchesStr.split("of")[1].trim());

            coverageData.setDiffMissedCxty(diffElements.get(5).text().replace(",", ""));
            coverageData.setDiffTotalCxty(diffElements.get(6).text().replace(",", ""));

            coverageData.setDiffMissedLines(diffElements.get(7).text().replace(",", ""));
            coverageData.setDiffTotalLines(diffElements.get(8).text().replace(",", ""));

            coverageData.setDiffMissedMethods(diffElements.get(9).text().replace(",", ""));
            coverageData.setDiffTotalMethods(diffElements.get(10).text().replace(",", ""));

            coverageData.setDiffMissedClasses(diffElements.get(11).text().replace(",", ""));
            coverageData.setDiffTotalClasses(diffElements.get(12).text().replace(",", ""));

            if (versionId != null && versionId!=0) {
                coverageData.setDiffCoverageReportPath(systemConfig.reportBaseUrl + "projectCovPath/" + projectName + "/" + diffHtmlReport.toString().substring(diffHtmlReport.toString().indexOf(reportId + "")).replace("\\", "/"));
            } else {
                coverageData.setDiffCoverageReportPath(systemConfig.reportBaseUrl + "taskID/" + diffHtmlReport.toString().substring(diffHtmlReport.toString().indexOf(reportId + "")).replace("\\", "/"));
            }

        }
    }

    public static void main(String[] args) {
//        Jsouphtml jsouphtml = new Jsouphtml(new File("D:\\jacocoreport\\20200702102328\\coveragereport\\index.html"),new File("D:\\jacocoreport\\20200702102328\\coveragediffreport\\index.html"));
        String diffhtmlreport = "D:\\codeCoverage\\taskID\\10010\\branchcoverage\\release_fin-2.3\\totalcoveragereport\\index.html";
        Long taskid = 10010L;
        System.out.println(diffhtmlreport.substring(diffhtmlreport.indexOf(taskid + "")).replace("\\", "/"));

    }
}
