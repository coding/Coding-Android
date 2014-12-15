package net.coding.program.common;

import android.content.Context;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.common.network.MyAsyncHttpClient;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by chaochen on 14-9-27.
 */
public class UnreadNotify {

    public static void update(Context context) {
        final MyApp myApp = (MyApp) context.getApplicationContext();
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);

        client.get(Global.HOST + "/api/user/unread-count", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getInt("code") == 0) {
                        JSONObject json = response.getJSONObject("data");

                        Unread unread = new Unread(json);

                        try {
                            myApp.sUnread = unread;
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    }

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    throw new Exception(errorResponse.toString());
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }
        });
    }

    public static void displayNotify(BadgeView badgeView, String messageCount) {
        if (messageCount.isEmpty()) {
            badgeView.setVisibility(View.INVISIBLE);
        } else {
            badgeView.setText(messageCount);
            badgeView.setVisibility(View.VISIBLE);
        }
    }
}
