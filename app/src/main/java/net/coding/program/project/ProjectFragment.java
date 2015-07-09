package net.coding.program.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SaveFragmentPagerAdapter;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.maopao.MaopaoAddActivity_;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.task.add.TaskAddActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project)
@OptionsMenu(R.menu.menu_fragment_project)
public class ProjectFragment extends BaseFragment implements ProjectListFragment.UpdateData, SwipeRefreshLayout.OnRefreshListener {

    public static final String RECEIVER_INTENT_REFRESH_PROJECT = "net.coding.program.project.receiver.refresh";
    final String host = Global.HOST_API + "/projects?pageSize=100&type=all&sort=hot";
    @StringArrayRes
    String[] program_title;
    @ViewById
    net.coding.program.third.WechatTab tabs;
    @ViewById(R.id.pagerFragmentProgram)
    ViewPager pager;
    boolean requestOk = true;
    boolean needRefresh = true;
    private ArrayList<ProjectObject> mData = new ArrayList<>();
    private MyPagerAdapter adapter;
    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVER_INTENT_REFRESH_PROJECT)) {
                needRefresh = true;
            }
        }
    };

    @AfterViews
    protected void init() {
        hideProgressDialog();
        mData = AccountInfo.loadProjects(getActivity());
        adapter = new MyPagerAdapter(getChildFragmentManager());

        pager.setAdapter(adapter);

        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
    }

    @Override
    public void onRefresh() {
        getNetwork(host, host);
    }

    @Override
    public void updateRead(int id) {
        List<WeakReference<Fragment>> fragmentList = adapter.getFragments();
        for (WeakReference<Fragment> item : fragmentList) {
            Fragment fragment = item.get();
            if (fragment instanceof ProjectListFragment) {
                ((ProjectListFragment) fragment).setRead(id);
            }
        }
    }

    @Override
    public void updatePin(int id, boolean pin) {
        List<WeakReference<Fragment>> fragmentList = adapter.getFragments();
        for (WeakReference<Fragment> item : fragmentList) {
            Fragment fragment = item.get();
            if (fragment instanceof ProjectListFragment) {
                ((ProjectListFragment) fragment).setPin(id, pin);
            }
        }
    }

    @OptionsItem
    void action_search() {
        SearchProjectActivity_.intent(this).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @OptionsItem
    void action_create_friend() {
        
    }

    @OptionsItem
    final void action_create() {
        ProjectCreateActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_create_task() {
        TaskAddActivity_.intent(this).mUserOwner(MyApp.sUserObject).start();
    }

    @OptionsItem
    final void action_create_maopao() {
        MaopaoAddActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_2fa() {
        Global.start2FAActivity(getActivity());
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(host)) {
            if (code == 0) {
                requestOk = true;
                mData.clear();
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                int pinCount = 0;
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    ProjectObject oneData = new ProjectObject(item);
                    if (oneData.isPin()) {
                        mData.add(pinCount++, oneData);
                    } else {
                        mData.add(oneData);
                    }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<WeakReference<Fragment>> fragmentList = adapter.getFragments();
        for (WeakReference<Fragment> item : fragmentList) {
            Fragment fragment = item.get();
            if (fragment instanceof ProjectListFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void registerRefreshReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVER_INTENT_REFRESH_PROJECT);
        try {
            getActivity().registerReceiver(refreshReceiver, intentFilter);
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (needRefresh) {
            needRefresh = false;
            onRefresh();
        }
    }

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
            Global.errorLog(e);
        }

        super.onDestroy();
    }

    private class MyPagerAdapter extends SaveFragmentPagerAdapter {

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

            saveFragment(fragment);

            return fragment;
        }

        private ArrayList<ProjectObject> getChildData(int position) {
            ArrayList<ProjectObject> childData = new ArrayList<>();

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
}