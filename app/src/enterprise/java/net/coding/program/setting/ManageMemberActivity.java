package net.coding.program.setting;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.EnterpriseApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.model.EnterpriseInfo;
import net.coding.program.model.team.TeamMember;
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

@EActivity(R.layout.activity_manage_member)
public class ManageMemberActivity extends BackActivity {

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
        if (EnterpriseInfo.instance().isOwner() && (user.getType() != MemberAuthority.ower)) {
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

    private void actionRemove(TeamMember user) {

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
