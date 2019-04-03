package net.coding.program.project.git;


import android.support.v4.app.Fragment;
import android.widget.EditText;

import net.coding.program.R;
import net.coding.program.common.model.GitFileObject;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.fragment_edit_code)
public class EditCodeFragment extends Fragment {

    @ViewById
    EditText editText;

    @Override
    public void onResume() {
        super.onResume();

        GitFileObject file = ((EditCodeActivity) getActivity()).getFile().getGitFileObject();
        editText.setText(file.data);
    }

    public String getInput() {
        return editText.getText().toString();
    }

    public boolean isModify() {
        GitFileObject file = ((EditCodeActivity) getActivity()).getFile().getGitFileObject();
        return !editText.getText().toString().equals(file.data);
    }
}
