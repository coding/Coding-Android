package net.coding.program.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/10.
 * 可以在显示 GIF 标示的 ImageView
 */
public class GifMarkImageView extends ImageView {

    private boolean showFlag;
    private int flagWidth = 20;
    private int flagHeigh = 10;
    private int flagMerge = 3;

    public GifMarkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float scal = context.getResources().getDisplayMetrics().density;
        flagWidth = (int) (scal * flagWidth);
        flagHeigh = (int) (scal * flagHeigh);
        flagMerge = (int) (scal * flagMerge);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showFlag) {
            int cavasWidth = canvas.getWidth();
            int cavasHeight = canvas.getHeight();
            Rect lineRect = new Rect(0, 0, 50, 50);
            lineRect.left = cavasWidth - flagWidth - flagMerge;
            lineRect.right = cavasWidth - flagMerge;
            lineRect.top = cavasHeight - flagHeigh - flagMerge;
            lineRect.bottom = cavasHeight - flagMerge;
            canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.ic_flag_gif))).getBitmap(), null, lineRect, null);
        }
    }

    public void showGifFlag(boolean show) {
        if (show == showFlag) {
            return;
        }

        showFlag = show;
        invalidate();
    }

    public void showGifFlag(String url) {
        showGifFlag(url.toLowerCase().endsWith(".gif"));
    }
}
