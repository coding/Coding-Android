package net.coding.program.project.init;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import net.coding.program.MainActivity_;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
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

   public  static boolean  saveBitmap2file(Bitmap bmp,String filename){
        Bitmap.CompressFormat format= Bitmap.CompressFormat.PNG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    private static String getDefaultIconName(String iconUrl){
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(iconUrl);
        return m.replaceAll("").trim();
    }

    //默认的随机图片上传处理相关，目前的处理方式不是很理想,后期建议接口能够避免上传默认图片文件，改为上传图片url
    public static String getDefaultIconPath(Context context,Bitmap bitmap,String iconUrl){
        String defaultIconName=getDefaultIconName(iconUrl)+".png";
        File defaultIcon=new File(getDiskCacheDir(context,"icon"),defaultIconName);
        if (defaultIcon.exists()){
            return defaultIcon.getAbsolutePath();
        }
        String pathCacheDiskPath=getDiskCacheDir(context,"icon").getAbsolutePath();
        if (saveBitmap2file(bitmap,pathCacheDiskPath+File.separator+defaultIconName)){
            return defaultIcon.getAbsolutePath();
        }
        return null;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        File cacheFilePath=new File(cachePath + File.separator + uniqueName);
        if (!cacheFilePath.exists()){
            cacheFilePath.mkdir();
        }
        return cacheFilePath;
    }


}
