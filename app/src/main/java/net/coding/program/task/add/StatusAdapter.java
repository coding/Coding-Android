package net.coding.program.task.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringArrayRes;

/**
 * Created by chenchao on 15/7/7.
 * 任务是否完成的 adapter
 */
@EBean
public class StatusAdapter extends BaseAdapter {

    @RootContext
    Context mContext;

    @StringArrayRes(R.array.task_status)
    String[] mData;

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public Object getItem(int position) {
        return mData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_status_list_item, parent, false);
            holder.mTitle = (TextView) convertView.findViewById(R.id.title);
            holder.mCheck = (ImageView) convertView.findViewById(R.id.check);
            holder.mIcon = convertView.findViewById(R.id.icon);
            holder.mIcon.setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(mData[position]);
        if (position == getSelectPos())
            holder.mCheck.setVisibility(View.VISIBLE);
        else
            holder.mCheck.setVisibility(View.GONE);
        return convertView;
    }

    private int getSelectPos() {
        TaskParams mNewParam = ((NewTaskParam) mContext).getNewParam();
        if (mNewParam.status == 1) {
            return 0;
        } else {
            return 1;
        }
    }

    static class ViewHolder {
        View mIcon;
        ImageView mCheck;
        TextView mTitle;
    }
}
