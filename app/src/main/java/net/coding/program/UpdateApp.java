package net.coding.program;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.third.UpdateManager;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by chaochen on 14-12-4.
 */
public class UpdateApp {

    private UpdateManager mUpdateManager;
    private Context mContext;

    public UpdateApp(Context context) {
        mContext = context;
    }

    public void runInBackground() {
        if (isWifiConnected(mContext)) {
            Log.d("", "ddd is wifi");
            run(true);
        }
    }

    private boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activityNetwork = mConnectivityManager.getActiveNetworkInfo();
            return activityNetwork != null && activityNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public void deleteOldApk() {
        UpdateManager.deleteOldApk(mContext, getVersion());
    }

    public void runForeground() {
        if (isConnect(mContext)) {
            run(false);
        } else {
            Toast.makeText(mContext, "没有网络连接", Toast.LENGTH_LONG).show();
        }
    }

    boolean isConnect(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /*
     * @param downloadNotify
     * 是否下载完了再通知客户有更新
     */
    private void run(final boolean background) {
        AsyncHttpClient client = MyAsyncHttpClient.createClient(mContext);
        client.get(mContext, Global.HOST + "/api/update/app", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int newVersion = response.getInt("build");
                    String newMessage = response.getString("message");
                    int required = response.getInt("required");
                    String url = response.getString("url");

                    int versionCode = getVersion();

                    if (newVersion > versionCode) {
                        if (!background) {
                            mUpdateManager.runForeground();
                        } else {
                            SharedPreferences share = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
                            int jumpVersion = share.getInt("jump", 0);


                            if (newVersion > jumpVersion) {
                                UpdateManager mUpdateManager = new UpdateManager(mContext, url, newMessage,
                                        newVersion, required);
                                mUpdateManager.runBackground();
                            }
                        }
                    } else {
                        if (!background) {
                            Toast.makeText(mContext, "你的软件已经是最新版本", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    private int getVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (Exception e) {
            Global.errorLog(e);
        }

        return 1;
    }


}
