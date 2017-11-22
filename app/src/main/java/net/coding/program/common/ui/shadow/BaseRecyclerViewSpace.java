package net.coding.program.common.ui.shadow;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;

import net.coding.program.R;
import net.coding.program.common.GlobalCommon;


/**
 * Created by chenchao on 16/4/20.
 */
public class BaseRecyclerViewSpace extends RecyclerView.ItemDecoration {

    public final int DIVIDE_COLOR;
    public final int DIVIDE_LINE_COLOR;
    public final int ITEM_BG;

    protected final int topSpace;
    protected final int lineSpace;
    protected final int bottomShadowSpace;
    protected final int shadowHigh;
    protected int bottomSpace;
    protected Paint paintDivideLine;
    protected Drawable shadowTop;
    protected Drawable shadowBottom;
    protected Paint paintDivide;
    protected Paint paintBg;

    public BaseRecyclerViewSpace(Context context) {
        Resources resources = context.getResources();
        topSpace = resources.getDimensionPixelSize(R.dimen.list_header_space);
        lineSpace = resources.getDimensionPixelSize(R.dimen.list_divide_line_height);
        bottomSpace = resources.getDimensionPixelSize(R.dimen.list_footer_space);
        bottomShadowSpace = resources.getDimensionPixelSize(R.dimen.list_footer_shadow_space);

        DIVIDE_COLOR = resources.getColor(R.color.divide);
        DIVIDE_LINE_COLOR = resources.getColor(R.color.divide_line);
        ITEM_BG = resources.getColor(R.color.stand_bg);

        paintDivideLine = new Paint();
        paintDivideLine.setColor(DIVIDE_LINE_COLOR);

        paintDivide = new Paint();
        paintDivide.setColor(DIVIDE_COLOR);
        paintBg = new Paint();
        paintBg.setColor(ITEM_BG);

        shadowTop = resources.getDrawable(R.mipmap.shadow_top);
        shadowBottom = resources.getDrawable(R.mipmap.shadow_bottom);
        shadowHigh = GlobalCommon.dpToPx(5);
    }

    public void setBottomSpace(int dp) {
        bottomSpace = GlobalCommon.dpToPx(dp);

    }
}
