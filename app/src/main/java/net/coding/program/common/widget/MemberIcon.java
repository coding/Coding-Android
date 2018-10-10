package net.coding.program.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import net.coding.program.common.GlobalCommon;
import net.coding.program.common.model.UserObject;
import net.coding.program.network.constant.VIP;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chenchao on 2017/5/31.
 * 会员头像，可以显示会员表示，比方说钻石会员就会显示钻石
 */

public class MemberIcon extends CircleImageView {

    private Rect bounds = new Rect();
    //    private Size diamondSize;
//    private Size GoldSize;
    private Drawable flagIcon = null;

    public MemberIcon(Context context) {
        super(context);
    }

    public MemberIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        setFlagIcon(tag);
    }

    public void setFlagIcon(Object tag) {
        if (tag instanceof UserObject) {
            VIP vip = ((UserObject) tag).vip;
            if (vip == null || vip.getIcon() == 0) {
                flagIcon = null;
            } else {
                flagIcon = getResources().getDrawable(vip.getIcon());
            }
        }
    }

    @Override
    public void setTag(int key, Object tag) {
        super.setTag(key, tag);
        setFlagIcon(tag);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (flagIcon != null) {
            mergeRect();
            flagIcon.setBounds(bounds);
            flagIcon.draw(canvas);
        }
    }

    private void mergeRect() {
        if (bounds.right > 0 && bounds.bottom > 0) {
            return;
        }

        int widthPx = getWidth();
        int heightPx = getHeight();

        if (widthPx > 0 && heightPx > 0) {
            int widthDp = GlobalCommon.pxToDp(widthPx);

            int iconWidthDp = 0;
            if (widthDp >= 73) {
                iconWidthDp = 28;
            } else if (widthDp >= 48) {
                iconWidthDp = 18;
            } else if (widthDp >= 43) {
                iconWidthDp = 16;
            } else if (widthDp >= 38) {
                iconWidthDp = 14;
            } else if (widthDp >= 31) {
                iconWidthDp = 12;
            } else if (widthDp >= 28) {
                iconWidthDp = 10;
            }

            int iconWidthPx = GlobalCommon.dpToPx(iconWidthDp);
            bounds.right = widthPx + GlobalCommon.dpToPx(0);
            bounds.bottom = heightPx + GlobalCommon.dpToPx(0);
            bounds.left = bounds.right - iconWidthPx;
            bounds.top = bounds.bottom - iconWidthPx;
        }
    }
}
