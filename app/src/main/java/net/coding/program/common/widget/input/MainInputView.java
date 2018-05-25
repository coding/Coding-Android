package net.coding.program.common.widget.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.coding.program.R;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 16/1/21.
 * 私信的输入框
 */
@EViewGroup(R.layout.input_view_main)
public class MainInputView extends FrameLayout implements KeyboardControl, InputOperate {

    private final boolean showEmojiOnly;

    AppCompatActivity activity;

    @ViewById
    TopBar_ topBar;
    @ViewById
    VoiceView_ voiceView;
    @ViewById
    EmojiKeyboard_ emojiKeyboard;

    public MainInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MainInputView);

        showEmojiOnly = array.getBoolean(R.styleable.MainInputView_showEmojiOnly, false);
        array.recycle();

        this.activity = (AppCompatActivity) Global.getActivityFromView(this);
    }

    @Override
    public void setClickSend(OnClickListener click) {
        topBar.setClickSend(click);
    }

    @AfterViews
    void initMainInputView() {
        emojiKeyboard.setInputAction(topBar);
        topBar.setKeyboardControl(this);

        if (showEmojiOnly) {
            emojiKeyboard.showEmojiOnly();
        }

        showSystemInput(false);
    }

    public void setShowEmojiOnly(boolean only) {
        if (only) {
            emojiKeyboard.showEmojiOnly();
            topBar.setShowSendVoice(false);
        }
    }

    @Override
    public void showSystemInput(boolean show) {
        Global.popSoftkeyboard(activity, topBar.getEditText(), show);
        voiceView.setVisibility(GONE);
        emojiKeyboard.setVisibility(GONE);

        topBar.showSystemInput(show);
    }

    @Override
    public void showVoiceInput() {
        Global.popSoftkeyboard(activity, topBar.getEditText(), false);
        voiceView.setVisibility(VISIBLE);
        emojiKeyboard.setVisibility(View.GONE);

        topBar.showVoiceInput();
    }

    @Override
    public void showEmojiInput() {
        Global.popSoftkeyboard(activity, topBar.getEditText(), false);
        voiceView.setVisibility(View.GONE);
        emojiKeyboard.setVisibility(View.VISIBLE);

        topBar.showEmojiInput();
    }

    @Override
    public void hideCustomInput() {
        voiceView.setVisibility(GONE);
        emojiKeyboard.setVisibility(GONE);

        topBar.showSystemInput(false);
    }

    @Override
    public void clearContent() {
        topBar.clearContent();
    }

    @Override
    public void closeCustomKeyboard() {
        showSystemInput(false);
    }

    @Override
    public String getContent() {
        return topBar.getContent();
    }

    @Override
    public void setContent(String s) {
        topBar.setContent(s);
    }

    @Override
    public void hideKeyboard() {
        topBar.hideKeyboard();
    }

    @Override
    public void insertText(String s) {
        topBar.insertText(s);
    }

    @Override
    public boolean isPopCustomKeyboard() {
        return voiceView.getVisibility() == VISIBLE
                || emojiKeyboard.getVisibility() == VISIBLE;
    }

    @Override
    public void addTextWatcher(TextWatcher textWatcher) {
        topBar.addTextWatcher(textWatcher);
    }

    public void restoreLoad(Object object) {
        topBar.restoreLoad(object);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    public void restoreSaveStop() {
        topBar.restoreSaveStop();
    }

    public void restoreDelete(Object comment) {
        topBar.restoreDelete(comment);
    }

    public EditText getEditText() {
        return topBar.getEditText();
    }
}
