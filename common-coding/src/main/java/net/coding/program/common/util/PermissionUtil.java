package net.coding.program.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 2016/12/16.
 * 检查权限
 */
public class PermissionUtil {

    public static final int RESULT_CAMERA = 2002;
    public static final int RESULT_MICROPHONE = 2003;
    public static final int RESULT_LOCATION = 2004;
    public static final int RESULT_STORAGE = 2001;
    public static final int RESULT_CAMERA_STORAGE = 2005;
    public static final int RESULT_PHONE_STATUS = 2006;

    public static final String[] PERMISSION_STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static boolean writeExtralStorage(Activity activity) {
        return checkPermission(activity, RESULT_STORAGE, PERMISSION_STORAGE, "请开启 \"存储空间\" 权限");
    }

    public static boolean checkCamera(Activity activity) {
        final String[] permission = {
                Manifest.permission.CAMERA
        };

        return checkPermission(activity, RESULT_CAMERA, permission, "开启相机权限后才能使用");
    }

    public static boolean checkMicrophone(Activity activity) {
        final String[] permission = {
                Manifest.permission.RECORD_AUDIO
        };

        return checkPermission(activity, RESULT_MICROPHONE, permission, "开启麦克风权限后才能使用");
    }

    public static boolean checkPhoneState(Activity activity) {
        final String[] permission = {
                Manifest.permission.READ_PHONE_STATE
        };

        return checkPermission(activity, RESULT_PHONE_STATUS, permission, "开启 \"电话\" 权限后才能使用推送");
    }

    public static boolean checkLocation(Activity activity) {
        final String[] permission = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        return checkPermission(activity, RESULT_LOCATION, permission, "开启位置信息权限后才能使用");
    }


    private static boolean checkPermission(Activity activity, int result, String[] permission, String tipString) {
        List<String> needApply = new ArrayList<>();
        for (String item : permission) {
            if (ActivityCompat.checkSelfPermission(activity, item) != PackageManager.PERMISSION_GRANTED) {
                needApply.add(item);
            }
        }

        if (needApply.isEmpty()) {
            return true;
        }

        String[] applys = new String[needApply.size()];
        applys = needApply.toArray(applys);

        Toast.makeText(activity, tipString, Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(
                activity,
                applys,
                result
        );

        return false;
    }
}
