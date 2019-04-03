package net.coding.program.user;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.common.model.EnterpriseUserObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by chenchao on 2017/1/7.
 */
@EActivity(R.layout.activity_enterprise_add_member)
public abstract class BaseEnterpriseUserListActivity extends BackActivity {

    private static final String TAG_HOST_MEMBERS = "TAG_HOST_MEMBERS";

    protected ArrayList<UserObject> listData = new ArrayList<>();
    protected ArrayList<UserObject> allListData = new ArrayList<>();

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
        allListData.clear();
        JSONArray jsonArray = respanse.optJSONArray("data");
        for (int i = 0; i < jsonArray.length(); ++i) {
            EnterpriseUserObject user = new EnterpriseUserObject(jsonArray.optJSONObject(i));
            allListData.add(user.user);
        }

        Collections.sort(allListData);
        listData.addAll(allListData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.users_fans, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setIcon(R.drawable.ic_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchItem(s);
                return true;
            }
        });

        return true;
    }

    protected void searchItem(String s) {
        s = s.toLowerCase();

        listData.clear();

        if (TextUtils.isEmpty(s)) {
            listData.addAll(allListData);
        } else {
            for (UserObject item : allListData) {
                if (item.global_key.toLowerCase().contains(s) ||
                        item.name.toLowerCase().contains(s)) {
                    listData.add(item);
                }
            }
        }
    }

}
