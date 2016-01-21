package net.coding.program.common.widget.input;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.coding.program.R;

import org.androidannotations.annotations.EViewGroup;

/**
 * Created by chenchao on 16/1/21.
 * 按下发语音模块
 */

@EViewGroup(R.layout.input_view_voice_view)
public class VoiceView extends FrameLayout {

    private Activity mActivity;
    private boolean firstLayout = true;
    private int contentViewHeight;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLp;
    private ImageView mWindowView;
    private int statusBarHeight;

    View rootView;

    public VoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        mActivity = (Activity) getContext();
        Activity activity = mActivity;
        rootView = mActivity.findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                    }
                }
        );

    }

    protected int getContentViewHeight(Activity activity) {
        Rect rect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        if (statusBarHeight == 0) {
            statusBarHeight = rect.top;
        }
        return rect.bottom - rect.top;
    }

}
