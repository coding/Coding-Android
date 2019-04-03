package net.coding.program.user;

import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.UserObject;
import net.coding.program.message.MessageListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

// TODO: 2018/1/4  删除
@EActivity(R.layout.enterprise_activity_my_detail)
public class EnterpriseUserDetailActivity extends UserDetailCommonActivity {

    private final String TAG_HOST_USER_INFO = "TAG_HOST_USER_INFO";
    private final String HOST_USER_INFO = Global.HOST_API + "/user/key/";

    @Extra
    String globalKey;

    @ViewById
    TextView moreDetail;

    @AfterViews
    void initEnterpriseUserDetailActivity() {
        if (GlobalData.sUserObject.global_key.equals(globalKey)) {
            EnterpriseMyDetailActivity_.intent(this).start();
            finish();
            return;
        }

        getNetwork(HOST_USER_INFO + globalKey, TAG_HOST_USER_INFO);

    }

    @Click
    void moreDetail() {
        if (mUserObject == null) {
            return;
        }

        UserDetailMoreActivity_.intent(this)
                .mUserObject(mUserObject)
                .start();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_USER_INFO)) {
            if (code == 0) {
                moreDetail.setVisibility(View.VISIBLE);
                mUserObject = new UserObject(respanse.getJSONObject("data"));
                bindUI(mUserObject);
            } else {
                showButtomToast("获取用户信息错误");
            }
        }
        openActivenessResult(code, respanse, tag);
    }

    @Override
    protected void bindUI(UserObject mUserObject) {
        super.bindUI(mUserObject);
        tv_follow_state.setText("发送私信");
        tv_follow_state.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_private_letter, 0, 0, 0);
        rl_follow_state.setOnClickListener(v -> {
            MessageListActivity_.intent(v.getContext()).mGlobalKey(globalKey).start();
        });
    }

    public int getActionBarSize() {
        return GlobalCommon.dpToPx(48);
    }


}
