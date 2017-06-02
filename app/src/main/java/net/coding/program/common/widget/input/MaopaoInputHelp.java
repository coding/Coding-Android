package net.coding.program.common.widget.input;

import android.text.Editable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;

/**
 * Created by chenchao on 2017/6/2.
 * 冒泡的 emoji 输入
 */
public class MaopaoInputHelp implements InputBaseCallback, InputAction {

    private EmojiKeyboard emojiKeyboard;
    public EmojiEditText editText;
    private CheckBox popEmoji;

    private boolean disableCheckedChange;

    public MaopaoInputHelp(View v) {
        emojiKeyboard = (EmojiKeyboard) v.findViewById(R.id.emojiKeyboard);
        editText = (EmojiEditText) v.findViewById(R.id.editText);
        editText.setCallback(this);

        emojiKeyboard.setInputAction(this);
        popEmoji = (CheckBox) v.findViewById(R.id.popEmoji);
        popEmoji.setOnClickListener(v1 -> {
            if (disableCheckedChange) {
                return;
            }

            if (popEmoji.isChecked()) { // 需要弹出 emoji 键盘
                slowlyPop();
            } else {
                hideCustomInput();
                popKeyboard();
            }
        });
    }

    private void slowlyPop() {
        if (editText.isPopSystemInput()) {
            editText.postAfterSystemInputHide(() -> emojiKeyboard.setVisibility(View.VISIBLE));
        } else {
            hideCustomInput();
        }
    }

    public void hideCustomInput() {
        disableCheckedChange = true;
        popEmoji.setChecked(false);
        emojiKeyboard.setVisibility(View.GONE);
        disableCheckedChange = false;
    }

    public void popKeyboard() {
        editText.requestFocus();
        Global.popSoftkeyboard(editText.getContext(), editText, true);
    }

    public void closeEnterPanel() {
        emojiKeyboard.setVisibility(View.GONE);
    }

    public void insertText(String insert) {
        insertText(editText, insert);
    }

    public void clearContent() {
        editText.setText("");
    }

    @Override
    public void popSystemInput() {
        hideCustomInput();
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

    public static void insertText(EditText edit, String s) {
        edit.requestFocus();
        int insertPos = edit.getSelectionStart();

        String insertString = s + " ";
        Editable editable = edit.getText();
        editable.insert(insertPos, insertString);
    }


}
