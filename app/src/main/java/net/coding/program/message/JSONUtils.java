package net.coding.program.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Json解析工具包
 * todo 删除，改用 gson，没必要引入这么多 json 解析库
 */
public class JSONUtils {

    public static <T> T getData(String key, String jsonString, Class<T> cls) {
        return JSON.parseObject(getJSONString(key, jsonString), cls);
    }

    public static <T> T getData(String jsonString, Class<T> cls) {
        return JSONObject.parseObject(jsonString, cls);
    }

    public static <T> List<T> getList(String jsonString, Class<T> cls) {
        return JSONArray.parseArray(jsonString, cls);
    }

    public static <T> List<T> getList(String key, String jsonString, Class<T> cls) {
        return JSONArray.parseArray(getJSONString(key, jsonString), cls);
    }

    public static String getJSONString(String key, String jsonStringString) {
        return JSONObject.parseObject(jsonStringString).getString(key);
    }

    public static long getJSONLong(String key, String jsonStringString) {
        return JSONObject.parseObject(jsonStringString).getLong(key);
    }
}
