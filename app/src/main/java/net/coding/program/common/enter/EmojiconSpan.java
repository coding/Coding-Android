package net.coding.program.common.enter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import net.coding.program.R;
import net.coding.program.message.EmojiFragment;

import java.lang.reflect.Field;

/**
 * Created by chaochen on 14-11-12.
 */
class EmojiconSpan extends DynamicDrawableSpan {
    private final Context mContext;
    private final int mResourceId;
    private Drawable mDrawable;
    private boolean mIsMonkey;

    public EmojiconSpan(Context context, String iconName) {
        super();
        mContext = context;

        String name = EmojiFragment.textToMonkdyMap.get(iconName);
        if (name == null) {
            name = iconName;
            mIsMonkey = false;
        } else {
            mIsMonkey = true;
        }

        int id = R.drawable.ic_launcher;
        try {
            Field field = R.drawable.class.getField(name);
            id = Integer.parseInt(field.get(null).toString());
        } catch (Exception e) {
        }

        mResourceId = id;
    }

    @Override
    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = mContext.getResources().getDrawable(mResourceId);
                DrawableTool.zoomDrwable(mDrawable, mIsMonkey);
            } catch (Exception e) {
            }
        }
        return mDrawable;
    }
}