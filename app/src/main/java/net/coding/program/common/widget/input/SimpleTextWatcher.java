package net.coding.program.common.widget.input;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by chaochen on 15/1/15.
 * 简化匿名类
 */
public class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
