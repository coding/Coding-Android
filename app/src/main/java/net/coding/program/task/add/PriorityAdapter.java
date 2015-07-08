package net.coding.program.task.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringArrayRes;

/**
 * Created by chenchao on 15/7/8.
 * 任务优先级的 adpter
 */
@EBean
public class PriorityAdapter extends BaseAdapter {

    final int priorityDrawableInverse[] = new int[]{
            R.drawable.ic_task_priority_3,
            R.drawable.ic_task_priority_2,
            R.drawable.ic_task_priority_1,
            R.drawable.ic_task_priority_0
    };
    protected LayoutInflater mInflater;
    @RootContext
    Context mContext;
    @StringArrayRes(R.array.strings_priority_inverse)
    String[] mData;

    @AfterInject
    void initPriorityAdapter() {
        mInflater = LayoutInflater.from(mContext);
    }

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
            convertView = mInflater.inflate(R.layout.activity_task_status_list_item, parent, false);
            holder.mTitle = (TextView) convertView.findViewById(R.id.title);
            holder.mCheck = (ImageView) convertView.findViewById(R.id.check);
            holder.mIcon = convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(mData[position]);
        holder.mIcon.setBackgroundResource(priorityDrawableInverse[position]);
        if (position == getSelectPos())
            holder.mCheck.setVisibility(android.view.View.VISIBLE);
        else
            holder.mCheck.setVisibility(android.view.View.GONE);
        return convertView;
    }

    private int getSelectPos() {
        TaskParams mNewParam = ((NewTaskParam) mContext).getNewParam();
        return priorityDrawableInverse.length - 1 - mNewParam.priority;
    }

    static class ViewHolder {
        View mIcon;
        ImageView mCheck;
        TextView mTitle;
    }
}
