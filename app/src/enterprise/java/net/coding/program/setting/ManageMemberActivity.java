package net.coding.program.setting;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.EnterpriseApp;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.EnterpriseInfo;
import net.coding.program.model.team.TeamMember;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.constant.MemberAuthority;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_manage_member)
public class ManageMemberActivity extends BackActivity implements Handler.Callback {

    private static final String TAG_PROJECT = "TAG_PROJECT";

    private static final int RESULT_SET_ENTERPRISE_ROLE = 1;
    private static final int RESULT_REMOVE_MEMBER = 2;

    @ViewById
    UltimateRecyclerView listView;

    MemberAdapter adapter;

    ArrayList<TeamMember> listData = new ArrayList<>();

    @AfterViews
    void initManageMemberActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));
        listView.setEmptyView(R.layout.fragment_enterprise_project_empty, R.layout.fragment_enterprise_project_empty);

        adapter = new MemberAdapter(listData);
        listView.setAdapter(adapter);
        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.enableDefaultSwipeRefresh(true);

        onRefresh();

        hander2fa = new WeakRefHander(this, 100);
    }

    private void onRefresh() {
        String host = String.format("%s/team/%s/members", Global.HOST_API, EnterpriseApp.getEnterpriseGK());
        getNetwork(host, TAG_PROJECT);
    }

    protected class MemberAdapter extends easyRegularAdapter<TeamMember, ProjectHolder> {

        public MemberAdapter(List<TeamMember> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.enterprise_manage_member_list_item;
        }

        @Override
        protected ProjectHolder newViewHolder(View view) {
            ProjectHolder holder = new ProjectHolder(view);
            holder.actionMore.setOnClickListener(clickItemMore);
            holder.rootLayout.setOnClickListener(clickItemMore);
            holder.rootLayout.setOnLongClickListener(longClickItem);
            return holder;
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, TeamMember data, int position) {
            TeamMember item = getItem(position);
            holder.name.setText(item.user.name);
            holder.joinTime.setText(String.format("加入时间：%s", Global.mDateYMDHH.format(data.updatedat)));
            holder.rootLayout.setTag(item);
            holder.actionMore.setTag(item);

            MemberAuthority memberType = item.getType();
            int iconRes = memberType.getIcon();
            holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0);
            iconfromNetwork(holder.image, item.user.avatar);
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

    private View.OnClickListener clickItemMore = this::actionMore;

    private View.OnLongClickListener longClickItem = v -> {
        actionMore(v);
        return true;
    };

    private void actionMore(View v) {
        TeamMember user = (TeamMember) v.getTag();
        if ((user.getType() == MemberAuthority.ower)) {
            new AlertDialog.Builder(this)
                    .setItems(R.array.manager_member_to_owner, ((dialog, which) -> {
                        if (which == 0) {
                            actionModifyProjectRole(user);
                        }
                    }))
                    .show();
        } else if (EnterpriseInfo.instance().isOwner()) {
            new AlertDialog.Builder(this)
                    .setItems(R.array.manager_member_by_owner, ((dialog, which) -> {
                        if (which == 0) {
                            actionModifyEnterpriseRole(user);
                        } else if (which == 1) {
                            actionModifyProjectRole(user);
                        } else if (which == 2) {
                            actionRemove(user);
                        }
                    }))
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setItems(R.array.manager_member_by_manager, ((dialog, which) -> {
                        if (which == 0) {
                            actionModifyProjectRole(user);
                        } else if (which == 1) {
                            actionRemove(user);
                        }
                    }))
                    .show();
        }
    }

    public static class ProjectHolder extends UltimateRecyclerviewViewHolder {

        public TextView name;
        public TextView joinTime;
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
            joinTime = (TextView) view.findViewById(R.id.joinTime);
            image = (ImageView) view.findViewById(R.id.icon);
            actionMore = view.findViewById(R.id.actionMore);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_PROJECT)) {
            if (code == 0) {
                listData.clear();
                JSONArray array = respanse.optJSONArray("data");
                for (int i = 0; i < array.length(); ++i) {
                    listData.add(new TeamMember(array.optJSONObject(i)));
                }

                adapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    Handler hander2fa;
    private EditText edit2fa;

    private void actionRemove(TeamMember user) {
        hander2fa.sendEmptyMessage(0);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project_2fa, null);
        edit2fa = (EditText) textEntryView.findViewById(R.id.edit1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setView(textEntryView)
                .setPositiveButton("确定", (dialog1, whichButton) -> {
                    String editStr1 = edit2fa.getText().toString().trim();
                    if (TextUtils.isEmpty(editStr1)) {
                        Toast.makeText(ManageMemberActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    actionDelete2FA(user, editStr1);
                })
                .setNegativeButton("取消", null)
                .show();

        dialog.setOnDismissListener(dialog1 -> {
            hander2fa.removeMessages(0);
            edit2fa = null;
        });
    }

    private void actionDelete2FA(TeamMember user, String code) {
        Network.getRetrofit(this)
                .removeEnterpriseMember(MyApp.getEnterpriseGK(), user.user.global_key, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        showProgressBar(false);

                        listData.remove(user);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });

        showProgressBar(true);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (edit2fa != null) {
            String secret = AccountInfo.loadAuth(this, MyApp.sUserObject.global_key);
            if (secret.isEmpty()) {
                return true;
            }

            String code2FA = new AuthInfo(secret, new TotpClock(this)).getCode();
            edit2fa.setText(code2FA);
        }
        return true;
    }

    private void actionModifyProjectRole(TeamMember user) {
        CommonPickProjectActivity_.intent(this)
                .member(user)
                .start();
    }

    private void actionModifyEnterpriseRole(TeamMember user) {
        SetEnterpriseAuthorityActivity_.intent(this)
                .globayKey(user.user.global_key)
                .authority(user.getType())
                .startForResult(RESULT_SET_ENTERPRISE_ROLE);
    }

    @OnActivityResult(RESULT_SET_ENTERPRISE_ROLE)
    void onResultSetEnterpriseRole(int resultCode, @OnActivityResult.Extra int intentData,
                                   @OnActivityResult.Extra String intentData1) {
        if (resultCode == RESULT_OK) {
            for (TeamMember item : listData) {
                if (item.user.global_key.equals(intentData1)) {
                    item.role = intentData;
                    break;
                }
            }
        }
    }

}
