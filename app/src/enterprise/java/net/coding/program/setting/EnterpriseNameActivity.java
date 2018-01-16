package net.coding.program.setting;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.EnterpriseDetail;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_enterprise_name)
@OptionsMenu(R.menu.enterprise_edit_name_menu)
public class EnterpriseNameActivity extends BackActivity {
    private static final String HOST_UPDATE_NAME = "HOST_UPDATE_NAME";
    public final String HOST_CURRENT = getHostCurrent();

    public static String getHostCurrent() {
        String host = String.format("%s/team/%s", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        return host + "/update";
    }

    @ViewById
    EditText enterpriseNameEt;

    @AfterViews
    void initView() {
        setActionBarTitle(getString(R.string.enterprise_name));
        enterpriseNameEt.setText(EnterpriseInfo.instance().getName());
    }

    @OptionsItem
    void action_edit_name() {
        String name = enterpriseNameEt.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            RequestParams params = new RequestParams();
            params.put("name", name);
            params.put("introduction", "");
            params.put("global_key", EnterpriseInfo.instance().getGlobalkey());
            postNetwork(HOST_CURRENT, params, HOST_UPDATE_NAME);
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_UPDATE_NAME)) {
            if (code == 0) {
                JSONObject dataObject = respanse.optJSONObject("data");
                EnterpriseDetail detail = new EnterpriseDetail(dataObject);
                EnterpriseInfo.instance().update(this, detail);
                setResult(Activity.RESULT_OK);
                this.finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
