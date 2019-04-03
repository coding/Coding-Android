package net.coding.program.task.board;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_welcome_board)
public class WelcomeBoardFragment extends BaseFragment {

    @Click
    void clickCreate() {
        ((TaskBoardActivity) getActivity()).createDefaultList();
    }

    @Click
    void clickNoCreate() {
        ((TaskBoardActivity) getActivity()).hideInitBoard();
    }
}
