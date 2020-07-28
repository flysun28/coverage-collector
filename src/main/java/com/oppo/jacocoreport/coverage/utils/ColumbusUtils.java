package com.oppo.jacocoreport.coverage.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.oppo.jacocoreport.coverage.cloud.AppDeployInfo;
import com.oppo.jacocoreport.coverage.cloud.AppVersionResponse;
import com.oppo.jacocoreport.response.DefinitionException;
import com.oppo.jacocoreport.response.ErrorEnum;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.*;

public class ColumbusUtils {
    private static String url = "http://columbus.os.adc.com";
    private static String download_version_url = "http://ocs-cn-south.oppoer.me";
    private static String API_VERSION_INFO = "/openapi/version_info";
    private static String app_code = "";
    private static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";


    public static ArrayList<AppVersionResponse> getBuildVersionList(String appId,String buildVersionName) {
        Map<String, String> params = new HashMap<>();
        params.put("ts", String.valueOf(System.currentTimeMillis()));
        params.put("app_code", appId);
        //取测试环境版本
        params.put("env", "test");

        String sortedParams = sortParams(params);
        List<AppVersionResponse> appVersionResponses = null;
        ArrayList<AppVersionResponse> appBranchResponses = new ArrayList<>();

        try {
            String signature = HMAC_MD5_encode("123456789", sortedParams);
            String ret = null;
            ret = HttpUtils.sendGet(url + API_VERSION_INFO + "?" + sortedParams + "&signature=" + signature + "&pageNum=1&pageSize=20");
            appVersionResponses = new Gson().fromJson(ret, new TypeToken<List<AppVersionResponse>>() {
            }.getType());
            for (AppVersionResponse appVersionResponse : appVersionResponses) {
                if (buildVersionName.equals(appVersionResponse.getVersionName())) {
                    appBranchResponses.add(appVersionResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回当期测试版本列表数据
        return appBranchResponses;
    }

    /**
     * 参数排序(按key升序排)
     *
     * @param params
     * @return
     */
    public static String sortParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) throw new RuntimeException("params can't be empty");
        List<String> keyList = new ArrayList<>(params.keySet());
        Collections.sort(keyList);
        StringBuilder sb = new StringBuilder();
        for (String key : keyList) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }

        return sb.toString().substring(0, sb.length() - 1);
    }

    public static String HMAC_MD5_encode(String appsecret, String message) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
                appsecret.getBytes(),
                "HmacMD5"
        );
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(keySpec);

        byte[] rawHmac = mac.doFinal(message.getBytes());
        return Hex.encodeHexString(rawHmac);
    }

    public static StringBuffer getAppDeployInfoList(String versionName) throws DefinitionException {
        ArrayList<AppDeployInfo> appDeployInfos = new ArrayList<AppDeployInfo>();
        StringBuffer iplist = new StringBuffer();
        try {
            String ret = null;
            Gson gson = new Gson();
            ret = HttpUtils.sendGet(CLOUD_URL + versionName);
            JsonObject obj = gson.fromJson(ret, JsonObject.class);
            JsonObject listJsonObject = obj.get("data").getAsJsonObject().get("list").getAsJsonObject();
            if (null != listJsonObject && !"null".equals(listJsonObject.get("result").toString())) {
                JsonArray jsonArray = listJsonObject.getAsJsonArray("result");
                appDeployInfos = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<AppDeployInfo>>() {
                }.getType());
            }
            for(AppDeployInfo appDeployInfo: appDeployInfos){
                iplist.append(appDeployInfo.getIp());
                iplist.append(",");

            }
            if(iplist.toString().equals("")){
                System.out.println("test environment ip is null");
                throw new DefinitionException(ErrorEnum.GET_EVIRONMENTIP.getErrorCode(),ErrorEnum.GET_EVIRONMENTIP.getErrorMsg());
            }
            return iplist;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.GET_EVIRONMENTIP.getErrorCode(),ErrorEnum.GET_EVIRONMENTIP.getErrorMsg());
        }
    }

    public static HashMap getAppDeployInfoFromBuildVersionList(String appID, String buildVersionName) {
        StringBuffer applicationIP = new StringBuffer();
        HashMap<String,Object> hashMap = new HashMap<>();
        String commitID = "";
        String buildBranch = "";
        String repositoryUrl = "";
        ArrayList<AppVersionResponse> appVersionList = ColumbusUtils.getBuildVersionList(appID, buildVersionName);
        if (appVersionList.size() > 0) {
            for(AppVersionResponse appVersionResponse :appVersionList){
                System.out.println(appVersionResponse.getSourceBranch());
                System.out.println(appVersionResponse.getCommitId());
            }
            commitID = appVersionList.get(0).getCommitId();
            buildBranch = appVersionList.get(0).getSourceBranch();
            repositoryUrl = appVersionList.get(0).getRepositoryUrl();


        }
        applicationIP = ColumbusUtils.getAppDeployInfoList(buildVersionName);
        hashMap.put("applicationIP",applicationIP.toString());
        hashMap.put("commitID",commitID);
        hashMap.put("buildBranch",buildBranch);
        hashMap.put("repositoryUrl",repositoryUrl);
        return hashMap;
    }
    public static String downloadColumbusBuildVersion(String repositoryUrl,String downloadPath){
        String fileName = "";
        File downloadFilePath = null;
        try {
            Map<String, String > headers = new HashMap<>();
            String nonce =  String.valueOf(new Random().nextInt(10000));
            String curTime = String.valueOf((new Date()).getTime() / 1000L);
            // 设置请求的header
            headers.put("Nonce", nonce);
            headers.put("CurTime", curTime);
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            fileName = repositoryUrl.substring(repositoryUrl.lastIndexOf("/")+1);
            String downloadUrl = download_version_url+"/"+repositoryUrl;
            System.out.println(downloadUrl);
            downloadFilePath = new File(downloadPath,"downloadzip");
            if(!downloadFilePath.exists()){
                downloadFilePath.mkdir();
            }
            downloadFilePath = new File(downloadFilePath,fileName);
            if(!downloadFilePath.exists()) {
//                HttpClientUtils.getInstance().download(
//                        downloadUrl, downloadFilePath.toString(),
//                        new HttpClientUtils.HttpClientDownLoadProgress() {
//
//                            @Override
//                            public void onProgress(int progress) {
////                            System.out.println("download progress = " + progress);
//                            }
//
//                        }, headers);
                HttpClientUtils.getInstance().httpDownloadFile(downloadUrl, downloadFilePath.toString(),null,headers);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return downloadFilePath.toString();
    }
    public static  File extractColumsBuildVersionClasses(String downloadZipFile,String targetPath,String applicationID,Map<String ,Map> applicationsrclist){
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String basicPath = new File(downloadZipFile).getParentFile().getAbsolutePath();
        if("".equals(downloadZipFile) || downloadZipFile == null){
            System.out.println("please input extract path:");
        }
        fileOperateUtil.delAllFile(targetPath);

        System.out.println("extract path:"+basicPath);

        Execute execute = new Execute();

        File zipfile = new File(downloadZipFile);
        String resultPath = execute.extractFile(zipfile);
        fileOperateUtil.copyFolder(resultPath+"\\lib",targetPath);
        File targetPathFolder = new File(targetPath);
        File[] files = targetPathFolder.listFiles();
        File classFile = null;

        if(files != null){
            int filesNum = files.length;
            for(int i =0;i<filesNum;i++) {
                String fileName = files[i].getName();
                if (!fileName.endsWith("sources.jar")&& fileName.endsWith(".jar")) {
                    String classPath = execute.extractFile(files[i]);
                    File baseclassFile = new File(classPath, "BOOT-INF");
                    classFile = new File(baseclassFile, "classes");
                    String buildversionprex = fileName.substring(applicationID.length());
                    for(String applicationsrcname :applicationsrclist.keySet()){
                        if(!applicationID.equals(applicationsrcname)) {
                            String dependentjar = applicationsrcname + buildversionprex;
                            String dependentPathJar = classFile + File.separator + dependentjar;
                            fileOperateUtil.copyFile(baseclassFile + File.separator + "lib" + File.separator + dependentjar, dependentPathJar);
                            fileOperateUtil.unZipFiles(dependentPathJar,classFile.toString(),true);
                        }
                    }
                    fileOperateUtil.delAllFile(baseclassFile + File.separator + "lib");
                }
            }

            fileOperateUtil.delAllFile(basicPath);
        }
        return classFile;
    }
    public static void main(String[] args) throws Exception {
        ColumbusUtils.getAppDeployInfoList("pandora-server-web_20200604145728");
//        ColumbusUtils.getApplicationIP("pandora-server-web_20200604145728","test2");

    }
}
