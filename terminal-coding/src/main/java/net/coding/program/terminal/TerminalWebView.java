package net.coding.program.terminal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.orhanobut.logger.Logger;

public class TerminalWebView extends WebView {

    public TerminalWebView(Context context) {
        super(context);
    }

    public TerminalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TerminalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Logger.d("CKEY ww " + event);
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        Logger.d("CKEY ww " + event);
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        Logger.d("CKEY ww " + event);
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Logger.d("CKEY ww " + event);
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        Logger.d("CKEY ww " + event);
        return super.dispatchTrackballEvent(event);
    }
}
