package net.coding.program.subject;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.Maopao;
import net.coding.program.model.Subject;
import net.coding.program.subject.adapter.SubjectSearchListAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-21.
 * <p/>
 * 搜索冒泡的数据
 */
@EFragment(R.layout.subject_search_fragment)
public class SubjectSearchFragment extends BaseFragment {

    @ViewById
    ListView listView;
    SubjectSearchListAdapter mSubjectSearchListAdapter;

    private String searchUrl = Global.HOST_API + "/search/quick?q=";
    private String searchTag = "search_tag";


    private TextView mSearchResultView;

    // 当前的搜索条件
    private String mCondition = "";
    // 热门话题列表的数据
    private List<Maopao.MaopaoObject> maopaoObjectList = new ArrayList<Maopao.MaopaoObject>();

    public void updateData(String condition) {
        if (!mCondition.equals(condition)) {
            mCondition = condition;
            searchMaopao();
        }
    }

    @AfterViews
    void init() {
        mSubjectSearchListAdapter = new SubjectSearchListAdapter(getActivity(), maopaoObjectList);
        initSearchHeaderView();
        listView.setAdapter(mSubjectSearchListAdapter);
        notifyDataSetChange();

    }

    void initSearchHeaderView() {
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.subject_search_list_header, null);
        mSearchResultView = (TextView) headerView.findViewById(R.id.maopao_search_result);
        listView.addHeaderView(headerView);

    }

    private void searchMaopao() {
        if (!TextUtils.isEmpty(mCondition)) {
            showDialogLoading();
            getNetwork(searchUrl + mCondition, searchTag);
        }
    }

    private void showResultView() {
        mSearchResultView.setText(String.format("共搜索到 %s 个与\"%s\"相关的冒泡", maopaoObjectList.size(), mCondition));
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (searchTag.equals(tag)) {
            hideProgressDialog();
            maopaoObjectList.clear();
            JSONArray jsonArray = respanse.optJSONObject("data").optJSONObject("tweets").optJSONArray("list");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject json = jsonArray.getJSONObject(i);
                Maopao.MaopaoObject maopaoObject = new Maopao.MaopaoObject(json);
                maopaoObjectList.add(maopaoObject);
            }
            notifyDataSetChange();
        }
    }

    private void notifyDataSetChange() {
        if (mSubjectSearchListAdapter != null) {
            mSubjectSearchListAdapter.notifyDataSetChanged();
            showResultView();
        }
    }

}
