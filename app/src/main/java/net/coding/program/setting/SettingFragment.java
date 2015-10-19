package net.coding.program.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.MainActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.FileUtil;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.detail.file.FileSaveHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.regex.Pattern;

@EFragment(R.layout.fragment_setting)
public class SettingFragment extends BaseFragment {

    private static final int RESULT_ABOUT_ACTIVITY = 1;
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
        String path = new FileSaveHelp(getActivity()).getFileDownloadPath();

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
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.show();
        ((BaseActivity) getActivity()).dialogTitleLineColor(dialog);
    }

    @Click
    void feedback() {
        FeedbackActivity_.intent(getActivity()).start();
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
    void loginOut() {
        showDialog(MyApp.sUserObject.global_key, "退出当前账号?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                XGPushManager.registerPush(getActivity(), "*");
                AccountInfo.loginOut(getActivity());
                startActivity(new Intent(getActivity(), GuideActivity.class));
                getActivity().finish();
            }
        });
    }
}
