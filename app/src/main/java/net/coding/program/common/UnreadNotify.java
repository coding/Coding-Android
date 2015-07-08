package net.coding.program.common;

import android.content.Context;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.MyApp;
import net.coding.program.common.network.MyAsyncHttpClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by chaochen on 14-9-27.
 */
public class UnreadNotify {

    public static void update(Context context) {
        final MyApp myApp = (MyApp) context.getApplicationContext();
        AsyncHttpClient client = MyAsyncHttpClient.createClient(context);

        client.get(Global.HOST_API + "/user/unread-count", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getInt("code") == 0) {
                        JSONObject json = response.getJSONObject("data");
                        Unread unread = new Unread(json);
                        MyApp.sUnread = unread;

                        UnreadNotifySubject.getInstance().notifyObserver();
                    }

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

    public interface UnreadNotifyObserver {
        void update();
    }

    public static class UnreadNotifySubject {

        private static UnreadNotifySubject sInstance;
        private ArrayList<WeakReference<UnreadNotifyObserver>> mArray = new ArrayList<>();

        private UnreadNotifySubject() {
        }

        public static UnreadNotifySubject getInstance() {
            if (sInstance == null) {
                sInstance = new UnreadNotifySubject();
            }
            return sInstance;
        }

        public void registerObserver(UnreadNotifyObserver observer) {
            for (WeakReference<UnreadNotifyObserver> item : mArray) {
                if (item.get() == observer) {
                    return;
                }
            }

            mArray.add(new WeakReference<>(observer));
        }

        public void unregisterObserver(UnreadNotifyObserver observer) {
            for (int i = 0; i < mArray.size(); ++i) {
                if (mArray.get(i).get() == observer) {
                    mArray.remove(i);
                    break;
                }
            }
        }

        public void notifyObserver() {
            for (int i = 0; i < mArray.size(); ++i) {
                UnreadNotifyObserver observer = mArray.get(i).get();
                if (observer != null) {
                    observer.update();
                }
            }
        }

    }
}
