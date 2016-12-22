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
    public static boolean writeExtralStorage(Activity activity) {
        final int result = 2001;
        final String[] permission = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        return checkPermission(activity, result, permission, "开启存储空间权限后才能下载");
    }

    public static boolean checkCamera(Activity activity) {
        final int result = 2002;
        final String[] permission = {
                Manifest.permission.CAMERA
        };

        return checkPermission(activity, result, permission, "开启相机权限后才能使用");
    }

    public static boolean checkMicrophone(Activity activity) {
        final int result = 2003;
        final String[] permission = {
                Manifest.permission.RECORD_AUDIO
        };

        return checkPermission(activity, result, permission, "开启麦克风权限后才能使用");
    }

    public static boolean checkLocation(Activity activity) {
        final int result = 2004;
        final String[] permission = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        return checkPermission(activity, result, permission, "开启位置信息权限后才能使用");
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
