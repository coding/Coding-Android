package net.coding.program.common.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 15/9/25.
 * 基本的listActivity，没有分隔条，没有分页
 */
@EActivity
public abstract class BaseListActivity<T> extends BaseActivity {

    abstract String getActionbarTitle();
    abstract String getGetUrl();
    abstract View getBindView(int position, View convertView, ViewGroup parent);

    private static final String TAG_HTTP_BASE_LIST_ACTIVITY = "TAG_HTTP_BASE_LIST_ACTIVITY";

    ArrayList<T> mData = new ArrayList<>();

    @AfterViews
    protected final void BaseListActivity() {
        getSupportActionBar().setTitle(getActionbarTitle());
        showDialogLoading();
        getNetwork(getGetUrl(), TAG_HTTP_BASE_LIST_ACTIVITY);
    }


    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_BASE_LIST_ACTIVITY)) {
            hideProgressDialog();
            if (code == 0) {
                JSONArray jsonArray = respanse.optJSONArray("data");
                if (jsonArray == null) {

                } else {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject item = jsonArray.optJSONObject(i);
                    }
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    class CustomArrayAdapter<T> extends ArrayAdapter<T> {

        public CustomArrayAdapter(Context context, List<T> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getBindView(position, convertView, parent);
        }
    }
}
