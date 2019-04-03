package net.coding.program.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.SingleTask;
import net.coding.program.search.HoloUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Vernon on 15/11/26.
 */
public class SearchReslutAdapter extends BaseAdapter {
    private List<SingleTask> mData;
    private Context context;
    private String key;


    public SearchReslutAdapter(List<SingleTask> mData, Context context, String key) {
        this.mData = mData;
        this.context = context;
        this.key = key;

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_task_list, null);
            holder = new ViewHolder();
            holder.nameTask = (TextView) convertView.findViewById(R.id.nameTask);
            holder.iconTask = (ImageView) convertView.findViewById(R.id.iconTask);
            holder.bottomName = (TextView) convertView.findViewById(R.id.bottomName);
            holder.bottomTime = (TextView) convertView.findViewById(R.id.bottomTime);
            holder.bottomHeartCount = (TextView) convertView.findViewById(R.id.bottomHeartCount);
            holder.bottomCommentCount = (TextView) convertView.findViewById(R.id.bottomCommentCount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SingleTask bean = mData.get(position);
        HoloUtils.setHoloText(holder.nameTask, bean.content);
        holder.bottomName.setText(bean.creator.name);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        holder.bottomTime.setText(format.format(bean.created_at));
        holder.bottomCommentCount.setText(bean.comments + "");
        holder.bottomHeartCount.setText("");
        ImageLoader.getInstance().displayImage(bean.owner.avatar, holder.iconTask, ImageLoadTool.optionsImage);
        return convertView;
    }

    static class ViewHolder {
        TextView nameTask;
        TextView bottomName;
        TextView bottomTime;
        TextView bottomCommentCount;
        TextView bottomHeartCount;
        ImageView iconTask;
    }

}
