package net.coding.program.task.add;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 16/1/5.
 * 添加任务的 item
 */
public class TaskAttrItem extends FrameLayout {

    private final TextView mTextView2;
    private final ImageView mIcon;
    private final TextView mTextView1;

    public TaskAttrItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.task_attr_item, this);

        mTextView1 = (TextView) findViewById(R.id.text1);
        mTextView2 = (TextView) findViewById(R.id.text2);

        View topLine = findViewById(R.id.topLine);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TaskAttrItem);

        boolean showUser = array.getBoolean(R.styleable.TaskAttrItem_taskIconUser, false);
        ImageView user = (ImageView) findViewById(R.id.userIcon);
        ImageView normal = (ImageView) findViewById(R.id.circleIcon);
        if (showUser) {
            normal.setVisibility(GONE);
            mIcon = user;
        } else {
            user.setVisibility(GONE);
            mIcon = normal;
        }

        String text1 = array.getString(R.styleable.TaskAttrItem_taskText1);
        if (text1 == null) {
            text1 = "";
        }
        mTextView1.setText(text1);

        String text2 = array.getString(R.styleable.TaskAttrItem_taskText2);
        if (text2 == null) {
            text2 = "";
        }
        mTextView2.setText(text2);

        int imageResId = array.getResourceId(R.styleable.TaskAttrItem_taskIcon, R.drawable.icon_user_monkey);
        mIcon.setImageResource(imageResId);

        boolean showTopLine = array.getBoolean(R.styleable.TaskAttrItem_taskTopLine, true);
        topLine.setVisibility(showTopLine ? VISIBLE : GONE);

        array.recycle();
    }

    public void setText2(String s) {
        mTextView2.setText(s);
    }

    public void setText2(int stringId) {
        mTextView2.setText(stringId);
    }

    public ImageView getImage() {
        return mIcon;
    }

}
