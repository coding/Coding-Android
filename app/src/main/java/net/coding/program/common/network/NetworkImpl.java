package net.coding.program.common.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import net.coding.program.LoginActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.user.UserDetailActivity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NetworkImpl {
    public static final int NETWORK_ERROR = -1;
    public static final int NETWORK_ERROR_SERVICE = -2;
    private final NetworkCallback callback;
    private final String ERROR_MSG_CONNECT_FAIL = "连接服务器失败，请检查网络或稍后重试";

    public HashMap<String, PageInfo> mPages = new HashMap<>();
    Context appContext;
    private HashMap<String, Boolean> mUpdateing = new HashMap<>();

    public NetworkImpl(Context ctx, NetworkCallback networkCallback) {
        this.appContext = ctx;
        this.callback = networkCallback;
    }

    // 是否需要刷新所有数据
    public boolean isLoadingFirstPage(String tag) {
        PageInfo info = mPages.get(tag);
        return info == null || info.isNewRequest;
    }


    protected void umengEvent(String s, String param) {
        MobclickAgent.onEvent(appContext, s, param);
    }

    public void loadData(String url, RequestParams params, final String tag, final int dataPos, final Object data, final Request type) {
        Log.d("", "url " + type + " " + url);
        if (!url.startsWith("http")) {
            url = Global.HOST + url;
        }

        final String finalUrl = url;

        if (mUpdateing.containsKey(tag) && mUpdateing.get(tag)) {
            Log.d("", "url#" + (params == null ? "get " : "post ") + url);
            return;
        }

        mUpdateing.put(tag, true);

        AsyncHttpClient client = MyAsyncHttpClient.createClient(appContext);

        final String cacheName = url;

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

            private final int HTTP_CODE_RELOGIN = 1000;
            private final int HTTP_CODE_RELOGIN_2FA = 3207;

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int code = response.getInt("code");

                    if (code == HTTP_CODE_RELOGIN || code == HTTP_CODE_RELOGIN_2FA) {
                        appContext.startActivity(new Intent(appContext, LoginActivity_.class));
                    }

                    try {
                        updatePage(response, tag);
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }

                    if (type == Request.Get) {
                        if (code == 0) {
                            AccountInfo.saveGetRequestCache(appContext, cacheName, response);
                        }
                    }

                    if (tag.equals(UserDetailActivity.HOST_FOLLOW)) {
                        umengEvent(UmengEvent.USER, "关注好友");
                    } else if (tag.equals(UserDetailActivity.HOST_UNFOLLOW)) {
                        umengEvent(UmengEvent.USER, "取消关注好友");
                    } else if (tag.equals(MaopaoListFragment.TAG_DELETE_MAOPAO)) {
                        umengEvent(UmengEvent.MAOPAO, "删除冒泡");
                    } else if (tag.equals(MaopaoListBaseFragment.TAG_COMMENT)) {
                        umengEvent(UmengEvent.MAOPAO, "添加冒泡评论");
                    } else if (tag.equals(MaopaoListFragment.TAG_DELETE_MAOPAO_COMMENT)) {
                        umengEvent(UmengEvent.MAOPAO, "删除冒泡评论");
                    } else if (tag.equals(MaopaoListFragment.HOST_GOOD)) {
                        if (finalUrl.endsWith("like")) {
                            umengEvent(UmengEvent.MAOPAO, "冒泡点赞");
                        } else {
                            umengEvent(UmengEvent.MAOPAO, "冒泡取消点赞");
                        }
                    }

                    callback.parseJson(code, response, tag, dataPos, data);

                    try {
                        updateRequest(response, tag);
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }

                } catch (Exception e) {
                    Global.errorLog(e);
                }
                mUpdateing.put(tag, false);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    int translateStatusCode = translateErrorCode(statusCode);

                    JSONObject lastCache;
                    if (type == Request.Get
                            && (lastCache = AccountInfo.getGetRequestCache(appContext, cacheName)).length() > 0) {

                        try {
                            updatePage(lastCache, tag);
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }

                        Toast.makeText(appContext, ERROR_MSG_CONNECT_FAIL, Toast.LENGTH_SHORT).show();

                        callback.parseJson(0, lastCache, tag, dataPos, data);
                        try {
                            updateRequest(lastCache, tag);
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    } else {
                        if (errorResponse == null) {
                            errorResponse = makeErrorJson(translateStatusCode);
                        }
                        callback.parseJson(translateStatusCode, errorResponse, tag, dataPos, data);
                    }
                } catch (Exception e) {
                    Global.errorLog(e);
                }
                mUpdateing.put(tag, false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                try {
                    int translateErrorCode = translateErrorCode(statusCode);
                    JSONObject json = makeErrorJson(translateErrorCode);

                    callback.parseJson(translateErrorCode, json, tag, dataPos, data);

                } catch (Exception e) {
                    Global.errorLog(e);
                }
                mUpdateing.put(tag, false);
            }

            private int translateErrorCode(int statusCode) {
                if (statusCode == 0) { // 我这里的设计有问题，statusCode为0的时候表示网络不通，而code==0又表示请求成功，parseJson函数的第一个参数是以0来表示成功的
                    statusCode = NETWORK_ERROR;
                } else {
                    statusCode = NETWORK_ERROR_SERVICE;
                }

                return statusCode;
            }

            private JSONObject makeErrorJson(int statusCode) {
                JSONObject json = new JSONObject();
                try {
                    JSONObject jsonErrorMsg = new JSONObject();

                    String errorMessage;
                    if (statusCode == NETWORK_ERROR_SERVICE) {
                        errorMessage = "服务器内部错误，有人要扣奖金了";
                    } else {
                        errorMessage = ERROR_MSG_CONNECT_FAIL;
                    }
                    jsonErrorMsg.put("msg", errorMessage);

                    json.put("code", statusCode);
                    json.put("msg", jsonErrorMsg);
                } catch (Exception e) {
                    Global.errorLog(e);
                }

                return json;
            }

            @Override
            public void onFinish() {
            }
        };

        switch (type) {
            case Get:
                client.get(url, jsonHttpResponseHandler);
                break;

            case Post:
                client.post(url, params, jsonHttpResponseHandler);
                break;

            case Put:
                client.put(url, params, jsonHttpResponseHandler);
                break;

            case Delete:
                client.delete(url, jsonHttpResponseHandler);
                break;
        }
    }

    public void initSetting() {
        mPages = new HashMap<>();
    }

    private boolean isPageRequest(String tag) {
        return mPages.containsKey(tag);
    }

    private void updatePage(JSONObject json, final String tag) throws JSONException {
        if (!isPageRequest(tag)) {
            return;
        }

        PageInfo pageInfo = mPages.get(tag);

        JSONObject jsonData = json.optJSONObject("data");
        if (jsonData != null) {
            if (jsonData.has("totalPage")) {
                pageInfo.pageAll = jsonData.getInt("totalPage");
                pageInfo.pageIndex = jsonData.getInt("page");
            } else if (jsonData.has("page")) {
                pageInfo.pageIndex = jsonData.getInt("page");
                pageInfo.pageAll = jsonData.getInt("pageSize");
            } else if (jsonData.has("commits")) {
                JSONObject jsonCommits = jsonData.getJSONObject("commits");
                if (jsonCommits.has("totalPage")) {
                    pageInfo.pageAll = jsonCommits.getInt("totalPage");
                    pageInfo.pageIndex = jsonCommits.getInt("page");
                } else {
                    pageInfo.pageIndex = 0;
                    pageInfo.pageAll = 0;
                }
            } else {
                pageInfo.pageIndex = 0;
                pageInfo.pageAll = 0;
            }
        } else {
            pageInfo.pageIndex = 0;
            pageInfo.pageAll = 0;
        }

//        if (pageInfo.isLoadingLastPage()) {
//            callback.setPageBottom(NetworkCallback.PageStyle.NoData);
//        } else {
//            callback.setPageBottom(NetworkCallback.PageStyle.Loading);
//        }
    }

    private void updateRequest(JSONObject json, final String tag) throws JSONException {
        if (!isPageRequest(tag)) {
            return;
        }

        PageInfo pageInfo = mPages.get(tag);
        pageInfo.isNewRequest = false;
    }

    public boolean isLoadingLastPage(String tag) {
        PageInfo pageInfo = mPages.get(tag);
        return pageInfo != null && pageInfo.isLoadingLastPage();
    }

    public void getNextPageNetwork(String url, final String tag) {
        PageInfo pageInfo = mPages.get(tag);
        if (pageInfo == null) {
            pageInfo = new PageInfo();
            mPages.put(tag, pageInfo);
        }

        if (pageInfo.isLoadingLastPage()) {
//            callback.setPageBottom(NetworkCallback.PageStyle.NoData);
            return;
        }

        String pageUrl = url + "&page=" + (pageInfo.pageIndex + 1);
        callback.getNetwork(pageUrl, tag);
    }

    public enum Request {
        Get, Post, Put, Delete
    }

}