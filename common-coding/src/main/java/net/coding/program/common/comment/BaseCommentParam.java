package net.coding.program.common.comment;

import android.text.Html;
import android.view.View;

import net.coding.program.common.ImageLoadTool;

/**
 * Created by chenchao on 15/6/2.
 * 为了简化参数，不用每次传一堆
 */
public class BaseCommentParam {

    public View.OnClickListener onClickComment;
    public Html.ImageGetter imageGetter;
    public ImageLoadTool imageLoadTool;
    public View.OnClickListener clickUser;
    public View.OnClickListener mClickImage;

    public BaseCommentParam(View.OnClickListener clickImage, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        this.mClickImage = clickImage;
        this.onClickComment = onClickComment;
        this.imageGetter = imageGetter;
        this.imageLoadTool = imageLoadTool;
        this.clickUser = clickUser;
    }
}
