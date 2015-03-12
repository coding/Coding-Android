package net.coding.program.common;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.MyApp;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AccountInfo;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by chaochen on 15/1/5.
 */
public class LoginBackground {

    private Context context;

    final String URL_DOWNLOAD = Global.HOST + "/api/wallpaper/wallpapers?type=4";

    public LoginBackground(Context context) {
        this.context = context;
    }

    public void update() {
        if (!Global.isWifiConnected(context)) {
            return;
        }

        if (needUpdate()) {
            AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
            client.get(URL_DOWNLOAD, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("", "getDataFail1");
                    if (response.optInt("code", -1) == 0) {
                        ArrayList<PhotoItem> photoItems = new ArrayList();
                        JSONArray data = response.optJSONArray("data");
                        for (int i = 0; i < data.length(); ++i) {
                            PhotoItem item = new PhotoItem(data.optJSONObject(i));
                            photoItems.add(item);
                        }

                        AccountInfo.saveBackgrounds(context, photoItems);
                        downloadPhotos();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                }

                @Override
                public void onFinish() {
                    AccountInfo.setCheckLoginBackground(context);
                }
            });
        } else {
            downloadPhotos();
        }
    }

    public PhotoItem getPhoto() {
        ArrayList<PhotoItem> list = AccountInfo.loadBackgrounds(context);
        ArrayList<PhotoItem> cached = new ArrayList();
        for (PhotoItem item : list) {
            if (item.isCached(context)) {
                cached.add(item);
            }
        }

        int max = cached.size();
        if (max == 0) {
            return new PhotoItem();
        }

        int index = new Random().nextInt(max);
        return cached.get(index);
    }

    public int getPhotoCount() {
        return AccountInfo.loadBackgrounds(context).size();
    }

    private boolean needUpdate() {
        return true;
    }

    private void downloadPhotos() {
        if (!Global.isWifiConnected(context)) {
            return;
        }

        ArrayList<PhotoItem> lists = AccountInfo.loadBackgrounds(context);
        for (PhotoItem item : lists) {
            File file = item.getCacheFile(context);
            if (!file.exists()) {
                AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
                String url = String.format("%s?imageMogr2/thumbnail/!%d", item.getUrl(), MyApp.sWidthPix);
                client.get(context, url, new FileAsyncHttpResponseHandler(file) {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, File file) {

                    }
                });
                // 图片较大，可能有几兆，超时设长一点
                client.setTimeout(10 * 60 * 1000);
            }
        }
    }

    public static class PhotoItem implements Serializable {
        Group group = new Group();
        String url = "";

        public PhotoItem(JSONObject json) {
            group = new Group(json.optJSONObject("group"));
            url = json.optString("url");
        }

        public PhotoItem() {
        }

        class Group implements Serializable {
            String name = "";
            String author = "";
            String link = "";
            String description = "";
            int id;

            Group(JSONObject json) {
                name = json.optString("name");
                author = json.optString("author");
                link = json.optString("link");
                description = json.optString("description");
                id = json.optInt("id");
            }

            Group() {
            }
        }

        public String getUrl() {
            return url;
        }

        // 表明不要显示其它的控件，只显示图片
        public boolean isGuoguo() {
            return group.author.equals("guoguo");
        }

        public String getTitle() {
            return String.format("%s © %s", group.name, group.author);
        }

        private String getCacheName() {
            return String.valueOf(group.id);
        }

        public File getCacheFile(Context ctx) {
            File file = new File(getPhotoDir(ctx), getCacheName());
            return file;
        }

        public boolean isCached(Context ctx) {
            return getCacheFile(ctx).exists();
        }

        private File getPhotoDir(Context ctx) {
            final String dirName = "BACKGROUND";
            File root = ctx.getExternalFilesDir(null);
            File dir = new File(root, dirName);
            if (!dir.exists() || dir.isDirectory()) {
                dir.mkdir();
            }

            return dir;
        }
    }
}
