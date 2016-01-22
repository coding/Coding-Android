package net.coding.program.common.widget.input;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.coding.program.R;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 16/1/21.
 * 输入栏
 */

@EViewGroup(R.layout.input_view_top_bar)
public class TopBar extends FrameLayout implements InputAction, KeyboardControl {

    FragmentActivity mActivity;

    View rootView;
    int rootViewHigh;

    KeyboardControl keyboardControl;

    @ViewById
    EditText editText;

//    @ViewById
//    View voiceButton;

    @ViewById
    View voiceLayout, editTextLayout;

    @ViewById
    CheckBox popVoice, popEditButton;

    @ViewById
    CheckBox popEmoji;

    private boolean disableCheckedChange = false;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (FragmentActivity) getContext();

        rootView = mActivity.findViewById(android.R.id.content);
    }

    public EditText getEditText() {
        return editText;
    }

    public void setKeyboardControl(KeyboardControl keyboardControl) {
        this.keyboardControl = keyboardControl;
    }

    @AfterViews
    void initTopBar() {
    }

    @CheckedChange
    void popEmoji(boolean checked) {
        if (disableCheckedChange) {
            return;
        }

        if (checked) { // 需要弹出 emoji 键盘
            slowlyPop(true);
        } else {
            keyboardControl.showSystemInput(true);
        }
    }

    /*
     优化弹出过程, 防止系统键盘和自定义键盘同时出现
     showEmoji 为 true 表示弹出 emoji 键盘, 为 false 弹出 voice 键盘
     */
    private void slowlyPop(boolean showEmoji) {
        rootViewHigh = rootView.getHeight();

        final int bottomHigh = Global.dpToPx(100); // 底部虚拟按键高度，nexus5是73dp，以防万一，所以设大一点
        int rootParentHigh = rootView.getRootView().getHeight();
        if (rootParentHigh - rootViewHigh > bottomHigh) {
            // 说明键盘已经弹出来了，等键盘消失后再设置 emoji keyboard 可见
            Global.popSoftkeyboard(mActivity, editText, false);

            // 魅族手机的 rootView 无论输入法是否弹出高度都是不变的，只好搞个延时做这个事
            rootView.postDelayed(() -> {
                rootView.setLayoutParams(rootView.getLayoutParams());
                showCustomKeyboard(showEmoji);
            }, 200);

        } else {
            rootViewHigh = 0;
            showCustomKeyboard(showEmoji);
        }
    }

    private void showCustomKeyboard(boolean showEmoji) {
        if (showEmoji) {
            keyboardControl.showEmojiInput();
        } else {
            keyboardControl.showVoiceInput();
        }
    }

    @Click
    void popEditButton() {
        keyboardControl.showSystemInput(false);
    }

    @CheckedChange
    void popVoice(boolean checked) {
        if (disableCheckedChange) {
            return;
        }

        if (checked) {
            slowlyPop(false);
        } else {
            if (popEmoji.isChecked()) {
                keyboardControl.showEmojiInput();
            } else {
                keyboardControl.showSystemInput(true);
            }
        }
    }

    @Override
    public void showEmojiInput() {
        disableCheckedChange = true;

        popVoice.setChecked(false);
        voiceLayout.setVisibility(INVISIBLE);
        editTextLayout.setVisibility(VISIBLE);
        popEmoji.setChecked(true);

        disableCheckedChange = false;
    }

    @Override
    public void showVoiceInput() {
        disableCheckedChange = true;

        popVoice.setChecked(true);
        voiceLayout.setVisibility(VISIBLE);
        editTextLayout.setVisibility(GONE);

        disableCheckedChange = false;
    }

    @Override
    public void showSystemInput(boolean show) {
        disableCheckedChange = true;

        popVoice.setChecked(false);
        voiceLayout.setVisibility(INVISIBLE);
        editTextLayout.setVisibility(VISIBLE);
        popEmoji.setChecked(false);

        disableCheckedChange = false;
    }

    @Click
    void popEmojiButton() {
        keyboardControl.showEmojiInput();
    }

    @Override
    public void deleteOneChar() {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void insertEmoji(String s) {
        int insertPos = editText.getSelectionStart();
        final String format = ":%s:";
        String replaced = String.format(format, s);

        Editable editable = editText.getText();
        editable.insert(insertPos, String.format(format, s));
        editable.setSpan(new EmojiconSpan(mActivity, s), insertPos, insertPos + replaced.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
