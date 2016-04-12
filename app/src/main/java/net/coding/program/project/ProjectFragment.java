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
import android.view.MenuItem;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.NoHorizontalScrollViewPager;
import net.coding.program.event.EventPosition;
import net.coding.program.event.EventRefresh;
import net.coding.program.maopao.MaopaoAddActivity_;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.create.ProjectCreateActivity_;
import net.coding.program.search.SearchProjectActivity_;
import net.coding.program.task.add.TaskAddActivity_;
import net.coding.program.user.AddFollowActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_project)
@OptionsMenu(R.menu.menu_project_item)
public class ProjectFragment extends BaseFragment implements ViewPager.OnPageChangeListener, ProjectListFragment.UpdateData, SwipeRefreshLayout.OnRefreshListener {

    public static final String RECEIVER_INTENT_REFRESH_PROJECT = "net.coding.program.project.receiver.refresh";
    static final int RESULT_PROJECT_SEARCH_PICK = 88;
    final String host = Global.HOST_API + "/projects?pageSize=100&type=all&sort=hot";
    String[] program_title;

    @ViewById(R.id.pagerFragmentProgram)
    NoHorizontalScrollViewPager pager;
    @FragmentArg
    Type type = Type.Main;
    boolean requestOk = true;
    private int pageIndex = 0;
    boolean needRefresh = true;
    ArrayList<ProjectObject> mData = new ArrayList<>();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    // 用于处理推送
    public void onEventMainThread(EventPosition event) {
        int position = event.position;
        pager.setCurrentItem(position, false);
    }

    @AfterViews
    protected void init() {
        hideProgressDialog();
        mData = AccountInfo.loadProjects(getActivity());
        pager.setOnPageChangeListener(this);
        setHasOptionsMenu(true);
        if (type == Type.Main) {
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
    }

    @Override
    public void onRefresh() {
        getNetwork(host, host);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (type == Type.Main) {
            inflater.inflate(R.menu.menu_fragment_project, menu);
        } else {
            inflater.inflate(R.menu.menu_project_pick_search, menu);
        }
        MenuItem menuItem = menu.findItem(R.id.action_filter);
        menuItem.setIcon(R.drawable.ic_filter_normal);

        super.onCreateOptionsMenu(menu, inflater);
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
//            ProjectObject projectObject = (ProjectObject) intent.getSerializableExtra("data");
//            Intent intent1 = new Intent();
//            intent1.putExtra("data", proj);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @OptionsItem
    void action_create_friend() {
        umengEvent(UmengEvent.LOCAL, "快捷添加好友");
        AddFollowActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_create() {
        umengEvent(UmengEvent.LOCAL, "快捷创建项目");
        ProjectCreateActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_create_task() {
        umengEvent(UmengEvent.LOCAL, "快捷创建任务");
        TaskAddActivity_.intent(this).mUserOwner(MyApp.sUserObject).start();
    }

    @OptionsItem
    final void action_create_maopao() {
        umengEvent(UmengEvent.LOCAL, "快捷创建冒泡");
        MaopaoAddActivity_.intent(this).start();
    }

    @OptionsItem
    final void action_scan() {
        Global.start2FAActivity(getActivity());
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
        Main, Pick, Filter
    }

}