package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import net.coding.program.R;

/**
 * Created by chenchao on 15/12/17.
 */
public class LoginEditTextNew extends LoginEditText {

    protected int getLayoutId() {
        return R.layout.login_edit_text_new;
    }

    public LoginEditTextNew(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnEditFocusChange(((v, hasFocus) -> {
            topLine.setBackgroundColor(hasFocus ? 0xFF323A45 : 0xFFD8DDE4);
        }));

        if (editText instanceof LoginAutoCompleteEdit) {
            ((LoginAutoCompleteEdit) editText).showClear(false);
        }

        hideDisplayPassword();
    }
}
