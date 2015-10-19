package net.coding.program.project.detail.file;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import net.coding.program.common.FileUtil;

import java.io.File;

/**
 * Created by chenchao on 15/8/25.
 * 帮助获取文件保存目录
 */
public class FileSaveHelp {

    private SharedPreferences share;
    private String defaultPath;

    public FileSaveHelp(Context context) {
        share = context.getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
    }

    public String getFileDownloadPath() {
        String path;
        if (share.contains(FileUtil.DOWNLOAD_PATH)) {
            path = share.getString(FileUtil.DOWNLOAD_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER);
        } else {
            path = defaultPath;
        }
        return path;
    }

    public void alwaysHideHint() {
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean(FileUtil.DOWNLOAD_SETTING_HINT, true);
        editor.commit();
    }

    public boolean needShowHint() {
        return !share.contains(FileUtil.DOWNLOAD_SETTING_HINT);
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public static String getFileDownloadPath(Context context) {
        if (context == null) {
            return Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
        }

        return  new FileSaveHelp(context).getFileDownloadPath();
    }

    public static String getFileDownloadAbsolutePath(Context context) {
        String path = new FileSaveHelp(context).getFileDownloadPath();
        return FileUtil.getDestinationInExternalPublicDir(path);
    }
}
