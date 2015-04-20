package net.coding.program.maopao.item;

import android.text.Html;
import android.view.View;

import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.comment.BaseCommentHolder;
import net.coding.program.model.BaseComment;

/**
 * Created by chenchao on 15/3/31.
 * 可以带多张小图片的评论item
 */
public class ImageCommentHolder extends BaseCommentHolder {

    private ContentAreaMuchImages contentArea;

    public ImageCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser);
        contentArea = new ContentAreaMuchImages(convertView, onClickComment, clickImage, imageGetter, imageLoadTool, Global.dpToPx(32)); //
    }

    public void setTaskCommentContent(BaseComment comment) {
        super.setContent(comment);
        contentArea.setData(comment);
    }
}
