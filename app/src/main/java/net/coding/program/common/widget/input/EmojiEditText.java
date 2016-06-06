package net.coding.program.common.widget.input;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import net.coding.program.common.EmojiTranslate;
import net.coding.program.common.Global;
import net.coding.program.common.enter.SimpleTextWatcher;

/**
 * Created by chenchao on 16/1/25.
 */

public class EmojiEditText extends EditText {

    View rootView;
    int rootViewHigh;

    AppCompatActivity mActivity;

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivity = (AppCompatActivity) getContext();
        rootView = mActivity.findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int lastHigh = rootViewHigh;
            rootViewHigh = rootView.getHeight();
            if (lastHigh > rootViewHigh && callback != null) {
                callback.popSystemInput();
            }

//            if (isPopSystemInput() && callback != null) {
//                callback.popSystemInput();
//            }
        });

        init();
    }

    private void init() {
        addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 || count == 2) {
                    String newString = s.subSequence(start, start + count).toString();
                    String imgName = EmojiTranslate.sEmojiMap.get(newString);
                    if (imgName != null) {
                        final String format = ":%s:";
                        String replaced = String.format(format, imgName);

                        Editable editable = getText();
                        editable.replace(start, start + count, replaced);
                        editable.setSpan(new EmojiconSpan(mActivity, imgName), start, start + replaced.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    int emojiStart = 0;
                    int emojiEnd;
                    boolean startFinded = false;
                    int end = start + count;
                    for (int i = start; i < end; ++i) {
                        if (s.charAt(i) == ':') {
                            if (!startFinded) {
                                emojiStart = i;
                                startFinded = true;
                            } else {
                                emojiEnd = i;
                                if (emojiEnd - emojiStart < 2) { // 指示的是两个：的位置，如果是表情的话，间距肯定大于1
                                    emojiStart = emojiEnd;
                                } else {
                                    String newString = s.subSequence(emojiStart + 1, emojiEnd).toString();
                                    EmojiconSpan emojiSpan = new EmojiconSpan(mActivity, newString);
                                    if (!emojiSpan.isDefault() && emojiSpan.getDrawable() != null) {
                                        Editable editable = getText();
                                        editable.setSpan(emojiSpan, emojiStart, emojiEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        startFinded = false;
                                    } else {
                                        emojiStart = emojiEnd;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void deleteOneChar() {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        dispatchKeyEvent(event);
    }

    public void insertEmoji(String s) {
        int insertPos = getSelectionStart();
        final String format = ":%s:";
        String replaced = String.format(format, s);

        Editable editable = getText();
        editable.insert(insertPos, String.format(format, s));
        editable.setSpan(new EmojiconSpan(mActivity, s), insertPos, insertPos + replaced.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public boolean isPopSystemInput() {
        int rootViewHigh = rootView.getHeight();
        final int bottomHigh = Global.dpToPx(100); // 底部虚拟按键高度，nexus5是73dp，以防万一，所以设大一点
        int rootParentHigh = rootView.getRootView().getHeight();
        return rootParentHigh - rootViewHigh > bottomHigh;
    }

    InputBaseCallback callback;

    public void setCallback(InputBaseCallback callback) {
        this.callback = callback;
    }

    public void postAfterSystemInputHide(Runnable run) {
        // 说明键盘已经弹出来了，等键盘消失后再设置 emoji keyboard 可见
        Global.popSoftkeyboard(mActivity, this, false);
        postDelayed(run, 200);
    }
}
