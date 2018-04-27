package net.coding.program.task.board;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.flyco.roundview.RoundTextView;

import net.coding.program.R;
import net.coding.program.common.event.EventBoardFinish;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.network.PagerData;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.project.detail.merge.CodingRecyclerLoadMoreView;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_board_list)
public class BoardFragment extends BaseFragment {

    @FragmentArg
    int boardListId;

    @ViewById(R.id.listView)
    RecyclerView codingRecyclerView;

    LoadMoreAdapter codingAdapter;

    @ViewById(R.id.refreshLayout)
    SwipeRefreshLayout codingSwipeLayout;

    @ViewById
    TextView boardListTitle;

    BoardList boardList;

    PagerData<SingleTask> listData = new PagerData<>();

    @AfterViews
    void initBoardFragment() {
        boardList = ((TaskBoardActivity) getActivity()).getBoardList(boardListId);
        listData.addData(boardList.tasks);
        boardListTitle.setText(boardList.title);

        codingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        codingSwipeLayout.setColorSchemeResources(R.color.font_green);

        codingAdapter = new LoadMoreAdapter(listData.data);
        codingAdapter.setLoadMoreView(new CodingRecyclerLoadMoreView());
        codingAdapter.setOnLoadMoreListener(() -> requestPage(), codingRecyclerView);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            SingleTask task = (SingleTask) adapter.getItem(position);
            TaskAddActivity_.intent(this)
                    .mSingleTask(task)
                    .start();
        });

        codingRecyclerView.setAdapter(codingAdapter);
        codingRecyclerView.addItemDecoration(new CodingRecyclerViewSpace(getActivity()));
        onRefrush();

        codingSwipeLayout.setOnRefreshListener(this::onRefrush);
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBoardFinish(EventBoardFinish event) {
        onRefrush();
    }

    public void onRefrush() {

    }

    private void requestPage() {

    }

    static class LoadMoreAdapter extends BaseQuickAdapter<SingleTask, BaseViewHolder> {

        public LoadMoreAdapter(@Nullable List<SingleTask> data) {
            super(R.layout.board_list_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, SingleTask item) {
            helper.getView(R.id.taskPriority).setBackgroundResource(item.getPriorityIcon());
            helper.setText(R.id.title, item.content);
            TextView deadline = helper.getView(R.id.deadline);
            SingleTask.setDeadline(deadline, item);

            ViewGroup tagLayout = helper.getView(R.id.tagLayout);
            if (item.labels == null || item.labels.isEmpty()) {
                tagLayout.setVisibility(View.GONE);
            } else {
                tagLayout.setVisibility(View.VISIBLE);
                tagLayout.removeAllViews();
                for (TopicLabelObject label : item.labels) {
                    RoundTextView labelView = (RoundTextView) mLayoutInflater.inflate(R.layout.board_list_item_tag, tagLayout, false);
                    labelView.getDelegate().setBackgroundColor(label.getColor());
                }
            }

            CheckBox checkBox = helper.getView(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(item.isDone());
            if (!item.isDone()) {
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Network.getRetrofit(mContext)
                                .modifyTaskStatus(item.getId(),  2)   // 2 完成，1 未完成
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new BaseHttpObserver(mContext) {
                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();
                                        remove(helper.getAdapterPosition());
                                        EventBus.getDefault().post(new EventBoardFinish());
                                    }

                                    @Override
                                    public void onFail(int errorCode, @NonNull String error) {
                                        super.onFail(errorCode, error);
                                    }
                                });
                    }
                });
            } else {
                // 已完成列表
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Network.getRetrofit(mContext)
                                .modifyTaskStatus(item.getId(),  1)   // 2 完成，1 未完成
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new BaseHttpObserver(mContext) {
                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();
                                        remove(helper.getAdapterPosition());
                                    }

                                    @Override
                                    public void onFail(int errorCode, @NonNull String error) {
                                        super.onFail(errorCode, error);
                                    }
                                });
                    }
                });
            }
        }

        private void modifyTask(SingleTask task, boolean done) {

        }
    }

}
