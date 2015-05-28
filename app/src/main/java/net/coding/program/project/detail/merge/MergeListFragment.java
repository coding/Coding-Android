package net.coding.program.project.detail.merge;


import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.Merge;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@EFragment(R.layout.fragment_merge_list2)
public class MergeListFragment extends BaseFragment {

    public static final int TYPE_OPEN = 0;
    public static final int TYPE_CLOSE = 1;

    @ViewById
    ListView listView;

    @FragmentArg
    ProjectObject mProjectObject;

    @FragmentArg
    int mType;

    private MergeAdapter mMergeAdapter;

    private static final String HOST_MERGE = "HOST_MERGE";
    private String mUrlMerge;

    @AfterViews
    protected final void initMergeListFragment() {
        mUrlMerge = mProjectObject.getHttpMerge(mType == TYPE_OPEN);
        getNextPageNetwork(mUrlMerge, HOST_MERGE);
        mMergeAdapter = new MergeAdapter(new ArrayList<Merge>());
        listView.setAdapter(mMergeAdapter);
    }

    @ItemClick
    protected final void listView(Merge merge) {
        MergeDetailActivity_.intent(this).mMerge(merge).start();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_MERGE)) {
            if (code == 0) {
                JSONArray jsonArray = respanse.getJSONObject("data").getJSONArray("list");
                ArrayList<Merge> parseData = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); ++i) {
                    parseData.add(new Merge(jsonArray.getJSONObject(i)));
                }
                mMergeAdapter.appendData(parseData);

            } else {
                showErrorMsg(code, respanse);
            }
        }
    }
}
