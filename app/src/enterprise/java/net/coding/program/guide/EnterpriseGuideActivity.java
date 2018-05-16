package net.coding.program.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.fivehundredpx.android.blur.BlurringView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.user.EnterpriseLoginActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayDeque;
import java.util.Queue;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


@EActivity(R.layout.activity_enterprise_guide)
public class EnterpriseGuideActivity extends AppCompatActivity {

    private GestureDetectorCompat gesture;

    @ViewById
    TextView gifTitle, gifContent;

    @ViewById
    View blurrSourceView;

    @ViewById
    BlurringView blurringView;

    @ViewById
    GifImageView gifView;

    @ViewById
    View pager0, pager1;

    @ViewById
    View ball0, ball1, ball2;

    @ViewById
    IndicatorView pageIndicate;

    @StringArrayRes(R.array.guide_content)
    String[] guideContent;

    @StringArrayRes(R.array.guide_title)
    String[] guideTitle;

    @StringArrayRes(R.array.guide_gif_down)
    String[] gifDown;

    @StringArrayRes(R.array.guide_gif_up)
    String[] gifUp;

    public static final int PAGER_COUNT = 5;

    private int indicatePos = 0;

    Queue<String> gifQueue = new ArrayDeque<>();

    ObjectAnimator titleAnimatorOut;
    ObjectAnimator titleAnimatorIn;
    float titleAlpha = 1;

    ObjectAnimator contentAnimatorOut;
    ObjectAnimator contentAnimatorIn;

    int textInTime = 500;
    int textOutTime = 300;

    boolean isGifPlaying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(EventLoginSuccess event) {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @AfterViews
    void initEnterpriseGuideActivity() {
        blurringView.setBlurredView(blurrSourceView);

        gesture = new GestureDetectorCompat(this, gestureListener);

        titleAnimatorOut = ObjectAnimator.ofFloat(gifTitle, "alpha", titleAlpha, 0);
        titleAnimatorOut.setDuration(textOutTime);
        titleAnimatorOut.setInterpolator(new AccelerateInterpolator());
        titleAnimatorIn = ObjectAnimator.ofFloat(gifTitle, "alpha", 0, 1);
        titleAnimatorIn.setDuration(textInTime);
        titleAnimatorIn.setInterpolator(new AccelerateInterpolator());

        contentAnimatorOut = ObjectAnimator.ofFloat(gifContent, "alpha", titleAlpha, 0);
        contentAnimatorOut.setDuration(textOutTime);
        contentAnimatorOut.setInterpolator(new AccelerateInterpolator());
        contentAnimatorIn = ObjectAnimator.ofFloat(gifContent, "alpha", 0, 1);
        contentAnimatorIn.setDuration(textInTime);
        contentAnimatorIn.setInterpolator(new AccelerateInterpolator());

        blurringView.invalidate();

        int width = GlobalData.sWidthPix;
        setPoint(ball0, width);
        setPoint(ball1, width);
        setPoint(ball2, width);

        int halfWidth = width / 4;

        ObjectAnimator ballAnimator = ObjectAnimator.ofFloat(ball0, "translationX", 0, halfWidth, 0, -halfWidth, 0).setDuration(20 * 1000);
        ballAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        ballAnimator.addUpdateListener(animation -> blurringView.invalidate());
        ballAnimator.start();

        ObjectAnimator ballAnimator1 = ObjectAnimator.ofFloat(ball1, "translationX", 0, halfWidth, 0, -halfWidth, 0).setDuration(15 * 1000);
        ballAnimator1.setRepeatCount(ValueAnimator.INFINITE);
        ballAnimator1.start();

        ObjectAnimator ballAnimator2 = ObjectAnimator.ofFloat(ball2, "translationX", 0, -halfWidth, 0, halfWidth, 0).setDuration(20 * 1000);
        ballAnimator2.setRepeatCount(ValueAnimator.INFINITE);
        ballAnimator2.start();

        pageIndicate.setPointStyle(true);
        pageIndicate.setSelect(0);
    }

    private void setPoint(View v, int size) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        lp.width = size;
        lp.height = size;
        v.setLayoutParams(lp);
    }

    private void setGifTitle(final String s) {
        titleAnimatorOut.start();
        titleAnimatorOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                gifTitle.setText(s);
                titleAnimatorIn.start();
            }
        });
    }

    private void setGifContent(final String content) {
        contentAnimatorOut.start();
        contentAnimatorOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                gifContent.setText(content);
                contentAnimatorIn.start();
            }
        });
    }

    @Click
    void sendButton() {
        EnterpriseLoginActivity_.intent(this).start();
    }

    @Click
    void sendButtonPrivate() {
        EnterpriseLoginActivity_.intent(this).isPrivate(true).start();
    }

    private void playGif() {
        if (isGifPlaying) {
            return;
        }

        playGifReal();
    }

    private void playGifReal() {
        try {
            if (!gifQueue.isEmpty()) {
                isGifPlaying = true;

                String name = gifQueue.poll();

                GifDrawable drawable = new GifDrawable(getAssets(), name);

                gifView.setImageDrawable(drawable);
                drawable.start();

                drawable.addAnimationListener(playGifListener);
            } else {
                isGifPlaying = false;
            }
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    AnimationListener playGifListener = loopNumber -> {
        if (gifQueue.isEmpty()) {
            isGifPlaying = false;
        } else {
            playGifReal();
        }
    };

    private void showPager0(boolean show) {
        View showView = pager0;
        View hideView = pager1;
        if (!show) {
            showView = pager1;
            hideView = pager0;
        }

        ObjectAnimator.ofFloat(showView, "alpha", 0f, 1f).setDuration(800).start();
        ObjectAnimator.ofFloat(hideView, "alpha", 1f, 0f).setDuration(800).start();

        pageIndicate.setPointStyle(show);
    }

    // 每次添加 2 个动画，第一个是退出动画，第二个是出现动画 因为添加动画到队列后，会立刻取出一个动画开始播放，所以队列中元素的数量只可能为 1 或 0（也可能出现更多，但手速达不到那么快）
    // 为 1 说明还在播放上上个退出动画，所以上个出现动画可以删除了，这次也不用传入退出动画
    // 为 0 上个出现动画正在播放或已经播放完毕，所以要传入退出动画和出现动画
    private void addAnimate(int pos) {
        if (gifQueue.isEmpty()) {
            String down = gifDown[indicatePos];
            if (!TextUtils.isEmpty(down)) {
                gifQueue.add(down);
            }
        } else {
            gifQueue.clear();
        }

        String up = gifUp[pos];
        if (!TextUtils.isEmpty(up)) {
            gifQueue.add(up);
        }

        playGif();
    }

    private void scrollPager(int pos) {
        if (pos < 0 || PAGER_COUNT <= pos) {
            return;
        }

        if (pos == indicatePos) {
            return;
        }

        if (pos == 0) {
            showPager0(true);
        } else if (indicatePos == 0) {
            showPager0(false);
        }

        pageIndicate.setSelect(pos);

        setGifTitle(guideTitle[pos]);
        setGifContent(guideContent[pos]);
        addAnimate(pos);

        indicatePos = pos;
    }


    GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        int scrollSum = 0;
        boolean scrolling = false;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (scrolling) {
                scrollSum += distanceX;
                if (scrollSum > 100) {
                    if (indicatePos < (PAGER_COUNT - 1)) {
                        scrollPager(indicatePos + 1);
                        scrolling = false;
                    }
                } else if (scrollSum < -100) {
                    if (indicatePos > 0) {
                        scrollPager(indicatePos - 1);
                        scrolling = false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            scrollSum = 0;
            scrolling = true;
            return true;
        }

    };
}
