package net.coding.program.common.util;

import android.app.Activity;
import android.support.v4.app.Fragment;

import net.coding.program.common.TermsActivity_;

/**
 * Created by chenchao on 15/12/31.
 * 方便启动一些公用的 Activity，如用户协议
 */
public class ActivityNavigate {

    public static void startTermActivity(Activity activity) {
        TermsActivity_.intent(activity).start();
    }

    public static void startTermActivity(Fragment activity) {
        TermsActivity_.intent(activity).start();
    }
}
