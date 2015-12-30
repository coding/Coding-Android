package net.coding.program.login.phone;

import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_phone_set_password)
public class SetGlobalKeyActivity extends BackActivity implements ParentActivity {

    private static boolean isShowing = false;

    public static boolean isShowing() {
        return isShowing;
    }

    @Extra
    String phone;

    private RequestParams requestParams = new RequestParams();

    @AfterViews
    final void initPhoneSetPasswordActivity() {
        requestParams.put("phone", phone);
        Fragment fragment = PhoneSetGlobalFragment_.builder().showFragmentTop(false).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @Override
    public RequestParams getRequestParmas() {
        return requestParams;
    }

    @Override
    public void next() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        isShowing = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShowing = false;
    }
}
