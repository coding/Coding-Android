package net.coding.program.common.comment;

import android.text.Html;
import android.view.View;

import net.coding.program.common.ImageLoadTool;

/**
 * Created by chenchao on 15/6/2.
 */
public class BaseCommentParam {

    View.OnClickListener onClickComment;
    Html.ImageGetter imageGetter;
    ImageLoadTool imageLoadTool;
    View.OnClickListener clickUser;

    public BaseCommentParam(View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        this.onClickComment = onClickComment;
        this.imageGetter = imageGetter;
        this.imageLoadTool = imageLoadTool;
        this.clickUser = clickUser;
    }
}
