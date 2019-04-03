package net.coding.program.project.detail.merge;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.PagerData;
import net.coding.program.network.model.Pager;
import net.coding.program.network.model.code.Release;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_code_branch_manager)
public class ReleaseManageFragment extends BaseFragment {

    private static final int RESULT_DETAIL = 1;

    @FragmentArg
    ProjectObject mProjectObject;

    @ViewById(R.id.codingSwipeLayout)
    SwipeRefreshLayout codingSwipeLayout;

    @ViewById(R.id.codingRecyclerView)
    RecyclerView codingRecyclerView;

    LoadMoreAdapter codingAdapter;

    PagerData<Release> listData = new PagerData<>();

    @AfterViews
    void initBranchManageFragment() {
        codingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        codingSwipeLayout.setColorSchemeResources(R.color.font_green);

        codingAdapter = new LoadMoreAdapter(listData.data);
        codingAdapter.setLoadMoreView(new CodingRecyclerLoadMoreView());
        codingAdapter.setOnLoadMoreListener(() -> requestPage(), codingRecyclerView);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            Release branch = (Release) adapter.getItem(position);
            ReleaseDetailActivity_.intent(ReleaseManageFragment.this)
                    .projectObject(mProjectObject)
                    .release(branch)
                    .startForResult(RESULT_DETAIL);
//            BranchMainActivity_.intent(ReleaseManageFragment.this).mProjectPath(mProjectObject.getProjectPath()).mVersion(branch.name).start();
        });


        codingAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Release branch = (Release) adapter.getItem(position);
                if (!branch.author.global_key.equals(GlobalData.sUserObject.global_key)) {
                    showMiddleToast("只能删除自己创建的版本");
                    return true;
                }

                new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                        .setItems(new String[]{"删除"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showDialog(String.format("请确认是否要删除版本 %s ？", branch.tagName), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Network.getRetrofit(getActivity())
                                                .deleteRelease(mProjectObject.owner_user_name, mProjectObject.name, branch.tagName)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new BaseHttpObserver(getActivity()) {
                                                    @Override
                                                    public void onSuccess() {
                                                        super.onSuccess();

                                                        showButtomToast(String.format("版本 %s 已删除", branch.tagName));
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
        requestPage();
    }

    private void requestPage() {
        Network.getRetrofit(getActivity())
                .getReleases(mProjectObject.owner_user_name, mProjectObject.name, listData.page + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Pager<Release>>(getActivity()) {

                    @Override
                    public void onSuccess(Pager<Release> data) {
                        super.onSuccess(data);
                        super.onSuccess(data);
                        if (listData.page == 0) {
                            listData.clear();
                        }
                        listData.addData(data);

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

    @OnActivityResult(RESULT_DETAIL)
    void onResultDetail(int result) {
        if (result == Activity.RESULT_OK) {
            onRefrush();
        }
    }

    static class LoadMoreAdapter extends BaseQuickAdapter<Release, BaseViewHolder> {

        LoadMoreAdapter(@Nullable List<Release> data) {
            super(R.layout.release_list_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Release item) {
            String title = item.title;
            if (TextUtils.isEmpty(title)) title = item.tagName;
            helper.setText(R.id.name, title);
            helper.setText(R.id.time, "发布于 " + Global.simpleDayByNow(item.createdAt));
            String ownerString = String.format("%s  %s", item.tagName, item.author.name);
            helper.setText(R.id.owner, ownerString);
            helper.getView(R.id.pre).setVisibility(item.pre ? View.VISIBLE : View.GONE);
            helper.getView(R.id.draft).setVisibility(View.GONE);
        }
    }
}
