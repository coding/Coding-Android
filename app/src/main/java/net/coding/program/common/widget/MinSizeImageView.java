package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.coding.program.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by chaochen on 15/2/4.
 */
public class MinSizeImageView extends GifMarkImageView {

    protected int[] ATTR = new int[]{
            R.attr.minWidth,
            R.attr.minHeight,
            R.attr.microSize
    };

    int mMinWidth;
    int mMinHeight;
    int mMicroSize;

    public MinSizeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, ATTR);
        mMinWidth = a.getDimensionPixelSize(0, 1);
        mMinHeight = a.getDimensionPixelSize(1, 1);
        mMicroSize = a.getDimensionPixelSize(2, 1);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            // 反射调用 resolveUri
            reflectMethod(ImageView.class, "resolveUri");

            int w;
            int h;

            Drawable mDrawable = getDrawable();

            // Desired aspect ratio of the view's contents (not including padding)
            float desiredAspect = 0.0f;

            // We are allowed to change the view's width
            boolean resizeWidth = false;

            // We are allowed to change the view's height
            boolean resizeHeight = false;

            final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

            if (mDrawable == null) {
                // If no drawable, its intrinsic size is 0.
//                mDrawableWidth = -1;
//                mDrawableHeight = -1;
                reflectIntSet("mDrawableWidth", -1);
                reflectIntSet("mDrawableHeight", -1);

                w = h = 0;
            } else {
                // 反射获取
                w = reflectInt(ImageView.class, "mDrawableWidth");
                h = reflectInt(ImageView.class, "mDrawableHeight");
                if (w <= 0) w = 1;
                if (h <= 0) h = 1;

                // We are supposed to adjust view bounds to match the aspect
                // ratio of our drawable. See if that is possible.
                boolean mAdjustViewBounds = reflectBoolean("mAdjustViewBounds");
                if (mAdjustViewBounds) {
                    resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                    resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

                    desiredAspect = (float) w / (float) h;
                }
            }

            // 反射获取
            int pleft = reflectInt(View.class, "mPaddingLeft");
            int pright = reflectInt(View.class, "mPaddingRight");
            int ptop = reflectInt(View.class, "mPaddingTop");
            int pbottom = reflectInt(View.class, "mPaddingBottom");

            int widthSize;
            int heightSize;

            // 反射获取
            boolean mAdjustViewBoundsCompat = reflectBoolean("mAdjustViewBoundsCompat");

            int mMaxWidth = reflectInt(ImageView.class, "mMaxWidth");
            int mMaxHeight = reflectInt(ImageView.class, "mMaxHeight");

            if (resizeWidth || resizeHeight) {
            /* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension.
            */

                // Get the max possible width given our constraints
                widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec);

                // Get the max possible height given our constraints
                heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec);

                if (desiredAspect != 0.0f) {
                    // See what our actual aspect ratio is
                    float actualAspect = (float) (widthSize - pleft - pright) /
                            (heightSize - ptop - pbottom);

                    if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                        boolean done = false;

                        // Try adjusting width to be proportional to height
                        if (resizeWidth) {
                            int newWidth = (int) (desiredAspect * (heightSize - ptop - pbottom)) +
                                    pleft + pright;

                            // Allow the width to outgrow its original estimate if height is fixed.
                            if (!resizeHeight && !mAdjustViewBoundsCompat) {
                                widthSize = resolveAdjustedSize(newWidth, mMaxWidth, widthMeasureSpec);
                            }

                            if (newWidth <= widthSize) {
                                widthSize = newWidth;
                                done = true;
                            }
                        }

                        // Try adjusting height to be proportional to width
                        if (!done && resizeHeight) {
                            int newHeight = (int) ((widthSize - pleft - pright) / desiredAspect) +
                                    ptop + pbottom;

                            // Allow the height to outgrow its original estimate if width is fixed.
                            if (!resizeWidth && !mAdjustViewBoundsCompat) {
                                heightSize = resolveAdjustedSize(newHeight, mMaxHeight,
                                        heightMeasureSpec);
                            }

                            if (newHeight <= heightSize) {
                                heightSize = newHeight;
                            }
                        }
                    }
                }
            } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
                w += pleft + pright;
                h += ptop + pbottom;

                w = Math.max(w, getSuggestedMinimumWidth());
                h = Math.max(h, getSuggestedMinimumHeight());

                widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
                heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
            }

            if (widthSize < mMinWidth && heightSize < mMinHeight) {
                float ratioWidth = (float) mMinWidth / (float) widthSize;
                float ratioHeight = (float) mMinHeight / (float) heightSize;
                if (ratioWidth < ratioHeight) {
                    widthSize = mMinWidth;
                    heightSize *= ratioWidth;
                } else {
                    heightSize = mMinHeight;
                    widthSize *= ratioHeight;
                }
            }

            if (widthSize < mMicroSize || heightSize < mMicroSize) {
                if (widthSize < mMicroSize) {
                    float ratio = (float) mMicroSize / (float) widthSize;
                    widthSize = mMicroSize;
                    heightSize *= ratio;
                    if (heightSize > mMaxHeight) {
                        heightSize = mMaxHeight;
                    }
                } else {
                    float ratio = (float) mMicroSize / (float) heightSize;
                    heightSize = mMicroSize;
                    widthSize *= ratio;
                    if (widthSize > mMaxWidth) {
                        widthSize = mMaxWidth;
                    }
                }
            }

            setMeasuredDimension(widthSize, heightSize);
        } catch (Exception e) {
            Log.d("", "hh " + e);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void reflectMethod(Class<?> cls, String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = cls.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(this);
    }

    private int reflectInt(Class cls, String name) throws Exception {
        Field intField = cls.getDeclaredField(name);
        intField.setAccessible(true);
        return intField.getInt(this);
    }

    private void reflectIntSet(String name, int value) throws Exception {
        Class<?> cls = ImageView.class;
        Field intField = cls.getDeclaredField(name);
        intField.setAccessible(true);
        intField.setInt(this, value);
    }

    private boolean reflectBoolean(String name) throws Exception {
        Class<?> cls = ImageView.class;
        Field booleanField = cls.getDeclaredField(name);
        booleanField.setAccessible(true);
        return booleanField.getBoolean(this);
    }


    private int resolveAdjustedSize(int desiredSize, int maxSize,
                                    int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }
}
