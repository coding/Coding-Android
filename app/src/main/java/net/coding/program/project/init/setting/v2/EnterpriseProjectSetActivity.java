package net.coding.program.project.init.setting.v2;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.PickPhotoActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.project.EventProjectModify;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chenchao on 2017/5/23.
 * 企业版设置项目
 */

@EActivity(R.layout.activity_enterprise_project_set)
public class EnterpriseProjectSetActivity extends PickPhotoActivity {

    @Extra
    ProjectObject project;

    @ViewById
    ImageView projectIcon;

    @ViewById
    View projectInfoLayout;

    @ViewById
    EditText projectName, description;

    private MenuItem mMenuSave;

    private SimpleTextWatcher watcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            updateSendButton();
        }
    };

    @AfterViews
    void initEnterpriseProjectSetActivity() {
        if (project.isManagerLevel()) {
            projectName.setText(project.name);
            description.setText(project.getDescription());

            description.addTextChangedListener(watcher);
            projectName.addTextChangedListener(watcher);
            projectInfoLayout.setVisibility(View.VISIBLE);

            projectIconfromNetwork(projectIcon, project.icon);
        } else {
            projectInfoLayout.setVisibility(View.GONE);
        }
    }

    private void enableSendButton(boolean enable) {
        if (mMenuSave == null) {
            return;
        }

        if (enable) {
            mMenuSave.setIcon(R.drawable.ic_menu_ok);
            mMenuSave.setEnabled(true);
        } else {
            mMenuSave.setIcon(R.drawable.ic_menu_ok_unable);
            mMenuSave.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (project.isManagerLevel()) {
            getMenuInflater().inflate(R.menu.menu_fragment_create, menu);
            mMenuSave = menu.findItem(R.id.action_finish);
            updateSendButton();
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void updateSendButton() {
        String titleString = projectName.getText().toString();
        String descString = description.getText().toString().trim();

        if (titleString.isEmpty()) {
            enableSendButton(false);
            return;
        }

        enableSendButton(!descString.equals(project.description) || !titleString.equals(project.name));
    }

    @OptionsItem(R.id.action_finish)
    void actionFinish() {
        String titleString = projectName.getText().toString();
        String descString = description.getText().toString().trim();
        Network.getRetrofit(this)
                .setProjectInfo(project.id, titleString, descString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<ProjectObject>(this) {
                    @Override
                    public void onSuccess(ProjectObject data) {
                        super.onSuccess(data);
                        showProgressBar(false);

                        umengEvent(UmengEvent.PROJECT, "修改项目");
                        showButtomToast("修改成功");

                        Global.hideSoftKeyboard(EnterpriseProjectSetActivity.this);

                        setResult(Activity.RESULT_OK);
                        EventBus.getDefault().post(new EventProjectModify().setProjectUrl(data.getHttpProjectObject()));
                        finish();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
        showProgressBar(true);
    }

    @Click
    void exitProject() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("退出项目")
                .setMessage(String.format("您确定要退出 %s 项目吗？", project.name))
                .setPositiveButton("确定", (dialog1, which) -> {
                    Network.getRetrofit(EnterpriseProjectSetActivity.this)
                            .quitProject(project.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new BaseHttpObserver(EnterpriseProjectSetActivity.this) {
                                @Override
                                public void onSuccess() {
                                    showProgressBar(false);

                                    umengEvent(UmengEvent.PROJECT, "退出项目");

                                    showButtomToast("已退出项目");
                                    EventBus.getDefault().post(new EventProjectModify().setExit());
                                    finish();
                                }

                                @Override
                                public void onFail(int errorCode, @NonNull String error) {
                                    super.onFail(errorCode, error);
                                    showProgressBar(false);
                                }
                            });
                    showProgressBar(true);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Click
    void projectIcon() {
        pickPhoto();
    }

    @Override
    protected void pickImageCallback(Uri uri, String path) {
        try {
            File file = new File(path);
            RequestBody body = RequestBody.create(MediaType.parse("*/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);

            Network.getRetrofit(this)
                    .setProjectIcon(project.id, part)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new HttpObserver<ProjectObject>(this) {
                        @Override
                        public void onSuccess(ProjectObject data) {
                            super.onSuccess(data);
                            showProgressBar(false);

                            iconfromNetwork(projectIcon, data.icon);

                            Global.hideSoftKeyboard(EnterpriseProjectSetActivity.this);

                            setResult(Activity.RESULT_OK);
                            EventBus.getDefault().post(new EventProjectModify());
                        }

                        @Override
                        public void onFail(int errorCode, @NonNull String error) {
                            super.onFail(errorCode, error);
                            showProgressBar(false);
                        }
                    });

            showProgressBar(true, "正在上传图片...");
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }
}
