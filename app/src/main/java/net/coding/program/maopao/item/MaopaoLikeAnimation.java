package net.coding.program.maopao.item;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by chenchao on 15/9/14.
 * 冒泡点赞动画
 */
public class MaopaoLikeAnimation {

    public static void playAnimation(final View viewSelf, View startView) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(2000);
        float pivotX = (startView.getLeft() + startView.getRight()) / 2 -  viewSelf.getWidth() / 2;
        float pivotY = (startView.getTop() + startView.getBottom()) / 2 - viewSelf.getHeight() / 2;

        valueAnimator.setObjectValues(
                new PointScal(pivotX, pivotY, 0),
                new PointScal(pivotX, pivotY, 0)
        );

        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setEvaluator(new TypeEvaluator<PointScal>() {
            @Override
            public PointScal evaluate(float fraction, PointScal start, PointScal endValue) {

                PointF pointStart = start.mPoint;
                PointF point = new PointF();
                point.x = 33 * (fraction * 3) * (fraction * 3) + pointStart.x;
                point.y = - 200 * (fraction * 3) + pointStart.y;
                return new PointScal(point, fraction);
            }
        });

        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                PointScal pointScal = (PointScal) animation.getAnimatedValue();

                PointF point = pointScal.mPoint;
                viewSelf.setX(point.x);
                viewSelf.setY(point.y);

                float value = pointScal.mFraction;
                float alpha = 1 - value * 2f;
                if (alpha < 0) {
                    alpha = 0;
                }

                viewSelf.setAlpha(alpha);
                viewSelf.setRotation(value * 60);
                float scal = value * 2 + 1;
                viewSelf.setScaleX(scal);
                viewSelf.setScaleY(scal);
            }
        });
    }

    private static class PointScal {
        public PointF mPoint;
        public Float mFraction;

        public PointScal(PointF mPoint, float mFraction) {
            this.mPoint = mPoint;
            this.mFraction = mFraction;
        }

        public PointScal(float x, float y, float frac) {
            this(new PointF(x, y), frac);
        }
    }
}
