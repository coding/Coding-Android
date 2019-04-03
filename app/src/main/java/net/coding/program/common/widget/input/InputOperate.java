package net.coding.program.common.widget.input;

import android.text.TextWatcher;
import android.view.View;

/**
 * Created by chenchao on 16/1/22.
 */
public interface InputOperate {
    String getContent();

    void setContent(String s);

    void clearContent();

    void hideKeyboard();

    void insertText(String s);

    boolean isPopCustomKeyboard();

    void closeCustomKeyboard();

    void setClickSend(View.OnClickListener click);

    void addTextWatcher(TextWatcher textWatcher);
}
