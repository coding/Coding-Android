package net.coding.program.common.util;

import android.text.Editable;
import android.view.MenuItem;
import android.view.View;

import net.coding.program.common.widget.input.SimpleTextWatcher;

/**
 * Created by chenchao on 15/12/22.
 */
public class ViewStyleUtil {

    private static InputRequest sNoEmptyRequest = s -> s.length() > 0;

    // 只有所有 EditText 都填写了，Button 才是可点击状态
    public static void editTextBindButton(View button, OnTextChange... edits) {
        editTextBindButton(button, sNoEmptyRequest, edits);
    }

    public static void editTextBindButton(View button, InputRequest request, OnTextChange... edits) {
        for (OnTextChange edit : edits) {
            edit.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    updateStyle(button, request, edits);
                }
            });
        }

        updateStyle(button, request, edits);
    }

    private static void updateStyle(View button, InputRequest request, OnTextChange[] edits) {
        for (OnTextChange item : edits) {
            if (item instanceof View) {
                View v = (View) item;
                if (v.getVisibility() != View.VISIBLE) {
                    continue;
                }
            }

            String input = item.getText().toString();
            if (!request.isCurrectFormat(input)) {
                button.setEnabled(false);
                return;
            }
        }
        button.setEnabled(true);
    }

    public static void editTextBindButton(MenuItem button, OnTextChange... edits) {
        editTextBindButton(button, sNoEmptyRequest, edits);
    }


    public static void editTextBindButton(MenuItem button, InputRequest request, OnTextChange... edits) {
        for (OnTextChange edit : edits) {
            edit.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    updateStyle(button, request, edits);
                }
            });
        }

        updateStyle(button, request, edits);
    }

    private static void updateStyle(MenuItem button, InputRequest request, OnTextChange[] edits) {
        for (OnTextChange item : edits) {
            if (item instanceof View) {
                View v = (View) item;
                if (v.getVisibility() != View.VISIBLE) {
                    continue;
                }
            }

            String input = item.getText().toString();
            if (!request.isCurrectFormat(input)) {
                button.setEnabled(false);
                return;
            }
        }
        button.setEnabled(true);
    }

}
