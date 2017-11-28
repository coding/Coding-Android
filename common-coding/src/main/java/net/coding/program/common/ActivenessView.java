package net.coding.program.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.model.user.ActiveModel;

import static net.coding.program.common.util.DensityUtil.dip2px;


/**
 * 活动图
 * Created by anfs on 2016/11/28.
 */
public class ActivenessView extends View {

    private final int COUNT_LINE = 7;
    private final int WIDTH = 15;
    ActiveModel activeModel;
    //高度宽度相关
    private int CHUNK_WIDTH;
    private int CHUNK_HEIGHT;
    private int CHUNK_GREY;
    private int LINE_HEIGHT;
    private int countX;
    //颜色
    private int LINE_WHITE;
    private int CHUNK_GREEN1;
    private int CHUNK_GREEN2;
    private int CHUNK_GREEN3;
    private int CHUNK_GREEN4;
    private Context mContext;
    private Paint paint;

    public ActivenessView(Context context) {
        this(context, null);
    }

    public ActivenessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivenessView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        paint = new Paint();

        CHUNK_HEIGHT = CHUNK_WIDTH = dip2px(context, WIDTH);
        LINE_HEIGHT = dip2px(context, 1);

        LINE_WHITE = context.getResources().getColor(R.color.chunkWhite);
        CHUNK_GREY = context.getResources().getColor(R.color.chunkGrey);
        CHUNK_GREEN1 = context.getResources().getColor(R.color.chunkGreen1);
        CHUNK_GREEN2 = context.getResources().getColor(R.color.chunkGreen2);
        CHUNK_GREEN3 = context.getResources().getColor(R.color.chunkGreen3);
        CHUNK_GREEN4 = context.getResources().getColor(R.color.chunkGreen4);
    }

    public void setActiveModel(ActiveModel activeModel) {
        this.activeModel = activeModel;
        int size = activeModel.daily_activeness.size();
        countX = size / 7;
        if (size % 7 != 0) {
            countX = countX + 1;
        }
        invalidate();
    }

    public int getTrendWidth() {
        return countX * dip2px(mContext, WIDTH);
    }

    public int getTrendHeight() {
        return COUNT_LINE * dip2px(mContext, WIDTH);
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (activeModel == null) {
            return;
        }

        //背景白色的
        canvas.drawColor(LINE_WHITE);
        canvas.save();
        canvas.restore();

        int line = LINE_HEIGHT / 2;

        int size = activeModel.daily_activeness.size();
        //防止数组越界
        int index = 0;

        //绘制每个方格
        for (int x = 0; x < countX; x++) {
            for (int y = 0; y < COUNT_LINE; y++) {
                if (index == size) {
                    continue;
                }

                Rect rect = new Rect(
                        x * CHUNK_WIDTH + line,
                        y * CHUNK_HEIGHT + line,
                        x * CHUNK_WIDTH + CHUNK_WIDTH - line,
                        y * CHUNK_WIDTH + CHUNK_HEIGHT - line);

                long percentage = activeModel.daily_activeness.get(index).count;
                if (percentage == 0) {
                    paint.setColor(CHUNK_GREY);
                } else if (1 <= percentage && percentage <= 24) {
                    paint.setColor(CHUNK_GREEN1);
                } else if (25 <= percentage && percentage <= 49) {
                    paint.setColor(CHUNK_GREEN2);
                } else if (50 <= percentage && percentage <= 74) {
                    paint.setColor(CHUNK_GREEN3);
                } else if (75 <= percentage) {
                    paint.setColor(CHUNK_GREEN4);
                } else {
                    paint.setColor(CHUNK_GREY);
                }
                canvas.drawRect(rect, paint);

                index++;
            }
        }

        canvas.save();
    }
}
