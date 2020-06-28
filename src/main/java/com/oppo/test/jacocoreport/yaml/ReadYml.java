package com.oppo.test.jacocoreport.yaml;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReadYml {
    private static Map<String, Map<String, Object>> properties;

    private ReadYml() {
        if (SingletonHolder.instance != null) {
            throw new IllegalStateException();
        }
    }

    private static class SingletonHolder {
        private static ReadYml instance = new ReadYml();
    }

    public static ReadYml getInstance() {
        return SingletonHolder.instance;
    }

    public static void setProperties(String resourceName) {
        InputStream in = null;
        try {
            properties = new HashMap<>();
            Yaml yaml = new Yaml();
            in = ReadYml.class.getClassLoader().getResourceAsStream(resourceName);
            properties = yaml.loadAs(in, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
              in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public Object getValueByKey(String resourceName, String root, String key) {
        setProperties(resourceName);
        Map<String, Object> rootProperty = properties.getOrDefault(root, new HashMap<>());
        System.out.println(rootProperty);
        return rootProperty.getOrDefault(key, new HashMap<>());
    }

    public Map<String, Object> getValuesByRootKey(String resourceName, String root) {
        setProperties(resourceName);
        Map<String, Object> rootProperty = properties.getOrDefault(root, new HashMap<>());
        return rootProperty;
    }

    public boolean existKeyValue(String resourceName, String root) {
        setProperties(resourceName);
        Map rootProperty = properties.get(root);
        if (null == rootProperty || rootProperty.size() == 0) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * @param resourceName      testenvironment-pandora.yml
     * @param root              environment1
     * @param applicationIdList [pandora-server-web]
     * @return
     */
    public Object setValueByKey(String resourceName, String root, ArrayList<File> applicationIdList) {
        Yaml yaml = new Yaml();
        //读取配置文件testenvironment-pandora.yml
        File file = new File(resourceName);
        try {
            //读取环境信息 environment1
            Map m1 = (Map) yaml.load(new FileInputStream(file));
            Map environment = new HashMap();

            for (File applicationidPath : applicationIdList) {

                //应用名，例如pandora-common-biz
                Map applicationMap = (Map) environment.get(applicationidPath.getName());
                if (applicationMap == null) {
                    applicationMap = new HashMap();
                }
                String ip = applicationMap.getOrDefault("ip", "").toString();
                //application设置ip属性
                if (ip.equals("")) {
                    applicationMap.put("ip", "127.0.0.1");
                }
                applicationMap.put("sourceDirectory", applicationidPath.toString());
                environment.put(applicationidPath.getName(), applicationMap);
            }
            m1.put(root, environment);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(yaml.dump(m1));
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object setValueByMap(String resourceName, String root, Map applicationMap) {
        Yaml yaml = new Yaml();
        //读取配置文件testenvironment-pandora.yml
        File file = new File(resourceName);
        try {
            //读取环境信息 environment1
            Map m1 = (Map) yaml.load(new FileInputStream(file));
            m1.put(root, applicationMap);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(yaml.dump(m1));
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(final String[] args) {
//        ArrayList filelist = new ArrayList();
//        ArrayList applicationNames = GitUtil.getApplicationNames(new File("D:\\codeCoverage\\luckymonkey"),filelist);
//        ReadYml.getInstance().setValueByKey("testenvironment_luckymonkey.yml","environment",applicationNames);
//        System.out.println(ReadYml.getInstance().getValueByKey("testenvironment_luckymonkey.yml","environment","luckymonkey"));
//        System.out.println(ReadYml.getInstance().existKeyValue("testenvironment-pandora.yml","environment2"));
        //获取应用配置信息
        Map<String, Object> applicationMap = ReadYml.getInstance().getValuesByRootKey("testenvironment-pandora.yml", "environment2");
        for (String key : applicationMap.keySet()) {
            System.out.println(key);
            System.out.println(applicationMap.get(key));
        }
    }
}
