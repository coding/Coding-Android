package net.coding.program.message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.coding.program.common.model.TaskLabelModel;

import org.json.JSONObject;

import java.util.List;

/**
 * Json解析工具包
 */
public class JSONUtils {

    public static <T> T getData(String jsonString, Class<T> cls) {
        return new Gson().fromJson(jsonString, cls);
    }

    public static <T> List<T> getTaskLabelModelList(String jsonString) {
        return new Gson().fromJson(jsonString, new TypeToken<List<TaskLabelModel>>() {
        }.getType());
    }

    public static long getJSONLong(String key, String jsonStringString) {
        try {
            return new JSONObject(jsonStringString).optLong(key);
        } catch (Exception e) {
            return 0;
        }
    }
}
