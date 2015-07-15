package net.coding.program.project.init;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import net.coding.program.MainActivity_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jack wang on 2015/4/1.
 */
public class InitProUtils {

    public static final String FLAG_REFRESH = "init.pro.refresh";
    public static final String FLAG_UPDATE_DYNAMIC = "FLAG_UPDATE_DYNAMIC";

    public static final int REQUEST_PRO_UPDATE = 1001;

    //跳转到主界面并刷新项目列表
    public static void intentToMain(Context context) {
        Intent intent = new Intent(context, MainActivity_.class);
        intent.putExtra("action", FLAG_REFRESH);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void updateDynamic(Activity activity, int projectId) {
        Intent intent = new Intent();
        intent.putExtra("action", FLAG_UPDATE_DYNAMIC);
        intent.putExtra("projectId", projectId);
        activity.setResult(Activity.RESULT_OK, intent);
    }

    //返回到主界面并刷新项目列表
    public static void backIntentToMain(Activity activity) {
        Intent intent = new Intent();
        intent.putExtra("action", FLAG_REFRESH);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public static boolean textValidate(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        Matcher matcher = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]+$").matcher(text);
        return matcher.find();
    }

    public static void hideSoftInput(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}
