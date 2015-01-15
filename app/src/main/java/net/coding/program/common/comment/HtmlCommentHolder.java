package net.coding.program.common.comment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.maopao.ContentArea;
import net.coding.program.model.BaseComment;

/**
 * Created by chaochen on 14-10-27.
 */
public class HtmlCommentHolder extends BaseCommentHolder {

    protected TextView content;

    public HtmlCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser);

        content = (TextView) convertView.findViewById(R.id.content);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.performClick();
            }
        });

        content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setItems(R.array.message_action_text_copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Global.copy(((TextView) v).getText().toString(), v.getContext());
                            Toast.makeText(v.getContext(), "已复制", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                AlertDialog dialog = builder.show();
                CustomDialog.dialogTitleLineColor(v.getContext(), dialog);
                return true;
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
