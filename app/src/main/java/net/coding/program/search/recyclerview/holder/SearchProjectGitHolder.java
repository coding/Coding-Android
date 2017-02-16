package net.coding.program.search.recyclerview.holder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.coding.program.R;
import net.coding.program.recycler.holder.RecyclerBaseHolder;
import net.coding.program.search.recyclerview.adapter.SearchProjectGitItemAdapter;

/**
 * Created by zjh on 2017/2/16.
 * 代码搜索holder
 */

public class SearchProjectGitHolder extends RecyclerBaseHolder {

    public SearchProjectGitHolder(View itemView, Context mContext) {
        super(itemView, mContext);
        RecyclerView recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new SearchProjectGitItemAdapter(mContext));
    }
}
