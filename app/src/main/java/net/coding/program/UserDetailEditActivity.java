package net.coding.program;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.common.CameraPhotoUtil;
import net.coding.program.common.DatePickerFragment;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ListModify;
import net.coding.program.common.maopao.ClickImageParam;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.FileProviderHelp;
import net.coding.program.databinding.ActivityUserDetailEditBinding;
import net.coding.program.network.constant.VIP;
import net.coding.program.pickphoto.ClickSmallImage;
import net.coding.program.user.ProvincesPickerDialog;
import net.coding.program.user.SetUserInfoActivity_;
import net.coding.program.user.SetUserInfoListActivity_;
import net.coding.program.user.SetUserSkillsActivity_;
import net.coding.program.user.SetUserTagActivity_;
import net.coding.program.user.UserProvincesDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

@EActivity(R.layout.activity_user_detail_edit)
@OptionsMenu(R.menu.user_detail_edit)
public class UserDetailEditActivity extends BackActivity implements DatePickerFragment.DateSet {

    public final static int USERINFO_NAME = 0;
    public final static int USERINFO_SEX = 1;
    public final static int USERINFO_BIRTHDAY = 2;
    public final static int USERINFO_LOCATION = 3;
    public final static int USERINFO_SLOGAN = 4;
    public final static int USERINFO_DEGREE = 5;
    public final static int USERINFO_SCHOOL = 6;
    public final static int USERINFO_COMPANY = 7;
    public final static int USERINFO_JOB = 8;
    public final static int USERINFO_SKILL = 9;
    public final static int USERINFO_TAGS = 10;
    final String HOST_CURRENT = Global.HOST_API + "/current_user";
    final String HOST_USERINFO = Global.HOST_API + "/user/updateInfo";
    private final int RESULT_REQUEST_PHOTO = 1005;
    private final int RESULT_REQUEST_PHOTO_CROP = 1006;
    public String HOST_USER_AVATAR = Global.HOST_API + "/user/avatar?update=1";
    ImageView icon;
    @StringArrayRes
    String[] sexs;
    UserObject user;
    @StringArrayRes
    String[] user_info_list_first;
    @StringArrayRes(R.array.user_degree)
    String[] userDegree;
    String[] user_info_list_second;
    @ViewById
    ListView listView;

    ActivityUserDetailEditBinding binding;

    private VIP lastVIP = VIP.normal;

    String[] user_jobs;
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
                convertView = mInflater.inflate(R.layout.list_item_userinfo, parent, false);
                holder = new ViewHolder();
                holder.first = (TextView) convertView.findViewById(R.id.first);
                holder.second = (TextView) convertView.findViewById(R.id.second);
                holder.third = (TextView) convertView.findViewById(R.id.third);
                holder.divide = convertView.findViewById(R.id.divide);
                holder.divideLine = convertView.findViewById(R.id.divideLine);
                holder.contentLayout = convertView.findViewById(R.id.contentLayout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.first.setText(user_info_list_first[position]);

            String seondString = user_info_list_second[position];

            if (seondString.isEmpty()) {
                seondString = "未填写";
            }

            int lastPos = getCount() - 1;
            int lastPosPre = lastPos - 1;
            if (position == lastPos || position == lastPosPre) { // 最后一项 个性标签 要换行显示
                holder.third.setVisibility(View.VISIBLE);
                holder.third.setText(seondString);
                seondString = "";
            } else {
                holder.third.setVisibility(View.GONE);
            }

            holder.second.setText(seondString);

            if (position == 10) {
                holder.divide.setVisibility(View.GONE);
                holder.divideLine.setVisibility(View.GONE);
            } else if (position == 4 || position == 8 || position == 9) {
                holder.divide.setVisibility(View.VISIBLE);
                holder.divideLine.setVisibility(View.GONE);
            } else {
                holder.divide.setVisibility(View.GONE);
                holder.divideLine.setVisibility(View.VISIBLE);
            }


            // 企业版隐藏几项
            if (GlobalData.isEnterprise()) {
                if (position == 5 || position == 6 || position == 9) {
                    holder.contentLayout.setVisibility(View.GONE);
                } else {
                    holder.contentLayout.setVisibility(View.VISIBLE);
                }

                if (position == 6) {
                    holder.divide.setVisibility(View.VISIBLE);
                    holder.divideLine.setVisibility(View.GONE);
                }
            }


            return convertView;
        }

    };

    String HOST_JOB = Global.HOST_API + "/options/jobs";
    private Uri fileCropUri;
    private Uri fileUri;

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //showButtomToast("position" + position);
            switch ((int) id) {

                case USERINFO_NAME:
                    //昵称
                    SetUserInfoActivity_.intent(UserDetailEditActivity.this)
                            .title("名字")
                            .row(USERINFO_NAME).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_SEX:
                    //性别
                    setSexs();
                    break;

                case USERINFO_BIRTHDAY:
                    //生日
                    DialogFragment newFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(DatePickerFragment.PARAM_MAX_TODYA, true);
                    bundle.putString(DatePickerFragment.PARAM_DATA, user.birthday);
                    newFragment.setArguments(bundle);
                    newFragment.setCancelable(true);
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                    getSupportFragmentManager().executePendingTransactions();
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
                    break;

                case USERINFO_SLOGAN:
                    //座右铭
                    SetUserInfoActivity_.intent(UserDetailEditActivity.this).title("座右铭").row(USERINFO_SLOGAN).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_DEGREE:
                    chooseDegree();
                    break;

                case USERINFO_SCHOOL:
                    SetUserInfoListActivity_.intent(UserDetailEditActivity.this).title("学校").row(USERINFO_SCHOOL).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_COMPANY:
                    //公司
                    SetUserInfoActivity_.intent(UserDetailEditActivity.this)
                            .title(GlobalData.isEnterprise() ? "部门" : "公司")
                            .row(USERINFO_COMPANY).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_JOB:
                    //职位
                    chooseJob();
                    break;

                case USERINFO_SKILL:
                    SetUserSkillsActivity_.intent(UserDetailEditActivity.this).startForResult(ListModify.RESULT_EDIT_LIST);
                    break;

                case USERINFO_TAGS:
                    //个性标签
                    SetUserTagActivity_.intent(UserDetailEditActivity.this).title("个性标签").startForResult(ListModify.RESULT_EDIT_LIST);
                    break;
            }
        }
    };

    @SuppressLint("CheckResult")
    void setIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("更换头像")
                .setItems(R.array.camera_gallery, (dialog, which) -> {
                    if (which == 0) {
                        new RxPermissions(UserDetailEditActivity.this)
                                .request(PermissionUtil.CAMERA_STORAGE)
                                .subscribe(granted -> {
                                    if (granted) {
                                        camera();
                                    }
                                });
                    } else {
                        photo();
                    }
                });
        builder.show();
    }

//    private void camera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        tempFile = CameraPhotoUtil.getCacheFile(this);
//        fileUri = CameraPhotoUtil.fileToUri(this, tempFile);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//
////        FileProviderHelp.setIntentDataAndType(this, intent, "image/*", tempFile, true);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////            intent.setDataAndType(getUriForFile(context, file), type);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }
//
//        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
//    }

    private void camera() {
        File tempFile = CameraPhotoUtil.getCacheFile(this);
        fileUri = FileProviderHelp.getUriForFile(this, tempFile);

        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0及以上
            intentFromCapture.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            intentFromCapture.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        startActivityForResult(intentFromCapture, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }

    @AfterViews
    protected final void initUserDetailEditActivity() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding = ActivityUserDetailEditBinding.bind(findViewById(R.id.rootLayout));

        listViewAddFootSection(listView);
        user = AccountInfo.loadAccount(this);
        lastVIP = user.vip;

//        if (user.phoneAndEmailValid() || user.isHighLevel() || GlobalData.isEnterprise()) {
//            topTip.setVisibility(View.GONE);
//        } else {
//            topTip.setVisibility(View.VISIBLE);
////            binding.topTipText.setText(Html.fromHtml("验证手机和邮箱后完善资料才能升级，<u>去验证</u>"));
//            topTip.setOnClickListener(v -> AccountSetting_.intent(UserDetailEditActivity
//                    .this).startForResult(ListModify.RESULT_EDIT_LIST));
//            binding.topTipFork.setOnClickListener(v -> topTip.setVisibility(View.GONE));
//        }

        View head = mInflater.inflate(R.layout.activity_user_info_head, listView, false);
        icon = head.findViewById(R.id.icon);
        icon.setOnClickListener(new ClickSmallImage(this));
        iconfromNetwork(icon, user.avatar);
        icon.setTag(new ClickImageParam(user.avatar));
        head.setOnClickListener(v -> setIcon());
        listView.addHeaderView(head);

        getUserInfoRows();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        getNetwork(HOST_CURRENT, HOST_CURRENT);

//        popUpgradeSuccessDialog();
    }

    void getUserInfoRows() {
        user_info_list_second = new String[]{
                user.name,
                sexs[user.sex],
                user.birthday,
                user.location,
                user.slogan,
                user.getUserDegree(),
                user.school,
                user.company,
                user.job_str,
                user.getUserSkills(),
                user.tags_str
        };
    }

    void action_done() {
        RequestParams params = new RequestParams();
        params.put("email", user.email);
        params.put("lavatar", user.lavatar);
        params.put("name", user.name);
        params.put("sex", user.sex);
//        params.put("phone", user.phone);
        params.put("birthday", user.birthday);
        params.put("location", user.location.trim());
        params.put("company", user.company);
        params.put("slogan", user.slogan);
        params.put("introduction", user.introduction);
        params.put("job", user.job);
        params.put("tags", user.tags);

        params.put("degree", user.degree);
        params.put("school", user.school);

        postNetwork(HOST_USERINFO, params, HOST_USERINFO);

        umengEvent(UmengEvent.USER, "修改个人信息");
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_CURRENT)) {
            if (code == 0) {
                user = new UserObject(respanse.getJSONObject("data"));

                AccountInfo.saveAccount(this, user);
                popUpgradeDialog();

                getUserInfoRows();
                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_USERINFO)) {
            if (code == 0) {
                user = new UserObject(respanse.getJSONObject("data"));

                getUserInfoRows();
                adapter.notifyDataSetChanged();
                getNetwork(HOST_CURRENT, HOST_CURRENT);
            } else {
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_USER_AVATAR)) {
            if (code == 0) {
                String iconUri = respanse.getString("data");
                iconfromNetwork(icon, iconUri);

                user.avatar = iconUri;
                AccountInfo.saveAccount(this, user);
                icon.setTag(new ClickImageParam(user.avatar));
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_JOB)) {
            if (code == 0) {
                umengEvent(UmengEvent.USER, "修改个人信息");

                ArrayList<String> jobs = new ArrayList<>();
                JSONObject jobJSONObject = respanse.getJSONObject("data");
                Iterator it = jobJSONObject.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = jobJSONObject.getString(key);
                    jobs.add(value);
                }
                user_jobs = new String[jobs.size()];
                for (int i = 0; i < jobs.size(); i++) {
                    user_jobs[i] = jobs.get(i);
                }
                showJobDialog();
            }
        }
    }

    // 填完资料，弹出白银会员升级提示框
    private void popUpgradeDialog() {
        if (lastVIP == VIP.normal && user.vip == VIP.silver) {
            lastVIP = user.vip;
            popUpgradeSuccessDialog();
        }
    }

    private void popUpgradeSuccessDialog() {
        View root = LayoutInflater.from(this).inflate(R.layout.upgrade_sliver_vip_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setView(root)
                .show();
        root.findViewById(R.id.closeDialog).setOnClickListener(v -> dialog.dismiss());
        root.findViewById(R.id.buttonReward).setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    fileUri = data.getData();
                }

                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                Global.startPhotoZoom(this, fileUri, fileCropUri, 512, 512, RESULT_REQUEST_PHOTO_CROP);
            }

        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filePath = FileUtil.getPath(this, fileCropUri);
                    RequestParams params = new RequestParams();
                    params.put("file", new File(filePath));
                    postNetwork(HOST_USER_AVATAR, params, HOST_USER_AVATAR);

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }
        } else if (requestCode == ListModify.RESULT_EDIT_LIST) {
            //showButtomToast("EDITED");
            //updateUserinfo();
            user = AccountInfo.loadAccount(this);
            getUserInfoRows();
            adapter.notifyDataSetChanged();
            popUpgradeDialog();
        }
    }

    void setSexs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
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
    }

    @Override
    public void dateSetResult(String date, boolean clear) {
        if (!user.birthday.equals(date)) {
            user.birthday = date;
            action_done();
        }
    }

    public void chooseDegree() {
        showDegreeDialog();
    }

    private void showDegreeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("学历")
                .setItems(userDegree, (dialog, which) -> {
                    user.degree = which + 1;
                    action_done();
                });

        //builder.create().show();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        AlertDialog dialog = builder.create();
        dialog.show();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        Point outSize = new Point();
        d.getSize(outSize);
//        p.height = (int) (outSize.y * 0.6); // 高度设置为屏幕的0.6
        //p.width = (int) (d.getWidth() * 0.8);
        dialog.getWindow().setAttributes(p);
    }

    public void chooseJob() {
        if (user_jobs == null) {
            getNetwork(HOST_JOB, HOST_JOB);
        } else {
            showJobDialog();
        }
    }

    private void showJobDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("工作")
                .setItems(user_jobs, (dialog, which) -> {
                    user.job = which + 1;
                    action_done();
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
    }

    static class ViewHolder {
        TextView first;
        TextView second;
        TextView third;
        View divide;
        View divideLine;
        View contentLayout;
    }

}