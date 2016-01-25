package net.coding.program.common.widget.input;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.coding.program.R;
import net.coding.program.common.EmojiTranslate;
import net.coding.program.common.Global;
import net.coding.program.common.enter.SimpleTextWatcher;

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
public class TopBar extends FrameLayout implements InputAction, KeyboardControl, InputOperate {

    FragmentActivity mActivity;

    View rootView;
    int rootViewHigh;

    KeyboardControl keyboardControl;

    @ViewById
    EditText editText;

    @ViewById
    View voiceLayout, editTextLayout;

    @ViewById
    CheckBox popVoice, popEditButton;

    @ViewById
    CheckBox popEmoji;

    @ViewById
    View sendText, send;

    private boolean disableCheckedChange = false;

    private final OnClickListener sendImage = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mActivity instanceof CameraAndPhoto) {
                CameraAndPhoto cameraAndPhoto = (CameraAndPhoto) mActivity;
                cameraAndPhoto.photo();
            }
        }
    };

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
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int high = rootView.getHeight();
            Log.d("", "topbar hhh2 " + high);

            int lastHeight = rootViewHigh;
            rootViewHigh = rootView.getHeight();
            if (rootViewHigh < lastHeight) {
                keyboardControl.hideCustomInput();
            }
        });

        if (mActivity instanceof CameraAndPhoto) {
            sendText.setVisibility(INVISIBLE);
            send.setVisibility(VISIBLE);
            send.setOnClickListener(sendImage);

        } else {
            sendText.setVisibility(VISIBLE);
            send.setVisibility(INVISIBLE);
        }

        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mActivity instanceof CameraAndPhoto) {
                    if (s.length() > 0) {
                        sendText.setVisibility(VISIBLE);
                        send.setVisibility(View.INVISIBLE);
                        popVoice.setVisibility(GONE);
                    } else {
                        sendText.setVisibility(INVISIBLE);
                        send.setVisibility(View.VISIBLE);
                        popVoice.setVisibility(VISIBLE);
                    }
                } else {
                    if (s.length() > 0) {
                        sendText.setEnabled(true);
                    } else {
                        sendText.setEnabled(false);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 || count == 2) {
                    String newString = s.subSequence(start, start + count).toString();
                    String imgName = EmojiTranslate.sEmojiMap.get(newString);
                    if (imgName != null) {
                        final String format = ":%s:";
                        String replaced = String.format(format, imgName);

                        Editable editable = editText.getText();
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
                                    if (emojiSpan.getDrawable() != null) {
                                        Editable editable = editText.getText();
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
        hideCustomInput();
    }

    @Override
    public void hideCustomInput() {
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

    @Override
    public String getContent() {
        return editText.getText().toString();
    }

    @Override
    public void clearContent() {
        editText.setText("");
    }

    @Override
    public void setContent(String s) {
        editText.requestFocus();
        Editable editable = editText.getText();
        editable.clear();

        editable.insert(0, s);
    }

    @Override
    public void hideKeyboard() {
        Global.popSoftkeyboard(mActivity, editText, false);
    }

    @Override
    public void insertText(String s) {
        insertText(editText, s);
    }

    public static void insertText(EditText edit, String s) {
        edit.requestFocus();
        int insertPos = edit.getSelectionStart();

        String insertString = s + " ";
        Editable editable = edit.getText();
        editable.insert(insertPos, insertString);
    }

    @Override
    public boolean isPopCustomKeyboard() {
        return false;
    }

    @Override
    public void closeCustomKeyboard() {
    }

    @Override
    public void setClickSend(OnClickListener click) {
        sendText.setOnClickListener(click);
    }

    @Override
    public void addTextWatcher(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }
}
