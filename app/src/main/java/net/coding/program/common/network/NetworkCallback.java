package net.coding.program.common.network;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaochen on 14-10-6.
 */
public interface NetworkCallback {
    void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException;

    void getNetwork(String uri, String tag);
}
