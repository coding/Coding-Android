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

    public static final String[] STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] CAMERA_STORAGE = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] CAMERA = new String[]{
            Manifest.permission.CAMERA,
    };

    public static final String[] LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };

}
