package net.coding.program.common.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/5/26.
 */
public class DataAdapter<T> extends BaseAdapter {
    ArrayList<T> mData;

    public DataAdapter(ArrayList<T> mData) {
        this.mData = mData;
    }

    public void appendData(ArrayList<T> data) {
        mData.addAll(data);
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
//        ImageCommentHolder holder;
//        if (convertView == null) {
//            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_comment_much_image, parent, false);
//            holder = new ImageCommentHolder(convertView, onClickComment, myImageGetter, getImageLoad(), mOnClickUser, onClickImage);
//            convertView.setTag(R.id.layout, holder);
//        } else {
//            holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
//        }
//
//        TopicObject data = (TopicObject) getItem(position);
//        holder.setTaskCommentContent(data);
//
//        return convertView;
        return null;
    }
}
