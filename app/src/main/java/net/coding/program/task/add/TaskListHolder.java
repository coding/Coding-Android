package net.coding.program.task.add;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/8.
 * 任务编辑历史列表的 item
 */
public class TaskListHolder {
    public ImageView mIcon;
    public TextView mContent;
    private View timeLineUp;
    private View timeLineDown;

    public TaskListHolder(View convertView) {
        mIcon = (ImageView) convertView.findViewById(R.id.icon);
        mContent = (TextView) convertView.findViewById(R.id.content);
        timeLineUp = convertView.findViewById(R.id.timeLineUp);
        timeLineDown = convertView.findViewById(R.id.timeLineDown);
        convertView.setTag(getTagId(), this);
    }

    public static int getTagId() {
        return R.id.layout;
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
