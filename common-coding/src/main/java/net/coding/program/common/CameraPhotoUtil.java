package net.coding.program.common;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import net.coding.program.common.widget.FileProviderHelp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chaochen on 15/1/13.
 */
public class CameraPhotoUtil {
    public static Uri getOutputMediaFileUri() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp
                + ".jpg");

        return Uri.fromFile(mediaFile);
    }

    public static File getCacheFile(Context context) {
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(context.getExternalCacheDir(),  "IMG_" + timeStamp
                + ".jpg");
//        try {
//            mediaFile.createNewFile();
//        } catch (IOException e) {
//            Global.errorLog(e);
//        }

        return mediaFile;
    }

    public static Uri fileToUri(Context context, File file) {
        return FileProviderHelp.getUriForFile(context, file);
    }
}
