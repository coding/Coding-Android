package net.coding.program.project.detail.merge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.coding.program.R;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.common.comment.BaseCommentParam;
import net.coding.program.maopao.item.ImageCommentHolder;
import net.coding.program.model.BaseComment;
import net.coding.program.model.DiffFile;

/**
 * Created by chenchao on 15/6/3.
 * item可以为file或者comment
 */
public class CommitFileAdapter extends MergeFileAdapter {

    public static final int VIEW_TAG_FILE_DATA = R.layout.mergefile_list_item;
    BaseCommentParam mCommentParam;
    private View.OnClickListener mClickFileItem;

    public CommitFileAdapter(BaseCommentParam param, View.OnClickListener clickFileItem) {
        super();

        mCommentParam = param;
        mClickFileItem = clickFileItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            View view = super.getView(position, convertView, parent);
            DiffFile.DiffSingleFile data = (DiffFile.DiffSingleFile) getItem(position);
            view.setTag(VIEW_TAG_FILE_DATA, data);
            return view;
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
}
