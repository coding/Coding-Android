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

public class SearchProjectGitHeaderHolder extends RecyclerBaseHolder {
    private TextView tvLength;
    public SearchProjectGitHeaderHolder(View itemView, Context mContext) {
        super(itemView, mContext);
        tvLength = (TextView) itemView.findViewById(R.id.tv_length);
    }

    public void setData(String length){
        tvLength.setText(length);
    }
}
