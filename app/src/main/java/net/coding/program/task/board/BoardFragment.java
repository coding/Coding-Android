package net.coding.program.task.board;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.flyco.roundview.RoundTextView;

import net.coding.program.R;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.network.PagerData;
import net.coding.program.network.model.task.BoardList;
import net.coding.program.project.detail.merge.CodingRecyclerLoadMoreView;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

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
        }
    }

}
