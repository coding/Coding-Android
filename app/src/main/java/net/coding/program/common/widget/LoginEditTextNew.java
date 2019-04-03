package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;

import net.coding.program.R;
import net.coding.program.common.CodingColor;

/**
 * Created by chenchao on 15/12/17.
 */
public class LoginEditTextNew extends LoginEditText {

    public LoginEditTextNew(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnEditFocusChange(((v, hasFocus) -> {
            topLine.setBackgroundColor(hasFocus ? CodingColor.font1 : 0xFFD8DDE4);
        }));

        if (editText instanceof LoginAutoCompleteEdit) {
            ((LoginAutoCompleteEdit) editText).showClear(false);
        }

        hideDisplayPassword();
    }

    protected int getLayoutId() {
        return R.layout.login_edit_text_new;
    }
}
