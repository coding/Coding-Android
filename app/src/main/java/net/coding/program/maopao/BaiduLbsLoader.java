package net.coding.program.maopao;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.AllThirdKeys;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.LocationObject;
import net.coding.program.model.UserObject;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BaiduLbsLoader {

    private static final String host = "http://api.map.baidu.com";
    private static final int PAGE_SIZE = 20; // 每页数量10~20,超过20服务器也只会返回20个
    private static final String RADIUS = "2000";

    private static String getUserId(Context context) {
        UserObject userObject = AccountInfo.loadAccount(context);
        return userObject == null ? "" : String.valueOf(userObject.id);
    }

    public static void searchCustom(Context context, String keyword, double latitude, double longitude, final int page, final LbsResultListener listener) {
        final String path = "/geosearch/v3/nearby";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("ak", AllThirdKeys.ak);
        UserObject userObject = AccountInfo.loadAccount(context);
        if (userObject != null) {
            params.put("filter", String.format("user_id:[%d]", userObject.id));
        }
        params.put("geotable_id", AllThirdKeys.geotable);
        params.put("q", keyword);
        params.put("location", String.format("%f,%f", longitude, latitude));
        params.put("pageIndex", String.valueOf(page));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        params.put("radius", "1000");
        params.put("sortby", "distance:1 ");
        String url;
        try {
            url = host + path + "?" + queryString(params) + "&sn=" + sn(path, params);
        } catch (UnsupportedEncodingException e) {
            Log.e("BaiduLbsLoader", "get url error", e);
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();

        final int searchPos = LocationSearchActivity.getSearchPos();
        client.get(context, url, new SearchResponseHandler(listener) {
            @Override
            protected LocationObject parseItem(JSONObject json) {
                if (json == null) return null;
                String id = json.optString("uid");
                String name = json.optString("title");
                String address = json.optString("address");
                LocationObject object = new LocationObject(id, name, address);
                JSONArray location = json.optJSONArray("location");
                if (location != null && location.length() == 2) {
                    object.longitude = location.optDouble(0, 0);
                    object.latitude = location.optDouble(1, 0);
                }
                object.distance = json.optInt("distance");
                return object;
            }

            @Override
            protected void parseResult(JSONObject json, LbsResultListener listener) {
                int lastPage = (json.optInt("total", 0) + PAGE_SIZE - 1) / PAGE_SIZE - 1;
                JSONArray array = json.optJSONArray("contents");
                if (searchPos == LocationSearchActivity.getSearchPos()) {
                    listener.onSearchResult(true, parseList(array), page < lastPage && array.length() >= PAGE_SIZE);
                }
            }
        });
    }

    // sdk的PoiSearch不支持多关键字搜索，所以用webapi实现
    public static void searchPublic(Context context, String keyword, double latitude, double longitude, final int page, final LbsResultListener listener) {
        final String path = "/place/v2/search";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("ak", AllThirdKeys.ak);
        params.put("location", String.format("%f,%f", latitude, longitude));
        params.put("output", "json");
        params.put("page_num", String.valueOf(page));
        params.put("page_size", String.valueOf(PAGE_SIZE));
        params.put("query", keyword);  // 经测试，复合查询只能用query而不能用q
        params.put("radius", RADIUS);
        params.put("scope", "1");
        String url = buildUrl(path, params);
        if (url == null) {
            listener.onSearchResult(false, null, false);
            return;
        }

        final int searchPos = LocationSearchActivity.getSearchPos();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(context, url, new SearchResponseHandler(listener) {
            @Override
            protected LocationObject parseItem(JSONObject json) {
                if (json == null) return null;
                String id = json.optString("uid");
                String name = json.optString("name");
                String address = json.optString("address");
                LocationObject object = new LocationObject(id, name, address);
                JSONObject location = json.optJSONObject("location");
                if (location != null) {
                    object.latitude = location.optDouble("lat", 0);
                    object.longitude = location.optDouble("lng", 0);
                }
                object.distance = json.optInt("distance");
                return object;
            }

            @Override
            protected void parseResult(JSONObject json, LbsResultListener listener) {
                int lastPage = (json.optInt("total", 0) + PAGE_SIZE - 1) / PAGE_SIZE - 1;
                JSONArray array = json.optJSONArray("results");
                if (searchPos == LocationSearchActivity.getSearchPos()) {
                    listener.onSearchResult(true, parseList(array), page < lastPage && array.length() >= PAGE_SIZE);
                }
            }
        });
    }

    private static String buildUrl(String path, LinkedHashMap<String, String> params) {
        try {
            return host + path + "?" + queryString(params) + "&sn=" + sn(path, params);
        } catch (Exception e) {
            Log.e("BaiduLbsLoader", "build url error", e);
            return null;
        }
    }

    public static void store(Context context, String name, String address, double latitude, double longitude, final StorePoiListener listener) {
        final String path = "/geodata/v3/poi/create";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("address", address);
        params.put("ak", AllThirdKeys.ak);
        params.put("coord_type", "3");
        params.put("geotable_id", AllThirdKeys.geotable);
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("title", name);
        params.put("user_id", getUserId(context));
        try {
            params.put("sn", sn(path, params));
        } catch (UnsupportedEncodingException e) {
            Log.e("BaiduLbsLoader", "get url error", e);
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, host + path, new RequestParams(params), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                String id = null;
                boolean success = json != null && !TextUtils.isEmpty(id = json.optString("id"));
                if (listener != null) listener.onStoreResult(success, id);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (listener != null) listener.onStoreResult(false, null);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (listener != null) listener.onStoreResult(false, null);
            }
        });
    }

    private static String sn(String path, LinkedHashMap<String, String> params) throws UnsupportedEncodingException {
        return md5(URLEncoder.encode(path + "?" + queryString(params) + AllThirdKeys.sk, "UTF-8"));
    }

    private static String queryString(Map<?, ?> data) throws UnsupportedEncodingException {
        StringBuffer qs = new StringBuffer();
        for (Entry<?, ?> pair : data.entrySet()) {
            qs.append(pair.getKey() + "=");
            qs.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
        }
        if (qs.length() > 0) {
            qs.deleteCharAt(qs.length() - 1);
        }
        return qs.toString();
    }

    private static String md5(String src) {
        try {
            byte[] array = MessageDigest.getInstance("MD5").digest(src.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }

    public interface LbsResultListener {
        void onSearchResult(boolean success, List<LocationObject> list, boolean hasMore);
    }

    public interface StorePoiListener {
        void onStoreResult(boolean success, String id);
    }

    private static abstract class SearchResponseHandler extends JsonHttpResponseHandler {
        private LbsResultListener listener;

        SearchResponseHandler(LbsResultListener listener) {
            this.listener = listener;
        }

        protected abstract LocationObject parseItem(JSONObject json);

        protected abstract void parseResult(JSONObject json, LbsResultListener listener);

        protected List<LocationObject> parseList(JSONArray array) {
            ArrayList<LocationObject> list = new ArrayList<>();
            if (array != null) {
                for (int i = 0, c = array.length(); i < c; ++i) {
                    LocationObject item = parseItem(array.optJSONObject(i));
                    if (item != null) list.add(item);
                }
            }
            return list;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
            if (json == null || json.optInt("status") != 0) {
                if (listener != null) listener.onSearchResult(false, null, false);
            } else if (listener != null) {
                parseResult(json, listener);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            if (listener != null) listener.onSearchResult(false, null, false);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            if (listener != null) listener.onSearchResult(false, null, false);
        }
    }
}