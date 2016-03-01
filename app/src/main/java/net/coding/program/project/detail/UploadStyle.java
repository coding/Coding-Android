package net.coding.program.project.detail;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by chenchao on 16/2/27.
 */
public interface UploadStyle {

    void onSuccess(int statusCode, Header[] headers, JSONObject response);
    void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse);
}
