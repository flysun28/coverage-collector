package com.oppo.jacocoreport.coverage.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.oppo.jacocoreport.coverage.cloud.AppDeployInfo;
import com.oppo.jacocoreport.coverage.cloud.AppVersionResponse;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

public class ColumbusUtils {
    private static String url = "http://columbus.os.adc.com";
    private static String API_VERSION_INFO = "/openapi/version_info";
    private static String app_code = "";
    private static String CLOUD_URL = "http://test-console.cloud.oppoer.me/baymax-go/api/v1/deploy_history?version=";

    public static AppVersionResponse getBuildVersionList(String appId, String branchName) {
        Map<String, String> params = new HashMap<>();
        params.put("ts", String.valueOf(System.currentTimeMillis()));
        params.put("app_code", appId);
        //取测试环境版本
        params.put("env", "test");

        String sortedParams = sortParams(params);
        List<AppVersionResponse> appVersionResponses = null;

        try {
            String signature = HMAC_MD5_encode("123456789", sortedParams);
            String ret = null;
            ret = HttpUtils.sendGet(url + API_VERSION_INFO + "?" + sortedParams + "&signature=" + signature + "&pageNum=1&pageSize=20");
            System.out.println(ret);
            appVersionResponses = new Gson().fromJson(ret, new TypeToken<List<AppVersionResponse>>() {
            }.getType());
            for (AppVersionResponse appVersionResponse : appVersionResponses) {
                if (branchName.equals(appVersionResponse.getSourceBranch())) {
                    return appVersionResponse;
                }
            }
            //返回当期测试版本列表数据
            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public static AppDeployInfo getApplicationIP(String versionName, String environmentName) {
        List<AppDeployInfo> appDeployInfos = null;
        AppDeployInfo appDeployInfo = null;
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
                //对查询出来的Deploy数据做倒序排列，优先对比最新发布的测试环境地址
                Collections.reverse(appDeployInfos);
                for (AppDeployInfo appDeployInfo1 : appDeployInfos) {
                    if (environmentName.equals(appDeployInfo1.getConfEnv())) {
                        System.out.println(appDeployInfo1.getIp() + appDeployInfo1.getConfEnv() + appDeployInfo1.getAppVersion());
                        return appDeployInfo1;
                    }
                }
            }
            return appDeployInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAppDeployInfoFromBuildVersionList(String appID, String branchName, String environmentName) {
        String applicationIP = null;
        AppDeployInfo appDeployInfo = null;
        AppVersionResponse appVersionResponse = ColumbusUtils.getBuildVersionList(appID, branchName);
        if (null != appVersionResponse) {
            System.out.println("searched source" + appVersionResponse.getSourceBranch() + appVersionResponse.getVersionName());
            appDeployInfo = ColumbusUtils.getApplicationIP(appVersionResponse.getVersionName(), environmentName);
            if (appDeployInfo != null && appDeployInfo.getIp() != null && appDeployInfo.getIp() != "") {
                applicationIP = appDeployInfo.getIp();
                return applicationIP;
            }
        }
        return applicationIP;
    }

    public static void main(String[] args) {
        ColumbusUtils.getAppDeployInfoFromBuildVersionList("pandora-server-web", "test-platform3.5-20200508", "test2");
//        ColumbusUtils.getApplicationIP("pandora-server-web_20200604145728","test2");

    }
}
