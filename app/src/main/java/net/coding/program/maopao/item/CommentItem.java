package net.coding.program.maopao.item;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.common.maopao.MaopaoRequestTag;
import net.coding.program.common.model.Maopao;
import net.coding.program.common.param.MessageParse;
import net.coding.program.maopao.MaopaoListBaseFragment;

/**
 * Created by chaochen on 15/1/14.
 */
class CommentItem {

    private TextView comment;
    private TextView name;
    private TextView time;
    private View layout;

    public CommentItem(View convertView, View.OnClickListener onClickComment, int i) {
        layout = convertView;
        layout.setOnClickListener(onClickComment);
        name = (TextView) convertView.findViewById(R.id.name);
        time = (TextView) convertView.findViewById(R.id.time);
        comment = (TextView) convertView.findViewById(R.id.comment);
        comment.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        comment.setOnClickListener(onClickComment);
        comment.setOnLongClickListener(DialogCopy.getInstance());
    }

    public void setContent(Maopao.Comment commentData, Html.ImageGetter imageGetter, Html.TagHandler tagHandler) {
        layout.setTag(MaopaoRequestTag.TAG_COMMENT, commentData);
        comment.setTag(MaopaoRequestTag.TAG_COMMENT, commentData);
        comment.setTag(MaopaoListBaseFragment.TAG_COMMENT_TEXT, commentData.content);

        name.setText(commentData.owner.name);
        time.setText(Global.dayToNow(commentData.created_at));
        MessageParse parse = HtmlContent.parseMessage(commentData.content);
        comment.setText(GlobalCommon.changeHyperlinkColor(parse.text, imageGetter, tagHandler));
    }

    public void setVisibility(int visibility) {
        layout.setVisibility(visibility);
    }
}
