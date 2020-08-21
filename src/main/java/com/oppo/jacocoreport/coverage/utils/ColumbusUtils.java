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
import java.util.regex.Matcher;

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
            ret = HttpUtils.sendGet(url + API_VERSION_INFO + "?" + sortedParams + "&signature=" + signature + "&pageNum=1&pageSize=50");
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

    public static HashMap getAppDeployInfoFromBuildVersionList(String appID, String buildVersionName) throws DefinitionException {
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

            applicationIP = ColumbusUtils.getAppDeployInfoList(buildVersionName);
            hashMap.put("applicationIP",applicationIP.toString());
            hashMap.put("commitID",commitID);
            hashMap.put("buildBranch",buildBranch);
            hashMap.put("repositoryUrl",repositoryUrl);
        }else{
            throw new DefinitionException(ErrorEnum.GETDOWNLOADPACKAGE_RAILED.getErrorCode(),ErrorEnum.GETDOWNLOADPACKAGE_RAILED.getErrorMsg());
        }

        return hashMap;
    }

    public static void filterIgnoreClass(String[] classArrayList,String[] packageArrayList,File basePath){
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        for(String classname:classArrayList){
            String classPath = classname.replaceAll("\\.", Matcher.quoteReplacement(File.separator))+".class";
            new File(basePath,classPath).delete();
        }
        for(String packagename:packageArrayList){
            String packagenamePath = packagename.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            fileOperateUtil.delAllFile(new File(basePath,packagenamePath).toString());
        }
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
    public static String getdeployJarPrefix(String doanloadZipFile){
        String[] applicationprelist = doanloadZipFile.split("-");
        String applicationpre = "";
        if(applicationprelist.length > 2){
            if(applicationprelist[1].matches("\\d*")){
                applicationpre = applicationprelist[0];
            }else{
                applicationpre = applicationprelist[0]+"-"+applicationprelist[1];
            }
        }else if(applicationprelist.length == 2){
            applicationpre = applicationprelist[0];
        }
        System.out.println("prefix "+applicationpre);
        return applicationpre;
    }
    public static  String extractColumsBuildVersionClasses(String downloadZipFile,String targetPath,String applicationID,Map<String ,Map> applicationsrclist) throws Exception{
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String basicPath = new File(downloadZipFile).getParentFile().toString();
        if("".equals(downloadZipFile) || downloadZipFile == null){
            System.out.println("please input extract path:");
        }
        fileOperateUtil.delAllFile(targetPath);

        System.out.println("extract path:"+basicPath);

        Execute execute = new Execute();

        File zipfile = new File(downloadZipFile);
        String deployJarprefix = getdeployJarPrefix(zipfile.getName());
        String resultPath = execute.extractFile(zipfile);
        ArrayList<File> packageList = new ArrayList<File>();
        //遍历所有文件夹，找出应用的jar包，并解压
        boolean existJar = extractJartoClass(resultPath,basicPath,deployJarprefix,applicationsrclist,applicationID);
        //再对解压的文件夹里，遍历解压一次
        if(existJar) {
            extractJartoClass(basicPath, basicPath, "",applicationsrclist,applicationID);
            packageList = getComPackagePath(new File(basicPath), packageList);
        }else{
            packageList = getComPackagePath(new File(resultPath), packageList);
        }

        for(File packagePath: packageList){
            fileOperateUtil.copyFolder(packagePath.toString(),targetPath);
        }
//        zipfile.delete();
//        fileOperateUtil.delAllFile(resultPath);

        return targetPath;
    }
    public static boolean extractJartoClass(String localpath,String targetPath,String deployJarprefix,Map<String ,Map> applicationsrclist,String applicationID){
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        Execute execute = new Execute();
        boolean existJar = false;
        File applicationJarPath = null;
        for(String applicationsrcname :applicationsrclist.keySet()){
            applicationJarPath = getapplicationJarPath(new File(localpath),applicationsrcname);
            if(applicationJarPath!=null) {
                existJar = true;
                fileOperateUtil.copyFile(applicationJarPath.toString(), targetPath+File.separator+applicationJarPath.getName());
                execute.extractFiles(targetPath);
            }
        }
        //如果没有找到jar包，通过压缩包前缀再搜索一次
        if(!existJar && !deployJarprefix.equals("")){
            applicationJarPath = getapplicationJarPath(new File(localpath),deployJarprefix);
            if(applicationJarPath!=null) {
                existJar = true;
                fileOperateUtil.copyFile(applicationJarPath.toString(), targetPath+File.separator+applicationJarPath.getName());
                execute.extractFiles(targetPath);
            }
        }
        //还没有找到jar包，再通过应用前缀再搜索一次
        if(!existJar){
            applicationJarPath = getapplicationJarPath(new File(localpath),applicationID.substring(0,applicationID.lastIndexOf("-")));
            if(applicationJarPath!=null){
                existJar = true;
                fileOperateUtil.copyFile(applicationJarPath.toString(), targetPath+File.separator+applicationJarPath.getName());
                execute.extractFiles(targetPath);
            }
        }
        //还没有找到jar包，再通过应用前缀再搜索一次
        if(!existJar){
            System.out.println(applicationID.substring(applicationID.indexOf("-")+1));
            applicationJarPath = getapplicationJarPath(new File(localpath),applicationID.substring(applicationID.indexOf("-")+1));
            if(applicationJarPath!=null){
                existJar = true;
                fileOperateUtil.copyFile(applicationJarPath.toString(), targetPath+File.separator+applicationJarPath.getName());
                execute.extractFiles(targetPath);
            }else{
                System.out.println("application postfix find jar failed");
            }
        }
        return existJar;
    }
    private static String getApplicationIDJarName(File filePath){
        String folderName = filePath.getName();
        return folderName.substring(0,folderName.indexOf("SNAPSHOT")+8)+".jar";
    }
    public static File getapplicationJarPath(File extractPath,String dependentjarname){
        File[] fileList = extractPath.listFiles();
        File denpentjarpath = null;
        //遍历代码工程
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //如果当前文件夹名== src
                denpentjarpath = getapplicationJarPath(f,dependentjarname);
                if(denpentjarpath != null){
                    return denpentjarpath;
                }
            }
            else{
                if(f.getName().contains(dependentjarname) && f.getName().endsWith(".jar") && !f.getName().endsWith("sources.jar")){
                    return f;
                }
            }
        }
        return denpentjarpath;
    }
    private static ArrayList getComPackagePath(File localPath,ArrayList<File> packageList){
        File[] fileList = localPath.listFiles();
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //如果当前文件夹名== com
                if("com".equals(f.getName())){
                    packageList.add(f.getParentFile());
                    return packageList;
                }else{
                    getComPackagePath(f,packageList);
                }
            }
        }
        return packageList;

    }
    public static void main(String[] args) throws Exception {
//        ColumbusUtils.getAppDeployInfoList("pandora-server-web_20200604145728");
//        ColumbusUtils.getApplicationIP("pandora-server-web_20200604145728","test2");
//        ArrayList<AppVersionResponse> appVersionResponses = getBuildVersionList("fin-wealth-api","fin-wealth-api_20200723141019");
//       for(AppVersionResponse appVersionResponse: appVersionResponses){
//           System.out.println(appVersionResponse.getRepositoryUrl());
//       }
//        getdeployJarPrefix("fin-20200721_0251-bin-20200721-7675751.zip");
        String downloadFilePath = downloadColumbusBuildVersion("http://ocs-cn-south.oppoer.me/columbus-file-repo/columbus-repo-202008/combine_844869-20200820-8448691.zip","D:\\execfile");
        extractColumsBuildVersionClasses(downloadFilePath,"D:\\execfile\\classes","annotate-data-product-service",new HashMap<>());
    }
}
