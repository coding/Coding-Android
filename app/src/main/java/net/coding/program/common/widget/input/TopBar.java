package net.coding.program.common.widget.input;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.CommentBackup;
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
public class TopBar extends FrameLayout implements InputAction, KeyboardControl, InputOperate, InputBaseCallback {

    FragmentActivity mActivity;
    private final OnClickListener sendImage = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mActivity instanceof CameraAndPhoto) {
                CameraAndPhoto cameraAndPhoto = (CameraAndPhoto) mActivity;
                cameraAndPhoto.photo();
            }
        }
    };
    KeyboardControl keyboardControl;
    @ViewById
    EmojiEditText editText;
    @ViewById
    View voiceLayout, editTextLayout;
    @ViewById
    CheckBox popVoice, popEditButton;
    @ViewById
    CheckBox popEmoji;
    @ViewById
    View send;
    @ViewById
    TextView sendText;

    boolean showSendVoice = true;

    private boolean disableCheckedChange = false;
    private TextWatcher restoreWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            Object tag = editText.getTag();
            if (tag == null) {
                return;
            }

            CommentBackup.getInstance().save(CommentBackup.BackupParam.create(tag), s.toString());
        }
    };

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (FragmentActivity) Global.getActivityFromView(this);
    }

    public static void insertText(EditText edit, String s) {
        edit.requestFocus();
        int insertPos = edit.getSelectionStart();

        String insertString = s + " ";
        Editable editable = edit.getText();
        editable.insert(insertPos, insertString);
    }

    public EditText getEditText() {
        return editText;
    }

    public void setKeyboardControl(KeyboardControl keyboardControl) {
        this.keyboardControl = keyboardControl;
    }

    @AfterViews
    void initTopBar() {
        editText.setCallback(this);

        if (mActivity instanceof CameraAndPhoto) {
            sendText.setVisibility(INVISIBLE);
            send.setVisibility(VISIBLE);
            send.setOnClickListener(sendImage);

        } else {
            sendText.setVisibility(VISIBLE);
            send.setVisibility(INVISIBLE);
        }

        if (mActivity instanceof VoiceRecordCompleteCallback && showSendVoice) {
            popVoice.setVisibility(VISIBLE);
        } else {
            popVoice.setVisibility(View.GONE);
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

                if (mActivity instanceof VoiceRecordCompleteCallback && showSendVoice) {
                    popVoice.setVisibility(VISIBLE);
                } else {
                    popVoice.setVisibility(View.GONE);
                }

                if (s.length() > 0) {
                    sendText.setBackgroundResource(R.drawable.edit_send_green);
                    sendText.setTextColor(CodingColor.fontWhite);
                } else {
                    sendText.setBackgroundResource(R.drawable.round_rect_send);
                    sendText.setTextColor(CodingColor.font3);
                }
            }

        });
        editText.setText("");
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
        if (editText.isPopSystemInput()) {
            editText.postAfterSystemInputHide(() -> showCustomKeyboard(showEmoji));

        } else {
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

    public void setShowSendVoice(boolean show) {
        showSendVoice = show;
        popVoice.setVisibility(show ? VISIBLE : GONE);
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

    @Override
    public void deleteOneChar() {
        editText.deleteOneChar();
    }

    @Override
    public void insertEmoji(String s) {
        editText.insertEmoji(s);
    }

    @Override
    public void numberAction(String number) {
        editText.numberAction(number);
    }

    @Override
    public void enterAction() {
        editText.enterAction();
    }

    @Override
    public String getContent() {
        return editText.getText().toString();
    }

    @Override
    public void setContent(String s) {
        editText.requestFocus();
        Editable editable = editText.getText();
        editable.clear();

        editable.insert(0, s);
    }

    @Override
    public void clearContent() {
        editText.setText("");
    }

    @Override
    public void hideKeyboard() {
        Global.popSoftkeyboard(mActivity, editText, false);
    }

    @Override
    public void insertText(String s) {
        insertText(editText, s);
    }

    @Override
    public void popSystemInput() {
        keyboardControl.hideCustomInput();
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

    private void restoreSaveStart() {
        editText.addTextChangedListener(restoreWatcher);
    }

    public void restoreSaveStop() {
        editText.removeTextChangedListener(restoreWatcher);
    }

    public void restoreDelete(Object comment) {
        CommentBackup.getInstance().delete(CommentBackup.BackupParam.create(comment));
    }

    public void restoreLoad(final Object object) {
        if (object == null) {
            return;
        }

        restoreSaveStop();
        clearContent();
        String lastInput = CommentBackup.getInstance().load(CommentBackup.BackupParam.create(object));
        editText.getText().append(lastInput);
        restoreSaveStart();
    }
}
