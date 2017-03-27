package net.coding.program.login.phone;

import android.view.View;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditTextNew;
import net.coding.program.common.widget.ValidePhoneView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_close2_fa)
public class Close2FAActivity extends BackActivity {

    private static final String TAG_CLOSE_2FA = "TAG_CLOSE_2FA";

    @ViewById
    View phoneLine, codeLine;

    @ViewById
    LoginEditTextNew editPhone, editCode;

    @ViewById
    ValidePhoneView sendPhoneMessage;

    @ViewById
    View loginButton;

    @AfterViews
    void initClose2FAActivity() {
        hideActionbarShade();

        getSupportActionBar().setElevation(0);
        sendPhoneMessage.setType(ValidePhoneView.Type.close2FA);
        sendPhoneMessage.setEditPhone(editPhone);
        ViewStyleUtil.editTextBindButton(loginButton, editPhone, editCode);

        editPhone.setOnEditFocusChange(createEditLineFocus(phoneLine));
        editCode.setOnEditFocusChange(createEditLineFocus(codeLine));
    }

    @Click
    void loginButton() {
        String url = Global.HOST_API + "/twofa/close";
        String phone = editPhone.getTextString();
        String code = editCode.getTextString();

        RequestParams params = new RequestParams();
        params.put("phone", phone);
        params.put("code", code);
        postNetwork(url, params, TAG_CLOSE_2FA);

        showProgressBar(true);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_CLOSE_2FA)) {
            showProgressBar(false);
            if (code == 0) {
                showMiddleToast("已关闭两步验证");
                setResult(RESULT_OK);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }
}
