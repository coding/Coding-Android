package net.coding.program.task;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MDEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;

@EFragment(R.layout.fragment_task_descrip_md)
public class TaskDescripMdFragment extends MDEditFragment {

    @FragmentArg
    String contentMd;

    ActionMode mActionMode;

    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        if (contentMd != null) {
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

    @OptionsItem
    void action_save() {
        ((TaskDescrip) getActivity()).closeAndSave(edit.getText().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_description_edit, menu);
    }

}
