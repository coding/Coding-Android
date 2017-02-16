package net.coding.program.search.recyclerview.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.recycler.holder.RecyclerBaseHolder;

/**
 * Created by zjh on 2017/2/16.
 * 代码搜索holder
 */

public class SearchProjectGitItemHolder extends RecyclerBaseHolder {
    private TextView tvName;
    public SearchProjectGitItemHolder(View itemView, Context mContext) {
        super(itemView, mContext);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
    }

    public void setData(String name){
        tvName.setText(name);
    }
}
