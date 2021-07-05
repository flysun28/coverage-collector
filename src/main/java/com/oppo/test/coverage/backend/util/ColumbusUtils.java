package com.oppo.test.coverage.backend.util;

import com.google.common.collect.Lists;
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
import com.oppo.test.coverage.backend.util.http.HttpRequestUtil;
import com.oppo.test.coverage.backend.util.http.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

@Service
public class ColumbusUtils {

    private static final Logger logger = LoggerFactory.getLogger(ColumbusUtils.class);


    @Value("${columbus.url}")
    private String url;

    @Value("${columbus.downloadVersionUrl}")
    private String download_version_url;

    @Value("${columbus.apiVersionInfo}")
    private String API_VERSION_INFO;

    @Value("${columbus.cloudUrlTest}")
    private String CLOUD_URL;

    @Value("${columbus.cloudUrlProd}")
    private String CLOUD_URL_PROD;

    @Value("${columbus.cloudUrlDev}")
    private String cloudUrlDev;


    public ArrayList<AppVersionResponse> getBuildVersionList(String appId, String buildVersionName, Integer testedEnv) {
        Map<String, String> params = new HashMap<>();
        params.put("ts", String.valueOf(System.currentTimeMillis()));
        params.put("app_code", appId);
        //取测试环境版本
        switch (testedEnv) {
            case 2:
                params.put("env", "prod");
                break;
            case 3:
                params.put("env", "dev");
                break;
            default:
                params.put("env", "test");
        }


        String sortedParams = sortParams(params);
        List<AppVersionResponse> appVersionResponses;
        ArrayList<AppVersionResponse> appBranchResponses = new ArrayList<>();

        try {
            String signature = HMAC_MD5_encode("123456789", sortedParams);
            String ret = HttpUtils.sendGet(url + API_VERSION_INFO + "?" + sortedParams + "&signature=" + signature + "&pageNum=1&pageSize=100&fuzzyQuery=0&versionName=");
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
    private static String sortParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("params can't be empty");
        }
        List<String> keyList = new ArrayList<>(params.keySet());
        Collections.sort(keyList);
        StringBuilder sb = new StringBuilder();
        for (String key : keyList) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }

        return sb.toString().substring(0, sb.length() - 1);
    }

    private static String HMAC_MD5_encode(String appSecret, String message) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
                appSecret.getBytes(),
                "HmacMD5"
        );
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(keySpec);

        byte[] rawHmac = mac.doFinal(message.getBytes());
        return Hex.encodeHexString(rawHmac);
    }

    private StringBuffer getAppDeployInfoList(String versionName, Integer testedEnv) throws DefinitionException {
        ArrayList<AppDeployInfo> appDeployInfos = new ArrayList<AppDeployInfo>();
        StringBuffer ipList = new StringBuffer();
        try {
            String ret;
            Gson gson = new Gson();
            switch (testedEnv) {
                case 2:
                    ret = HttpUtils.sendGet(CLOUD_URL_PROD + versionName);
                    break;
                case 3:
                    ret = HttpUtils.sendGet(cloudUrlDev + versionName);
                    break;
                default:
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
                ipList.append(appDeployInfo.getIp());
                ipList.append(",");

            }
            if ("".equals(ipList.toString())) {
                System.out.println("test environment ip is null");
                throw new DefinitionException(ErrorEnum.GET_ENVIRONMENT_IP);
            }
            return ipList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(ErrorEnum.GET_ENVIRONMENT_IP);
        }
    }

    public HashMap<String, Object> getAppDeployInfoFromBuildVersionList(String appId, String buildVersionName, Integer testedEnv) throws DefinitionException {
        StringBuffer applicationIp;
        HashMap<String, Object> hashMap = new HashMap<>();
        String commitId;
        String buildBranch;
        String repositoryUrl;
        ArrayList<AppVersionResponse> appVersionList = getBuildVersionList(appId, buildVersionName, testedEnv);
        if (appVersionList.size() > 0) {
            commitId = appVersionList.get(0).getCommitId();
            buildBranch = appVersionList.get(0).getSourceBranch();
            repositoryUrl = appVersionList.get(0).getRepositoryUrl();

            applicationIp = getAppDeployInfoList(buildVersionName, testedEnv);
            hashMap.put("applicationIP", applicationIp.toString());
            hashMap.put("commitID", commitId);
            hashMap.put("buildBranch", buildBranch);
            hashMap.put("repositoryUrl", repositoryUrl);
        } else {
            throw new DefinitionException(ErrorEnum.GET_DOWNLOAD_PACKAGE_FAILED);
        }

        return hashMap;
    }

    // TODO: 2021/6/11 这个方法指定有点大病
    public static void filterIgnoreClass(String[] classArrayList, File basePath) {

        if (classArrayList == null || classArrayList.length < 1) {
            return;
        }

        for (String ignoreClass : classArrayList) {
            if (ignoreClass.startsWith("***")) {
                FileOperateUtil.delAllFile(basePath + File.separator + ignoreClass.replace("***", ""));
            }
        }

        for (String classname : classArrayList) {
            if (classname.contains(".")) {
                String classPathLastStr = classname.substring(classname.lastIndexOf(".") + 1);
                String classParentPathStr = classname.substring(0, classname.lastIndexOf(".")).replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                File classParentPath = new File(basePath, classParentPathStr);
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

        if (packageArrayList == null || packageArrayList.length < 1) {
            return;
        }

        for (String packageName : packageArrayList) {
            if (packageName.contains(".")) {
                String packageNameLastStr = packageName.substring(packageName.lastIndexOf(".") + 1);
                String packageParentNameStr = packageName.substring(0, packageName.lastIndexOf("."));

                String packageParentNamePath = packageParentNameStr.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                File packageNameParentFile = new File(basePath, packageParentNamePath);
                if (packageNameLastStr.endsWith("*")) {
                    if (packageNameParentFile.exists()) {
                        for (File packageFile : Objects.requireNonNull(packageNameParentFile.listFiles())) {
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

    public static HashSet getContainPackageHashSet(String[] containPackageList, String localPath) {
        HashSet<String> containPackageSet = new HashSet<>();
        for (String packageName : containPackageList) {
            String packageNamePath = packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            String packagePath = localPath + File.separator + packageNamePath;
            if (new File(packagePath).exists()) {
                containPackageSet.add(packageNamePath);
            } else if (packageName.contains("*")) {
                containPackageSet.add(packageNamePath);
            }
        }
        return containPackageSet;
    }

    public static int packageContainPath(HashSet containpackageSet, File basePath) {
        //0,路径中包含包名 1,路径完全匹配包名 2 路径不包含包名
        int containresult = 2;
        if (containpackageSet == null) {
            return containresult;
        }

        String path = basePath.getPath();
        if (!path.contains("com")) {
            return containresult;
        }
        path = path.substring(path.indexOf("com"));
        for (Object o : containpackageSet) {
            String containPackageName = o.toString();
            if (containPackageName.contains("*")) {
                containPackageName = containPackageName.substring(0, containPackageName.indexOf("*"));
            }
            if (containPackageName.equals(path)) {
                containresult = 1;
                return containresult;
            } else if (containPackageName.contains(path)) {
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


    public String downloadColumbusBuildVersion(String repositoryUrl, String downloadPath) {
        String fileName;
        File downloadFilePath = null;
        //Map<String, String> headers = new HashMap<>();
        //String nonce = String.valueOf(new Random().nextInt(10000));
        //String curTime = String.valueOf((new Date()).getTime() / 1000L);
        // 设置请求的header
        //headers.put("Nonce", nonce);
        //headers.put("CurTime", curTime);
        //headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        fileName = repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1);
        String downloadUrl = download_version_url + "/" + repositoryUrl;
        logger.info("columbus build version download : {}", downloadUrl);
        try {
            downloadFilePath = new File(downloadPath, "downloadzip");
            if (!downloadFilePath.exists()) {
                downloadFilePath.mkdir();
            }
            downloadFilePath = new File(downloadFilePath, fileName);
            if (!downloadFilePath.exists()) {
                download(downloadUrl, downloadFilePath.toString());
            }

        } catch (Exception e) {
            logger.error(" downloadColumbusBuildVersion fail , retrying: {} , {}", downloadUrl, e.getMessage());
            e.printStackTrace();
            try {
                Thread.sleep(10000);
                download(downloadUrl, downloadFilePath.toString());
            } catch (Exception en) {
                logger.error("download retry fail : {} , {}", downloadUrl, e.getMessage());
                en.printStackTrace();
                throw new DefinitionException(ErrorEnum.DOWNLOAD_BUILD_VERSION_FAILED);
            }
        }
        return downloadFilePath.toString();
    }

    public static void download(String url, String filePath) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> HttpRequestUtil.downloadBuildVersionFile(url, filePath));
        if (!future.get(180, TimeUnit.SECONDS)) {
            throw new DefinitionException(ErrorEnum.DOWNLOAD_BUILD_VERSION_FAILED);
        }
    }


    public static boolean fileDownload(String url, String filePath) {
        try {
            URL httpUrl = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(httpUrl.openStream());
            FileOutputStream fos = new FileOutputStream(filePath);

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        } catch (IOException e) {
            logger.error("download file error : {} , {}, {}", url, filePath, e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getDeployJarPrefix(String downloadZipFile) {
        String applicationpre = "";

        if (downloadZipFile.contains("bin") && !downloadZipFile.contains("bingo")) {
            //包括bin则取bin之前？为啥？特殊的经验值处理？
            applicationpre = downloadZipFile.substring(0, downloadZipFile.indexOf("bin") - 1);
            return applicationpre;
        }

        //第一个数字所在的位置
        int numBeginIndex = getNumIndexFormStr(downloadZipFile);

        if (numBeginIndex == -1) {
            //没有数字
            applicationpre = downloadZipFile;
        } else {
            //取数字前的字符串 ci-demo-
            applicationpre = downloadZipFile.substring(0, numBeginIndex);
            if ("-".equals(applicationpre.substring(numBeginIndex - 1, numBeginIndex)) || "_".equals(applicationpre.substring(numBeginIndex - 1, numBeginIndex))) {
                //去掉末尾的_或者-
                applicationpre = downloadZipFile.substring(0, numBeginIndex - 1);
            }
        }

        System.out.println("prefix " + applicationpre);
        return applicationpre;
    }

    // 0 - 9   : 4-57
    private static int getNumIndexFormStr(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                return i;
            }
        }
        return -1;
    }

    private static String getJarPackageVersion(File jarPackage) {

        //获取头一个数字的位置
        int numBeginIndex = getNumIndexFormStr(jarPackage.getName());
        if (numBeginIndex == -1) {
            return "";
        }
        //获取数字的到后一个的-的位置
        int numlastindex = jarPackage.getName().indexOf("-", numBeginIndex);
        if (numlastindex == -1) {
            //没有-,就取全名
            numlastindex = jarPackage.getName().indexOf(".jar");
        }
        return jarPackage.getName().substring(numBeginIndex, numlastindex);

    }

    private static String getJarPackagePre(File jarPackage) {

        int numBeginIndex = getNumIndexFormStr(jarPackage.getName());

        if (numBeginIndex == -1) {
            if (jarPackage.getName().contains("SNAPSHOT")) {
                return jarPackage.getName().substring(0, jarPackage.getName().indexOf("SNAPSHOT") - 1);
            } else {
                return jarPackage.getName().substring(0, jarPackage.getName().indexOf(".") - 1);
            }
        } else {
            return jarPackage.getName().substring(0, numBeginIndex - 1);
        }
    }

    public static String getApplicationIDPrefix(String applicationID) {
        String applicationIDPrefix;
        if (applicationID.contains("-")) {
            applicationIDPrefix = applicationID.substring(0, applicationID.lastIndexOf("-"));
        } else {
            applicationIDPrefix = applicationID;
        }
        return applicationIDPrefix;
    }


    /**
     * @param downloadZipFile       : 下载后的文件.getName() /home/service/app/coveragebackend/xxxx/taskID/1049/downloadzip/ci-demo-xxx.tar.gz
     * @param targetPath            : /home/service/app/coveragebackend/xxxx/taskID/1049/classes
     * @param applicationID         : 应用id
     * @param sourceApplicationsMap : CIdemo-{"sourceDirectory":"/home/service/xxx/xxx/codeCoverage/CIdemo"}
     */
    public static String extractColumbusBuildVersionClasses(String downloadZipFile, String targetPath, String applicationID, Map sourceApplicationsMap) throws Exception {
        FileOperateUtil fileOperateUtil = new FileOperateUtil();

        //  /home/service/app/coveragebackend/xxxx/taskID/1049/downloadzip
        String basicPath = new File(downloadZipFile).getParentFile().toString();
        if ("".equals(downloadZipFile)) {
            System.out.println("please input extract path:");
        }
        FileOperateUtil.delAllFile(targetPath);

        System.out.println("extract path:" + basicPath);

        File zipFile = new File(downloadZipFile);

        // 拿到下载的版本包的前缀 : ci-demo(-20210408-14xxx.tar.gz)
        String deployJarPrefix = getDeployJarPrefix(zipFile.getName());

        //
        // 解压到目录(文件名-11个字符作为临时目录) : /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/ci-demo-20210408-1435
        String resultPath = FileExtractUtil.extractFile(zipFile);

        ArrayList<File> packageList = new ArrayList<>();

        HashSet jarPackageSet;

        //先替换下划线 应用id的下划线统一换成- ? why ?
        applicationID = applicationID.replaceAll("_", "-");

        // /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/jarpackage
        String jarPackagePath = basicPath + File.separator + "jarpackage";
        if (!new File(jarPackagePath).exists()) {
            new File(jarPackagePath).mkdir();
        }

        //遍历所有文件夹，找出应用的jar包，并解压
        jarPackageSet = extractJarToClass2(resultPath, jarPackagePath, deployJarPrefix, sourceApplicationsMap, applicationID);

        //再对解压的文件夹里，遍历解压一次
        if (jarPackageSet.size() > 0) {
            extractJarToClass2(jarPackagePath, jarPackagePath, "", sourceApplicationsMap, applicationID);
            getComPackagePath(new File(jarPackagePath), packageList);
        } else {
            getComPackagePath(new File(resultPath), packageList);
        }
        if (deployJarPrefix.contains("tomcat")) {
            getComPackagePath(new File(resultPath), packageList);
        }
        if (CollectionUtils.isEmpty(packageList)) {
            logger.error(" packageList is null ! It may cause non classes :{} , {} , {}", downloadZipFile, targetPath, applicationID);
        }

        for (File packagePath : packageList) {
            fileOperateUtil.copyFolder(packagePath.toString(), targetPath);
        }
        //zipFile.delete();
        FileOperateUtil.delAllFile(resultPath);

        return targetPath;
    }


    /**
     * @param localPath          : /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/ci-demo-20210408-1435,版本包解压后的目录
     * @param targetPath         : /home/service/app/coveragebackend/xxxxx/taskID/1049/downloadzip/jarpackage
     * @param deployJarPrefix    : ci-demo,压缩包的前缀
     * @param applicationsrclist : "CIdemo" : {"sourceDirectory":"/home/service/app/coveragebackend/xxxxx/codeCoverage/CIdemo"}
     * @param applicationID      : ci-demo
     */
    private static HashSet extractJarToClass2(String localPath, String targetPath, String deployJarPrefix, Map<String, Map> applicationsrclist, String applicationID) {
        Set<File> jarPackageSet = new HashSet<>();

        //版本包解压后的目录
        File localPathFile = new File(localPath);

        Set<File> jarPackageSet2;

        //4.硬编码配置,处理特殊配置的应用名
        String specialApplicationIDPrefix = getSpecialApplicationIDPrefix(applicationID);
        //5.一个应用对应多个不同前缀的jar包开始的情况
        List<String> specialAppCodePrefixList = getSpecialAppCodeListPrefix(applicationID);

        if (!StringUtils.isEmpty(specialApplicationIDPrefix)) {
            jarPackageSet2 = new HashSet<>();
            getApplicationJarList(localPathFile, specialApplicationIDPrefix, jarPackageSet2);
        } else if (!CollectionUtils.isEmpty(specialAppCodePrefixList)) {
            jarPackageSet2 = new HashSet<>();
            scanJarFileAndPickJarPackage(localPathFile, specialAppCodePrefixList, jarPackageSet2);
        } else {
            jarPackageSet2 = autoSearchJarPackage(applicationID, jarPackageSet, localPathFile, applicationsrclist);
        }


        //6.如果按应用前缀过滤jar包为零,则通过applicationsrclist再搜索一次,不过滤应用前缀
        if (jarPackageSet2.size() == 0) {
            List<String> keyName = new ArrayList<>();
            applicationsrclist.keySet().forEach(entry -> keyName.add(entry.replace("_", "-")));
            scanJarFileAndPickJarPackage(localPathFile, keyName, jarPackageSet2);
        }
        //7.如果没有找到jar包，通过压缩包前缀再搜索一次
        if (!"".equals(deployJarPrefix) && jarPackageSet2.size() == 0) {
            jarPackageSet2 = getApplicationJarList(localPathFile, deployJarPrefix, jarPackageSet);
        }

        HashSet<File> jarPackageSet3 = new HashSet<>();

        //拿到出现次数最多的jar版本号
        String jarVersion = getMaxCountVersion(jarPackageSet2, applicationID);
        //取到合适的jar
        for (File jarPackage : jarPackageSet2) {
            if (validPackageCheck(jarPackage, jarVersion, applicationID)) {
                FileOperateUtil.copyFile(jarPackage.toString(), targetPath + File.separator + jarPackage.getName());
                FileExtractUtil.extractFiles(targetPath);
                jarPackageSet3.add(jarPackage);
            }
        }

        return jarPackageSet3;
    }


    private static Set<File> autoSearchJarPackage(String appCode, Set<File> jarPackageSet, File localPathFile, Map<String, Map> applicationSrcList) {
        //拿这个appId的前一小段(去掉最后一个"-"以后的字符)
        String applicationIdPrefix = getApplicationIDPrefix(appCode.replaceAll("_", "-"));

        //1.先通过applicationID查找jar包
        //2.通过工程名称查找jar包
        //3.再通过应用前缀再搜索一次
        List<String> checkPrefix = Lists.newArrayList(appCode, applicationIdPrefix);
        applicationSrcList.keySet().forEach(srcName -> checkPrefix.add(srcName.replace("_", "-")));
        scanJarFileAndPickJarPackage(localPathFile, checkPrefix, jarPackageSet);

        // 搜索出来的jar,如果包含应用前缀,则加入set2
        HashSet<File> jarPackageSet2 = new HashSet<>();
        for (File jarPackage : jarPackageSet) {
            if (jarPackage.getName().replaceAll("_", "-").contains(applicationIdPrefix)) {
                jarPackageSet2.add(jarPackage);
            }
        }
        return jarPackageSet2;
    }


    /**
     * 判断jar包是否可取
     * lib目录下,未找到版本则都选中;找到则取对应版本;包含SNAPSHOT.jar的取中;
     * 非lib目录下的全取出
     */
    private static boolean validPackageCheck(File jarPackage, String jarVersion, String appId) {
        if (!jarPackage.toString().contains("lib")) {
            return true;
        }
        if (jarPackage.getName().contains("SNAPSHOT.jar") || jarPackage.getName().contains(jarVersion) || "nothing".equals(jarVersion)) {
            return true;
        }
        if ("ads-mix-foreign-show".equals(appId)) {
            return true;
        }
        return false;
    }

    private static String getSpecialApplicationIDPrefix(String applicationID) {
        List<String> browserMultiRegionPlugin = Lists.newArrayList("browser-feeds-channel-service-global",
                "browser-ucenter-service-global", "browser-feeds-list-service-global",
                "browser-feeds-resource-service-global", "browser-operation-position-global",
                "browser-strategy-global", "browser-grids-service-global",
                "browser-static-files-service-global", "browser-common-setting-service-global",
                "browser-skin-service-global", "browser-red-dot-service-global");
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
        } else if (browserMultiRegionPlugin.contains(applicationID)) {
            return "browser-multi-region-plugin";
        } else if ("insights-web".equals(applicationID)) {
            return "portal";
        } else if ("bot-platform-management".equals(applicationID)) {
            return "bot-manage";
        }
        return "";
    }

    /**
     * 用于当一应用打包出多个jar包,且jar包前缀不同的时候进行解析
     *
     * @param appCode : 应用名称
     * @return : jar包前缀
     */
    private static List<String> getSpecialAppCodeListPrefix(String appCode) {
        if ("insights-web".equals(appCode)) {
            return Lists.newArrayList("portal", "sms", "userinsight", "userorient");
        }
        return null;
    }

    /**
     * 最多的版本号
     */
    private static String getMaxCountVersion(Set<File> jarPackageSet, String appCode) {

        Map<String, Integer> versionCountMap = new HashMap<>(jarPackageSet.size());

        for (File jarFile : jarPackageSet) {
            if (!jarFile.toString().contains("lib")) {
                continue;
            }
            //数字开始到数字后的"-"之间的字符串
            String jarVersion = getJarPackageVersion(jarFile);
            //上面那个版本号的字符串
            String jarPackagePre = getJarPackagePre(jarFile);
            //如果jar包包含应用名,直接使用这个jar包的版本号
            if (jarPackagePre.equals(appCode)) {
                return jarVersion;
            }
            if (StringUtils.isEmpty(jarVersion)) {
                continue;
            }

            versionCountMap.merge(jarVersion, 1, Integer::sum);
        }

        int maxCount = 0;
        String maxVersion = "nothing";
        for (Map.Entry<String, Integer> entry : versionCountMap.entrySet()) {
            if (entry.getValue() >= maxCount) {
                maxCount = entry.getValue();
                maxVersion = entry.getKey();
            }
        }
        return maxVersion;
    }


    private static String getApplicationIDJarName(File filePath) {
        String folderName = filePath.getName();
        return folderName.substring(0, folderName.indexOf("SNAPSHOT") + 8) + ".jar";
    }

    private static File getapplicationJarPath(File extractPath, String dependentjarname) {
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

    /**
     * @param extractPath      : 解压出来的文件目录
     * @param dependentJarName : jar包前缀
     * @param jarPackageSet    : jar包set
     */
    private static Set<File> getApplicationJarList(File extractPath, String dependentJarName, Set<File> jarPackageSet) {
        File[] fileList = extractPath.listFiles();
        if (fileList == null || fileList.length < 1) {
            return jarPackageSet;
        }
        //遍历解压出的所有文件
        for (File f : fileList) {
            //判断是否文件夹目录
            if (f.isDirectory()) {
                //文件夹往下递归
                getApplicationJarList(f, dependentJarName, jarPackageSet);
            } else {
                //文件则判断是否为目标jar包
                String fileName = f.getName().replaceAll("_", "-");
                if (fileName.contains(dependentJarName) && fileName.endsWith(".jar") && !fileName.endsWith("sources.jar")) {
                    jarPackageSet.add(f);
                }
            }
        }
        return jarPackageSet;
    }

    private static Set<File> scanJarFileAndPickJarPackage(File unzipFilePath, List<String> prefix, Set<File> jarPackageSet) {
        File[] fileArray = unzipFilePath.listFiles();
        if (fileArray == null || fileArray.length < 1) {
            return jarPackageSet;
        }
        for (File file : fileArray) {
            if (file.isDirectory()) {
                scanJarFileAndPickJarPackage(file, prefix, jarPackageSet);
            } else {
                if (jarPackagePickCheck(file.getName(), prefix)) {
                    jarPackageSet.add(file);
                }
            }
        }
        return jarPackageSet;
    }

    private static boolean jarPackagePickCheck(String fileName, List<String> prefix) {
        fileName = fileName.replace("_", "-");
        if (!fileName.endsWith(".jar")) {
            return false;
        }
        if (fileName.endsWith("sources.jar")) {
            return false;
        }
        for (String containsString : prefix) {
            if (fileName.contains(containsString)) {
                return true;
            }
        }
        return false;
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
//        getDeployJarPrefix("fin-20200721_0251-bin-20200721-7675751.zip");
//        String downloadFilePath = downloadColumbusBuildVersion("http://ocs-cn-south.oppoer.me/columbus-file-repo/columbus-repo-202008/combine_844869-20200820-8448691.zip","D:\\execfile");
//        extractColumsBuildVersionClasses(downloadFilePath,"D:\\execfile\\classes","annotate-data-product-service",new HashMap<>());
//        String[] containPackages = {"com.oppo.fintech.loan.api.re*","com.oppo.fin.wealthe"};
//        HashSet containPackagesSet = ColumbusUtils.getcontainPackageHashSet(containPackages,"\"D:\\\\codeCoverage\\\\taskID\\\\10016\\\\classes");
//        ColumbusUtils.filterContainPackages(containPackagesSet,new File("D:\\codeCoverage\\taskID\\10015\\classes"));

//        System.out.println(ColumbusUtils.getBuildVersionList("cdo-card-theme-api", "cdo-card-theme-api_20210317151733"));
//        System.out.println(getSpecialApplicationIDPrefix("cdo-store-api"));
//        System.out.println(getAppDeployInfoFromBuildVersionList("ci-demo", "ci-demo-20210326163909-181", 1));
        String[] test = {"org.objectweb.asm.ClassReader"};
        filterIgnoreClass(test, new File("F:\\业务场景\\play35\\send-0.0.1-SNAPSHOT"));
    }
}
