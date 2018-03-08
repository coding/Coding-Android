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

        CodingPush.INSTANCE.initApplication(this, (context, params) -> {
            String url = params.get("param_url");
            String id = params.get("notification_id");
            if (TextUtils.isEmpty(url)) {
                return;
            }

            openNewActivityFromMain(context, url);

            if (!TextUtils.isEmpty(id)) {
                AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
                final String host = Global.HOST_API + "/notification/mark-read?id=%s";
                client.post(String.format(host, id), new JsonHttpResponseHandler() {
                });
            }
        });

        Global.AUTHOR = "net.coding.program.fileprovider";
    }
}
