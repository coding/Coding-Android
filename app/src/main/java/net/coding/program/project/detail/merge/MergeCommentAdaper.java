package net.coding.program.project.detail.merge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.coding.program.R;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.BaseComment;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/5/28.
 */
public class MergeCommentAdaper extends BaseAdapter {

    ArrayList<BaseComment> mData = new ArrayList<>();
    BaseCommentHolder.BaseCommentParam mCommentParam;

    public MergeCommentAdaper(BaseCommentHolder.BaseCommentParam param) {
        mCommentParam = param;
    }

    public void appendData(ArrayList<BaseComment> data) {
        mData.addAll(data);
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
        BaseCommentHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_comment_much_image, parent, false);
            holder = new BaseCommentHolder(convertView, mCommentParam);
            convertView.setTag(R.id.layout, holder);
        } else {
            holder = (ImageCommentHolder) convertView.getTag(R.id.layout);
        }

        BaseComment data = (BaseComment) getItem(position);
        holder.setContent(data);

        return convertView;
    }
}
