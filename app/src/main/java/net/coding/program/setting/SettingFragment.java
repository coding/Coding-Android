package net.coding.program.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.MainActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.FileUtil;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.detail.file.FileSaveHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.regex.Pattern;

@EFragment(R.layout.fragment_setting)
public class SettingFragment extends BaseFragment {

    private static final int RESULT_ABOUT_ACTIVITY = 1;

    @ViewById
    CheckBox allNotify;

    @ViewById
    TextView cacheSize;

    @AfterViews
    void init() {
        boolean mLastNotifySetting = AccountInfo.getNeedPush(getActivity());
        allNotify.setChecked(mLastNotifySetting);
        setHasOptionsMenu(true);

        updateCacheSize();
    }

    @Background
    void updateCacheSize() {
        File[] cacheDir = getAllCacheDir();

        long size = 0;
        for (File dir : cacheDir) {
            size += getFileSize(dir);
        }
        String sizeString = String.format("%.2f MB", (double) size / 1024 /1024);

        dispayCacheSize(sizeString);
    }

    File[] getAllCacheDir() {
        return new File[] {
                getActivity().getCacheDir(),
                getActivity().getExternalCacheDir()
        };
    }

    long getFileSize(File file) {
        if (file == null) {
            return 0;
        }

        if (file.isDirectory()) {
            long size = 0;
            for (File item : file.listFiles()) {
                size += getFileSize(item);
            }
            return size;
        } else {
            return file.length();
        }
    }

    void deleteFiles(File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                deleteFiles(item);
            }
            file.delete();
        } else if (file.isFile()){
            file.delete();
        }
    }

    @UiThread
    void dispayCacheSize(String size) {
        cacheSize.setText(size);
    }

    @Click
    void accountSetting() {
        AccountSetting_.intent(this).start();
    }

    @Click
    void pushSetting() {
//        NotifySetting_.intent(this).start();
        allNotify.performClick();
    }

    @Click
    void allNotify() {
        AccountInfo.setNeedPush(getActivity(), allNotify.isChecked());
        Intent intent = new Intent(MainActivity.BroadcastPushStyle);
        getActivity().sendBroadcast(intent);
    }

    @Click
    void downloadPathSetting() {
        final SharedPreferences share = getActivity().getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        String path = new FileSaveHelp(getActivity()).getFileDownloadPath();

        LayoutInflater li = LayoutInflater.from(getActivity());
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        final String oldPath = path;
        input.setText(oldPath);
        new AlertDialog.Builder(getActivity())
                .setTitle("下载路径设置")
                .setView(v1)
                .setPositiveButton("确定", (dialog, which) -> {
                    String newPath = input.getText().toString();
                    final String namePatternStr = "[,`~!@#$%^&*:;()''\"\"><|.\\ =]";// if(folder.name.match(/[,`~!@#$%^&*:;()''""><|.\ /=]/g))
                    Pattern namePattern = Pattern.compile(namePatternStr);
                    if (newPath.equals("")) {
                        showButtomToast("路径不能为空");
                    } else if (namePattern.matcher(newPath).find()) {
                        showButtomToast("路径：" + newPath + " 不能采用");
                    } else if (!oldPath.equals(newPath)) {
                        SharedPreferences.Editor editor = share.edit();
                        editor.putString(FileUtil.DOWNLOAD_PATH, newPath);
                        editor.commit();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Click
    void aboutCoding() {
        AboutActivity_.intent(SettingFragment.this).startForResult(RESULT_ABOUT_ACTIVITY);
    }

    @OnActivityResult(RESULT_ABOUT_ACTIVITY)
    void resultAboutActivity(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            AccountInfo.loginOut(getActivity());
            getActivity().finish();
            System.exit(0);
        }
    }

    @Click
    void clearCache() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.clear_cache_message)
                .setPositiveButton("确定", ((dialog, which) -> {
                    File[] cacheDir = getAllCacheDir();
                    for (File item : cacheDir) {
                        deleteFiles(item);
                    }
                    showMiddleToast("清除缓存成功");

                    updateCacheSize();
                }))
                .setNegativeButton("取消", null)
                .show();
    }

    @Click
    void loginOut() {
        showDialog(MyApp.sUserObject.global_key, "退出当前账号?", (dialog, which) -> {
            XGPushManager.registerPush(getActivity(), "*");
            AccountInfo.loginOut(getActivity());
            startActivity(new Intent(getActivity(), GuideActivity.class));
            getActivity().finish();
        });
    }
}
