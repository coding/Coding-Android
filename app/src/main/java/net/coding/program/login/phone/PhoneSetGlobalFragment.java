package net.coding.program.login.phone;


import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.MainActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.ActivityNavigate;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EFragment(R.layout.fragment_phone_set_password3)
public class PhoneSetGlobalFragment extends BaseFragment {

    @FragmentArg
    boolean showFragmentTop = true;

    @ViewById
    View activityTitle;

    @ViewById
    LoginEditText email, globalKey;

    @ViewById
    TextView loginButton, textClause;

    @AfterViews
    final void initPhoneSetPasswordFragment() {
        if (!showFragmentTop) {
            activityTitle.setVisibility(View.GONE);
        }

        ViewStyleUtil.editTextBindButton(loginButton, email, globalKey);
            textClause.setText(Html.fromHtml(PhoneSetPasswordActivity.REGIST_TIP));
    }

    @Click
    void loginButton() {
        String emailString = email.getText().toString();
        String globalKeyString = globalKey.getText().toString();

        if (!InputCheck.checkEmail(getContext(), emailString)) {
            showMiddleToast("请填写正确的邮箱");
            return;
        }

        if (globalKeyString.length() < 3) {
            showMiddleToast("个性后缀至少为3个字符");
            return;
        }

        String url = Global.HOST_API + "/account/activate/phone";
        RequestParams params = ((ParentActivity) getActivity()).getRequestParmas();
        params.put("email", emailString);
        params.put("global_key", globalKeyString);

        MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(getActivity()) {
            @Override
            public void onMySuccess(JSONObject respanse) {
                super.onMySuccess(respanse);

                UserObject user = new UserObject(respanse.optJSONObject("data"));
                AccountInfo.saveAccount(getActivity(), user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(getActivity(), user);

                Global.syncCookie(getActivity());

                AccountInfo.saveLastLoginName(getActivity(), user.name);

                getActivity().sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
//                startActivity(new Intent(getActivity(), MainActivity_.class));
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);

                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    @Click
    void textClause() {
        ActivityNavigate.startTermActivity(this);
    }

    protected void loadCurrentUser() {
        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
        String url = Global.HOST_API + "/current_user";
        client.get(getActivity(), url, new MyJsonResponse(getActivity()) {

            @Override
            public void onMySuccess(JSONObject respanse) {
                super.onMySuccess(respanse);
//                showProgressBar(false);
                UserObject user = new UserObject(respanse.optJSONObject("data"));
                AccountInfo.saveAccount(getActivity(), user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(getActivity(), user);

                Global.syncCookie(getActivity());

                AccountInfo.saveLastLoginName(getActivity(), user.name);

                getActivity().sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity_.class));
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false, "");
            }
        });
    }

//    protected void loadUserinfo() {
//        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
//        String url = Global.HOST_API + "/userinfo";
//        client.get(getActivity(), url, new MyJsonResponse(getActivity()) {
//            @Override
//            public void onMySuccess(JSONObject response) {
//                super.onMySuccess(response);
//                MyData.getInstance().update(getActivity(), response.optJSONObject("data"));
//                closeActivity();
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//                ((BaseActivity) getActivity()).showProgressBar(false, "");
//            }
//        });
//    }
}
