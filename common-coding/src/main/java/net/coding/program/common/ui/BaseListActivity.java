package net.coding.program.common.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoadMore;
import net.coding.program.common.model.ProjectObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * todo 重构这个页面
 * Created by chenchao on 15/9/25.
 * 基本的listActivity，没有分隔条，没有分页
 */
public class BaseListActivity extends BackActivity implements LoadMore {

//    public abstract String getActionbarTitle();
//    public abstract String getGetUrl();
//    public abstract View getBindView(int position, View convertView, ViewGroup parent);


    //    private static final String TAG_HTTP_BASE_LIST_ACTIVITY = "TAG_HTTP_BASE_LIST_ACTIVITY";
    private static final String TAG_LOAD_MORE = "TAG_LOAD_MORE";
    ActivityParam mActivityParam;
    private ArrayAdapter<Object> mAdapter;

    public ActivityParam getActivityParam() {
        throw new RuntimeException("need override this method");
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(mActivityParam.mUrl, TAG_LOAD_MORE);
    }

    protected final void initBaseListActivity() {
        mActivityParam = getActivityParam();
        getSupportActionBar().setTitle(mActivityParam.mTitle);
        showDialogLoading();

        ListView listView = (ListView) findViewById(R.id.listView);
        mAdapter = new CustomArrayAdapter<>(this, new ArrayList<>());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mActivityParam.mItemClick);

        loadMore();
//        getNetwork(mActivityParam.mUrl, TAG_HTTP_BASE_LIST_ACTIVITY);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_LOAD_MORE)) {
            hideProgressDialog();
            if (code == 0) {
                JSONArray jsonArray = respanse.optJSONArray("data");
                if (jsonArray == null) {
                    jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                }

                mAdapter.setNotifyOnChange(false);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject item = jsonArray.optJSONObject(i);
                    mAdapter.add(new ProjectObject(item));
                }

                mAdapter.notifyDataSetChanged();


            } else {
                showErrorMsg(code, respanse);
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    public interface BaseViewHold {
        int getLayout();

        void init(View v, ImageLoadTool imageLoadTool);

        void setData(Object data);
    }

    public static class ActivityParam {
        String mTitle;
        String mUrl;
        AdapterView.OnItemClickListener mItemClick;
        Class<BaseViewHold> mViewHold;

        public ActivityParam(String mTitle, String mUrl, Class<BaseViewHold> mViewHold, AdapterView.OnItemClickListener mItemClick) {
            this.mTitle = mTitle;
            this.mUrl = mUrl;
            this.mViewHold = mViewHold;
            this.mItemClick = mItemClick;
        }
    }

    class CustomArrayAdapter<T> extends ArrayAdapter<T> {

        public CustomArrayAdapter(Context context, List<T> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                BaseViewHold hold;
                if (convertView == null) {
                    hold = mActivityParam.mViewHold.newInstance();
                    convertView = LayoutInflater.from(parent.getContext()).inflate(hold.getLayout(), parent, false);
                    hold.init(convertView, getImageLoad());
                    convertView.setTag(hold);
                } else {
                    hold = (BaseViewHold) convertView.getTag();
                }

                hold.setData(getItem(position));

                if (position == getCount() - 1) {
                    loadMore();
                }

                return convertView;
            } catch (Exception e) {
                return new TextView(parent.getContext());
            }
        }

    }

}
