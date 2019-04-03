package net.coding.program.user;

import net.coding.program.R;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

// TODO: 2018/1/4  删除
@EActivity(R.layout.enterprise_activity_my_detail)
public class EnterpriseMyDetailActivity extends UserDetailCommonActivity {

    public final int RESULT_EDIT = 0;
    private final String TAG_HOST_USER_INFO = "TAG_HOST_USER_INFO";

    @AfterViews
    void initMyDetailActivity() {
        bindUI(GlobalData.sUserObject);
        tv_follow_state.setText("编辑资料");
        rl_follow_state.setOnClickListener(v -> {
            UserDetailEditActivity_
                    .intent(this)
                    .startForResult(RESULT_EDIT);
        });

        final String HOST_USER_INFO = Global.HOST_API + "/user/key/";
        getNetwork(HOST_USER_INFO + GlobalData.sUserObject.global_key, TAG_HOST_USER_INFO);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_USER_INFO)) {
            if (code == 0) {
                mUserObject = new UserObject(respanse.getJSONObject("data"));
                bindUI(mUserObject);
            } else {
                showButtomToast("获取用户信息错误");
            }
        }
        openActivenessResult(code, respanse, tag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindUI(GlobalData.sUserObject);
    }

    public int getActionBarSize() {
        return GlobalCommon.dpToPx(48);
    }

}
