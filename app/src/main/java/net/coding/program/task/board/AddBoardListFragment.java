package net.coding.program.task.board;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_add_board)
public class AddBoardListFragment extends BaseFragment {

    @Click
    void addTaskList() {
        ((TaskBoardActivity) getActivity()).jumpAddBoardList();
    }
}
