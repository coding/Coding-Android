package net.coding.program.maopao.item;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.common.model.Commit;
import net.coding.program.common.model.SingleTask;
import net.coding.program.common.param.MessageParse;
import net.coding.program.common.util.PhoneUtil;
import net.coding.program.maopao.MaopaoListBaseFragment;

/**
 * Created by chaochen on 14/12/22.
 */
public class ContentAreaBase {

    protected TextView content;
    protected Html.ImageGetter imageGetter;

    public ContentAreaBase(View convertView, View.OnClickListener onClickContent, Html.ImageGetter imageGetterParamer) {
        content = (TextView) convertView.findViewById(R.id.content);
        if (PhoneUtil.isFlyme()) {
            content.setMovementMethod(null);
        } else {
            content.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        }
        content.setOnClickListener(onClickContent);
        content.setOnLongClickListener(DialogCopy.getInstance());

        imageGetter = imageGetterParamer;
    }

    public void clearConentLongClick() {
        content.setOnLongClickListener(null);
    }

    public void setData(Object data) {
        String contentString = "";
        if (data instanceof SingleTask.TaskComment) {
            SingleTask.TaskComment comment = (SingleTask.TaskComment) data;
            contentString = comment.content;
        } else if (data instanceof Commit) {
            Commit commit = (Commit) data;
            contentString = commit.getTitle();
        }

        MessageParse maopaoData = HtmlContent.parseReplacePhoto(contentString);
        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setTag(MaopaoListBaseFragment.TAG_COMMENT_TEXT, maopaoData.text);
            content.setTag(data);
            content.setVisibility(View.VISIBLE);
            content.setText(GlobalCommon.changeHyperlinkColor(maopaoData.text, imageGetter, Global.tagHandler));
        }
    }
}
