package net.coding.program;

import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.CodingCompatImp;
import net.coding.program.push.CodingPush;

public class PersonApp extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        AllThirdKeys.initInApp(this);

        CodingCompat.init(new CodingCompatImp());

        CodingPush.instance().initApplication(this, (context, params) -> {
            if (params == null) {
                return;
            }
            String url = params.get("param_url");
            String id = params.get("notification_id");
            if (TextUtils.isEmpty(url)) {
                return;
            }

            if (url != null) {
                openNewActivityFromMain(context, url);
            }

            if (id != null && !id.isEmpty()) {
                AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
                final String host = Global.HOST_API + "/notification/mark-read?id=%s";
                client.post(String.format(host, id), new JsonHttpResponseHandler() {
                });
            }

        });
    }
}
