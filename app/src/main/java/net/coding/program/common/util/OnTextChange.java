package net.coding.program.common.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by chenchao on 15/12/22.
 */
public interface OnTextChange {
    public void addTextChangedListener(TextWatcher watcher);

    public boolean isEmpty();

    public Editable getText();
}
