package net.coding.program.common.comment;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.DialogCopy;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.model.BaseComment;

/**
 * Created by chaochen on 14-10-27.
 */
public class HtmlCommentHolder extends BaseCommentHolder {

    protected TextView content;

    public HtmlCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser);

        content = (TextView) convertView.findViewById(R.id.content);
        content.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.performClick();
            }
        });

        content.setOnLongClickListener(DialogCopy.getInstance());
    }

    public void setContent(BaseComment comment) {
        super.setContent(comment);

        String contentString = comment.content;
        Global.MessageParse parse = HtmlContent.parseMessage(contentString);
        content.setText(Global.changeHyperlinkColor(parse.text, imageGetter, Global.tagHandler));
        content.setTag(comment);
        content.setTag(MaopaoListBaseFragment.TAG_COMMENT_TEXT, parse.text);
    }

}
