package net.coding.program.search.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.coding.program.R;
import net.coding.program.search.recyclerview.holder.SearchProjectGitHeaderHolder;
import net.coding.program.search.recyclerview.holder.SearchProjectGitHolder;

import java.util.List;


/**
 * Created by zjh on 2017/2/16.
 *
 */

public class SearchProjectGitAdapter extends RecyclerView.Adapter {
    private static final int ITEM_HEADER = 0;
    private static final int ITEM = 1;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int currentType = 0;
    private SearchProjectGitHeaderHolder searchProjectGitHeaderHolder;
    private SearchProjectGitHolder searchProjectGitHolder;

    public SearchProjectGitAdapter(Context mContext) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_HEADER) {
            View view = mLayoutInflater.inflate(R.layout.search_project_git_holder_header, parent, false);
            searchProjectGitHeaderHolder = new SearchProjectGitHeaderHolder(view, mContext);
            return searchProjectGitHeaderHolder;
        }else if(viewType == ITEM){
            View view = mLayoutInflater.inflate(R.layout.search_project_git_holder, parent, false);
            searchProjectGitHolder = new SearchProjectGitHolder(view, mContext);
            return searchProjectGitHolder;
        }
        return null;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case ITEM_HEADER:
                currentType = ITEM_HEADER;
                break;
            case ITEM:
                currentType = ITEM;
                break;
        }
        return currentType;
    }

    public void setData(List<String> name){

    }
}
