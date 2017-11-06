package net.coding.program.push.xiaomi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;
import java.util.Map;

public class XiaomiPush implements PushAction {

    // user your appid the key.
    private static final String APP_ID = "2882303761517260238";
    // user your appid the key.
    private static final String APP_KEY = "5861726013238";

    public static final String TAG = "CodingPush xiaomi";

    static CommonPushClick clickPushAction = new CommonPushClick() {
        @Override
        public void click(Context context, Map<String, String> params) {

        }
    };

    @Override
    public boolean init(Context context, CommonPushClick clickPushActionName) {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit(context)) {
            MiPushClient.registerPush(context, APP_ID, APP_KEY);

            LoggerInterface newLogger = new LoggerInterface() {

                @Override
                public void setTag(String tag) {
                    // ignore
                }

                @Override
                public void log(String content, Throwable t) {
                    Log.d(TAG, content, t);
                }

                @Override
                public void log(String content) {
                    Log.d(TAG, content);
                }
            };
            Logger.setLogger(context, newLogger);
            Log.d(PushAction.TAG, "use xiaomi push true");

            clickPushAction = clickPushActionName;

            return true;
        }
        Log.d(PushAction.TAG, "use xiaomi push false");

        return false;
    }

    @Override
    public void bindGK(Context context, String gk) {
        Log.d(PushAction.TAG, "use xiaomi push bind " + gk);
        MiPushClient.setUserAccount(context, gk, null);
    }

    @Override
    public void unbindGK(Context context, String gk) {
        Log.d(PushAction.TAG, "use xiaomi push unbind " + gk);
        MiPushClient.unsetUserAccount(context, gk, null);
    }

    private boolean shouldInit(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

}
