package net.coding.program.third;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateManager {

    private Context mContext;

    private String updateMsg = "";
    private String apkUrl = "";
    private int mType;
    private int mVersion;

    private Dialog noticeDialog;

    /* 下载包安装路径 */
    private static final String savePath = "/sdcard/download/";

    private static String saveFileName;

    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int DOWN_OVER_MY = 3;

    private int progress;

    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (mProgress != null) {
                        mProgress.setProgress(progress);
                    }
                    break;

                case DOWN_OVER:
                    installApk();
                    break;

                case DOWN_OVER_MY:
                    showNoticeDialog();
                    break;

                default:
                    break;
            }
        }
    };

    public static void deleteOldApk(Context context, int version) {

        SharedPreferences share = context.getSharedPreferences("version", Context.MODE_PRIVATE);
        int jumpVersion = share.getInt("jump", 0);

        deleteFile(version);
        deleteFile(jumpVersion);
    }

    private static void deleteFile(int version) {
        String path = savePath + "coding" + version + ".apk";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public UpdateManager(Context context, String url, String msg, int version, int type) {
        this.mContext = context;

        apkUrl = url;
        updateMsg = msg;
        mType = type;
        mVersion = version;

        saveFileName = savePath + "coding" + version + ".apk";
    }

    public void runBackground() {
        if (isDownload()) {
            showNoticeDialog();
            return;
        }

        downloadApk(DOWN_OVER_MY);
    }

    //外部接口让主Activity调用
    public void runForeground() {
        showNoticeDialog();
    }

    private boolean isDownload() {
        File apkfile = new File(saveFileName);
        return apkfile.exists();
    }


    private void showNoticeDialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);

        if (isDownload()) {
            builder.setPositiveButton("安装", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installApk();
                }
            });
        } else {
            builder.setPositiveButton("下载", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (noticeDialog.isShowing()) {
                        noticeDialog.cancel();
                        showDownloadDialog();
                    }
                }
            });
        }

        builder.setCancelable(false);

        if (mType == 1 || mType == 0) {
            builder.setNegativeButton("以后再说", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton("跳过", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SharedPreferences share = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = share.edit();
                    editor.putInt("jump", mVersion);
                    editor.commit();
                }
            });
        }

        noticeDialog = builder.create();
        noticeDialog.show();
        CustomDialog.dialogTitleLineColor(mContext, noticeDialog);
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });

        builder.setCancelable(false);
        Dialog downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk(DOWN_OVER);
    }

    private void downloadApk(final int downloadFinish) {
        Thread downLoadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apkUrl);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    String apkFile = saveFileName;
                    File ApkFile = new File(apkFile);
                    FileOutputStream fos = new FileOutputStream(ApkFile);

                    int count = 0;
                    byte buf[] = new byte[1024];

                    do {
                        int numread = is.read(buf);
                        count += numread;
                        progress = (int) (((float) count / length) * 100);
                        //更新进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                        if (numread <= 0) {
                            //下载完成通知安装
                            mHandler.sendEmptyMessage(downloadFinish);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (!interceptFlag);//点击取消就停止下载.

                    fos.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        downLoadThread.start();
    }

    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
