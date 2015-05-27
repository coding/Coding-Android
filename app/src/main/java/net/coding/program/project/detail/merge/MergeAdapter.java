package net.coding.program.project.detail.merge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.model.Merge;
import net.coding.program.project.detail.TopicListFragment;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/5/27.
 */
public class MergeAdapter extends BaseAdapter {

    ArrayList<Merge> mData;

    public MergeAdapter(ArrayList<Merge> mData) {
        this.mData = mData;
    }

    public void appendData(ArrayList<Merge> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void resetData(ArrayList<Merge> data) {
        mData = data;
        notifyDataSetChanged();
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
        TopicListFragment.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_project_topic_list_item, parent, false);
            holder = new TopicListFragment.ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//            holder.icon.setOnClickListener(onClickUser);

            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.time.setFocusable(false);

            holder.discuss = (TextView) convertView.findViewById(R.id.discuss);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);

        } else {
            holder = (TopicListFragment.ViewHolder) convertView.getTag();
        }


        Merge data = (Merge) getItem(position);

//        iconfromNetwork(holder.icon, data.owner.avatar);
        holder.icon.setTag(data.getAuthor().global_key);

        holder.title.setText(Global.changeHyperlinkColor(data.getTitle()));

        holder.name.setText(data.getAuthor().name);
        holder.time.setText(Global.changeHyperlinkColor(Global.dayToNow(data.getCreatedAt())));
//        holder.discuss.setText(String.format("%d", data.child_count));


        return convertView;
    }
}
