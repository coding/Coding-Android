package net.coding.program.common;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import net.coding.program.common.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import top.zibin.luban.Luban;

/**
 * Created by chaochen on 14-9-22.
 */
public class PhotoOperate {

    private Context context;

    public PhotoOperate(Context context) {
        this.context = context;
    }

    private static void copyFileUsingFileChannels(File source, File dest)
            throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (Exception e) {
            Global.errorLog(e);
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    private File getTempFile(Context context) {
        File file = null;
        try {
            String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            file = File.createTempFile(fileName, ".jpg", context.getCacheDir());
        } catch (IOException e) {
        }
        return file;
    }

    public File getFile(Uri fileUri) throws Exception {
        String path = FileUtil.getPath(context, fileUri);
        return new File(translatePath(path));
    }

    public File scal(Uri fileUri) throws Exception {
        String path = FileUtil.getPath(context, fileUri);
        String prefix = "file://";
        if (path.toLowerCase().startsWith(prefix)) {
            path = path.substring(prefix.length(), path.length());
        }

        File oldFile = new File(path);
        if (Global.isGif(path)) {
            return oldFile;
        }

        try {
            return Luban.with(GlobalData.getInstance()).load(oldFile).get().get(0);
        } catch (Exception e) {
            return oldFile;
        }
    }

    @NonNull
    public static String translatePath(String path) {
        String prefix = "file://";
        if (path.toLowerCase().startsWith(prefix)) {
            path = path.substring(prefix.length(), path.length());
        }
        return path;
    }

}
