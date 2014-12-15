package net.coding.program.common.comment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.BaseComment;

/**
 * Created by chaochen on 14-10-27.
 */
public class HtmlCommentHolder extends BaseCommentHolder {

    protected TextView content;

    public HtmlCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser);

        content = (TextView) convertView.findViewById(R.id.comment);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((View) v.getParent()).callOnClick();
            }
        });
    }

    public void setContent(BaseComment comment) {
        super.setContent(comment);

        String contentString = comment.content;
        Global.MessageParse parse = HtmlContent.parseMessage(contentString);
        content.setText(Global.changeHyperlinkColor(parse.text, imageGetter, Global.tagHandler));
        content.setTag(comment);
    }
}
