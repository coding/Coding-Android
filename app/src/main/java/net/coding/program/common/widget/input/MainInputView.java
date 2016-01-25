package net.coding.program.common.widget.input;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
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
    
    AppCompatActivity activity;

    @ViewById
    TopBar_ topBar;

    @ViewById
    VoiceView_ voiceView;

    @ViewById
    EmojiKeyboard_ emojiKeyboard;

    public MainInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.activity = (AppCompatActivity) getContext();
    }

    @Override
    public void setClickSend(OnClickListener click) {
        topBar.setClickSend(click);
    }

    @AfterViews
    void initMainInputView() {
        emojiKeyboard.setInputAction(topBar);
        topBar.setKeyboardControl(this);

        showSystemInput(false);
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
        voiceView.setVisibility(View.GONE);
        emojiKeyboard.setVisibility(View.GONE);
    }

    @Override
    public String getContent() {
        return topBar.getContent();
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
        return voiceView.getVisibility() == GONE
                && emojiKeyboard.getVisibility() == GONE;
    }

    @Override
    public void addTextWatcher(TextWatcher textWatcher) {
        topBar.addTextWatcher(textWatcher);
    }

    @Override
    public void setContent(String s) {
        topBar.setContent(s);
    }
}
