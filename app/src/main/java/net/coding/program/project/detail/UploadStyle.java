package net.coding.program.project.detail;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

@Deprecated
public interface UploadStyle {

    void onSuccess(int statusCode, Header[] headers, JSONObject response);

    void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse);
}
