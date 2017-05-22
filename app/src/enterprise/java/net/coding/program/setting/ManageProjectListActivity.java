package net.coding.program.setting;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import net.coding.program.EnterpriseApp;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.widget.CommonAdapter;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.project.ProjectHomeActivity_;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.common_simple_listview)
public class ManageProjectListActivity extends BackActivity {

    private static final String TAG_PROJECT = "TAG_PROJECT";

    @ViewById
    CommonListView listView;

    ProjectAdapter adapter;

    ArrayList<ProjectObject> listData = new ArrayList<>();

    Handler hander2fa;
    private EditText edit2fa;

    @AfterViews
    void initManageProjectListActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));

        adapter = new ProjectAdapter(listData);
        listView.setAdapter(adapter);
        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.enableDefaultSwipeRefresh(true);

        onRefresh();

        hander2fa = new WeakRefHander(msg -> {
            if (edit2fa != null) {
                String secret = AccountInfo.loadAuth(getApplicationContext(), MyApp.sUserObject.global_key);
                if (secret.isEmpty()) {
                    return true;
                }

                String code2FA = new AuthInfo(secret, new TotpClock(getApplicationContext())).getCode();
                edit2fa.setText(code2FA);
            }
            return true;
        }, 100);

    }

    private void onRefresh() {
        String host = String.format("%s/team/%s/projects", Global.HOST_API, EnterpriseApp.getEnterpriseGK());
        getNetwork(host, TAG_PROJECT);
    }

    protected class ProjectAdapter extends CommonAdapter<ProjectObject, ProjectHolder> {

        public ProjectAdapter(List<ProjectObject> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.enterprise_manage_project_list_item;
        }

        @Override
        protected ProjectHolder newViewHolder(View view) {
            ProjectHolder holder = new ProjectHolder(view);
            holder.rootLayout.setOnClickListener(clickItem);
            holder.rootLayout.setOnLongClickListener(longClickItem);
            return holder;
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, ProjectObject data, int position) {
            ProjectObject item = getItem(position);
            holder.name.setText(item.name);
            holder.memberCount.setText(String.format("%s 人", data.memberNum));
            holder.rootLayout.setTag(item);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
        }

        @Override
        public ProjectHolder newHeaderHolder(View view) {
            return new ProjectHolder(view, true);
        }

        @Override
        public ProjectHolder newFooterHolder(View view) {
            return new ProjectHolder(view, true);
        }
    }

    public static class ProjectHolder extends UltimateRecyclerviewViewHolder {

        public TextView name;
        public TextView memberCount;
        public ImageView image;
        public View rootLayout;

        public ProjectHolder(View view, boolean isHeader) {
            super(view);
        }

        public ProjectHolder(View view) {
            super(view);
            rootLayout = view;
            name = (TextView) view.findViewById(R.id.name);
            memberCount = (TextView) view.findViewById(R.id.memberCount);
            image = (ImageView) view.findViewById(R.id.icon);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_PROJECT)) {
            if (code == 0) {
                listData.clear();
                JSONArray array = respanse.optJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    listData.add(new ProjectObject(array.optJSONObject(i)));
                }

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private View.OnClickListener clickItem = v -> {
        Object object = v.getTag();
        if (object instanceof ProjectObject) {
            ProjectObject item = (ProjectObject) object;

            if (item.isJoined()) {
                ProjectHomeActivity_.intent(this)
                        .mProjectObject(item)
                        .startForResult(InitProUtils.REQUEST_PRO_UPDATE);
            } else {
                showMiddleToast("无权进入项目");
            }
        }
    };

    private View.OnLongClickListener longClickItem = v -> {
        ProjectObject project = (ProjectObject) v.getTag();
        new AlertDialog.Builder(v.getContext())
                .setItems(R.array.manager_project_item, (dialog, which) -> {
                    if (which == 0) {
                        Network.getRetrofit(ManageProjectListActivity.this)
                                .need2FA()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new HttpObserver<String>(ManageProjectListActivity.this) {
                                    @Override
                                    public void onSuccess(String data) {
                                        if (data.equals("totp")) {
                                            showDeleteDialog2fa(project);
                                        } else { //  password
                                            showDeleteDialog(project);
                                        }
                                    }
                                });
                    }
                })
                .show();

        return true;
    };

    private void showDeleteDialog2fa(ProjectObject project) {
        hander2fa.sendEmptyMessage(0);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project_2fa, null);
        edit2fa = (EditText) textEntryView.findViewById(R.id.edit1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("需要验证码")
                .setView(textEntryView)
                .setPositiveButton("确定", (dialog1, whichButton) -> {
                    String editStr1 = edit2fa.getText().toString().trim();
                    if (TextUtils.isEmpty(editStr1)) {
                        showMiddleToast("验证码不能为空");
                        return;
                    }
                    actionDelete2FA(project, editStr1);
                })
                .setNegativeButton("取消", null)
                .show();

        dialog.setOnDismissListener(dialog1 -> {
            hander2fa.removeMessages(0);
            edit2fa = null;
        });
    }

    private void showDeleteDialog(ProjectObject project) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project, null);
        final EditText edit1 = (EditText) textEntryView.findViewById(R.id.edit1);
        new AlertDialog.Builder(this)
                .setTitle("需要验证密码")
                .setView(textEntryView)
                .setPositiveButton("确定", (dialog1, whichButton) -> {
                    String editStr1 = edit1.getText().toString().trim();
                    if (TextUtils.isEmpty(editStr1)) {
                        showMiddleToast("密码不能为空");
                        return;
                    }

                    actionDelete2FA(project, SimpleSHA1.sha1(editStr1));
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void actionDelete2FA(final ProjectObject project, String password) {
        Network.getRetrofit(this)
                .deleteProject(project.getV2PathById(), password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        showButtomToast("删除成功");
                        listData.remove(project);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
