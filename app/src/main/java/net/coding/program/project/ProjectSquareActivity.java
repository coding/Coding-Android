package net.coding.program.project;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.coding.program.R;
import net.coding.program.route.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.ui.shadow.LoadMoreRecyclerViewSpace;
import net.coding.program.common.widget.RefreshBaseActivity;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.model.Pager;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.search.SearchProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Vernon on 15/11/20.
 */
@OptionsMenu(R.menu.menu_project_pick_search)
@EActivity(R.layout.activity_project_sqaure)
public class ProjectSquareActivity extends RefreshBaseActivity implements OnClickListener {

    final String hostSquare = Global.HOST_API + "/public/all?page=1&pageSize=1000";

    @ViewById
    View emptyView, container;

    @ViewById(R.id.listView)
    RecyclerView listView;

    @ViewById
    View blankLayout;

    @ViewById(R.id.project_create_layout)
    LinearLayout projectCreateLayout;

    @ViewById
    Button btn_action;
    private ArrayList<ProjectObject> mData = new ArrayList<>();
    private BaseQuickAdapter<ProjectObject, BaseViewHolder> adapter;

    private boolean mRequestOk;
    private boolean requestOk;

    Pager<ProjectObject> listData = new Pager<>();

    private OnClickListener mOnClickRetry = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @AfterViews
    void init() {
        initRefreshBaseActivity();
        btn_action.setOnClickListener(this);
        showDialogLoading();

        adapter = new BaseQuickAdapter<ProjectObject, BaseViewHolder>(R.layout.project_all_list_item, listData.list) {
            @Override
            protected void convert(BaseViewHolder helper, ProjectObject item) {
                helper.setVisible(R.id.privateIcon, false);
                helper.setVisible(R.id.name2, true);
                helper.setText(R.id.name2, item.name);
                helper.setVisible(R.id.privateIcon, !item.isPublic());
                helper.setVisible(R.id.comment, false);
                helper.setText(R.id.txtDesc, item.getDescription());
                helper.setText(R.id.tv_follow_count, item.getWatchCountString());
                helper.setText(R.id.tv_start_count, item.getStarString());
                helper.setText(R.id.tv_fork_count, item.getForkCountString());
                helper.setVisible(R.id.badge, false);
                helper.setVisible(R.id.ll_bottom_menu, true);
                imagefromNetwork(helper.getView(R.id.icon), item.icon);
            }
        };

        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new LoadMoreRecyclerViewSpace(this));
        listView.setAdapter(adapter);
        adapter.setOnLoadMoreListener(this::loadMore, listView);
        adapter.setOnItemClickListener((adapter1, view, position) -> ProjectHomeActivity_.intent(ProjectSquareActivity.this).mProjectObject(listData.list.get(position)).start());

        loadMore();
    }

    private void loadMore() {
        getNetwork(hostSquare, hostSquare);

        Network.getRetrofit(this)
                .getAllPublic(listData.page + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<ProjectObject>>(this) {
                    @Override
                    public void onSuccess(Pager<ProjectObject> data) {
                        super.onSuccess(data);
                        setRefreshing(false);
                        hideProgressDialog();

                        if (data.page == 1) {
                            listData.list.clear();
                        }

                        listData.list.addAll(data.list);
                        listData.page = data.page;

                        if (data.page < data.totalPage) {
                            adapter.loadMoreComplete();
                        } else {
                            adapter.loadMoreEnd();
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        setRefreshing(false);
                        hideProgressDialog();

                        BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, mOnClickRetry);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onRefresh() {
        listData.page = 1;
        loadMore();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action:
                ProjectCreateActivity_.intent(this).start();
                break;
        }
    }

    @OptionsItem
    void action_search_pick() {
        SearchProjectActivity_.intent(this).start();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

//    @Override
//    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
//        if (tag.equals(hostSquare)) {
//            setRefreshing(false);
//            if (code == 0) {
//                requestOk = true;
//                hideProgressDialog();
//                mData.clear();
//                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
//                int pinCount = 0;
//                for (int i = 0; i < array.length(); ++i) {
//                    JSONObject item = array.getJSONObject(i);
//                    ProjectObject oneData = new ProjectObject(item);
//                    if (oneData.isPin()) {
//                        mData.add(pinCount++, oneData);
//                    } else {
//                        mData.add(oneData);
//                    }
//                }
//                AccountInfo.saveProjects(this, mData);
//                if (adapter == null) {
//                    adapter = new MyAdapter();
//                    listView.setAdapter(adapter);
//                } else {
//                    adapter.notifyDataSetChanged();
//                }
//                if (!(mData.size() > 0)) {
//                    projectCreateLayout.setVisibility(View.VISIBLE);
//                } else {
//                    projectCreateLayout.setVisibility(View.GONE);
//                }
//            } else {
//                requestOk = false;
//                showErrorMsg(code, respanse);
//                BlankViewDisplay.setBlank(mData.size(), this, mRequestOk, blankLayout, mOnClickRetry);
//            }
//        }
//    }

}

