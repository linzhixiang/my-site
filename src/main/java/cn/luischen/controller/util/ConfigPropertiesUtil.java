package cn.luischen.controller.util;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加载config.properties文件并提供get方法
 * @author wanglei
 */
public class ConfigPropertiesUtil {
    public static ResourceBundle configResource = null;
    public static Map<String, String> propertiesMap = new HashMap<String, String>();
    public static boolean inited = false;

    protected static String PROPERTITIES_FILE_NAME = "config";
    protected static String PROPERTITIES_HIVE = "hive";
    protected static Logger logger = LoggerFactory.getLogger(ConfigPropertiesUtil.class);
    
    /**
     * 使用已经载入的数据进行初始化
     * @param initMap 
     */
    public static void init(Map<String, String> initMap) {
        if(initMap!=null){
            propertiesMap = initMap;
            //设置已初始化标识
            inited = true;
        }else{
            inited = false;
        }
    }
    
    /**
     * 加载数据
     */
    protected static void loadConfigData(){
    	if(propertiesMap == null){
            propertiesMap = new HashMap<String, String>();
        }else{
            propertiesMap.clear();
        }
        initFromConfig(PROPERTITIES_FILE_NAME);
        initFromConfig(PROPERTITIES_HIVE);
    }
    
    /**
     * 从properties文件加载错误信息进行初始化
     * @param fileName
     */
    public static void initFromConfig(String fileName) {
        try {
            if(fileName!=null){
                configResource = ResourceBundle.getBundle(fileName);// file name
            }else{
                configResource = ResourceBundle.getBundle(PROPERTITIES_FILE_NAME);// file name
            }
            
            Set<String> keySet = configResource.keySet();
            if(keySet!=null && !keySet.isEmpty()){
                for(String key : keySet){
                    String value = new String((configResource.getString(key)).getBytes("utf-8"), "utf-8");
                    //System.out.println(key + ":=" + value);
                    propertiesMap.put(key, value);
                }
            }
            
            //设置已初始化标识
            inited = true;
        } catch (UnsupportedEncodingException e) {
            logger.info("config load error", e);
        }
    }

    /**
     * 根据error code获取error message内容
     * @param key
     * @return 
     */
    public static String getValue(String key) {
        String value = null;
        if (!inited) {
            loadConfigData();
        }
        if(key!=null && propertiesMap!=null && propertiesMap.containsKey(key)){
            value = propertiesMap.get(key);
        }
        return value;
    }

    /**
     * 根据error code获取error message内容<br>
     * 附带参数如{0},{1}...
     * @param key
     * @param parValues
     * @return 
     */
    public static String getValueWithPars(String key, Object... parValues){
        String value = getValue(key);
        if(value!=null && parValues!=null && parValues.length>0){
            value = MessageFormat.format(value, parValues);
        }
        return value;
    }

    public static Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public static void setPropertiesMap(Map<String, String> propertiesMap) {
        ConfigPropertiesUtil.propertiesMap = propertiesMap;
    }

    public static ResourceBundle getConfigResource() {
        return configResource;
    }

    public static void setConfigResource(ResourceBundle configResource) {
        ConfigPropertiesUtil.configResource = configResource;
    }

    public static boolean isInited() {
        return inited;
    }

    public static void setInited(boolean inited) {
        ConfigPropertiesUtil.inited = inited;
    }
}
