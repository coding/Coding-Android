package net.coding.program;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import net.coding.program.common.PushReceiver;
import net.coding.program.common.Unread;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.third.MyImageDownloader;

/**
 * Created by cc191954 on 14-8-9.
 */
public class MyApp extends Application {

    public static float sScale;
    public static int sWidthDp;
    public static int sWidthPix;

    public static int sEmojiNormal;
    public static int sEmojiMonkey;

    public static UserObject sUserObject;
    public static Unread sUnread;
    public static boolean sMainCreate = false;

    public static void setMainActivityState(boolean create) {
        sMainCreate = create;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        receiverPushBroadcast();

        initImageLoader(this);

        sScale = getResources().getDisplayMetrics().density;
        sWidthPix = getResources().getDisplayMetrics().widthPixels;
        sWidthDp = (int) (sWidthPix / sScale);

        sEmojiNormal = getResources().getDimensionPixelSize(R.dimen.emoji_normal);
        sEmojiMonkey = getResources().getDimensionPixelSize(R.dimen.emoji_monkey);

        sUserObject = AccountInfo.loadAccount(this);
        sUnread = new Unread();
    }

    public static final String PushClickBroadcast = "PushClickBroadcast";

    private void receiverPushBroadcast() {
        IntentFilter filter = new IntentFilter(PushClickBroadcast);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String url = intent.getStringExtra("data");

                if (url != null) {
                    closeNotify(url);
                    if (sMainCreate) {
                        Global.openActivityByUri(context, url, true);
                    } else {
                        Intent mainIntent = new Intent(context, MainActivity_.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.putExtra("mPushUrl", url);
                        context.startActivity(mainIntent);
                    }
                }

                String id = intent.getStringExtra("id");
                if (id != null && !id.isEmpty()) {
                    AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
                    final String host = "https://coding.net/api/notification/mark-read?id=%s";
                    client.post(String.format(host, id), new JsonHttpResponseHandler() {
                    });
                }
            }
        }, filter);
    }

    // 冒泡
    // https://coding.net/api/tweet/8206503/9275
    // 讨论
    // https://coding.net/u/8206503/p/AndroidCoding/topic/9243?page=1
    // 任务
    // https://coding.net/u/8206503/p/AndroidCoding/task/11664
    // 粉丝
    // new_fans
    // 私信
    // new_message
    private void closeNotify(String url) {
        String notifys[] = PushReceiver.sNotify;
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        for (int i = 0; i < notifys.length; ++i) {
            if (url.equals(notifys[i])) {
                mNotificationManager.cancel(i);
            }
        }
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .diskCacheFileCount(300)
                .imageDownloader(new MyImageDownloader(context))
                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                .writeDebugLogs() // Remove for release app
                .diskCacheExtraOptions(sWidthPix / 3, sWidthPix / 3, null)
                .build();

        ImageLoader.getInstance().init(config);
    }

}
