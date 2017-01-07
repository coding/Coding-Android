package net.coding.program.user;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.EnterpriseInfo;
import net.coding.program.model.EnterpriseUserObject;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenchao on 2017/1/7.
 */

@EActivity(R.layout.activity_enterprise_add_member)
public class BaseEnterpriseUserListActivity extends BackActivity {

    private static final String TAG_HOST_MEMBERS = "TAG_HOST_MEMBERS";

    protected ArrayList<UserObject> listData = new ArrayList<>();

    @AfterViews
    void initBaseEnterpriseUserListActivity() {
        String host = String.format("%s/team/%s/members", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        getNetwork(host, TAG_HOST_MEMBERS);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HOST_MEMBERS)) {
            if (code == 0) {
                parseUserJson(respanse);
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    protected void parseUserJson(JSONObject respanse) {
        listData.clear();
        JSONArray jsonArray = respanse.optJSONArray("data");
        for (int i = 0; i < jsonArray.length(); ++i) {
            EnterpriseUserObject user = new EnterpriseUserObject(jsonArray.optJSONObject(i));
            listData.add(user.user);
        }
    }
}
