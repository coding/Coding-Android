package net.coding.program.common.base;

import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by chenchao on 15/3/9.
 */
@EFragment
public abstract class CustomMoreFragment extends RefreshBaseFragment {

    protected abstract String getLink();

    @OptionsItem
    protected void action_copy() {
        Global.tipCopyLink(getContext(), getLink());
    }


    public void search(String text) {
    }

}
