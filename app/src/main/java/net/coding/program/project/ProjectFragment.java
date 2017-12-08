package net.coding.program.project;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.widget.NoHorizontalScrollViewPager;
import net.coding.program.common.event.EventFilter;
import net.coding.program.common.event.EventPosition;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.search.SearchProjectActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_project)
public class ProjectFragment extends BaseFragment implements ViewPager.OnPageChangeListener,
        ProjectListFragment.UpdateData, SwipeRefreshLayout.OnRefreshListener {

    public static final String RECEIVER_INTENT_REFRESH_PROJECT = "net.coding.program.project.receiver.refresh";
    static final int RESULT_PROJECT_SEARCH_PICK = 88;
    final String host = Global.HOST_API + "/projects?pageSize=100&type=all&sort=hot";
    String[] program_title;

    @ViewById(R.id.pagerFragmentProgram)
    NoHorizontalScrollViewPager pager;

    @FragmentArg
    Type type = Type.Main;

    boolean requestOk = true;
    boolean needRefresh = true;
    ArrayList<ProjectObject> mData = new ArrayList<>();
    private int pageIndex = 0;
    private MyProjectPagerAdapter adapter;
    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RECEIVER_INTENT_REFRESH_PROJECT)) {
                needRefresh = true;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnetFilter(EventFilter eventFilter) {
        if (eventFilter.index == 0) {
            action_filter();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProject(EventPosition event) {
        int position = event.position;
        pager.setCurrentItem(position, false);
    }

    @AfterViews
    protected void initProjectFragment() {
        hideDialogLoading();
        mData = AccountInfo.loadProjects(getActivity());
        pager.setOnPageChangeListener(this);
        setHasOptionsMenu(true);
        if (type == Type.Main || type == Type.Create) {
            program_title = getResources().getStringArray(R.array.program_title);
        } else {
            program_title = getResources().getStringArray(R.array.program_title_pick);
        }

        adapter = new MyProjectPagerAdapter(this, getChildFragmentManager());

        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(6);
        pager.setCurrentItem(0);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        if (type == Type.Create) {
            pager.setCurrentItem(MenuProjectFragment.POS_MY_CREATE, false);
        }
    }

    @Override
    public void onRefresh() {
        getNetwork(host, host);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (type != Type.Main && type != Type.Create) {
            inflater.inflate(R.menu.menu_project_pick_search, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void updateRead(int id) {
        // TODO: 2017/12/8  有 bug
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
        // TODO: 2017/12/8  有 bug
        List<WeakReference<Fragment>> fragmentList = adapter.getFragments();
        for (WeakReference<Fragment> item : fragmentList) {
            Fragment fragment = item.get();
            if (fragment instanceof ProjectListFragment) {
                ((ProjectListFragment) fragment).setPin(id, pin);
            }
        }
    }

    void action_filter() {
        if (pageIndex != program_title.length) {
            pager.setCurrentItem(program_title.length, false);
        } else {
            pager.setCurrentItem(program_title.length - pageIndex, false);
        }
    }

    @OptionsItem
    void action_search() {
        SearchProjectActivity_.intent(this).start();
        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @OptionsItem
    void action_search_pick() {
//        SearchProjectActivity_.intent(this).type(type).startForResult(RESULT_PROJECT_SEARCH_PICK);
//        getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @OnActivityResult(RESULT_PROJECT_SEARCH_PICK)
    final void resultPickSearch(int result, Intent intent) {
        if (result == Activity.RESULT_OK) {
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
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
                EventBus.getDefault().post(new EventRefresh(true));
            } else {
                requestOk = false;
                showErrorMsg(code, respanse);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: 2017/12/8  有 bug
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        pageIndex = position;

    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public enum Type {
        Main, Pick, Filter, Create
    }

}