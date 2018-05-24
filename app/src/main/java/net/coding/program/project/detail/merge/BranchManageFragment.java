package net.coding.program.project.detail.merge;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.flyco.roundview.RoundTextView;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.PagerData;
import net.coding.program.network.model.code.Branch;
import net.coding.program.network.model.code.BranchMetrics;
import net.coding.program.project.git.BranchMainActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_code_branch_manager)
public class BranchManageFragment extends BaseFragment {

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById(R.id.codingSwipeLayout)
    SwipeRefreshLayout codingSwipeLayout;

    @ViewById(R.id.codingRecyclerView)
    RecyclerView codingRecyclerView;

    Branch defaultBranch;

    LoadMoreAdapter codingAdapter;

    PagerData<Branch> listData = new PagerData<>();

    @AfterViews
    void initBranchManageFragment() {
        codingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        codingSwipeLayout.setColorSchemeResources(R.color.font_green);

        codingAdapter = new LoadMoreAdapter(listData.data);
        codingAdapter.setLoadMoreView(new CodingRecyclerLoadMoreView());
        codingAdapter.setOnLoadMoreListener(() -> requestPage(), codingRecyclerView);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            Branch branch = (Branch) adapter.getItem(position);
            BranchMainActivity_.intent(BranchManageFragment.this).mProjectPath(mProjectObject.getProjectPath()).mVersion(branch.name).start();
        });


        codingAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Branch branch = (Branch) adapter.getItem(position);
                if (branch.isDefaultBranch) {
                    showButtomToast("不能删除默认分支");
                    return true;
                }

                new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                        .setItems(new String[]{"删除"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showDialog(String.format("请确认是否要删除分支 %s ？", branch.name), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Network.getRetrofit(getActivity())
                                                .deleteBranch(mProjectObject.owner_user_name, mProjectObject.name, branch.name)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new BaseHttpObserver(getActivity()) {
                                                    @Override
                                                    public void onSuccess() {
                                                        super.onSuccess();

                                                        showButtomToast(String.format("分支 %s 已删除", branch.name));
                                                        codingAdapter.remove(position);
                                                    }
                                                });
                                    }
                                });
                            }
                        })
                        .show();

                return true;
            }
        });

        codingRecyclerView.setAdapter(codingAdapter);
        codingRecyclerView.addItemDecoration(new CodingRecyclerViewSpace(getActivity()));

        codingAdapter.setEmptyView(R.layout.loading_view, codingRecyclerView);

        onRefrush();

        codingSwipeLayout.setOnRefreshListener(this::onRefrush);
    }

    public void onRefrush() {
        codingAdapter.setEnableLoadMore(false);
        listData.page = 0;
        if (defaultBranch == null) {
            requestDefault();
        } else {
            requestPage();
        }
    }

    private void requestDefault() {
        Network.getRetrofit(getActivity())
                .getDefaultBranch(mProjectObject.owner_user_name, mProjectObject.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Branch>(getActivity()) {
                    @Override
                    public void onSuccess(Branch data) {
                        super.onSuccess(data);

                        defaultBranch = data;
                        requestPage();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {

                        codingSwipeLayout.setRefreshing(false);
                        if (errorCode == 1204) {
                            codingAdapter.setEmptyView(R.layout.empty_view, codingRecyclerView);
                            ((TextView) codingAdapter.getEmptyView().findViewById(R.id.message)).setText(error);
                        } else {
                            super.onFail(errorCode, error);
                        }

                    }
                });
    }

    private void requestPage() {
        Network.getRetrofit(getActivity())
                .getBranches(mProjectObject.owner_user_name, mProjectObject.name, listData.page + 1, "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(branchHttpPageResult -> {
                    if (listData.page == 0) {
                        listData.clear();
                    }
                    listData.addData(branchHttpPageResult.data);

                    StringBuilder sb = new StringBuilder();
                    boolean isFirst = true;
                    for (Branch item : branchHttpPageResult.data.list) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(item.lastCommit.commitId);
                    }
                    String base = defaultBranch.lastCommit.commitId;
                    return Network.getRetrofit(getActivity())
                            .getBranchMetrics(mProjectObject.owner_user_name, mProjectObject.name,
                                    base, sb.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribe(new HttpObserver<HashMap<String, BranchMetrics>>(getActivity()) {
                    @Override
                    public void onSuccess(HashMap<String, BranchMetrics> data) {
                        super.onSuccess(data);

                        for (Branch item : listData.data) {
                            if (item.metrics == null) {
                                item.metrics = data.get(item.lastCommit.commitId);
                            }
                        }

                        codingAdapter.notifyDataSetChanged();
                        codingSwipeLayout.setRefreshing(false);
                        codingAdapter.loadMoreEnd(listData.isLoadAll());

                        codingAdapter.setEnableLoadMore(true);

                        if (listData.isLoadAll()) {
                            codingAdapter.loadMoreEnd();
                        } else {
                            codingAdapter.loadMoreComplete();
                        }

                        codingAdapter.setEmptyView(R.layout.empty_view, codingRecyclerView);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        codingSwipeLayout.setRefreshing(false);
                        codingAdapter.loadMoreFail();
                    }
                });
    }

    static class LoadMoreAdapter extends BaseQuickAdapter<Branch, BaseViewHolder> {

        public LoadMoreAdapter(@Nullable List<Branch> data) {
            super(R.layout.branch_list_item, data);
            SingleTask.initDate();
        }

        @Override
        protected void convert(BaseViewHolder helper, Branch item) {
            RoundTextView nameText = helper.getView(R.id.name);
            nameText.getDelegate().setBackgroundColor(item.isDefaultBranch ? CodingColor.font1 : CodingColor.divideLine);
            nameText.setTextColor(item.isDefaultBranch ? CodingColor.fontWhite : CodingColor.font3);
            nameText.setText(item.name);

            helper.getView(R.id.flagSafe).setVisibility(item.isProtected ? View.VISIBLE : View.INVISIBLE);
            helper.setText(R.id.time, "更新于 " + Global.simpleDayByNow(item.lastCommit.commitTime));

            View metricsLayout = helper.getView(R.id.metricsLayout);
            if (item.metrics == null || item.isDefaultBranch) {
                metricsLayout.setVisibility(View.GONE);
            } else {
                metricsLayout.setVisibility(View.VISIBLE);
                setViewWidth(helper.getView(R.id.left), item.metrics.ahead);
                setViewWidth(helper.getView(R.id.right), item.metrics.behind);
                helper.setText(R.id.leftText, String.valueOf(item.metrics.ahead));
                helper.setText(R.id.rightText, String.valueOf(item.metrics.behind));
            }
        }

        void setViewWidth(View v, int dp) {
            if (dp > 40) dp = 40;
            int px = GlobalCommon.dpToPx(dp);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = px;
            v.setLayoutParams(lp);
        }
    }
}
