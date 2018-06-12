package net.coding.program.task.board;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.ui.shadow.CodingRecyclerViewSpace;
import net.coding.program.network.model.task.BoardList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.fragment_code_branch_manager)
public class PickTaskBoardActivity extends BackActivity {

    @Extra
    ArrayList<BoardList> boardLists;

    @Extra
    int pickBoardId;

    @ViewById(R.id.codingRecyclerView)
    RecyclerView codingRecyclerView;

    LoadMoreAdapter codingAdapter;

    @AfterViews
    void initPickTaskBoardActivity() {
        codingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        codingAdapter = new LoadMoreAdapter(boardLists, pickBoardId);
        codingAdapter.setOnItemClickListener((adapter, view, position) -> {
            BoardList data = (BoardList) adapter.getItem(position);
            Intent intent = new Intent();
            intent.putExtra("intentData", data);
            setResult(RESULT_OK, intent);
            finish();
        });

        codingRecyclerView.setAdapter(codingAdapter);
        codingRecyclerView.addItemDecoration(new CodingRecyclerViewSpace(this));

        codingAdapter.setEmptyView(R.layout.loading_view, codingRecyclerView);
    }

    static class LoadMoreAdapter extends BaseQuickAdapter<BoardList, BaseViewHolder> {

        int pick;

        LoadMoreAdapter(@Nullable List<BoardList> data, int pick) {
            super(R.layout.pick_board_list_item, data);
            this.pick = pick;
        }

        @Override
        protected void convert(BaseViewHolder helper, BoardList item) {
            helper.setText(R.id.name, item.title);
            helper.setVisible(R.id.check, item.id == pick);
        }

    }

}
