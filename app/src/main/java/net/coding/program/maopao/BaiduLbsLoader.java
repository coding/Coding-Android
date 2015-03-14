package net.coding.program.maopao;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

import static android.location.Location.convert;

public class BaiduLbsLoader {
    private static final String host = "http://api.map.baidu.com";
    private static final String geotable = "95956";
    private static final String sk = "qRxSyyCxklLkAbZgaIbG6IxGtSXfQ9vP";
    private static final String ak = "sXosVPjCcDXGkQG7YFKfpNof";
    private static final int PAGE_SIZE = 10;

    public static String getUserId(Context context) {
        UserObject userObject = AccountInfo.loadAccount(context);
        return userObject == null ? "" : String.valueOf(userObject.id);
    }

    public static interface LbsResultListener{
        void onSearchResult(boolean success, List<LocationObject>  list, boolean hasMore);
    }

    public static void search(Context context, String keyword, double latitude, double longitude,final int page, final LbsResultListener listener) {
        final String path = "/geosearch/v3/nearby";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("ak", ak);
//        params.put("filter", "userid:" +  AccountInfo.loadAccount(context).id);
        params.put("geotable_id", geotable);
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
        client.get(context, url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                int total = json.optInt("total");
                listener.onSearchResult(true,parseList(json), page * PAGE_SIZE < total);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("BaiduLbsLoader", "search " + statusCode + ", " + throwable.toString());
                listener.onSearchResult(false, null, false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e("BaiduLbsLoader", "search " + statusCode + ", " + responseString);
                listener.onSearchResult(false, null, false);
            }
        });
    }

    public static List<LocationObject> parseList(JSONObject json) {
        ArrayList<LocationObject> list = new ArrayList<>();
        if (json != null) {
            JSONArray array = json.optJSONArray("contents");
            if (array != null) {
                for (int i = 0, c = array.length(); i < c; ++i) {
                    LocationObject item = parseItem(array.optJSONObject(i));
                    if (item != null) list.add(item);
                }
            }
        }
        return list;
    }

    public static LocationObject parseItem(JSONObject json) {
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
        return object;
    }

    public static void store(Context context, String name, String address, double latitude, double longitude) {
        final String path = "/geodata/v3/poi/create";
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("address", address);
        params.put("ak", ak);
        params.put("coord_type", "3");
        params.put("geotable_id", geotable);
        params.put("latitude", String.valueOf(latitude));
        params.put("longitude", String.valueOf(longitude));
        params.put("title", name);
        params.put("userid", getUserId(context));

        try {
            params.put("sn", sn(path, params));
        } catch (UnsupportedEncodingException e) {
            Log.e("BaiduLbsLoader", "get url error", e);
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, host + path, new RequestParams(params), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("BaiduLbsLoader", "store " + statusCode + ", " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("BaiduLbsLoader", "store " + statusCode + ", " + throwable.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                Log.e("BaiduLbsLoader", "store " + statusCode + ", " + responseString);
            }
        });
    }

    private static String sn(String path, LinkedHashMap<String, String> params) throws UnsupportedEncodingException {
        return md5(URLEncoder.encode(path + "?" + queryString(params) + sk, "UTF-8"));
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
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }
}