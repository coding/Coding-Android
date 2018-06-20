package net.coding.program.project.detail.merge;


import android.support.v4.app.Fragment;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.base.BaseLoadMoreFragment;
import net.coding.program.common.event.EventRefreshMerge;
import net.coding.program.common.model.Merge;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.common_refresh_listview_divide)
public class MergeListFragment extends BaseLoadMoreFragment {

    public static final String EVENT_UPDATE = "EVENT_UPDATE";

    public static final int TYPE_OPEN = 0;
    public static final int TYPE_CLOSE = 1;
    public static final int RESULT_CHANGE = 1;
    private static final String HOST_MERGE = "HOST_MERGE";

    @FragmentArg
    ProjectObject mProjectObject;
    @FragmentArg
    int mType;
    @FragmentArg
    ProjectObject.MergeExamine mMineType;

    @ViewById
    View blankLayout;
    private MergeAdapter mMergeAdapter;
    private String mUrlMerge;
    private View.OnClickListener onClickRetry = v -> {
        onRefresh();
        loadMore();
    };

    @AfterViews
    protected final void initMergeListFragment() {
        listViewAddHeaderSection(listView);
        listView.setVisibility(View.INVISIBLE);
        initRefreshLayout();

        if (mProjectObject.isPublic()) {
            mUrlMerge = mProjectObject.getHttpMerge(mType == TYPE_OPEN);
        } else {
            mUrlMerge = mProjectObject.getHttpMergeExamine(mType == TYPE_OPEN, mMineType);
        }
        mMergeAdapter = new MergeAdapter(new ArrayList<>(), this, getImageLoad());
        listView.setAdapter(mMergeAdapter);
        loadMore();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventRefreshMerge event) {
        initSetting();
        loadMore();
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(mUrlMerge, HOST_MERGE);
    }

    @ItemClick
    protected final void listView(Merge merge) {
        Fragment fragment = getParentFragment();
        if (fragment == null) fragment = this;
        MergeDetailActivity_.intent(fragment).mMerge(merge).startForResult(RESULT_CHANGE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE)) {
            setRefreshing(false);
//            hideDialogLoading();
            if (code == 0) {
                if (isLoadingFirstPage(HOST_MERGE)) {
                    mMergeAdapter.clearData();
                }

                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                ArrayList<Merge> parseData = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); ++i) {
                    parseData.add(new Merge(jsonArray.getJSONObject(i)));
                }

                mMergeAdapter.appendDataUpdate(parseData);
            } else {
                showErrorMsg(code, respanse);
            }

            updateLoadingState(code, tag, mMergeAdapter.getCount());

            listView.setVisibility(mMergeAdapter.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            BlankViewDisplay.setBlank(mMergeAdapter.getCount(), this, code == 0, blankLayout, onClickRetry);
        }
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
    }
}
