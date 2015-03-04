package net.coding.program.project.detail;


import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_topic_edit)
@OptionsMenu(R.menu.topic_detail_edit)
public class TopicEditFragment extends BaseFragment {

    @ViewById
    protected EditText title;

    @ViewById
    protected EditText edit;
    private TopicAddActivity.TopicData mOldData;


    @AfterViews
    protected void init() {
        hasOptionsMenu();

        mOldData = ((SaveData) getActivity()).loadData();
        title.setText(mOldData.title);
        edit.setText(mOldData.content);
    }

    @OptionsItem
    protected void action_preview() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString()));
        saveData.switchPreview();
        Global.popSoftkeyboard(getActivity(), edit, false);
    }

    @OptionsItem
    protected void action_save() {
        SaveData saveData = (SaveData) getActivity();
        saveData.saveData(new TopicAddActivity.TopicData(title.getText().toString(), edit.getText().toString()));
        saveData.exit();
    }

    public boolean isContentModify() {
        // title 有可能为空，因为用户可能根本没有进入编辑界面
        if (title == null) {
            return false;
        }

        return !title.getText().toString().equals(mOldData.title) ||
                        !edit.getText().toString().equals(mOldData.content);
    }

    public interface SaveData {
        public void saveData(TopicAddActivity.TopicData data);

        public TopicAddActivity.TopicData loadData();

        public void switchPreview();

        public void switchEdit();

        public void exit();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    final String tipFont = "在此输入文字";

    @Click
    public void mdBold(View v) {
        insertString(" **", tipFont, "** ");
    }

    @Click
    public void mdItalic(View v) {
        insertString(" *", tipFont, "* ");
    }

    @Click
    public void mdHyperlink(View view) {
        insertString("[", tipFont, "]()");
    }

    @Click
    public void mdLinkQuote(View view) {
        insertString("\n> ", tipFont, "");
    }

    @Click
    public void mdCode(View v) {
        insertString("\n```\n",
                tipFont,
                "\n```\n");
    }

    @Click
    public void mdTitle(View view) {
        insertString("## ", tipFont, " ##");
    }

    @Click
    public void mdList(View v) {
        insertString("\n - ", tipFont, "");
    }

    @Click
    public void mdDivide(View v) {
        insertString("\n----------\n", tipFont, "");
    }

    private void insertString(String begin, String middle, String end) {
        edit.requestFocus();
        Global.popSoftkeyboard(getActivity(), edit, true);

        String insertString = String.format("%s%s%s", begin, middle, end);
        int insertPos = edit.getSelectionStart();
        int selectBegin = insertPos - begin.length();
        int selectEnd = selectBegin + insertString.length();

        Editable editable = edit.getText();
        String currentInput = editable.toString();

        if (0 <= selectBegin &&
                selectEnd <= currentInput.length() &&
                insertString.equals(currentInput.substring(selectBegin, selectEnd))) { //
            editable.replace(selectBegin, selectEnd, middle);
            edit.setSelection(selectBegin, selectBegin + middle.length());
        } else {
            editable.replace(insertPos, edit.getSelectionEnd(), insertString);
            edit.setSelection(insertPos + begin.length(), insertPos + begin.length() + middle.length());
        }
    }
}
