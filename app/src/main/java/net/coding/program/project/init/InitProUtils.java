package net.coding.program.project.init;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.coding.program.MainActivity_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jack wang on 2015/4/1.
 */
public class InitProUtils {

    public static final String FLAG_REFRESH="init.pro.refresh";

    //跳转到主界面并刷新项目列表
    public static void intentToMain(Context context){
        Intent intent = new Intent(context, MainActivity_.class);
        intent.putExtra("action", FLAG_REFRESH);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static boolean textValidate(String text){
        if (TextUtils.isEmpty(text)){
            return false;
        }
        Matcher matcher= Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]+$").matcher(text);
        if (matcher.find()){
            return true;
        }
        return false;
    }

}
