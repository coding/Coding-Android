package net.coding.program.common.comment;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.BaseComment;

/**
 * Created by chaochen on 14-10-27.
 */
public class BaseCommentHolder {

    protected ImageView icon;
    protected TextView name;
    protected TextView time;
    protected View layout;
    protected Html.ImageGetter imageGetter;

    protected ImageLoadTool imageLoadTool;

    public BaseCommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser) {
        layout = convertView.findViewById(R.id.Commentlayout);
        layout.setOnClickListener(onClickComment);

        icon = (ImageView) convertView.findViewById(R.id.icon);
        icon.setOnClickListener(clickUser);
        name = (TextView) convertView.findViewById(R.id.name);
        time = (TextView) convertView.findViewById(R.id.time);

        this.imageLoadTool = imageLoadTool;
        this.imageGetter = imageGetter;
    }

    public void setContent(BaseComment comment) {
        String nameString = comment.owner.name;
        long timeParam = comment.created_at;
        String iconUri = comment.owner.avatar;

        imageLoadTool.loadImage(icon, iconUri);
        icon.setTag(comment.owner.global_key);
        name.setText(nameString);
        time.setText(Global.dayToNow(timeParam));
        layout.setTag(comment);
    }
}
