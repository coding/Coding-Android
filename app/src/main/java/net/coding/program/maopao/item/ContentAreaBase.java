package net.coding.program.maopao.item;

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
import net.coding.program.message.MessageListActivity;
import net.coding.program.model.TaskObject;

/**
 * Created by chaochen on 14/12/22.
 */
public class ContentAreaBase {

    protected TextView content;
    protected Html.ImageGetter imageGetter;

    public ContentAreaBase(View convertView, View.OnClickListener onClickContent, Html.ImageGetter imageGetterParamer) {
        content = (TextView) convertView.findViewById(R.id.content);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        content.setOnClickListener(onClickContent);
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

        imageGetter = imageGetterParamer;
    }

    public void setData(TaskObject.TaskComment comment) {
        String data = comment.content;
        Global.MessageParse maopaoData = HtmlContent.parseReplacePhoto(data);

        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setTag(comment);
            content.setVisibility(View.VISIBLE);
            content.setText(Global.changeHyperlinkColor(maopaoData.text, imageGetter, Global.tagHandler));
        }
    }
}
