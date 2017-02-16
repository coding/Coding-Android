package net.coding.program.recycler.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import static net.coding.program.R.drawable.on;

/**
 * Created by zjh on 2017/2/16.
 * RecyclerView holder基类
 */

public class RecyclerBaseHolder extends RecyclerView.ViewHolder {
    protected Context mContext;
    private OnItemClickListener onItemClickListener;

    public RecyclerBaseHolder(View itemView) {
        this(itemView, null);
    }

    public RecyclerBaseHolder(View itemView, Context mContext) {
        super(itemView);
        this.mContext = mContext;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick();
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick();
    }
}
