package net.coding.program.setting;


import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

@EFragment(R.layout.fragment_main_setting)
@OptionsMenu(R.menu.main_setting)
public class MainSettingFragment extends BaseFragment {

    @OptionsItem
    void actionAddFollow() {

    }
}
