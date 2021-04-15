package com.oppo.test.coverage.backend.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.oppo.test.coverage.backend.model.constant.ErrorEnum;
import com.oppo.test.coverage.backend.model.response.AppDeployInfo;
import com.oppo.test.coverage.backend.model.response.AppVersionResponse;
import com.oppo.test.coverage.backend.model.response.DefinitionException;
import com.oppo.test.coverage.backend.util.file.FileExtractUtil;
import com.oppo.test.coverage.backend.util.file.FileOperateUtil;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jgit.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

public class ColumbusUtils {
    private static String url = "http://columbus.oppoer.me";
    private static String download_version_url = "http://ocs-cn-south.oppoer.me";
    private static String API_VERSION_INFO = "/openapi/version_info";
    private static String app_code = "";
    private static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";
    private static String CLOUD_URL_PROD = "http://prod-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";


    public static ArrayList<AppVersionResponse> getBuildVersionList(String appId, String buildVersionName) {
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
            ret = HttpUtils.sendGet(url + API_VERSION_INFO + "?" + sortedParams + "&signature=" + signature + "&pageNum=1&pageSize=100&fuzzyQuery=0&versionName=");
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

    public static StringBuffer getAppDeployInfoList(String versionName, Integer testedEnv) throws DefinitionException {
        ArrayList<AppDeployInfo> appDeployInfos = new ArrayList<AppDeployInfo>();
        StringBuffer iplist = new StringBuffer();
        try {
            String ret = null;
            Gson gson = new Gson();
            if (testedEnv == 2) {
                ret = HttpUtils.sendGet(CLOUD_URL_PROD + versionName);
            } else {
                ret = HttpUtils.sendGet(CLOUD_URL + versionName);
            }
            JsonObject obj = gson.fromJson(ret, JsonObject.class);
            JsonObject listJsonObject = obj.get("data").getAsJsonObject().get("list").getAsJsonObject();
            if (null != listJsonObject && !"null".equals(listJsonObject.get("result").toString())) {
                JsonArray jsonArray = listJsonObject.getAsJsonArray("result");
                appDeployInfos = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<AppDeployInfo>>() {
                }.getType());
            }
            for (AppDeployInfo appDeployInfo : appDeployInfos) {
                iplist.append(appDeployInfo.getIp());
                iplist.append(",");

            }
            if (iplist.toString().equals("")) {
                System.out.println("test environment ip is null");
                throw new DefinitionException(ErrorEnum.GET_EVIRONMENTIP.getErrorCode(), ErrorEnum.GET_EVIRONMENTIP.getErrorMsg());
            }
            return iplist;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.GET_EVIRONMENTIP.getErrorCode(), ErrorEnum.GET_EVIRONMENTIP.getErrorMsg());
        }
    }

    public static HashMap<String, Object> getAppDeployInfoFromBuildVersionList(String appId, String buildVersionName, Integer testedEnv) throws DefinitionException {
        StringBuffer applicationIp;
        HashMap<String, Object> hashMap = new HashMap<>();
        String commitID;
        String buildBranch;
        String repositoryUrl;
        ArrayList<AppVersionResponse> appVersionList = ColumbusUtils.getBuildVersionList(appId, buildVersionName);
        if (appVersionList.size() > 0) {
            for (AppVersionResponse appVersionResponse : appVersionList) {
                System.out.println(appVersionResponse.getSourceBranch());
                System.out.println(appVersionResponse.getCommitId());
            }
            commitID = appVersionList.get(0).getCommitId();
            buildBranch = appVersionList.get(0).getSourceBranch();
            repositoryUrl = appVersionList.get(0).getRepositoryUrl();

            applicationIp = ColumbusUtils.getAppDeployInfoList(buildVersionName, testedEnv);
            hashMap.put("applicationIP", applicationIp.toString());
            hashMap.put("commitID", commitID);
            hashMap.put("buildBranch", buildBranch);
            hashMap.put("repositoryUrl", repositoryUrl);
        } else {
            throw new DefinitionException(ErrorEnum.GETDOWNLOADPACKAGE_RAILED.getErrorCode(), ErrorEnum.GETDOWNLOADPACKAGE_RAILED.getErrorMsg());
        }

        return hashMap;
    }

    public static void filterIgnoreClass(String[] classArrayList, File basePath) {

        if (classArrayList == null || classArrayList.length < 1) {
            return;
        }

        for (String classname : classArrayList) {
            if (classname.contains(".")) {
                String classPathLastStr = classname.substring(classname.lastIndexOf(".") + 1);
                String classParentpathStr = classname.substring(0, classname.lastIndexOf("."));
                String classParentpath = classParentpathStr.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                File classParentPath = new File(basePath, classParentpath);
                if (classPathLastStr.endsWith("*")) {
                    String prefix = classPathLastStr.substring(0, classPathLastStr.indexOf("*"));
                    if (classParentPath.exists()) {
                        File[] listFile = classParentPath.listFiles();
                        for (File classFile : listFile) {
                            if (classFile.getName().startsWith(prefix) && classFile.exists()) {
                                classFile.delete();
                            }
                        }
                    }
                } else {
                    File classfilename = new File(classParentPath, classPathLastStr + ".class");
                    if (classfilename.exists()) {
                        classfilename.delete();
                    }
                }
            }
        }
    }

    public static void filterIgnorePackage(String[] packageArrayList, File basePath) {
        for (String packageName : packageArrayList) {
            if (packageName.contains(".")) {
                String packageNameLastStr = packageName.substring(packageName.lastIndexOf(".") + 1);
                String packageParentNameStr = packageName.substring(0, packageName.lastIndexOf("."));

                String packageParentNamePath = packageParentNameStr.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                File packageNameParentFile = new File(basePath, packageParentNamePath);
                if (packageNameLastStr.endsWith("*")) {
                    if (packageNameParentFile.exists()) {
                        for (File packageFile : packageNameParentFile.listFiles()) {
                            String prefix = packageNameLastStr.substring(0, packageNameLastStr.indexOf("*"));
                            if (packageFile.getName().contains(prefix) && packageFile.exists()) {
                                FileOperateUtil.delAllFile(packageFile.toString());
                            }
                        }

                    }
                } else {
                    if (new File(packageNameParentFile, packageNameLastStr).exists()) {
                        FileOperateUtil.delAllFile(new File(packageNameParentFile, packageNameLastStr).toString());
                    }
                }
            } else {
                if (!"".equals(packageName) && new File(basePath, packageName).exists()) {
                    FileOperateUtil.delAllFile(new File(basePath, packageName).toString());
                }
            }
        }
    }

    public static HashSet getcontainPackageHashSet(String[] containPackageList, String localPath) {
        HashSet containpackageSet = new HashSet();
        for (String packagename : containPackageList) {
            String packagenamePath = packagename.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            String packagPath = localPath + File.separator + packagenamePath;
            if (new File(packagPath).exists()) {
                containpackageSet.add(packagenamePath);
            } else if (packagename.contains("*")) {
                containpackageSet.add(packagenamePath);
            }
        }
        return containpackageSet;
    }

    public static int packageContainPath(HashSet containpackageSet, File basePath) {
        int containresult = 2; //0,路径中包含包名 1,路径完全匹配包名 2 路径不包含包名
        if (containpackageSet == null) {
            return containresult;
        }

        String path = basePath.getPath();
        if (!path.contains("com")) {
            return containresult;
        }
        path = path.substring(path.indexOf("com"));
        Iterator iterator = containpackageSet.iterator();
        while (iterator.hasNext()) {
            String containpackagename = iterator.next().toString();
            if (containpackagename.contains("*")) {
                containpackagename = containpackagename.substring(0, containpackagename.indexOf("*"));
            }
            if (containpackagename.equals(path)) {
                containresult = 1;
                return containresult;
            } else if (containpackagename.contains(path)) {
                containresult = 0;
                return containresult;
            }
        }
        return containresult;
    }

    public static void filterContainPackages(HashSet containPackageSet, File basePath) {
        if (basePath.isDirectory()) {
            File[] fileList = basePath.listFiles();
            if (fileList == null) {
                return;
            }
            //遍历代码工程
            for (File f : fileList) {
                //判断是否文件夹目录
                if (f.isDirectory()) {
                    int containResult = packageContainPath(containPackageSet, f);
                    //如果不是指定包名
                    if (containResult == 2) {
                        FileOperateUtil.delAllFile(f.toString());
                    }
                    //如果包含包名
                    else if (containResult == 0) {
                        filterContainPackages(containPackageSet, f);
                    }
                    //如果等于包名
                    else if (containResult == 1) {
                        //暂不处理
                    }
                } else {
                    int result = packageContainPath(containPackageSet, new File(f.getPath()));
                    if (result != 1) {
                        System.out.println(f.toString());
                        f.delete();
                    }
                }
            }
        }
    }


    public static String downloadColumbusBuildVersion(String repositoryUrl, String downloadPath) throws Exception {
        String fileName = "";
        File downloadFilePath = null;
        Map<String, String> headers = new HashMap<>();
        String nonce = String.valueOf(new Random().nextInt(10000));
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        // 设置请求的header
        headers.put("Nonce", nonce);
        headers.put("CurTime", curTime);
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        fileName = repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1);
        String downloadUrl = download_version_url + "/" + repositoryUrl;
        System.out.println(downloadUrl);
        try {
            downloadFilePath = new File(downloadPath, "downloadzip");
            if (!downloadFilePath.exists()) {
                downloadFilePath.mkdir();
            }
            downloadFilePath = new File(downloadFilePath, fileName);
            if (!downloadFilePath.exists()) {
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
                HttpUtils.httpDownloadFile(downloadUrl, downloadFilePath.toString(), headers);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(10000);
            try {
                HttpUtils.httpDownloadFile(downloadUrl, downloadFilePath.toString(), headers);
            } catch (Exception en) {
                en.printStackTrace();
                throw new DefinitionException(ErrorEnum.DOWNLOAD_BUILDVERSION_FAILED.getErrorCode(), ErrorEnum.DOWNLOAD_BUILDVERSION_FAILED.getErrorMsg());
            }
        }
        return downloadFilePath.toString();
    }

    public static String getdeployJarPrefix(String doanloadZipFile) {
        String applicationpre = "";
        if (doanloadZipFile.contains("bin")) {
            applicationpre = doanloadZipFile.substring(0, doanloadZipFile.indexOf("bin") - 1);
            return applicationpre;
        }
        int numbeginindex = getNumIndexFormStr(doanloadZipFile);
        if (numbeginindex == -1) {
            applicationpre = doanloadZipFile;
        } else {
            applicationpre = doanloadZipFile.substring(0, numbeginindex);
            if (applicationpre.substring(numbeginindex - 1, numbeginindex).equals("-") || applicationpre.substring(numbeginindex - 1, numbeginindex).equals("_")) {
                applicationpre = doanloadZipFile.substring(0, numbeginindex - 1);
            }
        }

        System.out.println("prefix " + applicationpre);
        return applicationpre;
    }

    private static int getNumIndexFormStr(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                return i;
            }
        }
        return -1;
    }

    private static String getJarPackageVersion(File jarPackage) {
        int numbeginindex = getNumIndexFormStr(jarPackage.getName());
        if (numbeginindex == -1) {
            return "";
        }
        int numlastindex = jarPackage.getName().indexOf("-", numbeginindex);
        if (numlastindex == -1) {
            numlastindex = jarPackage.getName().indexOf(".jar");
        }
        return jarPackage.getName().substring(numbeginindex, numlastindex);

    }

    private static String getJarPackagePre(File jarPackage) {
        int numbeginindex = getNumIndexFormStr(jarPackage.getName());
        if (numbeginindex == -1) {
            if (jarPackage.getName().contains("SNAPSHOT")) {
                return jarPackage.getName().substring(0, jarPackage.getName().indexOf("SNAPSHOT") - 1);
            } else {
                return jarPackage.getName().substring(0, jarPackage.getName().indexOf(".") - 1);
            }
        } else {
            return jarPackage.getName().substring(0, numbeginindex - 1);
        }

    }

    public static String getApplicationIDPrefix(String applicationID) {
        String applicationIDPrex = "";
        if (applicationID.contains("-")) {
            applicationIDPrex = applicationID.substring(0, applicationID.lastIndexOf("-"));

        } else {
            applicationIDPrex = applicationID;
        }
        return applicationIDPrex;
    }

    public static String extractColumbusBuildVersionClasses(String downloadZipFile, String targetPath, String applicationID, Map sourceApplicationsMap) throws Exception {
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        String basicPath = new File(downloadZipFile).getParentFile().toString();
        if ("".equals(downloadZipFile)) {
            System.out.println("please input extract path:");
        }
        FileOperateUtil.delAllFile(targetPath);

        System.out.println("extract path:" + basicPath);

        File zipFile = new File(downloadZipFile);
        String deployJarPrefix = getdeployJarPrefix(zipFile.getName());
        String resultPath = FileExtractUtil.extractFile(zipFile);
        ArrayList<File> packageList = new ArrayList<>();

        HashSet jarPackageSet = new HashSet();
        //先替换下划线
        applicationID = applicationID.replaceAll("_", "-");
        String jarPackagePath = basicPath + File.separator + "jarpackage";
        if (!new File(jarPackagePath).exists()) {
            new File(jarPackagePath).mkdir();
        }

        //遍历所有文件夹，找出应用的jar包，并解压
        jarPackageSet = extractJartoClass2(resultPath, jarPackagePath, deployJarPrefix, sourceApplicationsMap, applicationID);
//        fileOperateUtil.delAllFile(resultPath);
        //再对解压的文件夹里，遍历解压一次
        if (jarPackageSet.size() > 0) {
            extractJartoClass2(jarPackagePath, jarPackagePath, "", sourceApplicationsMap, applicationID);
            packageList = getComPackagePath(new File(jarPackagePath), packageList);
        } else {
            packageList = getComPackagePath(new File(resultPath), packageList);
        }
        if (deployJarPrefix.contains("tomcat")) {
            packageList = getComPackagePath(new File(resultPath), packageList);
        }

        for (File packagePath : packageList) {
            fileOperateUtil.copyFolder(packagePath.toString(), targetPath);
        }
        zipFile.delete();
        FileOperateUtil.delAllFile(resultPath);

        return targetPath;
    }

    public static HashSet extractJartoClass2(String localpath, String targetPath, String deployJarprefix, Map<String, Map> applicationsrclist, String applicationID) {
        FileOperateUtil fileOperateUtil = new FileOperateUtil();
        HashSet jarPackageSet = new HashSet();
        String applicationIDPrefix = getApplicationIDPrefix(applicationID.replaceAll("_", "-"));
        //先通过applicationID查找jar包
        jarPackageSet = getapplicationJarList(new File(localpath), applicationID, jarPackageSet);
        for (String applicationsrcname : applicationsrclist.keySet()) {
            jarPackageSet = getapplicationJarList(new File(localpath), applicationsrcname.replaceAll("_", "-"), jarPackageSet);
        }

        //还没有找到jar包，再通过应用前缀再搜索一次
        jarPackageSet = getapplicationJarList(new File(localpath), applicationIDPrefix, jarPackageSet);


        HashSet<File> jarPackageSet2 = new HashSet<File>();
        Iterator<File> itr = jarPackageSet.iterator();

        while (itr.hasNext()) {
            File jarPackage = itr.next();
            if (jarPackage.getName().replaceAll("_", "-").contains(applicationIDPrefix)) {
                jarPackageSet2.add(jarPackage);
            }
        }
        //针对特殊应用名处理
        String specialApplicationIDPrex = getSpecialApplicationIDPrex(applicationID);
        if (!StringUtils.isEmptyOrNull(specialApplicationIDPrex)) {
            jarPackageSet2 = new HashSet<File>();
            jarPackageSet2 = getapplicationJarList(new File(localpath), specialApplicationIDPrex, jarPackageSet2);
        }
        //如果按应用前缀过滤jar包为零,则通过applicationsrclist再搜索一次
        if (jarPackageSet2.size() == 0) {
            for (String applicationsrcname : applicationsrclist.keySet()) {
                jarPackageSet2 = getapplicationJarList(new File(localpath), applicationsrcname.replaceAll("_", "-"), jarPackageSet2);
            }
        }
        //如果没有找到jar包，通过压缩包前缀再搜索一次
        if (!deployJarprefix.equals("") && jarPackageSet2.size() == 0) {
            jarPackageSet2 = getapplicationJarList(new File(localpath), deployJarprefix, jarPackageSet);
        }
        HashSet<File> jarPackageSet3 = new HashSet<>();
        Iterator<File> itr2 = jarPackageSet2.iterator();
        String jarversion = getMaxCountVersion(jarPackageSet2, applicationID);
        while (itr2.hasNext()) {
            File jarPackage = itr2.next();
            if (jarPackage.toString().contains("lib") && jarPackage.getName().contains(jarversion)) {
                fileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            } else if (jarPackage.toString().contains("lib") && jarversion.equals("nothing")) {
                fileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            } else if (jarPackage.toString().contains("lib") && jarPackage.getName().contains("SNAPSHOT.jar")) {
                fileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            } else if (!jarPackage.toString().contains("lib")) {
                fileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            }
            //如果是特殊应用，所有jar包都需要解压
            if ("ads-mix-foreign-show".equals(applicationID)) {
                fileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            }

        }

        return jarPackageSet3;
    }

    private static String getSpecialApplicationIDPrex(String applicationID) {
        if ("finz-pay-core".equals(applicationID)) {
            return "dubhe-pay";
        } else if ("usercenter-business-dubbo-provider".equals(applicationID)) {
            return "usercenter-business";
        } else if ("magzine-service".equals(applicationID)) {
            return "com-oppo-browser-magzine-service";
        } else if ("compass-de-1-x".equals(applicationID)) {
            return "compass";
        } else if ("ad-show-performace-search-service".equals(applicationID)) {
            return "ad-show-performance";
        } else if ("ad-show-frequence-record-service".equals(applicationID)) {
            return "ad-show-frequence";
        } else if ("token-server-dubbo-provider".equals(applicationID)) {
            return "token-server";
        } else if ("userhistory-dubbo-provider".equals(applicationID)) {
            return "userhistory";
        } else if ("dubhe-order".equals(applicationID)) {
            return "order-center";
        } else if ("dubhe-fund".equals(applicationID)) {
            return "fund-center";
        } else if ("used-device-dubbo-provider".equals(applicationID)) {
            return "used-device";
        } else if ("phecda-variable-external-biz".equals(applicationID)) {
            return "phecda-variable";
        } else if ("magzine-media-service".equals(applicationID)) {
            return "com-oppo-browser-magzine-media-service";
        } else if ("cdo-page-rpc-store".equals(applicationID)) {
            return "cdo-page";
        } else if ("cdo-admix-rpc".equals(applicationID)) {
            return "cdo-admix";
        } else if ("cdo-detail-api-store".equals(applicationID)) {
            return "cdo-detail";
        } else if (applicationID.startsWith("cdo-")) {
            String[] appCodeArray = applicationID.split("-");
            return appCodeArray[0] + "-" + appCodeArray[1];
        }
        return "";
    }

    private static String getMaxCountVersion(HashSet<File> jarPackageSet, String applicationID) {
        Iterator<File> itr = jarPackageSet.iterator();
        HashMap maxMap = new HashMap();
        int maxcount = 0;
        while (itr.hasNext()) {
            File jarPackage = itr.next();
            if (jarPackage.toString().contains("lib")) {
                String jarversion = getJarPackageVersion(jarPackage);
                String jarpackagepre = getJarPackagePre(jarPackage);
                //如果jar包包含应用名,直接使用这个jar包的版本号
                if (jarpackagepre.equals(applicationID)) {
                    maxMap.put("jarversion", jarversion);
                    break;
                }
                if (!jarversion.equals("")) {
                    HashMap<String, Integer> hashMap = getCountVersion(jarPackageSet, jarversion);
                    if (hashMap.get(jarversion) > maxcount) {
                        maxcount = hashMap.get(jarversion);
                        maxMap.put("jarversion", jarversion);
                        maxMap.put("count", maxcount);
                    }
                }

            }

        }

        return maxMap.getOrDefault("jarversion", "nothing").toString();
    }

    private static HashMap<String, Integer> getCountVersion(HashSet<File> jarPackageSet, String jarversion) {
        Iterator<File> itr = jarPackageSet.iterator();
        HashMap hashMap = new HashMap();
        int count = 0;
        while (itr.hasNext()) {
            File jarPackage = itr.next();
            if (jarPackage.getName().contains(jarversion)) {
                count++;
            }
        }
        hashMap.put(jarversion, count);
        return hashMap;
    }

    private static String getApplicationIDJarName(File filePath) {
        String folderName = filePath.getName();
        return folderName.substring(0, folderName.indexOf("SNAPSHOT") + 8) + ".jar";
    }

    public static File getapplicationJarPath(File extractPath, String dependentjarname) {
        File[] fileList = extractPath.listFiles();
        File denpentjarpath = null;
        //遍历代码工程
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //如果当前文件夹名== src
                denpentjarpath = getapplicationJarPath(f, dependentjarname);
                if (denpentjarpath != null) {
                    return denpentjarpath;
                }
            } else {
                if (f.getName().contains(dependentjarname) && f.getName().endsWith(".jar") && !f.getName().endsWith("sources.jar")) {
                    return f;
                }
            }
        }
        return denpentjarpath;
    }

    public static HashSet getapplicationJarList(File extractPath, String dependentjarname, HashSet jarPackageSet) {
        File[] fileList = extractPath.listFiles();
        //遍历代码工程
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //如果当前文件夹名== src
                getapplicationJarList(f, dependentjarname, jarPackageSet);
            } else {
                String fname = f.getName().replaceAll("_", "-");
                if (fname.contains(dependentjarname) && fname.endsWith(".jar") && !fname.endsWith("sources.jar")) {
                    jarPackageSet.add(f);
                }
            }
        }
        return jarPackageSet;
    }

    private static ArrayList getComPackagePath(File localPath, ArrayList<File> packageList) {
        File[] fileList = localPath.listFiles();
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //如果当前文件夹名== com
                if ("com".equals(f.getName())) {
                    packageList.add(f.getParentFile());
                    return packageList;
                } else {
                    getComPackagePath(f, packageList);
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
//        String downloadFilePath = downloadColumbusBuildVersion("http://ocs-cn-south.oppoer.me/columbus-file-repo/columbus-repo-202008/combine_844869-20200820-8448691.zip","D:\\execfile");
//        extractColumsBuildVersionClasses(downloadFilePath,"D:\\execfile\\classes","annotate-data-product-service",new HashMap<>());
//        String[] containPackages = {"com.oppo.fintech.loan.api.re*","com.oppo.fin.wealthe"};
//        HashSet containPackagesSet = ColumbusUtils.getcontainPackageHashSet(containPackages,"\"D:\\\\codeCoverage\\\\taskID\\\\10016\\\\classes");
//        ColumbusUtils.filterContainPackages(containPackagesSet,new File("D:\\codeCoverage\\taskID\\10015\\classes"));

//        System.out.println(ColumbusUtils.getBuildVersionList("cdo-card-theme-api", "cdo-card-theme-api_20210317151733"));
        System.out.println(getSpecialApplicationIDPrex("cdo-store-api"));
    }
}
