package net.coding.program.user;

import android.app.Activity;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.R;
import net.coding.program.UserDetailEditActivity;
import net.coding.program.common.Global;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_user_edit)
@OptionsMenu(R.menu.set_password)
public class SetUserInfoActivity extends BaseActivity {

    final String HOST_USERINFO = Global.HOST_API + "/user/updateInfo";
    @Extra("title")
    String title;
    @Extra("row")
    int row;
    UserObject user;
    @ViewById
    TextView value;

    @AfterViews
    protected final void initSetUserInfoActivity() {
        getSupportActionBar().setTitle(title);
        user = AccountInfo.loadAccount(this);
        final String hintFormat = "请输入%s";
        value.setHint(String.format(hintFormat, title));
        value.setText(getRowValue());
        value.requestFocus();
    }

    @OptionsItem
    void submit() {
        RequestParams params = new RequestParams();

        setRowValue(value.getText().toString());

        try {
            params.put("email", user.email);
            params.put("lavatar", user.lavatar);
            params.put("name", user.name);
            params.put("sex", user.sex);
            params.put("phone", user.phone);
            params.put("birthday", user.birthday);
            params.put("location", user.location);
            params.put("company", user.company);
            params.put("slogan", user.slogan);
            params.put("introduction", user.introduction);
            params.put("job", user.job);
            params.put("tags", user.tags);

            postNetwork(HOST_USERINFO, params, HOST_USERINFO);
        } catch (Exception e) {
            showMiddleToast(e.toString());
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (code == 0) {
            showButtomToast("修改成功");
            setResult(Activity.RESULT_OK);
            AccountInfo.saveAccount(this, user);
            finish();
        } else {
            showErrorMsg(code, respanse);
        }
    }

    private String getRowValue() {
        String returnValue = "";
        switch (row) {

            case UserDetailEditActivity.USERINFO_NAME:
                //昵称
                returnValue = user.name;
                break;

            case UserDetailEditActivity.USERINFO_SEX:
                //性别
                break;

            case UserDetailEditActivity.USERINFO_BIRTHDAY:
                //生日
                break;

            case UserDetailEditActivity.USERINFO_LOCATION:
                //所在地
                break;

            case UserDetailEditActivity.USERINFO_SLOGAN:
                //座右铭
                returnValue = user.slogan;
                break;

            case UserDetailEditActivity.USERINFO_COMPANY:
                //公司
                returnValue = user.company;
                break;

            case UserDetailEditActivity.USERINFO_JOB:
                //职位
                break;

            case UserDetailEditActivity.USERINFO_TAGS:
                //个性标签
                break;
        }
        return returnValue;
    }

    private void setRowValue(String rowValue) {
        switch (row) {
            case UserDetailEditActivity.USERINFO_NAME:
                //昵称
                user.name = rowValue;
                break;

            case UserDetailEditActivity.USERINFO_SEX:
                //性别
                break;

            case UserDetailEditActivity.USERINFO_BIRTHDAY:
                //生日
                break;

            case UserDetailEditActivity.USERINFO_LOCATION:
                //所在地
                break;

            case UserDetailEditActivity.USERINFO_SLOGAN:
                //座右铭
                /*if(rowValue.length() > 50){
                    showButtomToast("座右铭太长了，请不要超过50个字");
                    result = false;
                }else{
                    user.slogan = rowValue;
                }*/
                user.slogan = rowValue;
                break;

            case UserDetailEditActivity.USERINFO_COMPANY:
                //公司
                user.company = rowValue;
                break;

            case UserDetailEditActivity.USERINFO_JOB:
                //职位
                break;

            case UserDetailEditActivity.USERINFO_TAGS:
                //个性标签
                break;
        }
    }
}
