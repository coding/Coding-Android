package net.coding.program.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project)
public class ProjectFragment extends BaseFragment implements ProjectListFragment.UpdateData, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<ProjectObject> mData = new ArrayList<ProjectObject>();

    @StringArrayRes
    String[] program_title;

    @ViewById
    net.coding.program.third.WechatTab tabs;

    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;

    private MyPagerAdapter adapter;
    final String host = Global.HOST + "/api/projects?pageSize=100&type=all&sort=hot";

    public static final String RECEIVER_INTENT_REFRESH_PROJECT = "net.coding.program.project.receiver.refresh";

    @AfterViews
    protected void
    init() {
        hideProgressDialog();
        mData = AccountInfo.loadProjects(getActivity());
        adapter = new MyPagerAdapter(getChildFragmentManager());

        pager.setAdapter(adapter);

        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        setTabsValue();
    }

    @Override
    public void onRefresh() {
        getNetwork(host, host);
    }

    @Override
    public void updateRead(String id) {
        List<Fragment> fragmentList = getChildFragmentManager().getFragments();

        for (Fragment item : fragmentList) {
            ((ProjectListFragment) item).setRead(id);
        }
    }

    private void setTabsValue() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(Color.parseColor("#3bbd79"));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
        tabs.setSelectedTextColor(Color.parseColor("#3bbd79"));
        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
    }

    boolean requestOk = true;

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(host)) {
            if (code == 0) {
                requestOk = true;
                mData.clear();
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    ProjectObject oneData = new ProjectObject(item);
                    mData.add(oneData);
                }
                AccountInfo.saveProjects(getActivity(), mData);

                adapter.notifyDataSetChanged();

            } else {
                requestOk = false;
                showErrorMsg(code, respanse);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return program_title[position];
        }

        @Override
        public int getCount() {
            return program_title.length;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ProjectListFragment fragment = (ProjectListFragment) super.instantiateItem(container, position);
            fragment.setData(getChildData(position), requestOk);

            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("", "all p " + position);
            ProjectListFragment fragment = new ProjectListFragment_();
            Bundle bundle = new Bundle();

            bundle.putSerializable("mData", getChildData(position));
            fragment.setArguments(bundle);

            return fragment;
        }

        private ArrayList<ProjectObject> getChildData(int position) {
            ArrayList<ProjectObject> childData = new ArrayList<ProjectObject>();

            switch (position) {
                case 1:
                    stuffChildData(childData, "member");
                    break;
                case 2:
                    stuffChildData(childData, "owner");
                    break;
                default:
                    childData.addAll(mData);
                    break;
            }

            return childData;
        }

        void stuffChildData(ArrayList<ProjectObject> child, String type) {
            for (int i = 0; i < mData.size(); ++i) {
                ProjectObject item = mData.get(i);
                if (item.current_user_role.equals(type)) {
                    child.add(item);
                }
            }
        }

    }

    private void registerRefreshReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_INTENT_REFRESH_PROJECT);
        try {
            getActivity().registerReceiver(refreshReceiver, intentFilter);
        } catch (Exception e) {

        }
    }

    boolean needRefresh = true;

    @Override
    public void onResume() {
        super.onResume();

        if (needRefresh) {
            needRefresh = false;
            onRefresh();
        }
    }

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVER_INTENT_REFRESH_PROJECT)) {
                needRefresh = true;
            }
        }
    };

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        registerRefreshReceiver();
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(refreshReceiver);
        } catch (Exception e) {

        }

        super.onDestroy();
    }

}