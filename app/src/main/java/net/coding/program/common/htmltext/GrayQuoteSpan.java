package net.coding.program.common.htmltext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;
import android.text.style.UnderlineSpan;

import net.coding.program.common.CodingColor;

/**
 * Created by chaochen on 15/1/12.
 */
public class GrayQuoteSpan extends UnderlineSpan implements LeadingMarginSpan {
    private static final int STRIPE_WIDTH = 4 * 3;
    private static final int GAP_WIDTH = 12 * 3;

    private final int mColor;

    public GrayQuoteSpan() {
        super();
        mColor = CodingColor.divideLine;
    }

    public GrayQuoteSpan(int color) {
        super();
        mColor = color;
    }

    public GrayQuoteSpan(Parcel src) {
        super(src);
        mColor = src.readInt();
    }

    public int getSpanTypeId() {
        // 看系统源代码是这么多
        return 9;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

    public int getColor() {
        return mColor;
    }

    public int getLeadingMargin(boolean first) {
        return STRIPE_WIDTH + GAP_WIDTH;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        c.drawRect(x, top, x + dir * STRIPE_WIDTH, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
