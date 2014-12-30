package net.coding.program.task;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_task_descrip_md)
public class TaskDescripMdFragment extends BaseFragment {

    @FragmentArg
    String contentMd;

    @ViewById
    EditText edit;

    @AfterViews
    void init() {
        setHasOptionsMenu(true);
        if (contentMd == null) {

        } else {
            edit.setText(contentMd);
        }
    }

    @OptionsItem
    void action_preview() {
        Fragment fragment = TaskDescripHtmlFragment_.builder()
                .contentMd(edit.getText().toString())
                .preview(true)
                .build();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.listview_fade_in, R.anim.listview_fade_out
                )
                .replace(R.id.container, fragment)
                .addToBackStack("pre")
                .commit();
    }

    @OptionsItem
    void action_save() {
        ((TaskDescrip) getActivity()).closeAndSave(edit.getText().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_description_edit, menu);
    }
}
