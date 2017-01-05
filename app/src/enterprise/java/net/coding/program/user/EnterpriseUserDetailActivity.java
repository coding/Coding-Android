package net.coding.program.user;

import net.coding.program.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_user_detail)
public class EnterpriseUserDetailActivity extends UserDetailCommonActivity {

    @Extra
    String globalKey;

}
