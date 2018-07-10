package net.coding.program.project.init.setting;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.project.EventProjectModify;
import net.coding.program.project.init.setting.v2.ProjectSetFragmentBase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by jack wang on 2015/3/31.
 * 项目设置页面
 */
@EFragment(R.layout.init_fragment_project_set)
@OptionsMenu(R.menu.menu_fragment_create)
public class ProjectSetFragment extends ProjectSetFragmentBase {

    private static final String TAG = "ProjectSetFragment";

    MenuItem mMenuSave;

    @ViewById
    EditText projectName;

    @ViewById
    EditText description;

    @ViewById
    View item, itemTransfer, iconPrivate, itemArchive;

    @ViewById
    View layoutManager, layoutOwner, layoutQuit;

    @AfterViews
    protected void init() {
        ((TextView) itemTransfer.findViewById(R.id.title)).setText("项目转让");
        ((TextView) itemArchive.findViewById(R.id.title)).setText("项目归档");
        ((TextView) item.findViewById(R.id.title)).setText("删除项目");

        iconfromNetwork(projectIcon, mProjectObject.icon, ImageLoadTool.optionsRounded2);
        projectName.setText(mProjectObject.name);
        description.setText(mProjectObject.description);
        iconPrivate.setVisibility(mProjectObject.isShared() ? View.VISIBLE : View.GONE);

        SimpleTextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        };
        description.addTextChangedListener(watcher);
        projectName.addTextChangedListener(watcher);

        Global.popSoftkeyboard(getActivity(), description, false);

        if (!mProjectObject.isPublic) {
            if (mProjectObject.isMy()) {
                layoutManager.setVisibility(View.VISIBLE);
                layoutOwner.setVisibility(View.VISIBLE);
                layoutQuit.setVisibility(View.GONE);
            } else if (mProjectObject.isManagerLevel()) {
                layoutManager.setVisibility(View.VISIBLE);
                layoutOwner.setVisibility(View.GONE);
                layoutQuit.setVisibility(View.VISIBLE);
            } else {
                layoutManager.setVisibility(View.GONE);
                layoutOwner.setVisibility(View.GONE);
                layoutQuit.setVisibility(View.VISIBLE);
            }
        } else {
            layoutManager.setVisibility(View.VISIBLE);
            layoutOwner.setVisibility(View.VISIBLE);
            layoutQuit.setVisibility(View.GONE);
        }
    }

    @Click
    public void item() {
        ProjectAdvanceSetActivity_
                .intent(this)
                .mProjectObject(mProjectObject)
                .start();
    }

    @Click
    public void itemTransfer() {
        ProjectTransferActivity_
                .intent(this)
                .mProjectObject(mProjectObject)
                .start();
    }

    @Click
    public void itemArchive() {
        ProjectArchiveActivity_.intent(this)
                .mProjectObject(mProjectObject)
                .start();
    }

    @Click
    public void layoutQuit() {
        String message = String.format("您确定要退出 %s 项目吗？", mProjectObject.name);
        showDialog("退出项目", message, (dialog1, which) -> {
            Network.getRetrofit(getActivity())
                    .quitProject(mProjectObject.owner_user_name, mProjectObject.name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseHttpObserver(getActivity()) {
                        @Override
                        public void onSuccess() {
                            super.onSuccess();
                            showProgressBar(false);

                            umengEvent(UmengEvent.PROJECT, "退出项目");
                            showButtomToast("已退出项目");
                            EventBus.getDefault().post(new EventProjectModify().setExit());
                            getActivity().onBackPressed();
                        }

                        @Override
                        public void onFail(int errorCode, @NonNull String error) {
                            super.onFail(errorCode, error);
                            showProgressBar(false);
                        }
                    });
            showProgressBar(true);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenuSave = menu.findItem(R.id.action_finish);
        updateSendButton();

        if (mProjectObject != null && !mProjectObject.isPublic && !mProjectObject.isManagerLevel()) {
            mMenuSave.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateSendButton() {
        String inputName = projectName.getText().toString().trim();
        String inputDes = description.getText().toString().trim();

        if (mProjectObject == null) {
            enableSendButton(!TextUtils.isEmpty(inputName));
        } else {
            enableSendButton(!TextUtils.isEmpty(inputName) &&
                    (!inputName.equals(mProjectObject.name) || !inputDes.equals(mProjectObject.description)));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.action_finish) {
            showProgressBar(true, "正在修改...");
            action_done();
            return true;
        }
        return false;
    }

    private void action_done() {
        RequestParams params = new RequestParams();
        params.put("name", projectName.getText().toString());
        params.put("description", description.getText().toString().trim());
        params.put("id", mProjectObject.getId());
        params.put("default_branch", "master");
        putNetwork(host, params, host, null);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "修改项目");
                showButtomToast("修改成功");
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));
                Global.hideSoftKeyboard(getActivity());

                getActivity().setResult(Activity.RESULT_OK);
                EventBus.getDefault().post(new EventProjectModify().setProjectUrl(mProjectObject.getHttpProjectObject()));

                getActivity().finish();

            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "修改项目图片");
                showButtomToast("图片上传成功...");
                mProjectObject = new ProjectObject(respanse.getJSONObject("data"));

                EventBus.getDefault().post(new EventProjectModify());
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }


}
