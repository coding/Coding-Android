package net.coding.program.setting;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import net.coding.program.R;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.RecyclerViewSpace;
import net.coding.program.common.widget.CommonAdapter;
import net.coding.program.common.widget.CommonListView;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.user.Member;
import net.coding.program.network.model.user.MemberRole;
import net.coding.program.project.detail.MemberAuthorityActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

// 从企业所有项目中选择一个项目进行操作
@EActivity(R.layout.common_simple_listview)
public class CommonPickProjectActivity extends BackActivity {

    private static final int RESULT_SET_AUTHORITY = 1;

    @Extra
    Member member;

    @ViewById
    CommonListView listView;

    ProjectAdapter adapter;

    ArrayList<MemberRole> listData = new ArrayList<>();

    @AfterViews
    void initCommonPickProjectActivity() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new RecyclerViewSpace(this));

        adapter = new ProjectAdapter(listData);
        listView.setAdapter(adapter);
        listView.setDefaultOnRefreshListener(() -> onRefresh());
        listView.enableDefaultSwipeRefresh(true);

        onRefresh();
    }

    private void onRefresh() {
        Network.getRetrofit(CommonPickProjectActivity.this, listView)
                .getUserJoinedProjects(GlobalData.getEnterpriseGK(), member.user.global_key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<List<MemberRole>>(this, listView) {
                    @Override
                    public void onSuccess(List<MemberRole> data) {
                        super.onSuccess(data);
                        listData.clear();
                        listData.addAll(data);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    protected class ProjectAdapter extends CommonAdapter<MemberRole, ProjectHolder> {

        public ProjectAdapter(List<MemberRole> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.common_pick_project_list_item;
        }

        @Override
        protected ProjectHolder newViewHolder(View view) {
            ProjectHolder holder = new ProjectHolder(view);
            holder.rootLayout.setOnClickListener(clickItem);
            return holder;
        }

        @Override
        protected void withBindHolder(ProjectHolder holder, MemberRole data, int position) {
            MemberRole item = getItem(position);
            holder.name.setText(item.project.name);
            holder.authority.setText(data.getAuthority().projectName);
            holder.rootLayout.setTag(item);
            iconfromNetwork(holder.image, item.project.icon, ImageLoadTool.optionsRounded2);
        }

        @Override
        public long generateHeaderId(int i) {
            return super.generateHeaderId(i);
        }

        @Override
        public ProjectHolder newHeaderHolder(View view) {
            return new ProjectHolder(view, true);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
            super.onBindHeaderViewHolder(viewHolder, pos);
        }

        @Override
        public UltimateRecyclerviewViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            return super.onCreateHeaderViewHolder(parent);
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
        public ImageView image;
        public TextView authority;
        public View rootLayout;

        public ProjectHolder(View view, boolean isHeader) {
            super(view);
        }

        public ProjectHolder(View view) {
            super(view);
            rootLayout = view;
            name = (TextView) view.findViewById(R.id.name);
            authority = (TextView) view.findViewById(R.id.authority);
            image = (ImageView) view.findViewById(R.id.icon);
        }
    }

    private View.OnClickListener clickItem = v -> {
        Object object = v.getTag();
        if (object instanceof MemberRole) {
            MemberRole item = (MemberRole) object;
            MemberAuthorityActivity_.intent(CommonPickProjectActivity.this)
                    .globayKey(member.user.global_key)
                    .authority(item.getAuthority())
                    .projectId(item.projectId)
                    .startForResult(RESULT_SET_AUTHORITY);
        }
    };

    @OnActivityResult(RESULT_SET_AUTHORITY)
    void onResultSetAuthority(int result, @OnActivityResult.Extra int intentData,
                              @OnActivityResult.Extra int intentData1) {
        if (result == RESULT_OK) {
            for (MemberRole item : listData) {
                if (item.projectId == intentData1) {
                    item.type = intentData;
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
