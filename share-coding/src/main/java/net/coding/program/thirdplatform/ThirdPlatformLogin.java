package net.coding.program.thirdplatform;

import android.app.Activity;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * Created by chenchao on 2017/12/20.
 */

public class ThirdPlatformLogin {

    public static void loginByWeixin(Activity activity, UMAuthListener authListener) {
        UMShareAPI.get(activity).doOauthVerify(activity, SHARE_MEDIA.WEIXIN, authListener);
    }

}
