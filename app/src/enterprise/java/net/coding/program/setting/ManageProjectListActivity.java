package net.coding.program.setting;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.CommonAdapter;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.project.EventProjectModify;
import net.coding.program.project.ProjectHomeActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.common_simple_listview)
public class ManageProjectListActivity extends BackActivity {

    @ViewById
    CommonListView listView;

    ProjectAdapter adapter;

    ArrayList<ProjectObject> listData = new ArrayList<>();
    private List<ProjectObject> listJoinData = new ArrayList<>();

    Handler hander2fa;
    private EditText edit2fa;

    @AfterViews
    void initManageProjectListActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));

        adapter = new ProjectAdapter(listData);
        StickyRecyclerHeadersDecoration stickyHeader = new StickyRecyclerHeadersDecoration(adapter);
        listView.addItemDecoration(stickyHeader);

        listView.setAdapter(adapter);
        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.enableDefaultSwipeRefresh(true);

        onRefresh();

        hander2fa = new WeakRefHander(msg -> {
            if (edit2fa != null) {
                String secret = AccountInfo.loadAuth(getApplicationContext(), GlobalData.sUserObject.global_key);
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
        Network.getRetrofit(this)
                .getProjects()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(listHttpResult -> {
                    if (listHttpResult == null) {
                        return null;
                    }
                    listJoinData.clear();
                    listJoinData.addAll(listHttpResult.data.list);
                    return Network.getRetrofit(ManageProjectListActivity.this, listView)
                            .getManagerProjects(GlobalData.getEnterpriseGK())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribe(new HttpObserver<List<ProjectObject>>(this, listView) {
                    @Override
                    public void onSuccess(List<ProjectObject> data) {
                        super.onSuccess(data);
                        listData.clear();

                        int joinedPos = 0;
                        for (ProjectObject item : data) {
                            boolean find = false;
                            for (ProjectObject join : listJoinData) {
                                if (join.id == item.id) {
                                    find = true;
                                    join.memberNum = item.memberNum;
                                    listData.add(joinedPos++, join);
                                    break;
                                }
                            }

                            if (!find) {
                                listData.add(item);
                            }
                        }
                    }
                });
    }

    public static class DynamicSectionHolder extends UltimateRecyclerviewViewHolder {

        public static final int LAYOUT = R.layout.fragment_project_dynamic_list_head;

        public TextView head;

        public DynamicSectionHolder(View v) {
            super(v);
            head = (TextView) v.findViewById(R.id.head);
        }
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
            holder.actionMore.setOnClickListener(clickItemMore);
            return holder;
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, ProjectObject data, int position) {
            ProjectObject item = getItem(position);
            holder.name.setText(item.name);
            holder.memberCount.setText(String.format("%s 人", data.memberNum));
            holder.rootLayout.setTag(item);
            holder.actionMore.setTag(item);
            iconfromNetwork(holder.image, item.icon, ImageLoadTool.optionsRounded2);
        }

        @Override
        public long generateHeaderId(int i) {
            if (getItem(i).isJoined()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public ProjectHolder newHeaderHolder(View view) {
            return new ProjectHolder(view, true);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
            DynamicSectionHolder holder = (DynamicSectionHolder) viewHolder;
            if (generateHeaderId(pos) == 0) {
                holder.head.setText("我参与的");
            } else {
                holder.head.setText("我未参与的");
            }
        }

        @Override
        public UltimateRecyclerviewViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext()).inflate(DynamicSectionHolder.LAYOUT, parent, false);
            return new DynamicSectionHolder(v);
        }

        @Override
        public boolean hasHeaderView() {
            return super.hasHeaderView();
        }

        @Override
        public long getHeaderId(int position) {
            return super.getHeaderId(position);
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
        public View actionMore;

        public ProjectHolder(View view, boolean isHeader) {
            super(view);
        }

        public ProjectHolder(View view) {
            super(view);
            rootLayout = view;
            name = (TextView) view.findViewById(R.id.name);
            memberCount = (TextView) view.findViewById(R.id.memberCount);
            image = (ImageView) view.findViewById(R.id.icon);
            actionMore = view.findViewById(R.id.actionMore);
        }
    }

    private View.OnClickListener clickItem = v -> {
        Object object = v.getTag();
        if (object instanceof ProjectObject) {
            ProjectObject item = (ProjectObject) object;

            if (item.isJoined()) {
                ProjectHomeActivity_.intent(this)
                        .mProjectObject(item)
                        .start();
            } else {
                showMiddleToast("无权进入项目");
            }
        }
    };

    private View.OnLongClickListener longClickItem = v -> {
        actionMore(v);

        return true;
    };

    private void actionMore(View v) {
        ProjectObject project = (ProjectObject) v.getTag();
        new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle)
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
    }

    private View.OnClickListener clickItemMore = this::actionMore;

    private void showDeleteDialog2fa(ProjectObject project) {
        hander2fa.sendEmptyMessage(0);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project_2fa, null);
        edit2fa = (EditText) textEntryView.findViewById(R.id.edit1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        AlertDialog dialog = builder
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
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
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

                        umengEvent(UmengEvent.E_USER_CENTER, "删除企业项目");

                        showButtomToast("删除成功");
                        listData.remove(project);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected boolean userEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        onRefresh();
    }
}
