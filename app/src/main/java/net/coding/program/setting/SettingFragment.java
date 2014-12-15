package net.coding.program.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.BaseFragmentActivity;
import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.UpdateApp;
import net.coding.program.common.FileUtil;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.io.File;
import java.util.regex.Pattern;

@EFragment(R.layout.fragment_setting)
public class SettingFragment extends BaseFragment {

    @ViewById
    ListView listView;

    @StringArrayRes
    String setting_list_items[];

    LayoutInflater mInflater;

    @AfterViews
    void init() {
        mInflater = LayoutInflater.from(getActivity());

        addFoot();
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) id) {
                    case 0:
                        NotifySetting_.intent(getActivity()).start();
                        break;

                    case 1:
                        SetPasswordActivity_.intent(getActivity()).start();
                        break;

                    case 2:
                        FeedbackActivity_.intent(getActivity()).start();
                        break;

                    case 3:
                        setFileDownloadPath();
                        break;

                    case 4:
                        gotoMarket();
                        break;

                    case 5:
                        new UpdateApp(getActivity()).runForeground();
                        break;

                    case 6:
                        AboutActivity_.intent(SettingFragment.this).startForResult(RESULT_CODE_LOGIN_OUT);
                        break;
                }
            }
        });
    }

    private void gotoMarket() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "软件市场里暂时没有找到Coding", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFoot() {
        View v = mInflater.inflate(R.layout.activity_setting_bottom, null, false);
        v.findViewById(R.id.esc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOut();
            }
        });
        listView.addFooterView(v);
    }

    private void loginOut() {
        XGPushManager.unregisterPush(getActivity().getApplicationContext());
        AccountInfo.loginOut(getActivity());
        startActivity(new Intent(getActivity(), LoginActivity_.class));
        getActivity().finish();
    }


    static final int RESULT_CODE_LOGIN_OUT = 30;

    @OnActivityResult(RESULT_CODE_LOGIN_OUT)
    void onResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            XGPushManager.unregisterPush(getActivity().getApplicationContext());
            AccountInfo.loginOut(getActivity());
            getActivity().finish();
            System.exit(0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_LOGIN_OUT) {
            if (resultCode == Activity.RESULT_OK) {
                loginOut();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return setting_list_items.length;
        }

        @Override
        public Object getItem(int position) {
            return setting_list_items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_2_text, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.first);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String title = (String) getItem(position);
            holder.title.setText(title);

            return convertView;
        }
    };

    static class ViewHolder {
        public TextView title;
    }

    private void setFileDownloadPath() {
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
        ((BaseFragmentActivity) getActivity()).dialogTitleLineColor(dialog);
        input.requestFocus();
    }
}
