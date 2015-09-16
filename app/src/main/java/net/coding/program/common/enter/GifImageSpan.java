package net.coding.program.common.enter;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * Created by chenchao on 15/9/15.
 */
public class GifImageSpan extends ImageSpan {

    private Drawable mDrawable = null;

    public GifImageSpan(Drawable d) {
        super(d);
        mDrawable = d;
    }

    public GifImageSpan(Drawable d, int verticalAlignment) {
        super(d, verticalAlignment);
        mDrawable = d;
    }

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }
}
