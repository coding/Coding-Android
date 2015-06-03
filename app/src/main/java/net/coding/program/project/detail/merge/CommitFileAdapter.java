package net.coding.program.project.detail.merge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.common.widget.DataAdapter;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.BaseComment;
import net.coding.program.model.DiffFile;

/**
 * Created by chenchao on 15/6/3.
 * item可以为file或者comment
 */
public class CommitFileAdapter extends DataAdapter<Object> {

    BaseCommentParam mCommentParam;

    public CommitFileAdapter(BaseCommentParam param) {
        mCommentParam = param;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            ViewHoder holder;
            if (convertView == null) {
                holder = new ViewHoder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mergefile_list_item, parent, false);
                holder.icon = convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.insertion = (TextView) convertView.findViewById(R.id.insertion);
                holder.deletion = (TextView) convertView.findViewById(R.id.deletion);
                convertView.setTag(holder);
            } else {
                holder = (ViewHoder) convertView.getTag();
            }

            DiffFile.DiffSingleFile data = (DiffFile.DiffSingleFile) getItem(position);
            holder.title.setText(data.getName());
            holder.insertion.setText(data.getInsertions());
            holder.deletion.setText(data.getDeletions());

            return convertView;
        } else {
            BaseCommentHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_comment_much_image, parent, false);
                holder = new ImageCommentHolder(convertView, mCommentParam);
                convertView.setTag(R.id.layout, holder);
            } else {
                holder = (BaseCommentHolder) convertView.getTag(R.id.layout);
            }

            BaseComment data = (BaseComment) getItem(position);
            holder.setContent(data);

            return convertView;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object object = getItem(position);
        if (object instanceof DiffFile.DiffSingleFile) {
            return 0;
        } else { // comment
            return 1;
        }
    }

    private static class ViewHoder {
        View icon;
        TextView title;
        TextView insertion;
        TextView deletion;
    }
}
