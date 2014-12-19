package net.coding.program;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.ListModify;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.user.ProvincesPickerDialog;
import net.coding.program.user.SetUserInfoActivity_;
import net.coding.program.user.SetUserTagActivity_;
import net.coding.program.user.UserProvincesDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

@EActivity(R.layout.activity_user_info)
@OptionsMenu(R.menu.user_info)
public class UserInfoActivity extends BaseFragmentActivity {

    ImageView icon;

    void setIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更换头像")
                .setItems(R.array.camera_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            camera();
                        } else {
                            photo();
                        }
                    }
                });
        //builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);
    }

    @StringArrayRes
    String[] sexs;

    UserObject user;

    @StringArrayRes
    String[] user_info_list_first;

    String[] user_info_list_second;

    @ViewById
    ListView listView;

    String[] user_jobs;

    final String HOST_USER = Global.HOST + "/api/user/key/%s";

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        user = AccountInfo.loadAccount(this);

        View head = mInflater.inflate(R.layout.activity_user_info_head, null, false);
        icon = (ImageView) head.findViewById(R.id.icon);
        icon.setOnClickListener(new ClickSmallImage(this));
        iconfromNetwork(icon, user.avatar);
        icon.setTag(new MaopaoListFragment.ClickImageParam(user.avatar));
        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIcon();
            }
        });
        listView.addHeaderView(head);

        getUserInfoRows();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        getNetwork(String.format(HOST_USER, user.global_key), HOST_USER);
    }

    void getUserInfoRows() {
        user_info_list_second = new String[]{
                user.name,
                sexs[user.sex],
                Global.dayFromTime(user.birthday),
                user.location,
                user.slogan,
                user.company,
                user.job_str,
                user.tags_str
        };
    }

    final String HOST_USERINFO = Global.HOST + "/api/user/updateInfo";

    void action_done() {
        RequestParams params = new RequestParams();
        params.put("email", user.email);
        params.put("lavatar", user.lavatar);
        params.put("name", user.name);
        params.put("sex", user.sex);
        params.put("phone", user.phone);
        params.put("birthday", Global.dayFromTime(user.birthday));
        params.put("location", user.location.trim());
        params.put("company", user.company);
        params.put("slogan", user.slogan);
        params.put("introduction", user.introduction);
        params.put("job", user.job);
        params.put("tags", user.tags);

        postNetwork(HOST_USERINFO, params, HOST_USERINFO);
    }

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return user_info_list_first.length;
        }

        @Override
        public Object getItem(int position) {
            return user_info_list_second[position];
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
                holder.first = (TextView) convertView.findViewById(R.id.first);
                holder.second = (TextView) convertView.findViewById(R.id.second);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.first.setText(user_info_list_first[position]);

            String seondString = user_info_list_second[position];
            if (seondString.isEmpty()) {
                seondString = "未填写";
            }
            holder.second.setText(seondString);

            return convertView;
        }
    };

    static class ViewHolder {
        TextView first;
        TextView second;
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    public String HOST_USER_AVATAR = Global.HOST + "/api/user/avatar?update=1";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USERINFO) || tag.equals(HOST_USER)) {
            if (code == 0) {
                user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                //MyApp.sUserObject = user;
                getUserInfoRows();
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_USER_AVATAR)) {
            if (code == 0) {
                String iconUri = respanse.getString("data");
                iconfromNetwork(icon, iconUri);

                user.avatar = iconUri;
                AccountInfo.saveAccount(this, user);
                icon.setTag(new MaopaoListFragment.ClickImageParam(user.avatar));


            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST)) {
            if (code == 0) {
                user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                getUserInfoRows();
                adapter.notifyDataSetChanged();
                //setControlContent(user);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_JOB)) {
            if (code == 0) {
                ArrayList<String> jobs = new ArrayList<String>();
                jobs.add("");
                JSONObject jobJSONObject = respanse.getJSONObject("data");
                Iterator it = jobJSONObject.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = jobJSONObject.getString(key);
                    jobs.add(value);
                }
                user_jobs = new String[jobs.size()];
                //jobs.toArray(user_jobs);
                user_jobs[0] = "不选择";
                for (int i = 1; i < jobs.size(); i++) {
                    user_jobs[i] = jobJSONObject.optString(i + "");
                }
                showJobDialog();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    fileUri = data.getData();
                }

                fileCropUri = getOutputMediaFileUri();
                cropImageUri(fileUri, fileCropUri, 640, 640, RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filePath = Global.getPath(this, fileCropUri);
                    RequestParams params = new RequestParams();
                    params.put("file", new File(filePath));
                    postNetwork(HOST_USER_AVATAR, params, HOST_USER_AVATAR);

                } catch (Exception e) {
                }
            }
        } else if (requestCode == ListModify.RESULT_EDIT_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                //showButtomToast("EDITED");
                //updateUserinfo();
                user = AccountInfo.loadAccount(this);
                getUserInfoRows();
                adapter.notifyDataSetChanged();
            } else {
                //showButtomToast("UNEDITED");
            }
        }
    }

    public final static int USERINFO_NAME = 0;
    public final static int USERINFO_SEX = 1;
    public final static int USERINFO_BIRTHDAY = 2;
    public final static int USERINFO_LOCATION = 3;
    public final static int USERINFO_SLOGAN = 4;
    public final static int USERINFO_COMPANY = 5;
    public final static int USERINFO_JOB = 6;
    public final static int USERINFO_TAGS = 7;

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //showButtomToast("position" + position);
            switch ((int) id) {

                case USERINFO_NAME:
                    //昵称
                    SetUserInfoActivity_.intent(UserInfoActivity.this).title("昵称").row(USERINFO_NAME).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_SEX:
                    //性别
                    setSexs();
                    break;

                case USERINFO_BIRTHDAY:
                    //生日
                    DialogFragment newFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong("date", user.birthday);
                    newFragment.setArguments(bundle);
                    newFragment.setCancelable(true);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                    getSupportFragmentManager().executePendingTransactions();
                    dialogTitleLineColor(newFragment.getDialog());
                    break;

                case USERINFO_LOCATION:
                    //所在地
                    UserProvincesDialogFragment provincesDialogFragment = new UserProvincesDialogFragment();
                    Bundle provincesBundle = new Bundle();
                    provincesBundle.putString(ProvincesPickerDialog.LOCATION, user.location);
                    provincesBundle.putString(ProvincesPickerDialog.TITLE, "选择所在地");
                    provincesDialogFragment.setArguments(provincesBundle);
                    provincesDialogFragment.setCallBack(new ProvincesPickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(String provinceStr) {
                            if (!user.location.trim().equals(provinceStr)) {
                                user.location = provinceStr;
                                action_done();
                            }
                        }
                    });
                    //provincesDialogFragment.setCancelable(true);
                    provincesDialogFragment.show(getSupportFragmentManager(), "provincesPicker");
                    getSupportFragmentManager().executePendingTransactions();
                    dialogTitleLineColor(provincesDialogFragment.getDialog());
                    break;

                case USERINFO_SLOGAN:
                    //座右铭
                    SetUserInfoActivity_.intent(UserInfoActivity.this).title("座右铭").row(USERINFO_SLOGAN).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_COMPANY:
                    //公司
                    SetUserInfoActivity_.intent(UserInfoActivity.this).title("公司").row(USERINFO_COMPANY).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_JOB:
                    //职位
                    chooseJob();
                    break;

                case USERINFO_TAGS:
                    //个性标签
                    SetUserTagActivity_.intent(UserInfoActivity.this).title("个性标签").startForResult(ListModify.RESULT_EDIT_LIST);
                    break;
            }
        }
    };

    public void updateUserinfo() {
        UserObject oldUser = AccountInfo.loadAccount(this);
        getNetwork(String.format(HOST, oldUser.global_key), HOST);
    }

    String HOST = Global.HOST + "/api/user/key/%s";

    void setSexs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("性别")
                .setItems(R.array.sexs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.sex = which;
                        action_done();
                    }
                });
        //builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.show();
        dialogTitleLineColor(dialog);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private UserInfoActivity mActivity;

        @Override
        public void onAttach(Activity activity) {
            if (activity instanceof UserInfoActivity) {
                mActivity = (UserInfoActivity) activity;
            }
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getArguments().getLong("date"));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            LinearLayout layoutParent = (LinearLayout) datePickerDialog.getDatePicker().getChildAt(0);
            LinearLayout layout = (LinearLayout) layoutParent.getChildAt(0);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                if (v instanceof NumberPicker) {
                    setNumberPicker((NumberPicker) v);
                }
            }
            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            final Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            if (mActivity != null) {
                mActivity.setUserinfoBirthday(c.getTimeInMillis());
            }
        }

        public void setNumberPicker(NumberPicker spindle) {
            java.lang.reflect.Field[] pickerFields = NumberPicker.class
                    .getDeclaredFields();
            for (java.lang.reflect.Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(spindle, getResources().getDrawable(R.drawable.line_green));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    void setUserinfoBirthday(long time) {
        if (user.birthday != time) {
            user.birthday = time;
            action_done();
        }
    }

    public void chooseJob() {
        if (user_jobs == null) {
            getNetwork(HOST_JOB, HOST_JOB);
        } else {
            showJobDialog();
        }
    }

    private void showJobDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("职位")
                .setItems(user_jobs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.job = which;
                        action_done();
                    }
                });

        //builder.create().show();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        AlertDialog dialog = builder.create();
        dialog.show();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        Point outSize = new Point();
        d.getSize(outSize);
        p.height = (int) (outSize.y * 0.6); // 高度设置为屏幕的0.6
        //p.width = (int) (d.getWidth() * 0.8);
        dialog.getWindow().setAttributes(p);
        dialogTitleLineColor(dialog);
    }

    String HOST_JOB = Global.HOST + "/api/options/jobs";

}