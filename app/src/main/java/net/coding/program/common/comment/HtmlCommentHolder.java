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
import net.coding.program.model.BaseComment;
import net.coding.program.model.Commit;

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

    public HtmlCommentHolder(View convertView, BaseCommentParam param, TextView content) {
        super(convertView, param);
        this.content = content;
    }

    public void setContent(Object data) {
        String contentString = "";
        if (data instanceof BaseComment) {
            BaseComment comment = (BaseComment) data;
            super.setContent(comment);

            contentString = comment.content;
        } else if (data instanceof Commit) {
            super.setContent(data);

            Commit commit = (Commit) data;
            contentString = commit.getTitle();
        }

        Global.MessageParse parse = HtmlContent.parseMessage(contentString);
        content.setText(Global.changeHyperlinkColor(parse.text, imageGetter, Global.tagHandler));
        content.setTag(data);
    }

}
