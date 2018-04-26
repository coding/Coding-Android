package net.coding.program.task.board;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.coding.program.R;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.network.model.code.Branch;
import net.coding.program.network.model.task.BoardList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_board_list)
public class BoardFragment extends BaseFragment {

    @FragmentArg
    int boardListId;

    @ViewById
    RecyclerView listView;

    @ViewById
    SwipeRefreshLayout refreshLayout;

    @ViewById
    TextView boardListTitle;

    BoardList boardList;

    @AfterViews
    void initBoardFragment() {
        boardList = ((TaskBoardActivity) getActivity()).getBoardList(boardListId);

    }

    static class LoadMoreAdapter extends BaseQuickAdapter<Branch, BaseViewHolder> {

        public LoadMoreAdapter(@Nullable List<Branch> data) {
            super(R.layout.board_list_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Branch item) {

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
