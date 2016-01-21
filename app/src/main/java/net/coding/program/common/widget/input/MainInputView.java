package net.coding.program.common.widget.input;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 16/1/21.
 * 私信的输入框
 */

@EViewGroup(R.layout.input_view_main)
public class MainInputView extends FrameLayout{

    @ViewById
    TopBar_ topBar;

    @ViewById
    VoiceView_ voiceView;

    @ViewById
    EmojiKeyboard_ emojiKeyboard;

    public MainInputView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @AfterViews
    void initMainInputView() {
        emojiKeyboard.setInputAction(topBar);
    }
}
