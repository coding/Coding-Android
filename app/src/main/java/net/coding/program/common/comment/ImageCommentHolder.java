package net.coding.program.common.comment;

import android.text.Html;
import android.view.View;

import net.coding.program.common.ImageLoadTool;
import net.coding.program.maopao.item.ContentArea;
import net.coding.program.model.BaseComment;
import net.coding.program.model.TaskObject;

/**
 * Created by chaochen on 14-10-27.
 */
public class ImageCommentHolder extends BaseCommentHolder {

    private ContentArea contentArea;

    public ImageCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser);

        contentArea = new ContentArea(convertView, onClickComment, clickImage, imageGetter, imageLoadTool);
    }

    public void setContent(BaseComment comment) {
        super.setContent(comment);

        contentArea.setData(comment.content, ContentArea.Type.Maopao);
    }

    public void setTaskCommentContent(TaskObject.TaskComment comment) {
        super.setContent(comment);
        contentArea.setData(comment);
    }
}
