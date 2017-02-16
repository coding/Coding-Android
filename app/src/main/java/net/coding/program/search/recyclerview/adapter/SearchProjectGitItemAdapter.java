package net.coding.program.search.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.coding.program.R;
import net.coding.program.search.recyclerview.holder.SearchProjectGitHolder;
import net.coding.program.search.recyclerview.holder.SearchProjectGitItemHolder;

/**
 * Created by zjh on 2017/2/16.
 * item
 */

public class SearchProjectGitItemAdapter extends RecyclerView.Adapter<SearchProjectGitItemHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public SearchProjectGitItemAdapter(Context mContext) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public SearchProjectGitItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.search_project_git_holder_item, parent, false);
        return new SearchProjectGitItemHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(SearchProjectGitItemHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }
}
