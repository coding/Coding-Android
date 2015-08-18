package net.coding.program.task.add;

import android.text.Html;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.maopao.item.ImageCommentHolder;

/**
 * Created by chenchao on 15/7/8.
 * 任务编辑列表的评论，是特殊处理的
 */
public class CommentHolder extends ImageCommentHolder {
    View timeLineUp;
    View timeLineDown;

    public CommentHolder(View convertView, View.OnClickListener onClickComment, Html.ImageGetter imageGetter, ImageLoadTool imageLoadTool, View.OnClickListener clickUser, View.OnClickListener clickImage) {
        super(convertView, onClickComment, imageGetter, imageLoadTool, clickUser, clickImage);

        timeLineUp = convertView.findViewById(R.id.timeLineUp);
        timeLineDown = convertView.findViewById(R.id.timeLineDown);
    }

    public void updateLine(int position, int count) {
        switch (count) {
            case 1:
                setLine(false, false);
                break;

            default:
                if (position == 0) {
                    setLine(false, true);
                } else if (position == count - 1) {
                    setLine(true, false);
                } else {
                    setLine(true, true);
                }
                break;
        }
    }

    private void setLine(boolean up, boolean down) {
        timeLineUp.setVisibility(up ? View.VISIBLE : View.INVISIBLE);
        timeLineDown.setVisibility(down ? View.VISIBLE : View.INVISIBLE);
    }
}
