package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_setting)
public class SettingActivity extends BackActivity {

    @AfterViews
    void initSettingActivity() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new SettingFragment_())
                .commit();
    }
}
