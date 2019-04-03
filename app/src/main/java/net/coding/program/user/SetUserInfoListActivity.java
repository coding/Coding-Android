package net.coding.program.user;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.UserDetailEditActivity;
import net.coding.program.common.Global;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_set_user_info_list)
@OptionsMenu(R.menu.set_password)
public class SetUserInfoListActivity extends BackActivity {

    final String HOST_USERINFO = Global.HOST_API + "/user/updateInfo";

    @Extra("title")
    String title;

    @Extra("row")
    int row;

    UserObject user;

    @ViewById(R.id.enterpriseNameEt)
    EditText value;

    @ViewById(R.id.listView)
    RecyclerView listView;

    BaseQuickAdapter adapter;
    List<String> allData = new ArrayList<>();
    List<String> searchData = new ArrayList<>();

    @AfterViews
    protected final void initSetUserInfoActivity() {
        getSupportActionBar().setTitle(title);
        user = AccountInfo.loadAccount(this);

        String jsonFile = Global.readAssets(this, "schools.json");
        allData = new Gson().fromJson(jsonFile, new TypeToken<ArrayList<String>>() {
        }.getType());
        searchData.addAll(allData);

        listView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BaseQuickAdapter<String, BaseViewHolder>(android.R.layout.simple_list_item_1, searchData) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                helper.setText(android.R.id.text1, item);
            }
        };
        adapter.setOnItemClickListener((adapter1, view, position) -> value.setText(searchData.get(position)));

        listView.setAdapter(adapter);

        final String hintFormat = "请输入%s";
        value.setHint(String.format(hintFormat, title));
        value.setText(getRowValue());
        value.requestFocus();
    }

    @TextChange(R.id.enterpriseNameEt)
    void onTextChange(TextView tv, CharSequence text) {
        updateSearchList(text);
    }

    private void updateSearchList(CharSequence text) {
        searchData.clear();
        if (TextUtils.isEmpty(text)) {
            searchData.addAll(allData);
        } else {
            for (String item : allData) {
                if (item.contains(text)) {
                    searchData.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


    @OptionsItem
    void submit() {
        RequestParams params = new RequestParams();
        setRowValue(value.getText().toString());

        try {
            params.put("tags", user.tags);
            params.put("job", user.job);
            params.put("sex", user.sex);
            params.put("birthday", user.birthday);
            params.put("location", user.location);
            params.put("company", user.company);
            params.put("slogan", user.slogan);
            params.put("introduction", user.introduction);
            params.put("lavatar", user.lavatar);
            params.put("global_key", user.global_key);
            params.put("name", user.name);
            params.put("email", user.email);
            params.put("id", user.id);

            params.put("degree", user.degree);
            params.put("school", value.getText().toString());

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
            case UserDetailEditActivity.USERINFO_SCHOOL:
                returnValue = user.school;
                break;
        }
        return returnValue;
    }

    private void setRowValue(String rowValue) {
        switch (row) {
            case UserDetailEditActivity.USERINFO_SCHOOL:
                user.school = rowValue;
                break;
        }
    }
}
