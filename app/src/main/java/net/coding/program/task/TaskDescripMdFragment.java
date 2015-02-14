package net.coding.program.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_task_descrip_md)
public class TaskDescripMdFragment extends BaseFragment {

    @FragmentArg
    String contentMd;

    @ViewById
    EditText edit;

    ActionMode mActionMode;

    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        if (contentMd != null)  {
            edit.setText(contentMd);
            mActionMode = getActivity().startActionMode(mActionModeCallback);
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        int id = 0;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            mode.getMenu().clear();
            mode.getMenuInflater().inflate(R.menu.task_description_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save:
                    ((TaskDescrip) getActivity()).closeAndSave(edit.getText().toString());
                    return true;

                case R.id.action_preview:
                    id = R.id.action_preview;
                    mActionMode.finish();
                    Global.popSoftkeyboard(getActivity(), edit, false);
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();

            if (id == R.id.action_preview) {
                Fragment fragment = TaskDescripHtmlFragment_.builder()
                        .contentMd(edit.getText().toString())
                        .preview(true)
                        .build();
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.alpha_in, R.anim.alpha_out,
                                R.anim.alpha_in, R.anim.alpha_out)
                        .replace(R.id.container, fragment)
                        .addToBackStack("pre")
                        .commit();
            } else {
                if (fragmentManager.getFragments().size() == 1) {
                    getActivity().finish();
                }
            }
        }
    };

    @Override
    public void onDetach() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        List<Fragment> lists = fragmentManager.getFragments();

        super.onDetach();
    }

    @OptionsItem
    void action_save() {
        ((TaskDescrip) getActivity()).closeAndSave(edit.getText().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_description_edit, menu);
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
        insertString("\n> ", tipFont,  "");
    }

    @Click
    public void mdCode(View v) {
        insertString("\n```\n" ,
                tipFont ,
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
