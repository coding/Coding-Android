package net.coding.program.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.BaseActivity;
import net.coding.program.LoginActivity_;
import net.coding.program.MainActivity;
import net.coding.program.R;
import net.coding.program.common.FileUtil;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.SearchProjectActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.regex.Pattern;

@EFragment(R.layout.fragment_setting)
public class SettingFragment extends BaseFragment {

    @ViewById
    CheckBox allNotify;

    @AfterViews
    void init() {
        boolean mLastNotifySetting = AccountInfo.getNeedPush(getActivity());
        allNotify.setChecked(mLastNotifySetting);
        setHasOptionsMenu(true);
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
        String path = "";
        final String defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
        if (share.contains(FileUtil.DOWNLOAD_PATH)) {
            path = share.getString(FileUtil.DOWNLOAD_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER);
        } else {
            path = defaultPath;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater li = LayoutInflater.from(getActivity());
        View v1 = li.inflate(R.layout.dialog_input, null);
        final EditText input = (EditText) v1.findViewById(R.id.value);
        final String oldPath = path;
        input.setText(oldPath);
        builder.setTitle("下载路径设置")
                .setView(v1).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            }
        }).setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        ((BaseActivity) getActivity()).dialogTitleLineColor(dialog);
        input.requestFocus();
    }

    @Click
    void feedback() {
        FeedbackActivity_.intent(getActivity()).start();
    }

    @Click
    void aboutCoding() {
        AboutActivity_.intent(SettingFragment.this).start();
    }

    @Click
    void loginOut() {
        XGPushManager.registerPush(getActivity(), "*");
        AccountInfo.loginOut(getActivity());
        startActivity(new Intent(getActivity(), LoginActivity_.class));
        getActivity().finish();
    }

}
