package net.coding.program.common.widget;

import net.coding.program.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by libo on 2015/12/3.
 */
public class DashedLine extends View {

    private Context context;

    public DashedLine(Context context) {
        super(context);
        this.context = context;
    }

    public DashedLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.divide_line));
        paint.setStrokeWidth(1);
        Path path = new Path();
        PathEffect effects = new DashPathEffect(new float[]{3, 3, 3, 3}, 1);
        paint.setPathEffect(effects);
//        canvas.drawPath(path, paint);
        canvas.drawLine(0, 0, 1080, 0, paint);
    }
}
